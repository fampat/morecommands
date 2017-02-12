package com.mrnobody.morecommands.command.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.MultipleCommands;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.settings.GlobalSettings;
import com.mrnobody.morecommands.settings.ServerPlayerSettings;
import com.mrnobody.morecommands.settings.SettingsProperty;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumChatFormatting;

@Command.MultipleCommand(
		name = {"settings_server", "settings_global"},
		description = {"command.settings.server.description", "command.settings.global.description"},
		example = {"command.settings.server.example", "command.settings.global.example"},
		syntax = {"command.settings.server.syntax", "command.settings.global.syntax"},
		videoURL = {"command.settings.videoURL", "command.settings.global.videoURL"}
		)
public class CommandSettings extends MultipleCommands implements ServerCommandProperties {
	private static final int PAGE_MAX = 15;
	
	public CommandSettings() {}
	
	public CommandSettings(int typeIndex) {
		super(typeIndex);
	}
	
	@Override
	public String[] getCommandNames() {
		return new String[] {"settings_server", "settings_global"};
	}

	@Override
	public String[] getCommandUsages() {
		return new String[] {"command.settings.server.sytnax", "command.settings.global.syntax"};
	}
	
	@Override
	public String execute(String commandName, CommandSender sender, String[] params) throws CommandException {
		boolean global = commandName.endsWith("global");
		ServerPlayerSettings settings = global ? null : getPlayerSettings(getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class));
		
		if (params.length > 0) {
			if (params[0].equalsIgnoreCase("list")) {
				List<String> list = new ArrayList<String>();
				
				if (global)
					list.addAll(GlobalSettings.GLOBAL_SETTINGS);
				else {
					list.addAll(ServerPlayerSettings.COMMON_SETTINGS);
					list.addAll(ServerPlayerSettings.SERVER_SETTINGS);
				}
				
				int page = 0;
				
				try {
					page = Integer.parseInt(params[1]) - 1; 
					if (page < 0) page = 0;
					else if (page * PAGE_MAX > list.size()) page = list.size() / PAGE_MAX;
				}
				catch (NumberFormatException e) {throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());}
				
				final int stop = (page + 1) * PAGE_MAX;;
				for (int i = page * PAGE_MAX; i < stop && i < list.size(); i++)
					sender.sendStringMessage(" - '" + list.get(i) + "'");
				
				sender.sendLangfileMessage("command.settings." + (global ? "global" : "server") + ".more", EnumChatFormatting.RED);
			}
			else if (params[0].equalsIgnoreCase("reload")) {
				if (!global) settings.refresh();
				else throw new CommandException("command.settings.reloadGlobal", sender);
				
				sender.sendLangfileMessage("command.settings.reloaded");
			}
			else if (params.length > 1) {
				String type = params[1];
				
				if (!global ? (!ServerPlayerSettings.SERVER_SETTINGS.contains(type) && !ServerPlayerSettings.COMMON_SETTINGS.contains(type)) :
					!GlobalSettings.GLOBAL_SETTINGS.contains(type)) throw new CommandException("command.settings.notASetting", sender, type);
				
				if (params[0].equalsIgnoreCase("reset")) {
					if (global)
						GlobalSettings.getInstance().setPutProperties(type, false, false);
					else 
						settings.setPutProperties(type, "waypoints".equals(type) ? new SettingsProperty[] {SettingsProperty.WORLD_PROPERTY, SettingsProperty.DIMENSION_POPERTY} : new SettingsProperty[0]);
					
					sender.sendLangfileMessage("command.settings.reseted", type);
				}
				else if (params[0].equalsIgnoreCase("set")) {
					if (global) {
						if (params.length > 2 && !params[2].equalsIgnoreCase(SettingsProperty.WORLD_PROPERTY.getName()) && !params[2].equalsIgnoreCase(SettingsProperty.DIMENSION_POPERTY.getName()))
							throw new CommandException("command.settings.invalidProperty", sender, params[2]);
						
						if (params.length > 3 && !params[3].equalsIgnoreCase(SettingsProperty.WORLD_PROPERTY.getName()) && !params[3].equalsIgnoreCase(SettingsProperty.DIMENSION_POPERTY.getName()))
							throw new CommandException("command.settings.invalidProperty", sender, params[3]);
						
						boolean world = (params.length > 2 && params[2].equalsIgnoreCase(SettingsProperty.WORLD_PROPERTY.getName())) || (params.length > 3 && params[3].equalsIgnoreCase(SettingsProperty.WORLD_PROPERTY.getName()));
						boolean dim = (params.length > 2 && params[2].equalsIgnoreCase(SettingsProperty.DIMENSION_POPERTY.getName())) || (params.length > 3 && params[3].equalsIgnoreCase(SettingsProperty.DIMENSION_POPERTY.getName()));
						
						GlobalSettings.getInstance().setPutProperties(type, world, dim);
					}
					else {
						Set<SettingsProperty> set = EnumSet.noneOf(SettingsProperty.class);
						
						if (params.length > 2) {
							for (String s : Arrays.copyOfRange(params, 2, params.length)) {
								SettingsProperty prop = SettingsProperty.getByName(s);
								if (prop == null) throw new CommandException("command.settings.invalidProperty", sender, s);
								set.add(prop);
							}
						}
						
						settings.setPutProperties(type, set.toArray(new SettingsProperty[set.size()]));
					}
					sender.sendLangfileMessage("command.settings.set", type);
				}
				else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
			}
			else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
		}
		else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
		
		return null;
	}

	@Override
	public CommandRequirement[] getRequirements() {
		return new CommandRequirement[0];
	}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}

	@Override
	public int getDefaultPermissionLevel(String[] args) {
		return this.getCommandName().endsWith("global") ? 2 : 0;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return commandName.endsWith("global") ? true : isSenderOfEntityType(sender, EntityPlayerMP.class);
	}
}
