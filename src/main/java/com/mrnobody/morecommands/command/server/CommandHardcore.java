package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Player;

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
    	Player player = sender.toPlayer();
    	
    	boolean enableHardcore = false;
    	boolean success = false;
    	
    	if (params.length >= 1) {
    		if (params[0].toLowerCase().equals("true")) {enableHardcore = true; success = true;}
    		else if (params[0].toLowerCase().equals("false")) {enableHardcore = false; success = true;}
    		else if (params[0].toLowerCase().equals("0")) {enableHardcore = false; success = true;}
    		else if (params[0].toLowerCase().equals("1")) {enableHardcore = true; success = true;}
    		else if (params[0].toLowerCase().equals("on")) {enableHardcore = true; success = true;}
    		else if (params[0].toLowerCase().equals("off")) {enableHardcore = false; success = true;}
    		else {success = false;}
    	}
    	else {enableHardcore = !player.getWorld().isHardcore(); success = true;}
    	
    	if (success) {player.getWorld().setHardcore(enableHardcore);}
    	
    	sender.sendLangfileMessageToPlayer(success ? player.getWorld().isHardcore() ? "command.hardcore.on" : "command.hardcore.off" : "command.hardcore.failure", new Object[0]);
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
}
