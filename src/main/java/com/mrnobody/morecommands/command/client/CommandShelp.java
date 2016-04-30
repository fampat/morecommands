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
	public String getUsage() {
		return "command.shelp.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		String args = "";
		for (String param : params) args += " " + param;
		Minecraft.getMinecraft().thePlayer.sendChatMessage("/help" + args);
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
	public int getDefaultPermissionLevel() {
		return 0;
	}
}
