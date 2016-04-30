package com.mrnobody.morecommands.command.client;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;
import com.mrnobody.morecommands.command.ClientCommandProperties;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.ClientPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.client.ClientCommandHandler;

@Command(
	name = "macro",
	description = "command.macro.description",
	syntax = "command.macro.syntax",
	example = "command.macro.example",
	videoURL = "command.macro.videoURL"
		)
public class CommandMacro extends StandardCommand implements ClientCommandProperties {
	@Override
	public boolean registerIfServerModded() {
		return true;
	}

	@Override
	public String getCommandName() {
		return "macro";
	}

	@Override
	public String getCommandUsage() {
		return "command.macro.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		if (!isSenderOfEntityType(sender.getMinecraftISender(), EntityPlayerSP.class))
			throw new CommandException("command.generic.notAPlayer", sender);
		
		ClientPlayerSettings settings = getPlayerSettings(getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerSP.class));
		
		if (params.length > 0) {
			if ((params[0].equalsIgnoreCase("delete") || params[0].equalsIgnoreCase("del") || params[0].equalsIgnoreCase("remove") || params[0].equalsIgnoreCase("rem")) && params.length > 1) {
				if (!settings.macros.containsKey(params[1])) throw new CommandException("command.macro.notFound", sender, params[1]);
				else {
					settings.macros = settings.removeAndUpdate("macros", params[1], (Class<List<String>>) (Class<?>) List.class, true);
					sender.sendLangfileMessage("command.macro.deleteSuccess", params[1]);
				}
			}
			else if ((params[0].equalsIgnoreCase("exec") || params[0].equalsIgnoreCase("execute")) && params.length > 1) {
				List<String> commands = settings.macros.get(params[1]);
				
				if (commands != null) {
					for (String command : commands)
						if (ClientCommandHandler.instance.executeCommand(Minecraft.getMinecraft().thePlayer, command) == 0)
							Minecraft.getMinecraft().thePlayer.sendChatMessage(command.startsWith("/") ? command : "/" + command);
				}
				else throw new CommandException("command.macro.notFound", sender, params[1]);
			}
			else if ((params[0].equalsIgnoreCase("add") || params[0].equalsIgnoreCase("new") || params[0].equalsIgnoreCase("create") || params[0].equalsIgnoreCase("edit")) && params.length > 2) {
				if (settings.macros.containsKey(params[1]) && (params[0].equalsIgnoreCase("add") || params[0].equalsIgnoreCase("new") || params[0].equalsIgnoreCase("create")))
						throw new CommandException("command.macro.exists", sender, params[1]);
				
				settings.macros = settings.putAndUpdate("macros", params[1],Lists.newArrayList(rejoinParams(Arrays.copyOfRange(params, 2, params.length)).split(";")),
						(Class<List<String>>) (Class<?>) List.class, true);
				sender.sendLangfileMessage("command.macro.createSuccess", params[1]);
			}
			else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
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
		return 0;
	}
}
