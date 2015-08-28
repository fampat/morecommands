package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.core.AppliedPatches;
import com.mrnobody.morecommands.core.AppliedPatches.PlayerPatches;
import com.mrnobody.morecommands.network.PacketDispatcher;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

@Command(
		name = "handshake",
		description = "command.handshake.description",
		example = "command.handshake.example",
		syntax = "command.handshake.syntax",
		videoURL = "command.handshake.videoURL"
		)
public class CommandHandshake extends ServerCommand {
	@Override
	public String getName() {
		return "handshake";
	}

	@Override
	public String getUsage() {
		return "command.handshake.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		PlayerPatches patches = AppliedPatches.playerPatchMapping.get(sender.getMinecraftISender());
		
		if (params.length > 0 && params[0].equalsIgnoreCase("status")) {
			if (patches.handshakeFinished()) sender.sendLangfileMessage("command.handshake.completed");
			else if (patches.clientModded()) sender.sendLangfileMessage("command.handshake.importantSent");
			else sender.sendLangfileMessage("command.handshake.errored");
		}
		else if (params.length > 0 && params[0].equalsIgnoreCase("redo")) {
			if (patches.handshakeFinished()) throw new CommandException("command.handshake.handshakeFinished", sender);
			sender.sendLangfileMessage("command.handshake.sendingHandshake");
			MoreCommands.getMoreCommands().getPacketDispatcher().sendS00Handshake((EntityPlayerMP) sender.getMinecraftISender());
		}
		else throw new CommandException("command.handshake.invalidArgs", sender);
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
