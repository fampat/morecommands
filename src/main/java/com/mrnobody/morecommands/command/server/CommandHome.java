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
		name = "home",
		description = "command.home.description",
		example = "command.home.example",
		syntax = "command.home.syntax",
		videoURL = "command.home.videoURL"
		)
public class CommandHome extends ServerCommand {

	@Override
	public String getCommandName() {
		return "home";
	}

	@Override
	public String getUsage() {
		return "command.home.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Player player = sender.toPlayer();
		Coordinate spawn = player.getSpawn() == null ? player.getWorld().getSpawn() : player.getSpawn();
		player.setPosition(spawn);
		sender.sendLangfileMessageToPlayer("command.home.atHome", new Object[0]);
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
