package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.patch.EntityPlayerMP;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "instantmine",
		description = "command.instantmine.description",
		example = "command.instantmine.example",
		syntax = "command.instantmine.syntax",
		videoURL = "command.instantmine.videoURL"
		)
public class CommandInstantmine extends ServerCommand {
	@Override
	public String getName() {
		return "instantmine";
	}

	@Override
	public String getUsage() {
		return "command.instantmine.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		EntityPlayerMP player = (EntityPlayerMP) sender.getMinecraftISender();
	    	
	    boolean instantmine = false;
	    boolean success = false;
	    	
	    if (params.length >= 1) {
	    	if (params[0].equalsIgnoreCase("true")) {instantmine = true; success = true;}
	    	else if (params[0].equalsIgnoreCase("false")) {instantmine = false; success = true;}
	    	else if (params[0].equalsIgnoreCase("0")) {instantmine = false; success = true;}
	    	else if (params[0].equalsIgnoreCase("1")) {instantmine = true; success = true;}
	    	else if (params[0].equalsIgnoreCase("on")) {instantmine = true; success = true;}
	    	else if (params[0].equalsIgnoreCase("off")) {instantmine = false; success = true;}
    		else if (params[0].equalsIgnoreCase("enable")) {instantmine = true; success = true;}
    		else if (params[0].equalsIgnoreCase("disable")) {instantmine = false; success = true;}
	    	else {success = false;}
	    }
	    else {instantmine = !player.getInstantmine(); success = true;}
	    	
	    if (success) player.setInstantmine(instantmine);
	    	
	    sender.sendLangfileMessage(success ? instantmine ? "command.instantmine.on" : "command.instantmine.off" : "command.instantmine.failure", new Object[0]);
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[] {Requirement.PATCH_ENTITYPLAYERMP};
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
