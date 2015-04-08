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
    public String getCommandName()
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
    	boolean enableHardcore = false;
    	boolean success = false;
    	
    	if (params.length >= 1) {
    		if (params[0].equalsIgnoreCase("true")) {enableHardcore = true; success = true;}
    		else if (params[0].equalsIgnoreCase("false")) {enableHardcore = false; success = true;}
    		else if (params[0].equalsIgnoreCase("0")) {enableHardcore = false; success = true;}
    		else if (params[0].equalsIgnoreCase("1")) {enableHardcore = true; success = true;}
    		else if (params[0].equalsIgnoreCase("on")) {enableHardcore = true; success = true;}
    		else if (params[0].equalsIgnoreCase("off")) {enableHardcore = false; success = true;}
    		else if (params[0].equalsIgnoreCase("enable")) {enableHardcore = true; success = true;}
    		else if (params[0].equalsIgnoreCase("disable")) {enableHardcore = false; success = true;}
    		else {success = false;}
    	}
    	else {enableHardcore = !sender.getWorld().isHardcore(); success = true;}
    	
    	if (success) {sender.getWorld().setHardcore(enableHardcore);}
    	
    	sender.sendLangfileMessage(success ? sender.getWorld().isHardcore() ? "command.hardcore.on" : "command.hardcore.off" : "command.hardcore.failure", new Object[0]);
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
