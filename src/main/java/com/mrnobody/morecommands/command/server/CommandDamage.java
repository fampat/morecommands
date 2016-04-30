package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.event.EventHandler;
import com.mrnobody.morecommands.event.Listeners.EventListener;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

@Command(
		name = "damage",
		description = "command.damage.description",
		example = "command.damage.example",
		syntax = "command.damage.syntax",
		videoURL = "command.damage.videoURL"
		)
public class CommandDamage extends StandardCommand implements ServerCommandProperties, EventListener<LivingAttackEvent> {
	
	//Disabling damage is possible on several ways
	// - via PlayerCababilities.disableDamage (actually only good for creative mode, disables much more than damage, e.g. enemies won't attack you)
	// - via Entity.invulnerable (needs Reflection, has unwanted side effects, e.g. enemies won't attack you)
	// - via a LivingHurtEvent (disables ONLY damage, but knockback, hurt sounds, etc. will remain)
	// - via a LivingAttackEvent (disables damage and everything belonging to that, e.g. knockback, hurt sounds, etc.) <---- That's what we want
	
	public CommandDamage() {
		EventHandler.ATTACK.register(this);
	}
	
	@Override
    public String getName()
    {
        return "damage";
    }

	@Override
    public String getUsage()
    {
        return "command.damage.syntax";
    }
    
	@Override
	public void onEvent(LivingAttackEvent event) {
		if (!(event.entity instanceof EntityPlayerMP)) return;
		
		if (!getPlayerSettings((EntityPlayerMP) event.entity).damage)
			event.setCanceled(true);
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		ServerPlayerSettings settings = getPlayerSettings(getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class));
    	
		try {settings.damage = parseTrueFalse(params, 0, settings.damage);}
		catch (IllegalArgumentException ex) {throw new CommandException("command.damage.failure", sender);}
		
		sender.sendLangfileMessage(settings.damage ? "command.damage.on" : "command.damage.off");
	}
	
	@Override
	public CommandRequirement[] getRequirements() {
		return new CommandRequirement[0];
	}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	@Override
	public int getDefaultPermissionLevel() {
		return 2;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return isSenderOfEntityType(sender, EntityPlayerMP.class);
	}
}
