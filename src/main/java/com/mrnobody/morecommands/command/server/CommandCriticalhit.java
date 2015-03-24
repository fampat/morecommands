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
		name = "criticalhit",
		description = "command.criticalhit.description",
		example = "command.criticalhit.example",
		syntax = "command.criticalhit.syntax",
		videoURL = "command.criticalhit.videoURL"
		)
public class CommandCriticalhit extends ServerCommand {

	@Override
	public String getCommandName() {
		return "criticalhit";
	}

	@Override
	public String getUsage() {
		return "command.criticalhit.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
	    EntityPlayerMP player = (EntityPlayerMP) sender.toPlayer().getMinecraftPlayer();
	    	
	    boolean criticalhit = false;
	    boolean success = false;
	    	
	    if (params.length >= 1) {
	    	if (params[0].toLowerCase().equals("true")) {criticalhit = true; success = true;}
	    	else if (params[0].toLowerCase().equals("false")) {criticalhit = false; success = true;}
	    	else if (params[0].toLowerCase().equals("0")) {criticalhit = false; success = true;}
	    	else if (params[0].toLowerCase().equals("1")) {criticalhit = true; success = true;}
	    	else if (params[0].toLowerCase().equals("on")) {criticalhit = true; success = true;}
	    	else if (params[0].toLowerCase().equals("off")) {criticalhit = false; success = true;}
	    	else {success = false;}
	    }
	    else {criticalhit = !player.getCriticalHit(); success = true;}
	    	
	    if (success) ((EntityPlayerMP) sender.toPlayer().getMinecraftPlayer()).setCriticalhit(criticalhit);
	    	
	    sender.sendLangfileMessageToPlayer(success ? player.getCriticalHit() ? "command.criticalhit.on" : "command.criticalhit.off" : "command.criticalhit.failure", new Object[0]);
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
