package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listeners.EventListener;
import com.mrnobody.morecommands.patch.EntityPlayerMP;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

@Command(
		name = "damage",
		description = "command.damage.description",
		example = "command.damage.example",
		syntax = "command.damage.syntax",
		videoURL = "command.damage.videoURL"
		)
public class CommandDamage extends ServerCommand implements EventListener<LivingAttackEvent> {
	
	//Disabling damage is possible on several ways
	// - via PlayerCababilities.disableDamage (actually only good for creative mode, disables much more than damage, e.g. enemies won't attack you)
	// - via Entity.invulnerable (needs Reflection, has unwanted side effects, e.g. enemies won't attack you)
	// - via a LivingHurtEvent (disables ONLY damage, but knockback, hurt sounds, etc. will remain)
	// - via a LivingAttackEvent (disables damage and everything belonging to that, e.g. knockback, hurt sounds, etc.) <---- That's what we want
	
	public CommandDamage() {
		EventHandler.ATTACK.getHandler().register(this);
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
		
		if (!ServerPlayerSettings.getPlayerSettings((EntityPlayerMP) event.entity).damage)
			event.setCanceled(true);
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		ServerPlayerSettings settings = ServerPlayerSettings.getPlayerSettings((EntityPlayerMP) sender.getMinecraftISender());
    	
        if (params.length > 0) {
        	if (params[0].equalsIgnoreCase("enable") || params[0].equalsIgnoreCase("1")
            	|| params[0].equalsIgnoreCase("on") || params[0].equalsIgnoreCase("true")) {
        		settings.damage = true;
            	sender.sendLangfileMessage("command.damage.on");
            }
            else if (params[0].equalsIgnoreCase("disable") || params[0].equalsIgnoreCase("0")
            		|| params[0].equalsIgnoreCase("off") || params[0].equalsIgnoreCase("false")) {
            	settings.damage = false;
            	sender.sendLangfileMessage("command.damage.off");
            }
            else throw new CommandException("command.damage.failure", sender);
        }
        else {
        	settings.damage = !settings.damage;
        	sender.sendLangfileMessage(settings.damage ? "command.damage.on" : "command.damage.off");
        }
	}

	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	@Override
	public int getPermissionLevel() {
		return 2;
	}
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return sender instanceof EntityPlayerMP;
	}
}
