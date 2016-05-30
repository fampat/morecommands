package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.AppliedPatches.PlayerPatches;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
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
public class CommandHandshake extends StandardCommand implements ServerCommandProperties {
	@Override
	public String getCommandName() {
		return "handshake";
	}

	@Override
	public String getCommandUsage() {
		return "command.handshake.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		PlayerPatches patches = getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class).getCapability(PlayerPatches.PATCHES_CAPABILITY, null);
		
		if (params.length > 0 && params[0].equalsIgnoreCase("status")) {
			if (patches != null && patches.clientModded()) sender.sendLangfileMessage("command.handshake.completed");
			else sender.sendLangfileMessage("command.handshake.errored");
		}
		else if (params.length > 0 && params[0].equalsIgnoreCase("redo")) {
			if (patches != null && patches.clientModded()) throw new CommandException("command.handshake.handshakeFinished", sender);
			sender.sendLangfileMessage("command.handshake.sendingHandshake");
			MoreCommands.INSTANCE.getPacketDispatcher().sendS00Handshake(getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class));
		}
		else throw new CommandException("command.handshake.invalidArgs", sender);
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
		return 0;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return isSenderOfEntityType(sender, EntityPlayerMP.class);
	}
}
