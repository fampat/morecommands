package com.mrnobody.morecommands.command.server;

import java.text.DecimalFormat;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;

import net.minecraft.command.ICommandSender;

@Command(
		name = "position",
		description = "command.position.description",
		example = "command.position.example",
		syntax = "command.position.syntax",
		videoURL = "command.position.videoURL"
		)
public class CommandPosition extends StandardCommand implements ServerCommandProperties {

	@Override
	public String getCommandName() {
		return "position";
	}

	@Override
	public String getCommandUsage() {
		return "command.position.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		DecimalFormat f = new DecimalFormat("#.##");
		
		sender.sendStringMessage("Your current position is:"
				+ " X = " + f.format(sender.getPosition().getX())
				+ "; Y = " + f.format(sender.getPosition().getY())
				+ "; Z = " + f.format(sender.getPosition().getZ()));
		
		return "x=" + sender.getPosition().getX() + ",y=" + sender.getPosition().getY() + ",z=" + sender.getPosition().getZ();
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
		return 0;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return true;
	}
}
