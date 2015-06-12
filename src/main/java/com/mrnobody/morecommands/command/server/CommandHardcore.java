package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "hardcore",
		description = "command.hardcore.description",
		example = "command.hardcore.example",
		syntax = "command.hardcore.syntax",
		videoURL = "command.hardcore.videoURL"
		)
public class CommandHardcore extends ServerCommand {

	@Override
    public String getName()
    {
        return "hardcore";
    }

	@Override
    public String getUsage()
    {
        return "command.hardcore.syntax";
    }
    
	@Override
    public void execute(CommandSender sender, String[] params) throws CommandException {
        if (params.length > 0) {
        	if (params[0].equalsIgnoreCase("enable") || params[0].equalsIgnoreCase("1")
            	|| params[0].equalsIgnoreCase("on") || params[0].equalsIgnoreCase("true")) {
        		sender.getWorld().setHardcore(true);
            	sender.sendLangfileMessage("command.hardcore.on");
            }
            else if (params[0].equalsIgnoreCase("disable") || params[0].equalsIgnoreCase("0")
            		|| params[0].equalsIgnoreCase("off") || params[0].equalsIgnoreCase("false")) {
            	sender.getWorld().setHardcore(false);
            	sender.sendLangfileMessage("command.hardcore.off");
            }
            else throw new CommandException("command.hardcore.failure", sender);
        }
        else {
        	sender.getWorld().setHardcore(sender.getWorld().isHardcore());
        	sender.sendLangfileMessage(sender.getWorld().isHardcore() ? "command.hardcore.on" : "command.hardcore.off");
        }
    }
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}
	
	@Override
	public void unregisterFromHandler() {}

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
		return true;
	}
}
