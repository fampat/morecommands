package com.mrnobody.morecommands.command.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.MultipleCommands;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.core.AppliedPatches.PlayerPatches;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.settings.GlobalSettings;
import com.mrnobody.morecommands.settings.MoreCommandsConfig;
import com.mrnobody.morecommands.settings.ServerPlayerSettings;
import com.mrnobody.morecommands.util.CalculationParser;

import net.minecraft.command.CommandNotFoundException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
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
	public String[] getCommandUsages() {
		return new String[] {"command.var.syntax", "command.var.global.syntax"};
	}

	@Override
	public String execute(String commandName, CommandSender sender, String[] params) throws CommandException {
		boolean global = commandName.endsWith("global");
		String world = sender.getWorld().getSaveHandler().getWorldDirectoryName(), dim = sender.getWorld().provider.getDimensionName();
		
		if (!global) {
			PlayerPatches playerInfo = MoreCommands.INSTANCE.getEntityProperties(PlayerPatches.class, PlayerPatches.PLAYERPATCHES_IDENTIFIER, getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class));
			if (playerInfo != null && playerInfo.clientModded()) throw new CommandException(new CommandNotFoundException());
		}
		
		if (global && !MoreCommandsConfig.enableGlobalVars)
			throw new CommandException("command.var.global.varsDisabled", sender);
		else if (!global && !MoreCommandsConfig.enablePlayerVars)
			throw new CommandException("command.var.varsDisabled", sender);
		
    	if (params.length > 0) {
    		ServerPlayerSettings settings = global ? null : getPlayerSettings(getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class));
    		Map<String, String> vars = global ? GlobalSettings.getInstance().variables.get(ImmutablePair.of(world, dim)) : settings.variables;
    		
    		if (params[0].equalsIgnoreCase("list")) {
    			String[] keys = vars.keySet().toArray(new String[vars.size()]); int page = 0;
    			
    			if (keys.length == 0) {sender.sendStringMessage("command.var.noVars", EnumChatFormatting.RED); return null;}
    			
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
    			if (!vars.containsKey(params[1]))
    				throw new CommandException("command.var.notFound", sender, params[1]);
    			
    			sender.sendLangfileMessage("command.var.get", params[1], vars.get(params[1]));
    		}
    		else if ((params[0].equalsIgnoreCase("del") || params[0].equalsIgnoreCase("delete") ||
    				params[0].equalsIgnoreCase("rm") || params[0].equalsIgnoreCase("remove")) && params.length > 1) {
    			if (!vars.containsKey(params[1]))
    				throw new CommandException("command.var.notFound", sender, params[1]);
    			
    			vars.remove(params[1]);
    			sender.sendLangfileMessage("command.var.deleted", params[1]);
    		}
    		else if ((params[0].equalsIgnoreCase("delAll") || params[0].equalsIgnoreCase("deleteAll") ||
    				params[0].equalsIgnoreCase("rmAll") || params[0].equalsIgnoreCase("removeAll"))) {
    			
    			vars.clear();
    			sender.sendLangfileMessage("command.var.deletedAll");
    		}
    		else if (params[0].equalsIgnoreCase("set") && params.length > 2) {
    			String value = rejoinParams(Arrays.copyOfRange(params, 2, params.length));
    			
    			vars.put(params[1], value);
    			sender.sendLangfileMessage("command.var.created", params[1], value);
    		}
    		else if (params[0].equalsIgnoreCase("calc") && params.length > 2) {
    			try {
    				String value = Double.toString(CalculationParser.parseCalculation(rejoinParams(Arrays.copyOfRange(params, 2, params.length))));
        			
    				vars.put(params[1], value);
        			sender.sendLangfileMessage("command.var.created", params[1], value);
    			}
    			catch (NumberFormatException nfe) {throw new CommandException("command.var.notNumeric", sender);}
    		}
    		else if (params[0].equalsIgnoreCase("grab") && params.length > 2) {
    			String var = params[1], command = rejoinParams(Arrays.copyOfRange(params, 2, params.length));
    			final List<String> messages = new ArrayList<String>();
    			
    			if (command.startsWith("macro") || command.startsWith("/macro"))
    				throw new CommandException("command.var.grabMacro", sender);
    			
    			if (!isSenderOfEntityType(sender.getMinecraftISender(), com.mrnobody.morecommands.patch.EntityPlayerMP.class))
    				throw new CommandException("command.generic.serverPlayerNotPatched", sender);
    			
				com.mrnobody.morecommands.patch.EntityPlayerMP player = getSenderAsEntity(sender.getMinecraftISender(), com.mrnobody.morecommands.patch.EntityPlayerMP.class);
				player.setCaptureNextCommandResult();
				
				MinecraftServer.getServer().getCommandManager().executeCommand(player, command);
    			String result = player.getCapturedCommandResult();
    			
    			if (result != null && !result.isEmpty()) vars.put(var, result);
    			sender.sendLangfileMessage("command.var.created", var, result);
    		}
    		else throw new CommandException("command.var.invalidArg", sender);
    	}
    	else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
		
		return null;
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
	public int getDefaultPermissionLevel(String[] args) {
		return 0;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return commandName.endsWith("global") ? true : isSenderOfEntityType(sender, EntityPlayerMP.class);
	}
}