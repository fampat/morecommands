package com.mrnobody.morecommands.command.client;

import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.command.ClientCommand;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.util.ClientPlayerSettings;
import com.mrnobody.morecommands.util.GlobalSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "var",
		description = "command.var.description",
		example = "command.var.example",
		syntax = "command.var.syntax",
		videoURL = "command.var.videoURL"
		)
public class CommandVar extends ClientCommand {

	@Override
	public String getName() {
		return "var";
	}

	@Override
	public String getUsage() {
		return "command.var.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
    	if (params.length > 0) {
    		if (params[0].equalsIgnoreCase("true") || params[0].equalsIgnoreCase("1")
    			|| params[0].equalsIgnoreCase("on") || params[0].equalsIgnoreCase("enable")) {
    			
    			GlobalSettings.enableVars = true;
    			sender.sendLangfileMessage("command.var.enabled");
    		}
    		else if (params[0].equalsIgnoreCase("false") || params[0].equalsIgnoreCase("0")
        			|| params[0].equalsIgnoreCase("off") || params[0].equalsIgnoreCase("disable")) {
    			
    			GlobalSettings.enableVars = false;
    			sender.sendLangfileMessage("command.var.disabled");
        	}
    		else if (params[0].equalsIgnoreCase("get") && params.length > 1) {
    			if (!ClientPlayerSettings.varMapping.containsKey(params[1]))
    				throw new CommandException("command.var.notFound", sender, params[1]);
    			
    			sender.sendLangfileMessage("command.var.get", params[1], ClientPlayerSettings.varMapping.get(params[1]));
    		}
    		else if ((params[0].equalsIgnoreCase("del") || params[0].equalsIgnoreCase("delete") ||
    				params[0].equalsIgnoreCase("rm") || params[0].equalsIgnoreCase("remove")) && params.length > 1) {
    			if (!ClientPlayerSettings.varMapping.containsKey(params[1]))
    				throw new CommandException("command.var.notFound", sender, params[1]);
    			
    			ClientPlayerSettings.varMapping.remove(params[1]);
    			ClientPlayerSettings.saveSettings();
    			sender.sendLangfileMessage("command.var.deleted", params[1]);
    		}
    		else if (params[0].equalsIgnoreCase("set") && params.length > 2) {
    			String value = "";
    			for (int index = 2; index < params.length; index++) value += " " + params[index];
    			value = value.trim();
    			
    			ClientPlayerSettings.varMapping.put(params[1], value);
    			ClientPlayerSettings.saveSettings();
    			sender.sendLangfileMessage("command.var.created", params[1], value);
    		}
    		else throw new CommandException("command.var.invalidArg", sender);
    	}
    	else throw new CommandException("command.var.invalidUsage", sender);
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[] {Requirement.PATCH_CLIENTCOMMANDHANDLER};
	}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	@Override
	public boolean registerIfServerModded() {
		return false;
	}
	
	@Override
	public int getPermissionLevel() {
		return 0;
	}
}
