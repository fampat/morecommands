package com.mrnobody.morecommands.command.server;

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
		name = "jump",
		description = "command.jump.description",
		example = "command.jump.example",
		syntax = "command.jump.syntax",
		videoURL = "command.jump.videoURL"
		)
public class CommandJump extends ServerCommand {

	@Override
	public String getCommandName() {
		return "jump";
	}

	@Override
	public String getUsage() {
		return "command.jump.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		com.mrnobody.morecommands.wrapper.Player player = sender.toPlayer();
		Coordinate hit = player.trace(128);
		
		if (hit == null) {sender.sendLangfileMessageToPlayer("command.jump.notInSight", new Object[0]);}
		else {
			int y = hit.getBlockY() + 1;
			while (y < 260) {
				if (player.isClear(new Coordinate(hit.getBlockX(), y++, hit.getBlockZ()))) {
					player.setPosition(new Coordinate(hit.getBlockX() + 0.5F, --y, hit.getBlockZ() + 0.5F));
					break;
				}
			}
		}
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
