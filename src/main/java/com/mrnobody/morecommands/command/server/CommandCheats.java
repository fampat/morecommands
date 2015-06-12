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
    	boolean allowCheats = false;
    	boolean success = false;
    	
    	if (params.length >= 1) {
    		if (params[0].equalsIgnoreCase("true")) {allowCheats = true; success = true;}
    		else if (params[0].equalsIgnoreCase("false")) {allowCheats = false; success = true;}
    		else if (params[0].equalsIgnoreCase("0")) {allowCheats = false; success = true;}
    		else if (params[0].equalsIgnoreCase("1")) {allowCheats = true; success = true;}
    		else if (params[0].equalsIgnoreCase("on")) {allowCheats = true; success = true;}
    		else if (params[0].equalsIgnoreCase("off")) {allowCheats = false; success = true;}
    		else if (params[0].equalsIgnoreCase("enable")) {allowCheats = true; success = true;}
    		else if (params[0].equalsIgnoreCase("disable")) {allowCheats = false; success = true;}
    		else {success = false;}
    	}
    	else {allowCheats = !sender.getWorld().isCheats(); success = true;}
    	
    	if (success) {sender.getWorld().setCheats(allowCheats);}
    	
    	sender.sendLangfileMessage(success ? sender.getWorld().isCheats() ? "command.cheats.on" : "command.cheats.off" : "command.cheats.failure", new Object[0]);
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
