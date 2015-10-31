package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listeners.EventListener;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

@Command(
		name = "mobdamage",
		description = "command.mobdamage.description",
		example = "command.mobdamage.example",
		syntax = "command.mobdamage.syntax",
		videoURL = "command.mobdamage.videoURL"
		)
public class CommandMobdamage extends ServerCommand implements EventListener<LivingAttackEvent> {
	private boolean mobdamage = true;
	
	public CommandMobdamage() {
		EventHandler.ATTACK.getHandler().register(this);
	}

	@Override
	public void onEvent(LivingAttackEvent event) {
		if (!(event.entity instanceof EntityPlayerMP)) return;
		
		if (!ServerPlayerSettings.getPlayerSettings((EntityPlayerMP) event.entity).mobdamage) {
			if (event.source.getSourceOfDamage() != null && (event.source.getSourceOfDamage() instanceof EntityCreature || event.source.getSourceOfDamage() instanceof EntityArrow)) {
				if (event.source.getSourceOfDamage() instanceof EntityCreature) event.setCanceled(true);
				if (event.source.getSourceOfDamage() instanceof EntityArrow && ((EntityArrow) event.source.getSourceOfDamage()).shootingEntity instanceof EntityCreature) event.setCanceled(true);
			}
		}
	}
	
	@Override
	public String getName() {
		return "mobdamage";
	}

	@Override
	public String getUsage() {
		return "command.mobdamage.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		ServerPlayerSettings settings = ServerPlayerSettings.getPlayerSettings((EntityPlayerMP) sender.getMinecraftISender());
    	
		try {settings.mobdamage = parseTrueFalse(params, 0, settings.mobdamage);}
		catch (IllegalArgumentException ex) {throw new CommandException("command.mobdamage.failure", sender);}
		
		sender.sendLangfileMessage(settings.mobdamage ? "command.mobdamage.on" : "command.mobdamage.off");
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
