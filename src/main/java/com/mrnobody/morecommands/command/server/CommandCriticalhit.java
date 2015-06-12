package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.patch.EntityPlayerMP;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

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
	    EntityPlayerMP player = (EntityPlayerMP) sender.getMinecraftISender();
	    	
	    boolean criticalhit = false;
	    boolean success = false;
	    	
	    if (params.length >= 1) {
	    	if (params[0].equalsIgnoreCase("true")) {criticalhit = true; success = true;}
	    	else if (params[0].equalsIgnoreCase("false")) {criticalhit = false; success = true;}
	    	else if (params[0].equalsIgnoreCase("0")) {criticalhit = false; success = true;}
	    	else if (params[0].equalsIgnoreCase("1")) {criticalhit = true; success = true;}
	    	else if (params[0].equalsIgnoreCase("on")) {criticalhit = true; success = true;}
	    	else if (params[0].equalsIgnoreCase("off")) {criticalhit = false; success = true;}
    		else if (params[0].equalsIgnoreCase("enable")) {criticalhit = true; success = true;}
    		else if (params[0].equalsIgnoreCase("disable")) {criticalhit = false; success = true;}
	    	else {success = false;}
	    }
	    else {criticalhit = !player.getCriticalHit(); success = true;}
	    	
	    if (success) ((EntityPlayerMP) sender.getMinecraftISender()).setCriticalhit(criticalhit);
	    	
	    sender.sendLangfileMessage(success ? player.getCriticalHit() ? "command.criticalhit.on" : "command.criticalhit.off" : "command.criticalhit.failure", new Object[0]);
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
