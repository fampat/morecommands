package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;

@Command(
		name = "cheats",
		description = "command.cheats.description",
		example = "command.cheats.example",
		syntax = "command.cheats.syntax",
		videoURL = "command.cheats.videoURL"
		)
public class CommandCheats extends ServerCommand {
	@Override
	public boolean canCommandSenderUse(ICommandSender sender) {return true;}
	
	@Override
	public String getName() {
		return "cheats";
	}

	@Override
	public String getUsage() {
		return "command.cheats.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		try {sender.getWorld().setCheats(parseTrueFalse(params, 0, sender.getWorld().isCheats()));}
		catch (IllegalArgumentException ex) {throw new CommandException("command.cheats.failure", sender);}
		
		sender.getWorld().setCheats(sender.getWorld().isCheats());
		sender.sendLangfileMessage(sender.getWorld().isCheats() ? "command.cheats.on" : "command.cheats.off");
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.INTEGRATED;
	}
	
	@Override
	public int getPermissionLevel() {
		return 0;
	}
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return true;
	}
}
