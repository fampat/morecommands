package com.mrnobody.morecommands.command.client;

import net.minecraft.client.Minecraft;

import com.mrnobody.morecommands.command.ClientCommand;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

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
        if (params.length > 0) {
        	if (params[0].equalsIgnoreCase("enable") || params[0].equalsIgnoreCase("1")
            	|| params[0].equalsIgnoreCase("on") || params[0].equalsIgnoreCase("true")) {
        		Minecraft.getMinecraft().gameSettings.clouds = true;
            	sender.sendLangfileMessage("command.clouds.on");
            }
            else if (params[0].equalsIgnoreCase("disable") || params[0].equalsIgnoreCase("0")
            		|| params[0].equalsIgnoreCase("off") || params[0].equalsIgnoreCase("false")) {
        		Minecraft.getMinecraft().gameSettings.clouds = false;
            	sender.sendLangfileMessage("command.clouds.off");
            }
            else throw new CommandException("command.clouds.failure", sender);
        }
        else {
        	Minecraft.getMinecraft().gameSettings.clouds = !Minecraft.getMinecraft().gameSettings.clouds;
        	sender.sendLangfileMessage(Minecraft.getMinecraft().gameSettings.clouds ? "command.clouds.on" : "command.clouds.off");
        }
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
