package com.mrnobody.morecommands.command.server;

import java.text.DecimalFormat;

import net.minecraft.util.ChunkCoordinates;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.command.CommandBase.Requirement;
import com.mrnobody.morecommands.command.CommandBase.ServerType;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Coordinate;
import com.mrnobody.morecommands.wrapper.Player;

import cpw.mods.fml.relauncher.Side;

@Command(
		name = "setspawn",
		description = "command.setspawn.description",
		example = "command.setspawn.example",
		syntax = "command.setspawn.syntax",
		videoURL = "command.setspawn.videoURL"
		)
public class CommandSetspawn extends ServerCommand {

	@Override
	public String getCommandName() {
		return "setspawn";
	}

	@Override
	public String getUsage() {
		return "command.setspawn.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Player player = sender.toPlayer();
		Coordinate coord = player.getPosition();
		DecimalFormat f = new DecimalFormat("#.##");
		
		if (params.length > 2) {
			try {coord = new Coordinate(Double.parseDouble(params[0]), Double.parseDouble(params[1]), Double.parseDouble(params[2]));}
			catch (NumberFormatException nfe) {sender.sendLangfileMessageToPlayer("command.setspawn.invalidPos", new Object[0]); return;}
		}
		
		player.setSpawn(coord);
		sender.sendStringMessageToPlayer("Spawn point set to:"
				+ " X = " + f.format(coord.getX())
				+ "; Y = " + f.format(coord.getY())
				+ "; Z = " + f.format(coord.getZ()));
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}
	
	@Override
	public void unregisterFromHandler() {}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	@Override
	public int getPermissionLevel() {
		return 2;
	}
}
