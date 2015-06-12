package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityList;
import net.minecraft.util.BlockPos;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Entity;

@Command(
		name = "spawn",
		description = "command.spawn.description",
		example = "command.spawn.example",
		syntax = "command.spawn.syntax",
		videoURL = "command.spawn.videoURL"
		)
public class CommandSpawn extends ServerCommand {
	@Override
	public String getName() {
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
				sender.sendStringMessage(list);
				return;
			}
			else if (params[0].equalsIgnoreCase("random")) {
				params[0] = Entity.getEntityList().get((int)(Math.random() * Entity.getEntityList().size()));
			}
			else if (Entity.getEntityClass(params[0]) == null) {
				try {
					params[0] = EntityList.getStringFromID(Integer.parseInt(params[0]));
					if (params[0] == null) {
						sender.sendLangfileMessage("command.spawn.unknownEntityID", new Object[0]);
						return;
					}
				} catch (NumberFormatException nfe) {sender.sendLangfileMessage("command.spawn.unknownEntity", new Object[0]); return;}
			}
			
			int quantity = 1;
			if (params.length > 1) {try {quantity = Integer.parseInt(params[1]);} catch (NumberFormatException nfe) {sender.sendLangfileMessage("command.spawn.NAN", new Object[0]);}}
			BlockPos coord;
			
			if (params.length > 4) {
				try {coord = new BlockPos(Double.parseDouble(params[2]), Double.parseDouble(params[3]), Double.parseDouble(params[4]));}
				catch (NumberFormatException nfe) {sender.sendLangfileMessage("command.spawn.invalidPos", new Object[0]); return;}
			}
			else {
				if (sender.getMinecraftISender() instanceof Entity)
					coord = new Entity((net.minecraft.entity.Entity) sender.getMinecraftISender()).traceBlock(128);
				else coord = sender.getPosition();
			
				if (coord == null) {
					coord = sender.getPosition();
					coord = new BlockPos(coord.getX() + (Math.random() * 10) - 5, coord.getY(), coord.getZ() + (Math.random() * 10) - 5);
				}
			}
			
			for (int i = 0; i < quantity; i++) {
				if (!Entity.spawnEntity(params[0], coord, sender.getWorld())) {
					sender.sendLangfileMessage("command.spawn.couldNotSpawn", new Object[] {params[0]});
				}
			}
		}
		else {sender.sendLangfileMessage("command.spawn.invalidUsage", new Object[0]);}
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
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return true;
	}
}
