package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Coordinate;
import com.mrnobody.morecommands.wrapper.EntityLivingBase;

import net.minecraft.command.ICommandSender;
import net.minecraft.init.Blocks;

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
	public String getUsage() {
		return "command.ignite.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Coordinate ignite;
		
		if (params.length > 2)
			ignite = getCoordFromParams(sender.getMinecraftISender(), params, 0);
		else
			ignite = new EntityLivingBase(getSenderAsEntity(sender.getMinecraftISender(), net.minecraft.entity.EntityLivingBase.class)).traceBlock(128D);
		
		if (ignite != null) {
			Coordinate fire = new Coordinate(ignite.getBlockX(), ignite.getBlockY() + 1, ignite.getBlockZ());
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
		return params.length > 2 ? true : isSenderOfEntityType(sender, net.minecraft.entity.EntityLivingBase.class);
	}
}
