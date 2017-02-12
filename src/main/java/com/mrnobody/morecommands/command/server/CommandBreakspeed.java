package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.event.EventHandler;
import com.mrnobody.morecommands.event.Listeners.EventListener;
import com.mrnobody.morecommands.settings.ServerPlayerSettings;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;

@Command(
		name = "breakspeed",
		description = "command.breakspeed.description",
		example = "command.breakspeed.example",
		syntax = "command.breakspeed.syntax",
		videoURL = "command.breakspeed.videoURL"
		)
public class CommandBreakspeed extends StandardCommand implements ServerCommandProperties, EventListener<BreakSpeed> {
	public CommandBreakspeed() {EventHandler.BREAKSPEED.register(this);}
	
	@Override
	public void onEvent(BreakSpeed event) {
		if (!(event.entityPlayer instanceof EntityPlayerMP)) return;
		
		ServerPlayerSettings settings = getPlayerSettings((EntityPlayerMP) event.entityPlayer);
		event.newSpeed = settings.breakspeed > 0 ? settings.breakspeed : event.originalSpeed;
	}

	@Override
	public String getCommandName() {
		return "breakspeed";
	}

	@Override
	public String getCommandUsage() {
		return "command.breakspeed.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		ServerPlayerSettings settings = getPlayerSettings(getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class));
		float speed = 0.0F;
		
		if (params.length > 0) {
			if (params[0].equalsIgnoreCase("reset")) {speed = -1F;}
			else if (params[0].equalsIgnoreCase("max")) {speed = Float.MAX_VALUE;}
			else if (params[0].equalsIgnoreCase("min")) {speed = Float.MIN_VALUE;}
			else {
				try {speed = Float.parseFloat(params[0]);}
				catch (NumberFormatException e) {throw new CommandException("command.breakspeed.invalidArg", sender);}
			}
		}
		else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
		
		settings.breakspeed = speed;
		sender.sendLangfileMessage(params.length > 0 && params[0].equalsIgnoreCase("reset") ? "command.breakspeed.reset" : 
			"command.breakspeed.setto", params.length > 0 && params[0].equalsIgnoreCase("reset") ? new Object[0] : speed);
		
		return null;
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
	public int getDefaultPermissionLevel(String[] args) {
		return 2;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return isSenderOfEntityType(sender, EntityPlayerMP.class);
	}
}
