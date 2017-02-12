package com.mrnobody.morecommands.command.server;

import java.text.DecimalFormat;
import java.util.Arrays;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.Coordinate;
import com.mrnobody.morecommands.util.EntityUtils;
import com.mrnobody.morecommands.util.WorldUtils;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

@Command(
		name = "setspawn",
		description = "command.setspawn.description",
		example = "command.setspawn.example",
		syntax = "command.setspawn.syntax",
		videoURL = "command.setspawn.videoURL"
		)
public class CommandSetspawn extends StandardCommand implements ServerCommandProperties {

	@Override
	public String getCommandName() {
		return "setspawn";
	}

	@Override
	public String getCommandUsage() {
		return "command.setspawn.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		boolean global = params.length > 0 && params[0].equalsIgnoreCase("global");
		if (global) params = Arrays.copyOfRange(params, 1, params.length);
		
		EntityPlayerMP player = global ? null : getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class);
		Coordinate coord = EntityUtils.getPosition(player);
		DecimalFormat f = new DecimalFormat("#.##");
		
		if (params.length > 2) {
			try {coord = getCoordFromParams(sender.getMinecraftISender(), params, 0);}
			catch (NumberFormatException nfe) {throw new CommandException("command.setspawn.invalidPos", sender);}
		}
		
		if (!global) EntityUtils.setSpawn(player, coord);
		else WorldUtils.setSpawn(sender.getWorld(), coord);
		
		sender.sendStringMessage((global ? "Global " : "") + "Spawn point set to:"
				+ " X = " + f.format(coord.getX())
				+ "; Y = " + f.format(coord.getY())
				+ "; Z = " + f.format(coord.getZ()));
		
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
		if (params.length > 0 && params[0].equalsIgnoreCase("global")) return true;
		else return isSenderOfEntityType(sender, EntityPlayerMP.class);
	}
}
