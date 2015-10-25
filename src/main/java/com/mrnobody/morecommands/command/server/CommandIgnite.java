package com.mrnobody.morecommands.command.server;

import net.minecraft.block.Block;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
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
	public String getCommandName() {
		return "ignite";
	}

	@Override
	public String getUsage() {
		return "command.ignite.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Player player = new Player((EntityPlayerMP) sender.getMinecraftISender());
		Coordinate ignite = player.traceBlock(128.0D);
		
		if (ignite != null) {
			Coordinate fire = new Coordinate(ignite.getBlockX(), ignite.getBlockY() + 1, ignite.getBlockZ());
			if (player.getWorld().isAirBlock(fire)) player.getWorld().setBlock(fire, BLOCK_FIRE);
		}
		else throw new CommandException("command.ignite.notInSight", sender);
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	@Override
	public int getPermissionLevel() {
		return 2;
	}
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return sender instanceof EntityPlayerMP;
	}
}
