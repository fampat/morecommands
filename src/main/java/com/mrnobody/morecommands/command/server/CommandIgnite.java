package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Entity;

import net.minecraft.command.ICommandSender;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

@Command(
		name = "ignite",
		description = "command.ignite.description",
		example = "command.ignite.example",
		syntax = "command.ignite.syntax",
		videoURL = "command.ignite.videoURL"
		)
public class CommandIgnite extends StandardCommand implements ServerCommandProperties {
	@Override
	public String getCommandName() {
		return "ignite";
	}

	@Override
	public String getCommandUsage() {
		return "command.ignite.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		BlockPos ignite;
		
		if (params.length > 2)
			ignite = getCoordFromParams(sender.getMinecraftISender(), params, 0);
		else
			ignite = new Entity(getSenderAsEntity(sender.getMinecraftISender(), net.minecraft.entity.Entity.class)).traceBlock(128D);
		
		if (ignite != null) {
			BlockPos fire = new BlockPos(ignite.getX(), ignite.getY() + 1, ignite.getZ());
			if (sender.getWorld().isAirBlock(fire)) sender.getWorld().setBlock(fire, Blocks.fire);
		}
		else throw new CommandException("command.ignite.notInSight", sender);
	}
	
	@Override
	public CommandRequirement[] getRequirements() {
		return new CommandRequirement[0];
	}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	@Override
	public int getDefaultPermissionLevel() {
		return 2;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return params.length > 2 ? true : isSenderOfEntityType(sender, net.minecraft.entity.Entity.class);
	}
}
