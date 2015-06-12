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
	private boolean clouds = true;
	
	@Override
	public String getName() {
		return "clouds";
	}

	@Override
	public String getUsage() {
		return "command.clouds.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {    		
    	boolean clouds = false;
    	boolean success = false;
    		
        if (params.length >= 1) {
        	if (params[0].equalsIgnoreCase("true")) {clouds = true; success = true;}
        	else if (params[0].equalsIgnoreCase("false")) {clouds = false; success = true;}
        	else if (params[0].equalsIgnoreCase("0")) {clouds = false; success = true;}
        	else if (params[0].equalsIgnoreCase("1")) {clouds = true; success = true;}
        	else if (params[0].equalsIgnoreCase("on")) {clouds = true; success = true;}
        	else if (params[0].equalsIgnoreCase("off")) {clouds = false; success = true;}
    		else if (params[0].equalsIgnoreCase("enable")) {clouds = true; success = true;}
    		else if (params[0].equalsIgnoreCase("disable")) {clouds = false; success = true;}
        	else {success = false;}
        }
        else {clouds = !this.clouds; success = true;}
        	
        if (success) {this.clouds = clouds; Minecraft.getMinecraft().gameSettings.clouds = clouds;}
        	
        sender.sendLangfileMessage(success ? clouds ? "command.clouds.on" : "command.clouds.off" : "command.clouds.failure", new Object[0]);
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
