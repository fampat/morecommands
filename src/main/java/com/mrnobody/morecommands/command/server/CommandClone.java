package com.mrnobody.morecommands.command.server;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Coordinate;
import com.mrnobody.morecommands.wrapper.World;

@Command(
		name = "clone",
		description = "command.clone.description",
		example = "command.clone.example",
		syntax = "command.clone.syntax",
		videoURL = "command.clone.videoURL"
		)
public class CommandClone extends ServerCommand {

	@Override
	public String getName() {
		return "clone";
	}

	@Override
	public String getUsage() {
		return "command.clone.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		int quantity = 1;
		
		if (params.length > 0) {
			try {quantity = Integer.parseInt(params[0]);}
			catch (NumberFormatException nfe) {sender.sendLangfileMessageToPlayer("command.clone.invalidArg", new Object[0]); return;}
		}
		
		Entity entity = sender.toPlayer().traceEntity(128.0D);
		
		if (entity == null) {
			sender.sendLangfileMessageToPlayer("command.clone.noNPCFound", new Object[0]);
			return;
		}
		
		String name = EntityList.getEntityString(entity);
		World world = sender.toPlayer().getWorld();
		Coordinate coord = new Coordinate(entity.posX, entity.posY, entity.posZ);
		
		for (int i = 0; i < quantity; i++) {
			if (!com.mrnobody.morecommands.wrapper.Entity.spawnEntity(name, coord, world)) {
				sender.sendStringMessageToPlayer("An Error occurred during cloning NPC '" + name + "'");
				return;
			}
		}
		
		sender.sendLangfileMessageToPlayer("command.clone.success", new Object[0]);
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
