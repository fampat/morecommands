package com.mrnobody.morecommands.command.server;

import java.util.Arrays;
import java.util.Map;

import com.google.common.collect.Sets;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.MultipleCommands;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.core.AppliedPatches.PlayerPatches;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.CalculationParser;
import com.mrnobody.morecommands.util.GlobalSettings;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.CommandNotFoundException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumChatFormatting;

@Command.MultipleCommand(
		name =  {"var", "var_global"},
		description = {"command.var.description", "command.var.global.description"},
		example = {"command.var.example", "command.var.global.example"},
		syntax = {"command.var.syntax", "command.var.global.syntax"},
		videoURL = {"command.var.videoURL", "command.var.global.videoURL"}
		)
public class CommandVar extends MultipleCommands implements ServerCommandProperties {
	private static final int PAGE_MAX = 15;
	
	public CommandVar() {
		super();
	}
	
	public CommandVar(int typeIndex) {
		super(typeIndex);
	}
	
	@Override
	public String[] getCommandNames() {
		return new String[] {"var", "var_global"};
	}

	@Override
	public String[] getUsages() {
		return new String[] {"command.var.syntax", "command.var.global.syntax"};
	}

	@Override
	public void execute(String commandName, CommandSender sender, String[] params) throws CommandException {
		boolean global = commandName.endsWith("global");
		String world = MoreCommands.getProxy().getCurrentWorld(), dim = sender.getMinecraftISender().getEntityWorld().provider.getDimensionName();
		Map<String, String> globalvars = !global ? null : GlobalSettings.getVariables(world, dim);
		
		if (!global) {
			PlayerPatches playerInfo = MoreCommands.INSTANCE.getEntityProperties(PlayerPatches.class, PlayerPatches.PLAYERPATCHES_IDENTIFIER, getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class));
			if (playerInfo != null && playerInfo.clientModded()) throw new CommandNotFoundException();
		}
		
		if (global && !GlobalSettings.enableGlobalVars)
			throw new CommandException("command.var.global.varsDisabled", sender);
		else if (!global && !GlobalSettings.enablePlayerVars)
			throw new CommandException("command.var.varsDisabled", sender);
		
    	if (params.length > 0) {
    		ServerPlayerSettings settings = global ? null : getPlayerSettings(getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class));
    		
    		if (params[0].equalsIgnoreCase("list")) {
    			Map<String, String> vars = global ? globalvars : settings.variables;
    			String[] keys = vars.keySet().toArray(new String[vars.size()]); int page = 0;
    			
    			if (keys.length == 0) {sender.sendStringMessage("command.var.noVars", EnumChatFormatting.RED); return;}
    			
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
    			
    			sender.sendLangfileMessage("command.var.more", EnumChatFormatting.RED);
    		}
    		else if (params[0].equalsIgnoreCase("get") && params.length > 1) {
    			if (global ? !globalvars.containsKey(params[1]) : !settings.variables.containsKey(params[1]))
    				throw new CommandException("command.var.notFound", sender, params[1]);
    			
    			sender.sendLangfileMessage("command.var.get", params[1], global ? globalvars.get(params[1]) : settings.variables.get(params[1]));
    		}
    		else if ((params[0].equalsIgnoreCase("del") || params[0].equalsIgnoreCase("delete") ||
    				params[0].equalsIgnoreCase("rm") || params[0].equalsIgnoreCase("remove")) && params.length > 1) {
    			if (global ? !globalvars.containsKey(params[1]) : !settings.variables.containsKey(params[1]))
    				throw new CommandException("command.var.notFound", sender, params[1]);
    			
    			if (global) GlobalSettings.removeVariable(world, dim, params[1]);
    			else settings.variables = settings.removeAndUpdate("variables", params[1], String.class, true);
    			sender.sendLangfileMessage("command.var.deleted", params[1]);
    		}
    		else if ((params[0].equalsIgnoreCase("delAll") || params[0].equalsIgnoreCase("deleteAll") ||
    				params[0].equalsIgnoreCase("rmAll") || params[0].equalsIgnoreCase("removeAll"))) {
    			if (global) GlobalSettings.removeAllVariables(world, dim);
    			else settings.variables = settings.removeAndUpdate("variables", Sets.newHashSet(settings.variables.keySet()), String.class, true, true);
    			sender.sendLangfileMessage("command.var.deletedAll");
    		}
    		else if (params[0].equalsIgnoreCase("set") && params.length > 2) {
    			String value = rejoinParams(Arrays.copyOfRange(params, 2, params.length));
    			
    			if (global) {
					GlobalSettings.removeVariable(world, dim, params[1]); 
					GlobalSettings.putVariable(GlobalSettings.getSaveProp("variables").getLeft() ? world : null, 
					GlobalSettings.getSaveProp("variables").getRight() ? dim : null, params[1], value);
				}
    			else settings.variables = settings.putAndUpdate("variables", params[1], value, String.class, true);
    			sender.sendLangfileMessage("command.var.created", params[1], value);
    		}
    		else if (params[0].equalsIgnoreCase("calc") && params.length > 2) {
    			try {
    				String value = Double.toString(CalculationParser.parseCalculation(rejoinParams(Arrays.copyOfRange(params, 2, params.length))));
        			if (global) {
    					GlobalSettings.removeVariable(world, dim, params[1]); 
    					GlobalSettings.putVariable(GlobalSettings.getSaveProp("variables").getLeft() ? world : null, 
    					GlobalSettings.getSaveProp("variables").getRight() ? dim : null, params[1], value);
    				}
        			else settings.variables = settings.putAndUpdate("variables", params[1], value, String.class, true);
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
		return new CommandRequirement[] {CommandRequirement.PATCH_SERVERCOMMANDHANDLER};
	}
	
	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	@Override
	public int getDefaultPermissionLevel() {
		return 0;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return commandName.endsWith("global") ? true : isSenderOfEntityType(sender, EntityPlayerMP.class);
	}
}