package com.mrnobody.morecommands.command.client;

import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.event.CommandEvent;

import com.mrnobody.morecommands.command.ClientCommand;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.core.Patcher;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listeners.Listener;
import com.mrnobody.morecommands.util.ClientPlayerSettings;
import com.mrnobody.morecommands.util.DummyCommand;
import com.mrnobody.morecommands.util.DummyCommand.DummyClientCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "alias",
		description = "command.alias.description",
		example = "command.alias.example",
		syntax = "command.alias.syntax",
		videoURL = "command.alias.videoURL"
		)
public class CommandAlias extends ClientCommand implements Listener<CommandEvent> {
	private final net.minecraft.command.CommandHandler commandHandler = ClientCommandHandler.instance;
	
	public CommandAlias() {
		EventHandler.COMMAND.getHandler().register(this);
	}
	
	@Override
	public void onEvent(CommandEvent event) {
		if (this.isEnabled(Minecraft.getMinecraft().thePlayer) && Patcher.serverModded()) return;
		if (event.command instanceof DummyClientCommand) {
			DummyClientCommand cmd = (DummyClientCommand) event.command;
			String command = cmd.getOriginalCommandName();
			
			event.exception = null;
			event.setCanceled(true);
				
			for (String p : event.parameters) command += " " + p;
			commandHandler.executeCommand(event.sender, command);
		}
	}

	@Override
	public String getCommandName() {
		return "alias";
	}

	@Override
	public String getUsage() {
		return "command.alias.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		if (params.length > 1) {
			String alias = params[0];
			String command = params[1];
			String parameters = "";
			
			if (params.length > 2) {
				int index = 0;
				
				for (String param : params) {
					if (index > 1) {parameters += " " + param;}
					index++;
				}
			}
			
			if (!command.equalsIgnoreCase(alias)) {
				if (commandHandler.getCommands().get(command) != null) {
					if (commandHandler.getCommands().get(command) instanceof DummyClientCommand) {
						command = ((DummyClientCommand) commandHandler.getCommands().get(command)).getOriginalCommandName();
					}
					
					if (commandHandler.getCommands().get(alias) == null) {
						DummyCommand cmd = new DummyClientCommand(alias, command + parameters);
						commandHandler.getCommands().put(alias, cmd);
					}
					else if (commandHandler.getCommands().get(alias) instanceof DummyClientCommand) {
						DummyClientCommand cmd = (DummyClientCommand) commandHandler.getCommands().get(alias);
						cmd.setOriginalCommandName(command + parameters);
					}
					else {
						sender.sendLangfileMessage("command.alias.overwrite", new Object[0]);
						return;
					}
					
					ClientPlayerSettings.aliasMapping.put(alias, command + parameters);
					ClientPlayerSettings.saveSettings();
					
					sender.sendLangfileMessage("command.alias.success", new Object[0]);
				}
				else {sender.sendLangfileMessage("command.generic.notFound", new Object[0]);}
			}
			else {sender.sendLangfileMessage("command.alias.infiniteRecursion", new Object[0]);}
		}
		else {sender.sendLangfileMessage("command.alias.invalidUsage", new Object[0]);}
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[] {Requirement.PATCH_CLIENTCOMMANDHANDLER};
	}
	
	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}

	@Override
	public boolean registerIfServerModded() {
		return false;
	}
	
	/**
	 * Reads aliases for the client player and registers them
	 */
	public static void registerAliases() {
		Map<String, String> aliases = ClientPlayerSettings.aliasMapping;
		net.minecraft.command.CommandHandler commandHandler = ClientCommandHandler.instance;
		String command;
		
		for (String alias : aliases.keySet()) {
			command = aliases.get(alias).split(" ")[0];
			
			if (!command.equalsIgnoreCase(alias)) {
				if (commandHandler.getCommands().get(command) != null) {
					if (commandHandler.getCommands().get(command) instanceof DummyClientCommand) {
						command = ((DummyClientCommand) commandHandler.getCommands().get(command)).getOriginalCommandName();
					}
					
					if (commandHandler.getCommands().get(alias) == null) {
						DummyCommand cmd = new DummyClientCommand(alias, aliases.get(alias));
						commandHandler.getCommands().put(alias, cmd);
					}
					else if (commandHandler.getCommands().get(alias) instanceof DummyClientCommand) {
						DummyClientCommand cmd = (DummyClientCommand) commandHandler.getCommands().get(alias);
						cmd.setOriginalCommandName(aliases.get(alias));
					}
				}
			}
		}
	}

	@Override
	public int getPermissionLevel() {
		return 0;
	}
}
