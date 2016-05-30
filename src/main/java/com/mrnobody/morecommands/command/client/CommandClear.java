package com.mrnobody.morecommands.command.client;

import com.mrnobody.morecommands.command.ClientCommandProperties;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.client.Minecraft;


@Command(
		name = "clear",
		description = "command.clear.description",
		example = "command.clear.example",
		syntax = "command.clear.syntax",
		videoURL = "command.clear.videoURL"
		)
public class CommandClear extends StandardCommand implements ClientCommandProperties {

	@Override
	public String getCommandName() {
		return "clear";
	}

	@Override
	public String getCommandUsage() {
		return "command.clear.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Minecraft.getMinecraft().ingameGUI.getChatGUI().clearChatMessages();
	}
	
	@Override
	public CommandRequirement[] getRequirements() {
		return new CommandRequirement[] {};
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
