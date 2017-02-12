package com.mrnobody.morecommands.command.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.mrnobody.morecommands.command.ClientCommandProperties;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.settings.ClientPlayerSettings;
import com.mrnobody.morecommands.settings.SettingsProperty;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.EnumChatFormatting;

@Command(
		name = "settings_client",
		description = "command.settings.client.description",
		example = "command.settings.client.example",
		syntax = "command.settings.client.syntax",
		videoURL = "command.settings.client.videoURL"
		)
public class CommandSettings extends StandardCommand implements ClientCommandProperties {
	private static final int PAGE_MAX = 15;
	
	@Override
	public String getCommandName() {
		return "settings_client";
	}
	
	@Override
	public String getCommandUsage() {
		return "command.settings.client.syntax";
	}
	
	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		if (!isSenderOfEntityType(sender.getMinecraftISender(), EntityPlayerSP.class))
			throw new CommandException("command.generic.notAPlayer", sender);

		ClientPlayerSettings settings = getPlayerSettings(getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerSP.class));
		
		if (params.length > 0) {
			if (params[0].equalsIgnoreCase("list")) {
				List<String> list = new ArrayList<String>();
				list.addAll(ClientPlayerSettings.COMMON_SETTINGS);
				list.addAll(ClientPlayerSettings.CLIENT_SETTINGS);
				
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
				
				sender.sendLangfileMessage("command.settings.client.more", EnumChatFormatting.RED);
			}
			else if (params[0].equalsIgnoreCase("reload")) {
				settings.refresh();
				sender.sendLangfileMessage("command.settings.reloaded");
			}
			else if (params.length > 1) {
				String type = params[1];
				
				if (!ClientPlayerSettings.CLIENT_SETTINGS.contains(type) && !ClientPlayerSettings.COMMON_SETTINGS.contains(type))
					throw new CommandException("command.settings.notASetting", sender, type);
				
				if (params[0].equalsIgnoreCase("reset")) {
					settings.setPutProperties(type, SettingsProperty.SERVER_PROPERTY);
					sender.sendLangfileMessage("command.settings.reseted", type);
				}
				else if (params[0].equalsIgnoreCase("set")){
					Set<SettingsProperty> set = EnumSet.noneOf(SettingsProperty.class);
					
					if (params.length > 2) {
						for (String s : Arrays.copyOfRange(params, 2, params.length)) {
							SettingsProperty prop = SettingsProperty.getByName(s);
							if (prop == null) throw new CommandException("command.settings.invalidProperty", sender, s);
							set.add(prop);
						}
					}
					
					settings.setPutProperties(type, set.toArray(new SettingsProperty[set.size()]));
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
		return 0;
	}
	
	@Override
	public boolean registerIfServerModded() {
		return true;
	}
}
