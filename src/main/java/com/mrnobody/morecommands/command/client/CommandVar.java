package com.mrnobody.morecommands.command.client;

import java.util.Arrays;
import java.util.Map;

import com.google.common.collect.Sets;
import com.mrnobody.morecommands.command.ClientCommandProperties;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.CalculationParser;
import com.mrnobody.morecommands.util.ClientPlayerSettings;
import com.mrnobody.morecommands.util.GlobalSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.text.TextFormatting;

@Command(
		name = "var",
		description = "command.var.description",
		example = "command.var.example",
		syntax = "command.var.syntax",
		videoURL = "command.var.videoURL"
		)
public class CommandVar extends StandardCommand implements ClientCommandProperties {
	private static final int PAGE_MAX = 15;
	
	@Override
	public String getCommandName() {
		return "var";
	}

	@Override
	public String getCommandUsage() {
		return "command.var.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		if (!isSenderOfEntityType(sender.getMinecraftISender(), EntityPlayerSP.class)) throw new CommandException("command.generic.notAPlayer", sender);
		ClientPlayerSettings settings = getPlayerSettings(getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerSP.class));
		
		if (!GlobalSettings.enablePlayerVars)
			throw new CommandException("command.var.varsDisabled", sender);
		
    	if (params.length > 0) {    		
    		if (params[0].equalsIgnoreCase("list")) {
    			Map<String, String> vars = settings.variables;
    			String[] keys = vars.keySet().toArray(new String[vars.size()]); int page = 0;
    			
    			if (keys.length == 0) {sender.sendStringMessage("command.var.noVars", TextFormatting.RED); return;}
    			
    			if (params.length > 1) {
    				try {
    					page = Integer.parseInt(params[1]) - 1; 
    					if (page < 0) page = 0;
    					else if (page * PAGE_MAX > keys.length) page = keys.length / PAGE_MAX;
    				}
    				catch (NumberFormatException e) {throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());}
    			}
    			
    			final int stop = (page + 1) * PAGE_MAX;
    			for (int i = page * PAGE_MAX; i < stop && i < keys.length; i++)
    				sender.sendStringMessage("'" + keys[i] + "' = '" + vars.get(keys[i]) + "'");
    			
    			sender.sendLangfileMessage("command.var.more", TextFormatting.RED);
    		}
    		else if (params[0].equalsIgnoreCase("get") && params.length > 1) {
    			if (settings.variables.containsKey(params[1]))
    				throw new CommandException("command.var.notFound", sender, params[1]);
    			
    			sender.sendLangfileMessage("command.var.get", params[1], settings.variables.get(params[1]));
    		}
    		else if ((params[0].equalsIgnoreCase("del") || params[0].equalsIgnoreCase("delete") ||
    				params[0].equalsIgnoreCase("rm") || params[0].equalsIgnoreCase("remove")) && params.length > 1) {
    			if (!settings.variables.containsKey(params[1]))
    				throw new CommandException("command.var.notFound", sender, params[1]);
    			
    			settings.variables = settings.removeAndUpdate("variables", params[1], String.class, true);
    			sender.sendLangfileMessage("command.var.deleted", params[1]);
    		}
    		else if ((params[0].equalsIgnoreCase("delAll") || params[0].equalsIgnoreCase("deleteAll") ||
    				params[0].equalsIgnoreCase("rmAll") || params[0].equalsIgnoreCase("removeAll"))) {
    			
    			settings.variables = settings.removeAndUpdate("variables", Sets.newHashSet(settings.variables.keySet()), String.class, true, true);
    			sender.sendLangfileMessage("command.var.deletedAll");
    		}
    		else if (params[0].equalsIgnoreCase("set") && params.length > 2) {
    			String value = rejoinParams(Arrays.copyOfRange(params, 2, params.length));
    			
    			settings.variables = settings.putAndUpdate("variables",  params[1], value, String.class, true);
    			sender.sendLangfileMessage("command.var.created", params[1], value);
    		}
    		else if (params[0].equalsIgnoreCase("calc") && params.length > 2) {
    			try {
    				String value = Double.toString(CalculationParser.parseCalculation(rejoinParams(Arrays.copyOfRange(params, 2, params.length))));
        			
    				settings.variables = settings.putAndUpdate("variables", params[1], value, String.class, true);
    				sender.sendLangfileMessage("command.var.created", params[1], value);
    			}
    			catch (NumberFormatException nfe) {throw new CommandException("command.var.notNumeric", sender);}
    		}
    		else throw new CommandException("command.var.invalidArg", sender);
    	}
    	else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
	}
	
	@Override
	public CommandRequirement[] getRequirements() {
		return new CommandRequirement[] {CommandRequirement.PATCH_CLIENTCOMMANDHANDLER};
	}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	@Override
	public boolean registerIfServerModded() {
		return true;
	}
	
	@Override
	public int getDefaultPermissionLevel() {
		return 0;
	}
}
