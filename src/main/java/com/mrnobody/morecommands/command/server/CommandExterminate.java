package com.mrnobody.morecommands.command.server;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Coordinate;
import com.mrnobody.morecommands.wrapper.Player;

@Command(
		name = "exterminate",
		description = "command.exterminate.description",
		example = "command.exterminate.example",
		syntax = "command.exterminate.syntax",
		videoURL = "command.exterminate.videoURL"
		)
public class CommandExterminate extends ServerCommand {

	@Override
	public String getName() {
		return "exterminate";
	}

	@Override
	public String getUsage() {
		return "command.exterminate.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		int strength = 4;
		Entity hit = sender.toPlayer().traceEntity(128.0D);
		
		if (hit != null) {
			if (hit instanceof EntityLiving)  {
				if (params.length > 0) {
					try {strength = Integer.parseInt(params[0]);}
					catch (NumberFormatException nfe) {sender.sendLangfileMessageToPlayer("command.exterminate.invalidArg", new Object[0]);}
				}
				
				Player player = sender.toPlayer();
				player.getWorld().createExplosion(player, new Coordinate(hit.posX, hit.posY, hit.posZ), strength);
				
				sender.sendLangfileMessageToPlayer("command.exterminate.boooom", new Object[0]);
			}
			else sender.sendLangfileMessageToPlayer("command.exterminate.notLiving", new Object[0]);
		}
		else sender.sendLangfileMessageToPlayer("command.exterminate.notFound", new Object[0]);
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
