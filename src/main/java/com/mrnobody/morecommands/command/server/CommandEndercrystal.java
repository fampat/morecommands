package com.mrnobody.morecommands.command.server;

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
		name = "endercrystal",
		description = "command.endercrystal.description",
		example = "command.endercrystal.example",
		syntax = "command.endercrystal.syntax",
		videoURL = "command.endercrystal.videoURL"
		)
public class CommandEndercrystal extends ServerCommand {

	@Override
	public String getCommandName() {
		return "endercrystal";
	}

	@Override
	public String getUsage() {
		return "command.endercrystal.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Player player = sender.toPlayer();
		Coordinate spawn = player.trace(128.0D);
		
		if (spawn == null) sender.sendLangfileMessageToPlayer("command.endercrystal.notFound", new Object[0]);
		else Entity.spawnEntity("EnderCrystal", spawn, player.getWorld());
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
