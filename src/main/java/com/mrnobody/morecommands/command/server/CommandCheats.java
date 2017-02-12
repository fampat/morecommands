package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.WorldUtils;

import net.minecraft.command.ICommandSender;

@Command(
		name = "cheats",
		description = "command.cheats.description",
		example = "command.cheats.example",
		syntax = "command.cheats.syntax",
		videoURL = "command.cheats.videoURL"
		)
public class CommandCheats extends StandardCommand implements ServerCommandProperties {
	@Override
	public boolean canCommandSenderUse(ICommandSender sender) {return true;}
	
	@Override
	public String getCommandName() {
		return "cheats";
	}

	@Override
	public String getCommandUsage() {
		return "command.cheats.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		try {WorldUtils.setCheats(sender.getWorld(), parseTrueFalse(params, 0, !WorldUtils.isCheats(sender.getWorld())));}
		catch (IllegalArgumentException ex) {throw new CommandException("command.cheats.failure", sender);}
		
		sender.sendLangfileMessage(WorldUtils.isCheats(sender.getWorld()) ? "command.cheats.on" : "command.cheats.off");
		return null;
	}
	
	@Override
	public CommandRequirement[] getRequirements() {
		return new CommandRequirement[0];
	}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.INTEGRATED;
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
