package com.mrnobody.morecommands.command.client;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.command.ICommand;
import net.minecraftforge.client.ClientCommandHandler;

import com.mrnobody.morecommands.command.ClientCommand;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "clientcommands",
		description = "command.clientcommands.description",
		example = "command.clientcommands.example",
		syntax = "command.clientcommands.syntax",
		videoURL = "command.clientcommands.videoURL"
		)
public class CommandClientcommands extends ClientCommand
{
	private static boolean enabled = true;
	
	public static boolean clientCommandsEnabled() {return enabled;}
	
	private Map<String, ICommand> clientCommands;
	
	public CommandClientcommands() {
		 this.clientCommands = new HashMap<String, ICommand>();
	}
	
	public CommandClientcommands(Map<String, ICommand> clientCommands) {
		 this.clientCommands = clientCommands;
	}
	
	@Override
    public String getName()
    {
        return "clientcommands";
    }

	@Override
    public String getUsage()
    {
        return "command.clientcommands.syntax";
    }
    
	@Override
    public void execute(CommandSender sender, String[] params) throws CommandException {
    	boolean clientcommands = false;
        boolean success = false;
        	
        if (params.length >= 1) {
        	if (params[0].toLowerCase().equals("enable")) {clientcommands = true; success = true;}
        	else if (params[0].toLowerCase().equals("disable")) {clientcommands = false; success = true;}
        	else if (params[0].toLowerCase().equals("0")) {clientcommands = false; success = true;}
        	else if (params[0].toLowerCase().equals("1")) {clientcommands = true; success = true;}
        	else if (params[0].toLowerCase().equals("on")) {clientcommands = true; success = true;}
        	else if (params[0].toLowerCase().equals("off")) {clientcommands = false; success = true;}
        }
        	
        if (success && clientcommands) this.enableClientCommands();
        else if (success && !clientcommands) this.disableClientCommands();
        	
        sender.sendLangfileMessageToPlayer(success ? clientcommands ? "command.clientcommands.on" : "command.clientcommands.off" : "command.clientcommands.failure", new Object[0]);
    }
	
	private void enableClientCommands() {
		for (ICommand command : this.clientCommands.values()) if (!(command instanceof CommandClientcommands)) ClientCommandHandler.instance.registerCommand(command);
		this.clientCommands.clear();
		enabled = true;
	}

	private void disableClientCommands() {
		if (this.clientCommands.size() != 0) return;
		this.clientCommands.clear();
		this.clientCommands.putAll(ClientCommandHandler.instance.getCommands());
		ClientCommandHandler.instance.getCommands().clear();
		ClientCommandHandler.instance.registerCommand(new CommandClientcommands(this.clientCommands));
		enabled = false;
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
