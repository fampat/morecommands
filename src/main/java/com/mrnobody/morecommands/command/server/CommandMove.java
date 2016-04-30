package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Coordinate;
import com.mrnobody.morecommands.wrapper.Entity;

import net.minecraft.command.ICommandSender;

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
	public String getUsage() {
		return "command.move.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Entity entity = new Entity(getSenderAsEntity(sender.getMinecraftISender(), net.minecraft.entity.Entity.class));
		
		if (params.length > 1) {
			try {
				int distance = Integer.parseInt(params[0]);
				
				if (params[1].toUpperCase().startsWith("N")) {
					entity.setPosition(new Coordinate(entity.getPosition().getX(), entity.getPosition().getY(), entity.getPosition().getZ() - distance));
				} else if (params[1].toUpperCase().startsWith("E")) {
					entity.setPosition(new Coordinate(entity.getPosition().getX() + distance, entity.getPosition().getY(), entity.getPosition().getZ()));
				} else if (params[1].toUpperCase().startsWith("S")) {
					entity.setPosition(new Coordinate(entity.getPosition().getX(), entity.getPosition().getY(), entity.getPosition().getZ() + distance));
				} else if (params[1].toUpperCase().startsWith("W")) {
					entity.setPosition(new Coordinate(entity.getPosition().getX() - distance, entity.getPosition().getY(), entity.getPosition().getZ()));
				} else if (params[1].toUpperCase().startsWith("U")) {
					entity.setPosition(new Coordinate(entity.getPosition().getX(), entity.getPosition().getY() + distance, entity.getPosition().getZ()));
				} else if (params[1].toUpperCase().startsWith("D")) {
					entity.setPosition(new Coordinate(entity.getPosition().getX(), entity.getPosition().getY() - distance, entity.getPosition().getZ()));
				} else throw new CommandException("command.move.invalidDirection", sender);
			} catch (NumberFormatException e) {throw new CommandException("command.move.NAN", sender);}
		}
		else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
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
		return isSenderOfEntityType(sender, net.minecraft.entity.Entity.class);
	}
}
