package com.mrnobody.morecommands.command.server;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Coordinate;
import com.mrnobody.morecommands.wrapper.Player;

@Command(
		name = "ignite",
		description = "command.ignite.description",
		example = "command.ignite.example",
		syntax = "command.ignite.syntax",
		videoURL = "command.ignite.videoURL"
		)
public class CommandIgnite extends ServerCommand {
	private final Block BLOCK_FIRE = Blocks.fire;

	@Override
	public String getName() {
		return "ignite";
	}

	@Override
	public String getUsage() {
		return "command.ignite.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Player player = sender.toPlayer();
		Coordinate ignite = player.trace(128.0D);
		
		if (ignite != null) {
			Coordinate fire = new Coordinate(ignite.getBlockX(), ignite.getBlockY() + 1, ignite.getBlockZ());
			if (player.getWorld().isAirBlock(fire)) player.getWorld().setBlock(fire.toBlockPos(), BLOCK_FIRE);
		}
		else sender.sendLangfileMessageToPlayer("command.ignite.notInSight", new Object[0]);
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
