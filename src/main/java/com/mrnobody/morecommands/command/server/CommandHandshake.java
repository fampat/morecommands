package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.AppliedPatches.PlayerPatches;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;

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
	public String execute(CommandSender sender, String[] params) throws CommandException {
		EntityPlayerMP player = getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class);
		PlayerPatches patches = player.getCapability(PlayerPatches.PATCHES_CAPABILITY, null);
		
		if (params.length > 0 && params[0].equalsIgnoreCase("status")) {
			if (patches != null && patches.clientModded()) sender.sendLangfileMessage("command.handshake.completed");
			else sender.sendLangfileMessage("command.handshake.errored");
		}
		else if (params.length > 0 && params[0].equalsIgnoreCase("redo")) {
			if (patches != null && patches.clientModded()) throw new CommandException("command.handshake.handshakeFinished", sender);
			sender.sendLangfileMessage("command.handshake.sendingHandshake");
			MoreCommands.INSTANCE.getPacketDispatcher().sendS00Handshake(player);
			MoreCommands.INSTANCE.getPacketDispatcher().sendS14RemoteWorld(player, player.worldObj.getSaveHandler().getWorldDirectory().getName());
		}
		else throw new CommandException("command.handshake.invalidArgs", sender);
		
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
		return 0;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return isSenderOfEntityType(sender, EntityPlayerMP.class);
	}
}
