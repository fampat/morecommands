package com.mrnobody.morecommands.command.server;

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.MultipleCommands;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.event.EventHandler;
import com.mrnobody.morecommands.event.Listeners.EventListener;
import com.mrnobody.morecommands.patch.PatchList;
import com.mrnobody.morecommands.patch.PatchManager;
import com.mrnobody.morecommands.patch.PatchManager.AppliedPatches;
import com.mrnobody.morecommands.settings.GlobalSettings;
import com.mrnobody.morecommands.settings.MoreCommandsConfig;
import com.mrnobody.morecommands.settings.ServerPlayerSettings;
import com.mrnobody.morecommands.util.DummyCommand;

import net.minecraft.command.CommandNotFoundException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.CommandEvent;

@Command.MultipleCommand(
		name = {"alias", "alias_global", "unalias", "unalias_global"},
		description = {"command.alias.description", "command.alias.global.description", "command.unalias.description", "command.unalias.global.description"},
		example = {"command.alias.example", "command.alias.global.example", "command.unalias.example", "command.unalias.global.example"},
		syntax = {"command.alias.syntax", "command.alias.global.syntax", "command.unalias.syntax", "command.unalias.global.syntax"},
		videoURL = {"command.alias.videoURL", "command.alias.global.videoURL", "command.unalias.videoURL", "command.unalias.global.videoURL"}
		)
public class CommandAlias extends MultipleCommands implements ServerCommandProperties, EventListener<CommandEvent> {
	public CommandAlias(int typeIndex) {
		super(typeIndex);
	}
	
	public CommandAlias() {
		super();
		EventHandler.COMMAND.register(this);
	}
	
	@Override
	public void onEvent(CommandEvent event) {
		if (event.command instanceof DummyCommand && !((DummyCommand) event.command).isClient()) {
			String command = null;
			
			if (isSenderOfEntityType(event.sender, EntityPlayerMP.class)) {
				AppliedPatches playerInfo = PatchManager.instance().getAppliedPatchesForPlayer(getSenderAsEntity(event.sender, EntityPlayerMP.class));
				command = playerInfo != null && playerInfo.wasPatchSuccessfullyApplied(PatchList.CLIENT_MODDED) ? null :
				getPlayerSettings(getSenderAsEntity(event.sender, EntityPlayerMP.class)).aliases.get(event.command.getName());
				
				if (command == null && MoreCommandsConfig.enableGlobalAliases)
					command = GlobalSettings.getInstance().aliases.get(ImmutablePair.of(event.sender.getEntityWorld().getSaveHandler().getWorldDirectoryName(), event.sender.getEntityWorld().provider.getDimensionName())).get(event.command.getName());
				else if (!MoreCommandsConfig.enablePlayerAliases)
					command = null;
			}
			else if (MoreCommandsConfig.enableGlobalAliases)
				command = GlobalSettings.getInstance().aliases.get(ImmutablePair.of(event.sender.getEntityWorld().getSaveHandler().getWorldDirectoryName(), event.sender.getEntityWorld().provider.getDimensionName())).get(event.command.getName());
			
			if (command != null) {
				event.exception = null;
				event.setCanceled(true);
				
				command += " " + rejoinParams(event.parameters);
				MinecraftServer.getServer().getCommandManager().executeCommand(event.sender, command);
			}
			else {
				event.exception = new CommandNotFoundException();
				event.setCanceled(true);
			}
		}
	}
	
	@Override
	public String[] getCommandNames() {
		return new String[] {"alias", "alias_global", "unalias", "unalias_global"};
	}

	@Override
	public String[] getCommandUsages() {
		return new String[] {"command.alias.syntax", "command.alias.global.syntax", "command.unalias.syntax", "command.unalias.global.syntax"};
	}

	@Override
	public String execute(String commandName, CommandSender sender, String[] params) throws CommandException {
		boolean global = commandName.endsWith("global"), remove = commandName.startsWith("unalias");
		ServerPlayerSettings settings = global ? null : getPlayerSettings(getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class));
		ServerCommandManager commandManager = (ServerCommandManager) MinecraftServer.getServer().getCommandManager();
		
		if (!global) {
			AppliedPatches playerInfo = PatchManager.instance().getAppliedPatchesForPlayer(getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class));
			if (playerInfo != null && playerInfo.wasPatchSuccessfullyApplied(PatchList.CLIENT_MODDED)) throw new CommandException(new CommandNotFoundException());
		}
		
		if (global && !MoreCommandsConfig.enableGlobalAliases)
			throw new CommandException("command.alias.global.aliasesDisabled", sender);
		else if (!global && !MoreCommandsConfig.enablePlayerAliases)
			throw new CommandException("command.alias.aliasesDisabled", sender);
		
		String world = sender.getWorld().getSaveHandler().getWorldDirectoryName(), dim = sender.getWorld().provider.getDimensionName();
		Map<String, String> aliases = global ? GlobalSettings.getInstance().aliases.get(ImmutablePair.of(world, dim)) : settings.aliases;
		
		if (remove) {
			if (params.length > 0) {
				String alias = params[0];
				ICommand command = (ICommand) commandManager.getCommands().get(alias);
				
				if (command != null && command instanceof DummyCommand && aliases.containsKey(alias)) {
					aliases.remove(alias);
					sender.sendLangfileMessage("command.unalias.success");
				}
				else throw new CommandException("command.unalias.notFound", sender);
			}
			else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
		}
		else {
			if (params.length > 1) {
				String alias = params[0];
				String command = params[1];
				String parameters = params.length > 2 ? " " + rejoinParams(Arrays.copyOfRange(params, 2, params.length)) : "";
				
				if (!command.equalsIgnoreCase(alias)) {
					if (commandManager.getCommands().get(alias) == null) {
						DummyCommand cmd = new DummyCommand(alias, false);
						commandManager.getCommands().put(alias, cmd);
					}
					else if (!(commandManager.getCommands().get(alias) instanceof DummyCommand))
						throw new CommandException("command.alias.overwrite", sender);
					
					aliases.put(alias, command + parameters);
					sender.sendLangfileMessage("command.alias.success");
				}
				else throw new CommandException("command.alias.infiniteRecursion", sender);
			}
			else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
		}
		
		return null;
	}
	
	@Override
	public CommandRequirement[] getRequirements() {
		return new CommandRequirement[] {};
	}
	
	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	@Override
	public int getDefaultPermissionLevel(String[] args) {
		return 0;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return commandName.endsWith("global") ? true : isSenderOfEntityType(sender, EntityPlayerMP.class);
	}
}
