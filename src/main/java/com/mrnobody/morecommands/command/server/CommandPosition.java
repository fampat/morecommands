package com.mrnobody.morecommands.command.server;

import java.text.DecimalFormat;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Player;

@Command(
		name = "position",
		description = "command.position.description",
		example = "command.position.example",
		syntax = "command.position.syntax",
		videoURL = "command.position.videoURL"
		)
public class CommandPosition extends ServerCommand {

	@Override
	public String getName() {
		return "position";
	}

	@Override
	public String getUsage() {
		return "command.position.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Player player = sender.toPlayer();
		DecimalFormat f = new DecimalFormat("#.##");
		
		sender.sendStringMessageToPlayer("Your current position is:"
				+ " X = " + f.format(player.getPosition().getX())
				+ "; Y = " + f.format(player.getPosition().getY())
				+ "; Z = " + f.format(player.getPosition().getZ()));
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
		return 0;
	}
}
