package com.mrnobody.morecommands.command.server;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.MultipleCommands;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.GlobalSettings;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextFormatting;

@Command.MultipleCommand(
		name = {"savemode_server", "savemode_global"},
		description = {"command.savemode.server.description", "command.savemode.global.description"},
		example = {"command.savemode.server.example", "command.savemode.global.example"},
		syntax = {"command.savemode.server.syntax", "command.savemode.global.syntax"},
		videoURL = {"command.savemode.videoURL", "command.savemode.global.videoURL"}
		)
public class CommandSavemode extends MultipleCommands implements ServerCommandProperties {
	private static final int PAGE_MAX = 15;
	
	public CommandSavemode() {}
	
	public CommandSavemode(int typeIndex) {
		super(typeIndex);
	}
	
	@Override
	public String[] getNames() {
		return new String[] {"savemode_server", "savemode_global"};
	}

	@Override
	public String[] getUsages() {
		return new String[] {"command.savemode.server.sytnax", "command.savemode.global.syntax"};
	}

	@Override
	public void execute(String commandName, CommandSender sender, String[] params) throws CommandException {
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
				
				sender.sendLangfileMessage("command.savemode." + (global ? "global" : "server") + ".more", TextFormatting.RED);
			}
			else if (params.length > 1) {
				String type = params[1];
				
				if (!global ? (!ServerPlayerSettings.SERVER_SETTINGS.contains(type) && !ServerPlayerSettings.COMMON_SETTINGS.contains(type)) :
					!GlobalSettings.GLOBAL_SETTINGS.contains(type)) throw new CommandException("command.savemode.notASetting", sender, type);
				
				Pair<Boolean, Boolean> default_;
				if (global) {default_ = ImmutablePair.of(GlobalSettings.getSaveProp(type).getLeft(), GlobalSettings.getSaveProp(type).getRight());}
				else {default_ = ImmutablePair.of(settings.getSaveProperties(type).getLeft(), GlobalSettings.getSaveProp(type).getRight());}
				
				if (params[0].equalsIgnoreCase("reset")) {
					if (global) GlobalSettings.putSaveProp(type, false, false);
					else settings.clearProps(type);
					sender.sendLangfileMessage("command.savemode.reseted", type);
				}
				else if (params.length > 2 && params[0].equalsIgnoreCase("set")) {
					try {
						boolean world = params.length > 2 ? parseTrueFalse(params, 3, default_.getLeft()) : default_.getLeft();
						boolean dim = params.length > 3 ? parseTrueFalse(params, 4, default_.getRight()) : default_.getRight();
						
						if (global) GlobalSettings.putSaveProp(type, world, dim);
						else settings.setProps(type, false, world, dim);
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
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return commandName.endsWith("global") ? true : isSenderOfEntityType(sender, EntityPlayerMP.class);
	}
}
