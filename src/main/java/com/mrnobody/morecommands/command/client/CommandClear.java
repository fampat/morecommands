package com.mrnobody.morecommands.command.client;

import net.minecraft.client.Minecraft;

import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.command.ClientCommand;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;


@Command(
		name = "clear",
		description = "command.clear.description",
		example = "command.clear.example",
		syntax = "command.clear.syntax",
		videoURL = "command.clear.videoURL"
		)
public class CommandClear extends ClientCommand {

	@Override
	public String getName() {
		return "clear";
	}

	@Override
	public String getUsage() {
		return "command.clear.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Minecraft.getMinecraft().ingameGUI.getChatGUI().clearChatMessages();
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[] {};
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
	public int getPermissionLevel() {
		return 0;
	}
}
