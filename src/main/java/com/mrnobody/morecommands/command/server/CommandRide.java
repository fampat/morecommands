package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.network.PacketDispatcher;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "ride",
		description = "command.ride.description",
		example = "command.ride.example",
		syntax = "command.ride.syntax",
		videoURL = "command.ride.videoURL"
		)
public class CommandRide extends ServerCommand {

	@Override
	public String getCommandName() {
		return "ride";
	}

	@Override
	public String getUsage() {
		return "command.ride.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		MoreCommands.getMoreCommands().getPacketDispatcher().sendS12Ride((EntityPlayerMP) sender.getMinecraftISender());
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
		return 0;
	}

	@Override
	public void unregisterFromHandler() {}
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return sender instanceof EntityPlayerMP;
	}
}
