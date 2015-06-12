package com.mrnobody.morecommands.command.server;

import java.util.Map;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.CommandEvent;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listeners.Listener;
import com.mrnobody.morecommands.util.DummyCommand;
import com.mrnobody.morecommands.util.DummyCommand.DummyServerCommand;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "alias",
		description = "command.alias.description",
		example = "command.alias.example",
		syntax = "command.alias.syntax",
		videoURL = "command.alias.videoURL"
		)
public class CommandAlias extends ServerCommand implements Listener<CommandEvent> {
	private final net.minecraft.command.CommandHandler commandHandler = (net.minecraft.command.CommandHandler) MinecraftServer.getServer().getCommandManager();
	
	public CommandAlias() {
		EventHandler.COMMAND.getHandler().register(this);
	}
	
	@Override
	public void onEvent(CommandEvent event) {
		if (event.command instanceof DummyServerCommand && event.sender instanceof EntityPlayerMP) {
			DummyServerCommand cmd = (DummyServerCommand) event.command;
			String command = cmd.getOriginalCommandName(event.sender);
			
			if (command != null) {
				event.exception = null;
				event.setCanceled(true);
				
				for (String p : event.parameters) command += " " + p;
				
				if (cmd.getSenderSideMapping().get(event.sender))
					MoreCommands.getMoreCommands().getPacketDispatcher().sendS09ExecuteClientCommand((EntityPlayerMP) event.sender, command);
				else commandHandler.executeCommand(event.sender, command);
			}
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
		ServerPlayerSettings settings = ServerPlayerSettings.playerSettingsMapping.get(sender.getMinecraftISender());
    	
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
				if (commandHandler.getCommands().get(command) != null || settings.clientCommands.contains(command)) {
					boolean clientSide = settings.clientCommands.contains(command);

					if (!clientSide && commandHandler.getCommands().get(command) instanceof DummyServerCommand) {
						command = ((DummyServerCommand) commandHandler.getCommands().get(command)).getOriginalCommandName(sender.getMinecraftISender());
					}
					
					if (commandHandler.getCommands().get(alias) == null) {
						DummyServerCommand cmd = new DummyServerCommand(alias, sender.getMinecraftISender(), command + parameters, clientSide);
						commandHandler.getCommands().put(alias, cmd);
					}
					else if (commandHandler.getCommands().get(alias) instanceof DummyServerCommand) {
						DummyServerCommand cmd = (DummyServerCommand) commandHandler.getCommands().get(alias);
						cmd.setOriginalCommandName(sender.getMinecraftISender(), command + parameters);
						cmd.getSenderSideMapping().put(sender.getMinecraftISender(), clientSide);
					}
					else throw new CommandException("command.alias.overwrite", sender);
					
					if (settings.clientCommands.contains(command)) settings.clientAliasMapping.put(alias, command + parameters);
					else settings.serverAliasMapping.put(alias, command + parameters);
					settings.saveSettings();
					
					sender.sendLangfileMessage("command.alias.success");
				}
				else throw new CommandException("command.generic.notFound", sender);
			}
			else throw new CommandException("command.alias.infiniteRecursion", sender);
		}
		else throw new CommandException("command.alias.invalidUsage", sender);
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}
	
	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	public void unregisterFromHandler() {
		EventHandler.COMMAND.getHandler().unregister(this);
	}
	
	/**
	 * Reads aliases for the given server player and registers them
	 */
	public static void registerAliases(EntityPlayerMP player) {
		Map<String, String> clientAliases = ServerPlayerSettings.playerSettingsMapping.get(player).clientAliasMapping;
		Map<String, String> serverAliases = ServerPlayerSettings.playerSettingsMapping.get(player).serverAliasMapping;
		
		net.minecraft.command.CommandHandler commandHandler = (net.minecraft.command.CommandHandler) MinecraftServer.getServer().getCommandManager();
		String command;
		
		for (String alias : serverAliases.keySet()) {
			command = serverAliases.get(alias).split(" ")[0];
			
			if (!command.equalsIgnoreCase(alias)) {
				if (commandHandler.getCommands().get(command) != null) {
					if (commandHandler.getCommands().get(command) instanceof DummyServerCommand) {
						command = ((DummyServerCommand) commandHandler.getCommands().get(command)).getOriginalCommandName(player);
					}
					
					if (commandHandler.getCommands().get(alias) == null) {
						DummyCommand cmd = new DummyServerCommand(alias, player, serverAliases.get(alias), false);
						commandHandler.getCommands().put(alias, cmd);
					}
					else if (commandHandler.getCommands().get(alias) instanceof DummyServerCommand) {
						DummyServerCommand cmd = (DummyServerCommand) commandHandler.getCommands().get(alias);
						cmd.setOriginalCommandName(player, serverAliases.get(alias));
					}
				}
			}
		}
		
		for (String alias : clientAliases.keySet()) {
			command = clientAliases.get(alias).split(" ")[0];
			
			if (!command.equalsIgnoreCase(alias)) {
				if (commandHandler.getCommands().get(alias) == null) {
					DummyCommand cmd = new DummyServerCommand(alias, player, clientAliases.get(alias), true);
					commandHandler.getCommands().put(alias, cmd);
				}
				else if (commandHandler.getCommands().get(alias) instanceof DummyServerCommand) {
					DummyServerCommand cmd = (DummyServerCommand) commandHandler.getCommands().get(alias);
					cmd.setOriginalCommandName(player, serverAliases.get(alias));
				}
			}
		}
	}
	
	@Override
	public int getPermissionLevel() {
		return 0;
	}
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return sender instanceof EntityPlayerMP;
	}
}
