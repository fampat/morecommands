package com.mrnobody.morecommands.command.server;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.AppliedPatches.PlayerPatches;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.settings.ServerPlayerSettings;

import net.minecraft.command.CommandNotFoundException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

@Command(
	name = "macro",
	description = "command.macro.description",
	syntax = "command.macro.syntax",
	example = "command.macro.example",
	videoURL = "command.macro.videoURL"
		)
public class CommandMacro extends StandardCommand implements ServerCommandProperties {
	@Override
	public String getCommandName() {
		return "macro";
	}

	@Override
	public String getCommandUsage() {
		return "command.macro.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		ServerPlayerSettings settings = getPlayerSettings(getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class));
		PlayerPatches playerInfo = getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class).getCapability(PlayerPatches.PATCHES_CAPABILITY, null);
		if (playerInfo != null && playerInfo.clientModded()) throw new CommandException(new CommandNotFoundException());
		
		if (params.length > 0) {
			if ((params[0].equalsIgnoreCase("delete") || params[0].equalsIgnoreCase("del") || params[0].equalsIgnoreCase("remove") || params[0].equalsIgnoreCase("rem")) && params.length > 1) {
				if (!settings.macros.containsKey(params[1])) throw new CommandException("command.macro.notFound", sender, params[1]);
				else {
					settings.macros.remove(params[1]);
				}
			}
			else if ((params[0].equalsIgnoreCase("exec") || params[0].equalsIgnoreCase("execute")) && params.length > 1) {
				List<String> commands = settings.macros.get(params[1]);
				
				if (commands != null) {
					for (String command : commands)
						sender.getServer().getCommandManager().executeCommand(sender.getMinecraftISender(), command);
				}
				else throw new CommandException("command.macro.notFound", sender, params[1]);
			}
			else if ((params[0].equalsIgnoreCase("add") || params[0].equalsIgnoreCase("new") || params[0].equalsIgnoreCase("create") || params[0].equalsIgnoreCase("edit")) && params.length > 2) {
				if (settings.macros.containsKey(params[1])) {
					if (params[0].equalsIgnoreCase("add") || params[0].equalsIgnoreCase("new") || params[0].equalsIgnoreCase("create"))
						throw new CommandException("command.macro.exists", sender, params[1]);
					settings.macros.remove(params[1]);
				}
				
				settings.macros.put(params[1], Lists.newArrayList(rejoinParams(Arrays.copyOfRange(params, 2, params.length)).split(";")));
				sender.sendLangfileMessage("command.macro.createSuccess", params[1]);
			}
			else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
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
		return 0;
	}

	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return isSenderOfEntityType(sender, EntityPlayerMP.class);
	}
}
