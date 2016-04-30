package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.GlobalSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.fml.common.FMLCommonHandler;

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
	public void execute(CommandSender sender, String[] params) throws CommandException {
		if (params.length > 2 && params[0].equalsIgnoreCase("set")) {
			String name = params[1]; int level;
			
			try {level = Integer.parseInt(params[2]);}
			catch (NumberFormatException nfe) {throw new CommandException("command.permission.notNumeric", sender);}
			
			level = level > 4 ? 4 : level < 0 ? 0 : level;
			ICommand command = (ICommand) sender.getServer().getCommandManager().getCommands().get(name);
			
			if (command instanceof ServerCommand<?>) {
				GlobalSettings.permissionMapping.put(name, level);
				((ServerCommand<?>) command).refreshPermissionLevel();
				sender.sendLangfileMessage("command.permission.permissionSet", command.getCommandName(), ((ServerCommand<?>) command).getRequiredPermissionLevel());
			}
			else throw new CommandException("command.permission.invalidCommand", sender);
		}
		else if (params.length > 1 && params[0].equalsIgnoreCase("get")) {
			String name = params[1];
			ICommand command = (ICommand) sender.getServer().getCommandManager().getCommands().get(name);
			
			if (command instanceof CommandBase)
				sender.sendLangfileMessage("command.permission.getPermission", command.getCommandName(), ((CommandBase) command).getRequiredPermissionLevel());
			else throw new CommandException("command.permission.invalidCommand", sender);
		}
		else if (params.length > 1 && params[0].equalsIgnoreCase("reset")) {
			String name = params[1];
			ICommand command = (ICommand) sender.getServer().getCommandManager().getCommands().get(name);
			
			if (command instanceof ServerCommand<?>) {
				GlobalSettings.permissionMapping.remove(command.getCommandName());
				((ServerCommand<?>) command).refreshPermissionLevel();
				sender.sendLangfileMessage("command.permission.reseted", command.getCommandName(), ((ServerCommand<?>) command).getRequiredPermissionLevel());
			}
			else throw new CommandException("command.permission.invalidCommand", sender);
		}
		else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
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
	public int getDefaultPermissionLevel() {
		return getRequiredPermissionLevel();
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return FMLCommonHandler.instance().getMinecraftServerInstance().getOpPermissionLevel();
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return true;
	}
}
