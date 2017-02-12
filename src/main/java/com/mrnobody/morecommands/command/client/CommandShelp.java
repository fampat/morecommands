package com.mrnobody.morecommands.command.client;

import com.mrnobody.morecommands.command.ClientCommandProperties;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;

import net.minecraft.client.Minecraft;

@Command(
		name = "shelp",
		description = "command.shelp.description",
		example = "command.shelp.example",
		syntax = "command.shelp.syntax",
		videoURL = "command.shelp.videoURL"
		)
public class CommandShelp extends StandardCommand implements ClientCommandProperties {
	@Override
	public String getCommandName() {
		return "shelp";
	}

	@Override
	public String getCommandUsage() {
		return "command.shelp.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		String args = "";
		for (String param : params) args += " " + param;
		Minecraft.getMinecraft().player.sendChatMessage("/help" + args);
		
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
	public boolean registerIfServerModded() {
		return true;
	}
	
	@Override
	public int getDefaultPermissionLevel(String[] args) {
		return 0;
	}
}
