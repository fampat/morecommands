package com.mrnobody.morecommands.command.server;

import java.util.List;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Entity;
import com.mrnobody.morecommands.wrapper.Player;

@Command(
		name = "killall",
		description = "command.killall.description",
		example = "command.killall.example",
		syntax = "command.killall.syntax",
		videoURL = "command.killall.videoURL"
		)
public class CommandKillall extends ServerCommand {

	@Override
	public String getName() {
		return "killall";
	}

	@Override
	public String getUsage() {
		return "command.killall.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Player player = sender.toPlayer();
		double radius = 128.0D;
		String entityType = "mob";
		
		if (params.length > 0) {
			entityType = params[0];
			
			if (Entity.getEntityClass(entityType) == null) {
				try {
					radius = Double.parseDouble(params[0]);
					entityType = "mob";
				}
				catch (NumberFormatException e) {sender.sendLangfileMessageToPlayer("command.killall.unknownEntity", new Object[0]);}
			}
			
			if (params.length > 1) {
				try {radius = Double.parseDouble(params[1]);}
				catch (NumberFormatException e) {sender.sendLangfileMessageToPlayer("command.killall.NAN", new Object[0]);}
			}
			
			if (radius <= 0 || radius > 256) {sender.sendLangfileMessageToPlayer("command.killall.invalidRadius", new Object[0]);}
			else {
				List<net.minecraft.entity.Entity> removedEntities = Entity.killEntities(entityType, player.getPosition(), player.getWorld(), radius);
				sender.sendLangfileMessageToPlayer("command.killall.killed", new Object[] {removedEntities.size()});
			}
		}
		else sender.sendLangfileMessageToPlayer("command.killall.invalidUsage", new Object[0]);
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
