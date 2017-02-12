package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.settings.ServerPlayerSettings;
import com.mrnobody.morecommands.util.Coordinate;
import com.mrnobody.morecommands.util.EntityUtils;
import com.mrnobody.morecommands.util.WorldUtils;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

@Command(
		name = "home",
		description = "command.home.description",
		example = "command.home.example",
		syntax = "command.home.syntax",
		videoURL = "command.home.videoURL"
		)
public class CommandHome extends StandardCommand implements ServerCommandProperties {

	@Override
	public String getCommandName() {
		return "home";
	}

	@Override
	public String getCommandUsage() {
		return "command.home.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		boolean global = params.length > 0 && params[0].equalsIgnoreCase("global");
		
		ServerPlayerSettings settings = getPlayerSettings(getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class));
		EntityPlayerMP player = getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class);
		
		if (settings != null)
			settings.lastTeleport = settings.lastPos = EntityUtils.getPosition(player);
		
		Coordinate spawn = global || EntityUtils.getSpawn(player) == null ? WorldUtils.getSpawn(player.worldObj) : EntityUtils.getSpawn(player);
		EntityUtils.setPosition(player, spawn);
		
		sender.sendLangfileMessage("command.home.atHome");
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
		return 2;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return isSenderOfEntityType(sender, EntityPlayerMP.class);
	}
}
