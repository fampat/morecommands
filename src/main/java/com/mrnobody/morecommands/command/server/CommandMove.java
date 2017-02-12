package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.EntityUtils;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;

@Command(
		name = "move",
		description = "command.move.description",
		example = "command.move.example",
		syntax = "command.move.syntax",
		videoURL = "command.move.videoURL"
		)
public class CommandMove extends StandardCommand implements ServerCommandProperties {

	@Override
	public String getCommandName() {
		return "move";
	}

	@Override
	public String getCommandUsage() {
		return "command.move.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		Entity entity = getSenderAsEntity(sender.getMinecraftISender(), Entity.class);
		
		if (params.length > 1) {
			try {
				int distance = Integer.parseInt(params[0]);
				
				if (params[1].toUpperCase().startsWith("N")) {
					EntityUtils.setPosition(entity, new BlockPos(entity.posX, entity.posY, entity.posZ - distance));
				} else if (params[1].toUpperCase().startsWith("E")) {
					EntityUtils.setPosition(entity, new BlockPos(entity.posX + distance, entity.posY, entity.posZ));
				} else if (params[1].toUpperCase().startsWith("S")) {
					EntityUtils.setPosition(entity, new BlockPos(entity.posX, entity.posY, entity.posZ + distance));
				} else if (params[1].toUpperCase().startsWith("W")) {
					EntityUtils.setPosition(entity, new BlockPos(entity.posX - distance, entity.posY, entity.posZ));
				} else if (params[1].toUpperCase().startsWith("U")) {
					EntityUtils.setPosition(entity, new BlockPos(entity.posX, entity.posY + distance, entity.posZ));
				} else if (params[1].toUpperCase().startsWith("D")) {
					EntityUtils.setPosition(entity, new BlockPos(entity.posX, entity.posY - distance, entity.posZ));
				} else throw new CommandException("command.move.invalidDirection", sender);
			} catch (NumberFormatException e) {throw new CommandException("command.move.NAN", sender);}
		}
		else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
		
		return null;
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
	public int getDefaultPermissionLevel(String[] args) {
		return 2;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return isSenderOfEntityType(sender, Entity.class);
	}
}
