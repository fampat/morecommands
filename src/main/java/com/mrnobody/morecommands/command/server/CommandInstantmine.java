package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.command.CommandBase.Requirement;
import com.mrnobody.morecommands.command.CommandBase.ServerType;
import com.mrnobody.morecommands.patch.EntityPlayerMP;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import cpw.mods.fml.relauncher.Side;

@Command(
		name = "instantmine",
		description = "command.instantmine.description",
		example = "command.instantmine.example",
		syntax = "command.instantmine.syntax",
		videoURL = "command.instantmine.videoURL"
		)
public class CommandInstantmine extends ServerCommand {
	@Override
	public String getCommandName() {
		return "instantmine";
	}

	@Override
	public String getUsage() {
		return "command.instantmine.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		EntityPlayerMP player = (EntityPlayerMP) sender.toPlayer().getMinecraftPlayer();
	    	
	    boolean instantmine = false;
	    boolean success = false;
	    	
	    if (params.length >= 1) {
	    	if (params[0].toLowerCase().equals("true")) {instantmine = true; success = true;}
	    	else if (params[0].toLowerCase().equals("false")) {instantmine = false; success = true;}
	    	else if (params[0].toLowerCase().equals("0")) {instantmine = false; success = true;}
	    	else if (params[0].toLowerCase().equals("1")) {instantmine = true; success = true;}
	    	else if (params[0].toLowerCase().equals("on")) {instantmine = true; success = true;}
	    	else if (params[0].toLowerCase().equals("off")) {instantmine = false; success = true;}
	    	else {success = false;}
	    }
	    else {instantmine = !player.getInstantmine(); success = true;}
	    	
	    if (success) player.setInstantmine(instantmine);
	    	
	    sender.sendLangfileMessageToPlayer(success ? instantmine ? "command.instantmine.on" : "command.instantmine.off" : "command.instantmine.failure", new Object[0]);
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
}
