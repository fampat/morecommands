package com.mrnobody.morecommands.command.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.command.ClientCommand;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommand;
import net.minecraftforge.client.ClientCommandHandler;

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
	
	private Map<String, ClientCommand> clientCommands;
	
	public CommandClientcommands() {
		 this.clientCommands = new HashMap<String, ClientCommand>();
	}
	
	public CommandClientcommands(Map<String, ClientCommand> clientCommands) {
		 this.clientCommands = clientCommands;
	}
	
	@Override
    public String getCommandName()
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
        if (params.length > 0) {
        	if (params[0].equalsIgnoreCase("enable") || params[0].equalsIgnoreCase("1")
        		|| params[0].equalsIgnoreCase("on") || params[0].equalsIgnoreCase("true")) {
        		this.enableClientCommands();
        		sender.sendLangfileMessage("command.clientcommands.on");
        	}
        	else if (params[0].equalsIgnoreCase("disable") || params[0].equalsIgnoreCase("0")
        			|| params[0].equalsIgnoreCase("off") || params[0].equalsIgnoreCase("false")) {
            	this.disableClientCommands();
            	sender.sendLangfileMessage("command.clientcommands.off");
            }
        	else throw new CommandException("command.clientcommands.failure", sender);
        }
        else throw new CommandException("command.clientcommands.failure", sender);
    }
	
	private void enableClientCommands() {
		for (ClientCommand command : this.clientCommands.values()) 
			if (!(command instanceof CommandClientcommands)) ClientCommandHandler.instance.registerCommand(command);
		
		this.clientCommands.clear();
		enabled = true;
	}

	private void disableClientCommands() {
		if (this.clientCommands.size() != 0) return;
		this.clientCommands.clear();
		
		for (Map.Entry<String, ICommand> entry : (Set<Map.Entry<String, ICommand>>) ClientCommandHandler.instance.getCommands().entrySet())
			if (entry.getValue() instanceof ClientCommand) this.clientCommands.put(entry.getKey(), (ClientCommand) entry.getValue());
		
		for (String command : this.clientCommands.keySet()) 
			ClientCommandHandler.instance.getCommands().remove(command);
		
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
