package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.patch.EntityPlayerMP;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
	description = "command.sprint.description",
	example = "command.sprint.example",
	name = "sprint",
	syntax = "command.sprint.syntax",
	videoURL = "command.sprint.videoURL"
		)
public class CommandSprint extends ServerCommand {

	@Override
	public void unregisterFromHandler() {}

	@Override
	public String getCommandName() {
		return "sprint";
	}

	@Override
	public String getUsage() {
		return "command.sprint.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		EntityPlayerMP player = (EntityPlayerMP) this.getCommandSenderAsPlayer(sender.getMinecraftISender());
		
    	boolean sprinting = false;
    	boolean success = false;
    	
    	if (params.length >= 1) {
    		if (params[0].toLowerCase().equals("true")) {sprinting = true; success = true;}
    		else if (params[0].toLowerCase().equals("false")) {sprinting = false; success = true;}
    		else if (params[0].toLowerCase().equals("0")) {sprinting = false; success = true;}
    		else if (params[0].toLowerCase().equals("1")) {sprinting = true; success = true;}
    		else if (params[0].toLowerCase().equals("on")) {sprinting = true; success = true;}
    		else if (params[0].toLowerCase().equals("off")) {sprinting = false; success = true;}
    		else {success = false;}
    	}
    	else {sprinting = !player.getInfniteSprinting(); success = true;}
    	
    	if (success) {player.setInfniteSprinting(sprinting); player.setSprinting(sprinting);}
    	
    	sender.sendLangfileMessageToPlayer(success ? sprinting ? "command.sprint.on" : "command.sprint.off" : "command.sprint.failure", new Object[0]);
	}

	@Override
	public Requirement[] getRequirements() {
		return new Requirement[] {Requirement.PATCH_ENTITYPLAYERMP};
	}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}

	@Override
	public int getPermissionLevel() {
		return 2;
	}
}
