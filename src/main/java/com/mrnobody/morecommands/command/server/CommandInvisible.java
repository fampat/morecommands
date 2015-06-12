package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "invisible",
		description = "command.invisible.description",
		example = "command.invisible.example",
		syntax = "command.invisible.syntax",
		videoURL = "command.invisible.videoURL"
		)
public class CommandInvisible extends ServerCommand{
	@Override
	public String getCommandName() {
		return "invisible";
	}

	@Override
	public String getUsage() {
		return "command.invisible.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
    	boolean invisible = true;
    	boolean success = false;
    	
    	if (params.length >= 1) {
    		if (params[0].equalsIgnoreCase("true")) {invisible = true; success = true;}
    		else if (params[0].equalsIgnoreCase("false")) {invisible = false; success = true;}
    		else if (params[0].equalsIgnoreCase("0")) {invisible = false; success = true;}
    		else if (params[0].equalsIgnoreCase("1")) {invisible = true; success = true;}
    		else if (params[0].equalsIgnoreCase("on")) {invisible = true; success = true;}
    		else if (params[0].equalsIgnoreCase("off")) {invisible = false; success = true;}
    		else if (params[0].equalsIgnoreCase("enable")) {invisible = true; success = true;}
    		else if (params[0].equalsIgnoreCase("disable")) {invisible = false; success = true;}
    		else {success = false;}
    	}
    	else {invisible = !((EntityPlayerMP) sender.getMinecraftISender()).isInvisible(); success = true;}
    	
    	if (success) ((EntityPlayerMP) sender.getMinecraftISender()).setInvisible(invisible);
    	
    	sender.sendLangfileMessage(success ? invisible ? "command.invisible.on" : "command.invisible.off" : "command.invisible.failure", new Object[0]);
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}
	
	@Override
	public void unregisterFromHandler() {}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	@Override
	public int getPermissionLevel() {
		return 2;
	}
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return sender instanceof EntityPlayerMP;
	}
}
