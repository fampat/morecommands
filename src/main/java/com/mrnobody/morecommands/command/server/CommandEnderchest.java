package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Player;

@Command(
		name = "enderchest",
		description = "command.enderchest.description",
		example = "command.enderchest.example",
		syntax = "command.enderchest.syntax",
		videoURL = "command.enderchest.videoURL"
		)
public class CommandEnderchest extends ServerCommand {

	@Override
	public String getName() {
		return "enderchest";
	}

	@Override
	public String getUsage() {
		return "command.enderchest.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Player player = sender.toPlayer();
		player.getMinecraftPlayer().displayGUIChest(player.getMinecraftPlayer().getInventoryEnderChest());
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
