package com.mrnobody.morecommands.command.server;

import org.apache.commons.lang3.tuple.MutablePair;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandBase;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.settings.MoreCommandsConfig;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

@Command(
		name = "permission",
		description = "command.permission.description",
		syntax = "command.permission.syntax",
		example = "command.permission.example",
		videoURL = "command.permission.videoURL"
		)
public class CommandPermission extends StandardCommand implements ServerCommandProperties {
	@Override
	public String getCommandName() {
		return "permission";
	}

	@Override
	public String getCommandUsage() {
		return "command.permission.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		if (params.length == 0)
			throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
		
		if ((params.length > 3 && params[0].equalsIgnoreCase("setaction")) || (params.length > 2 && params[0].equalsIgnoreCase("setbase"))) {
			String name = params[1]; int level;
			boolean action = params[0].endsWith("action");
			
			try {level = Integer.parseInt(action ? params[3] : params[2]);}
			catch (NumberFormatException nfe) {throw new CommandException("command.permission.notNumeric", sender);}
			
			level = Math.min(MinecraftServer.getServer().getOpPermissionLevel(), level);
			ICommand command = (ICommand) MinecraftServer.getServer().getCommandManager().getCommands().get(name);
			
			if (command instanceof ServerCommand<?>) {
				MutablePair<Integer, TObjectIntMap<String>> setting = MoreCommandsConfig.permissionMapping.get(name);
				
				if (setting == null) 
					MoreCommandsConfig.permissionMapping.put(name, 
							setting = MutablePair.of(-1, (TObjectIntMap<String>) new TObjectIntHashMap<String>(
															gnu.trove.impl.Constants.DEFAULT_CAPACITY, 
															gnu.trove.impl.Constants.DEFAULT_LOAD_FACTOR, 
															-1)));
				
				if (action)
					setting.getRight().put(params[2], level);
				else
					setting.setLeft(level);
				
				((ServerCommand<?>) command).refreshPermissionLevel();
				
				if (action)
					sender.sendLangfileMessage("command.permission.actionPermissionSet", command.getCommandName(), params[2],
							((ServerCommand<?>) command).getRequiredPermissionLevel(new String[] {params[2]}));
				else
					sender.sendLangfileMessage("command.permission.basePermissionSet", command.getCommandName(),
												((ServerCommand<?>) command).getRequiredPermissionLevel());
			}
			else throw new CommandException("command.permission.invalidCommand", sender);
		}
		else if ((params.length > 2 && params[0].equalsIgnoreCase("getaction")) || (params.length > 1 && params[0].equalsIgnoreCase("getbase"))) {
			String name = params[1]; boolean action = params[0].endsWith("action");
			ICommand command = (ICommand) MinecraftServer.getServer().getCommandManager().getCommands().get(name);
			
			if (action && command instanceof CommandBase<?>)
				sender.sendLangfileMessage("command.permission.actionPermissionGet", command.getCommandName(), params[2],
											((CommandBase<?>) command).getRequiredPermissionLevel(new String[] {params[2]}));
			else if (!action && command instanceof net.minecraft.command.CommandBase)
				sender.sendLangfileMessage("command.permission.basePermissionGet", command.getCommandName(), 
											((net.minecraft.command.CommandBase) command).getRequiredPermissionLevel());
			else 
				throw new CommandException("command.permission.invalidCommand", sender);
		}
		else if (params.length > 1 && params[0].toLowerCase().startsWith("reset")) {
			String type = params[0].substring("reset".length());
			
			if (!type.equalsIgnoreCase("base") && !type.equalsIgnoreCase("action") && !type.equalsIgnoreCase("all"))
				throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
			
			if (type.equalsIgnoreCase("action") && params.length <= 2)
				throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
			
			String name = params[1];
			ICommand command = (ICommand) MinecraftServer.getServer().getCommandManager().getCommands().get(name);
			
			if (command instanceof ServerCommand<?>) {
				MutablePair<Integer, TObjectIntMap<String>> setting = MoreCommandsConfig.permissionMapping.get(name);
				
				if (setting != null) {
					if (type.equalsIgnoreCase("base"))
						setting.setLeft(-1);
					else if (type.equalsIgnoreCase("action"))
						setting.getRight().remove(params[2]);
					else if (type.equalsIgnoreCase("all"))
						MoreCommandsConfig.permissionMapping.remove(name);
					
					if (!type.equalsIgnoreCase("all") && setting.getLeft() < 0 && setting.getRight().isEmpty())
						MoreCommandsConfig.permissionMapping.remove(name);
				}
				
				((ServerCommand<?>) command).refreshPermissionLevel();
				
				if (type.equalsIgnoreCase("action"))
					sender.sendLangfileMessage("command.permission.actionPermissionReset", command.getCommandName(), params[2],
												((ServerCommand<?>) command).getRequiredPermissionLevel(new String[] {params[2]}));
				else if (type.equalsIgnoreCase("base"))
					sender.sendLangfileMessage("command.permission.basePermissionReset", command.getCommandName(),
												((ServerCommand<?>) command).getRequiredPermissionLevel());	
				else 
					sender.sendLangfileMessage("command.permission.allReset", command.getCommandName());
			}
			else throw new CommandException("command.permission.invalidCommand", sender);
		}
		else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
		
		return null;
	}
	
	@Override
	public CommandRequirement[] getRequirements() {
		return new CommandRequirement[0];
	}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}

	@Override
	public int getDefaultPermissionLevel(String[] args) {
		return getRequiredPermissionLevel();
	}
	
	@Override
	public int getRequiredPermissionLevel(String[] args) {
		return getRequiredPermissionLevel();
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return MinecraftServer.getServer().getOpPermissionLevel();
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return true;
	}
}
