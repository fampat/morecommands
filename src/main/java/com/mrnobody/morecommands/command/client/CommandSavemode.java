package com.mrnobody.morecommands.command.client;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Triple;

import com.mrnobody.morecommands.command.ClientCommandProperties;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.ClientPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.util.EnumChatFormatting;

@Command(
		name = "savemode_client",
		description = "command.savemode.client.description",
		example = "command.savemode.client.example",
		syntax = "command.savemode.client.syntax",
		videoURL = "command.savemode.client.videoURL"
		)
public class CommandSavemode extends StandardCommand implements ClientCommandProperties {
	private static final int PAGE_MAX = 15;
	
	@Override
	public String getCommandName() {
		return "savemode_client";
	}

	@Override
	public String getUsage() {
		return "command.savemode.client.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		if (!isSenderOfEntityType(sender.getMinecraftISender(), EntityClientPlayerMP.class))
			throw new CommandException("command.generic.notAPlayer", sender);
		
		ClientPlayerSettings settings = getPlayerSettings(getSenderAsEntity(sender.getMinecraftISender(), EntityClientPlayerMP.class));
		
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
				
				sender.sendLangfileMessage("command.savemode.client.more", EnumChatFormatting.RED);
			}
			else if (params.length > 1) {
				String type = params[1];
				
				if (!ClientPlayerSettings.CLIENT_SETTINGS.contains(type) && !ClientPlayerSettings.COMMON_SETTINGS.contains(type))
					throw new CommandException("command.savemode.notASetting", sender, type);
				
				Triple<Boolean, Boolean, Boolean> default_ = settings.getSaveProperties(type);
				
				if (params[0].equalsIgnoreCase("reset")) {
					settings.clearProps(type);
					sender.sendLangfileMessage("command.savemode.reseted", type);
				}
				else if (params.length > 2 && params[0].equalsIgnoreCase("set")){
					try {
						boolean server = params.length > 2 ? parseTrueFalse(params, 2, default_.getLeft()) : default_.getLeft();
						boolean world = params.length > 3 ? parseTrueFalse(params, 3, default_.getMiddle()) : default_.getMiddle();
						boolean dim = params.length > 4 ? parseTrueFalse(params, 4, default_.getRight()) : default_.getRight();
						
						settings.setProps(type, server, world, dim);
						sender.sendLangfileMessage("command.savemode.set", type);
					}
					catch (IllegalArgumentException ex) {throw new CommandException("command.savemode.invalidArg", sender);}
				}
				else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
			}
			else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
		}
		else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
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
	public int getDefaultPermissionLevel() {
		return 0;
	}
	
	@Override
	public boolean registerIfServerModded() {
		return true;
	}
}
