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
		name = "fog",
		description = "command.fog.description",
		example = "command.fog.example",
		syntax = "command.fog.syntax",
		videoURL = "command.fog.videoURL"
		)
public class CommandFog extends StandardCommand implements ClientCommandProperties {

	@Override
	public String getCommandName() {
		return "fog";
	}

	@Override
	public String getUsage() {
		return "command.fog.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		if (params.length > 0) {
			int distance = 0;
			
			if (params[0].equalsIgnoreCase("tiny")) distance = 2;
			else if (params[0].equalsIgnoreCase("small")) distance = 6;
			else if (params[0].equalsIgnoreCase("normal")) distance = 10;
			else if (params[0].equalsIgnoreCase("far")) distance = 16;
			else throw new CommandException("command.fog.invalidArg", sender);
			
			Minecraft.getMinecraft().gameSettings.renderDistanceChunks = distance;
			
			sender.sendLangfileMessage("command.fog.success");
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
	public boolean registerIfServerModded() {
		return true;
	}
	
	@Override
	public int getDefaultPermissionLevel() {
		return 0;
	}
}
