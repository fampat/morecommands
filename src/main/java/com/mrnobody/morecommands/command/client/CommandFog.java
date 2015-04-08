package com.mrnobody.morecommands.command.client;

import net.minecraft.client.Minecraft;

import com.mrnobody.morecommands.command.ClientCommand;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "fog",
		description = "command.fog.description",
		example = "command.fog.example",
		syntax = "command.fog.syntax",
		videoURL = "command.fog.videoURL"
		)
public class CommandFog extends ClientCommand {

	@Override
	public String getCommandName() {
		return "fog";
	}

	@Override
	public String getUsage() {
		return "command.fog.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params)throws CommandException {
		if (params.length > 0) {
			int distance = 0;
			
			if (params[0].equalsIgnoreCase("tiny")) distance = 2;
			else if (params[0].equalsIgnoreCase("small")) distance = 6;
			else if (params[0].equalsIgnoreCase("normal")) distance = 10;
			else if (params[0].equalsIgnoreCase("far")) distance = 16;
			else {sender.sendLangfileMessage("command.fog.invalidArg", new Object[0]); return;}
			
			Minecraft.getMinecraft().gameSettings.renderDistanceChunks = distance;
			
			sender.sendLangfileMessage("command.fog.success", new Object[0]);
		}
		else sender.sendLangfileMessage("command.fog.invalidUsage", new Object[0]);
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
