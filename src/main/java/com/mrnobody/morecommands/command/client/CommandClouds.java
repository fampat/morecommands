package com.mrnobody.morecommands.command.client;

import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.command.ClientCommand;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.client.Minecraft;

@Command(
		name = "clouds",
		description = "command.clouds.description",
		example = "command.clouds.example",
		syntax = "command.clouds.syntax",
		videoURL = "command.clouds.videoURL"
		)
public class CommandClouds extends ClientCommand {
	@Override
	public String getCommandName() {
		return "clouds";
	}

	@Override
	public String getUsage() {
		return "command.clouds.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		try {Minecraft.getMinecraft().gameSettings.clouds = parseTrueFalse(params, 0, Minecraft.getMinecraft().gameSettings.clouds);}
		catch (IllegalArgumentException ex) {throw new CommandException("command.clouds.failure", sender);}
		
		sender.sendLangfileMessage(Minecraft.getMinecraft().gameSettings.clouds ? "command.clouds.on" : "command.clouds.off");
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
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
