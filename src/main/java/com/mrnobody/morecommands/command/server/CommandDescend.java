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
		name = "descend",
		description = "command.descend.description",
		example = "command.descend.example",
		syntax = "command.descend.syntax",
		videoURL = "command.descend.videoURL"
		)
public class CommandDescend extends ServerCommand {

	@Override
	public String getCommandName() {
		return "descend";
	}

	@Override
	public String getUsage() {
		return "command.descend.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
    	Player player = sender.toPlayer();
    	Coordinate coord = player.getPosition();
    	int y = coord.getBlockY() - 1;
    	
    	while (y > 0) {
    		if (player.isClear(new Coordinate(coord.getBlockX(), y--, coord.getBlockZ()))) {
    			player.setPosition(new Coordinate(coord.getBlockX() + 0.5F, ++y, coord.getBlockZ() + 0.5F));
    			sender.sendLangfileMessageToPlayer("command.descend.descended", new Object[] {Math.abs((y - coord.getBlockY()))});
    			break;
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
