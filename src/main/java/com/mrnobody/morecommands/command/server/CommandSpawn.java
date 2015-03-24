package com.mrnobody.morecommands.command.server;

import net.minecraft.entity.EntityList;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.command.CommandBase.Requirement;
import com.mrnobody.morecommands.command.CommandBase.ServerType;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Coordinate;
import com.mrnobody.morecommands.wrapper.Entity;
import com.mrnobody.morecommands.wrapper.Player;

import cpw.mods.fml.relauncher.Side;

@Command(
		name = "spawn",
		description = "command.spawn.description",
		example = "command.spawn.example",
		syntax = "command.spawn.syntax",
		videoURL = "command.spawn.videoURL"
		)
public class CommandSpawn extends ServerCommand {

	@Override
	public String getCommandName() {
		return "spawn";
	}

	@Override
	public String getUsage() {
		return "command.spawn.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		if (params.length > 0) {
			if (params[0].equalsIgnoreCase("list")) {
				String list = "";
				for (String name : Entity.getNameToIdEntityList().keySet()) {
					list += name + " (" + Entity.getNameToIdEntityList().get(name) + "), ";
				}
				list = list.substring(0, list.length() - 2);
				sender.sendStringMessageToPlayer(list);
				return;
			}
			else if (params[0].equalsIgnoreCase("random")) {
				params[0] = Entity.getEntityList().get((int)(Math.random() * Entity.getEntityList().size()));
			}
			else if (Entity.getEntityClass(params[0]) == null) {
				try {
					params[0] = EntityList.getStringFromID(Integer.parseInt(params[0]));
					if (params[0] == null) {
						sender.sendLangfileMessageToPlayer("command.spawn.unknownEntityID", new Object[0]);
						return;
					}
				} catch (NumberFormatException nfe) {sender.sendLangfileMessageToPlayer("command.spawn.unknownEntity", new Object[0]); return;}
			}
			
			int quantity = 1;
			if (params.length > 1) {try {quantity = Integer.parseInt(params[1]);} catch (NumberFormatException nfe) {sender.sendLangfileMessageToPlayer("command.spawn.NAN", new Object[0]);}}
			Player player = sender.toPlayer();
			Coordinate coord;
			
			if (params.length > 4) {
				try {coord = new Coordinate(Double.parseDouble(params[2]), Double.parseDouble(params[3]), Double.parseDouble(params[4]));}
				catch (NumberFormatException nfe) {sender.sendLangfileMessageToPlayer("command.spawn.invalidPos", new Object[0]); return;}
			}
			else {
				coord = player.trace(128);
			
				if (coord == null) {
					coord = player.getPosition();
					coord = new Coordinate(coord.getX() + (Math.random() * 10) - 5, coord.getY(), coord.getZ() + (Math.random() * 10) - 5);
				}
			}
			
			for (int i = 0; i < quantity; i++) {
				if (!Entity.spawnEntity(params[0], coord, player.getWorld())) {
					sender.sendLangfileMessageToPlayer("command.spawn.couldNotSpawn", new Object[] {params[0]});
				}
			}
		}
		else {sender.sendLangfileMessageToPlayer("command.spawn.invalidUsage", new Object[0]);}
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
