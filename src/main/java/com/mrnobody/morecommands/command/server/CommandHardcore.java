package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;

@Command(
		name = "hardcore",
		description = "command.hardcore.description",
		example = "command.hardcore.example",
		syntax = "command.hardcore.syntax",
		videoURL = "command.hardcore.videoURL"
		)
public class CommandHardcore extends ServerCommand {

	@Override
    public String getCommandName()
    {
        return "hardcore";
    }

	@Override
    public String getUsage()
    {
        return "command.hardcore.syntax";
    }
    
	@Override
    public void execute(CommandSender sender, String[] params) throws CommandException {
		try {sender.getWorld().setHardcore(parseTrueFalse(params, 0, sender.getWorld().isHardcore()));}
		catch (IllegalArgumentException ex) {throw new CommandException("command.hardcore.failure", sender);}
		
		sender.sendLangfileMessage(sender.getWorld().isHardcore() ? "command.hardcore.on" : "command.hardcore.off");  
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
	public int getPermissionLevel() {
		return 2;
	}
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return true;
	}
}
