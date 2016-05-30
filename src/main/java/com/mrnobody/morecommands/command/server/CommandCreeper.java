package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.event.EventHandler;
import com.mrnobody.morecommands.event.Listeners.EventListener;
import com.mrnobody.morecommands.util.GlobalSettings;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.world.ExplosionEvent;

@Command(
		name = "creeper",
		description = "command.creeper.description",
		example = "command.creeper.example",
		syntax = "command.creeper.syntax",
		videoURL = "command.creeper.videoURL"
		)
public class CommandCreeper extends StandardCommand implements ServerCommandProperties, EventListener<ExplosionEvent> {
	public CommandCreeper() {
		EventHandler.EXPLOSION.register(this);
	}

	@Override
	public void onEvent(ExplosionEvent event) {
		if (event instanceof ExplosionEvent.Start && event.getExplosion().getExplosivePlacedBy() instanceof EntityCreeper) {
			if (!GlobalSettings.creeperExplosion) {event.setCanceled(true); return;}
			
			EntityCreeper creeper = (EntityCreeper) event.getExplosion().getExplosivePlacedBy();
			
			if (creeper.getAttackTarget() instanceof EntityPlayerMP) {
				EntityPlayerMP player = (EntityPlayerMP) creeper.getAttackTarget();
				if (!getPlayerSettings(player).creeperExplosion) event.setCanceled(true);
			}
		}
	}
	
	@Override
	public String getCommandName() {
		return "creeper";
	}

	@Override
	public String getCommandUsage() {
		return "command.creeper.syntax";
	}
	
	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		boolean global = params.length > 0 && params[0].equalsIgnoreCase("global");
		ServerPlayerSettings settings = global ? null : getPlayerSettings(getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class));
		
		try {
			if (!global) settings.creeperExplosion = parseTrueFalse(params, 0, settings.creeperExplosion);
			else GlobalSettings.creeperExplosion = parseTrueFalse(params, 1, GlobalSettings.creeperExplosion);
		}
		catch (IllegalArgumentException ex) {throw new CommandException("command.creeper.failure", sender);}
		
		sender.sendLangfileMessage((global ? GlobalSettings.creeperExplosion : settings.creeperExplosion) ? "command.creeper.on" : "command.creeper.off");
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
		if (params.length > 0 && params[0].equalsIgnoreCase("global")) return true;
		else return isSenderOfEntityType(sender, EntityPlayerMP.class);
	}
}
