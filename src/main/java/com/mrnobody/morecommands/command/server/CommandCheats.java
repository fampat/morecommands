package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "cheats",
		description = "command.cheats.description",
		example = "command.cheats.example",
		syntax = "command.cheats.syntax",
		videoURL = "command.cheats.videoURL"
		)
public class CommandCheats extends ServerCommand {

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {return true;}
	
	@Override
	public String getCommandName() {
		return "cheats";
	}

	@Override
	public String getUsage() {
		return "command.cheats.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
        if (params.length > 0) {
        	if (params[0].equalsIgnoreCase("enable") || params[0].equalsIgnoreCase("1")
            	|| params[0].equalsIgnoreCase("on") || params[0].equalsIgnoreCase("true")) {
        		sender.getWorld().setCheats(true);
            	sender.sendLangfileMessage("command.cheats.on");
            }
            else if (params[0].equalsIgnoreCase("disable") || params[0].equalsIgnoreCase("0")
            		|| params[0].equalsIgnoreCase("off") || params[0].equalsIgnoreCase("false")) {
            	sender.getWorld().setCheats(false);
            	sender.sendLangfileMessage("command.cheats.off");
            }
            else throw new CommandException("command.cheats.failure", sender);
        }
        else {
        	sender.getWorld().setCheats(sender.getWorld().isCheats());
        	sender.sendLangfileMessage(sender.getWorld().isCheats() ? "command.cheats.on" : "command.cheats.off");
        }
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
	public void unregisterFromHandler() {}
	
	@Override
	public int getPermissionLevel() {
		return 0;
	}
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return true;
	}
}
