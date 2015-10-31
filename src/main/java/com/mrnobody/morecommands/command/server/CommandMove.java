package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Coordinate;
import com.mrnobody.morecommands.wrapper.Player;

@Command(
		name = "move",
		description = "command.move.description",
		example = "command.move.example",
		syntax = "command.move.syntax",
		videoURL = "command.move.videoURL"
		)
public class CommandMove extends ServerCommand {

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
		Player player = new Player((EntityPlayerMP) sender.getMinecraftISender());
		
		if (params.length > 1) {
			try {
				int distance = Integer.parseInt(params[0]);
				
				if (params[1].toUpperCase().startsWith("N")) {
					player.setPosition(new Coordinate(player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ() - distance));
				} else if (params[1].toUpperCase().startsWith("E")) {
					player.setPosition(new Coordinate(player.getPosition().getX() + distance, player.getPosition().getY(), player.getPosition().getZ()));
				} else if (params[1].toUpperCase().startsWith("S")) {
					player.setPosition(new Coordinate(player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ() + distance));
				} else if (params[1].toUpperCase().startsWith("W")) {
					player.setPosition(new Coordinate(player.getPosition().getX() - distance, player.getPosition().getY(), player.getPosition().getZ()));
				} else if (params[1].toUpperCase().startsWith("U")) {
					player.setPosition(new Coordinate(player.getPosition().getX(), player.getPosition().getY() + distance, player.getPosition().getZ()));
				} else if (params[1].toUpperCase().startsWith("D")) {
					player.setPosition(new Coordinate(player.getPosition().getX(), player.getPosition().getY() - distance, player.getPosition().getZ()));
				} else throw new CommandException("command.move.invalidDirection", sender);
			} catch (NumberFormatException e) {throw new CommandException("command.move.NAN", sender);}
		}
		else throw new CommandException("command.move.invalidUsage", sender);
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
