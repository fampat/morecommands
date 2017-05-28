package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.patch.PatchEntityPlayerMP.EntityPlayerMP;

import net.minecraft.command.ICommandSender;

@Command(
		name = "instantkill",
		description = "command.instantkill.description",
		example = "command.instantkill.example",
		syntax = "command.instantkill.syntax",
		videoURL = "command.instantkill.videoURL"
		)
public class CommandInstantkill extends StandardCommand implements ServerCommandProperties {
	@Override
	public String getCommandName() {
		return "instantkill";
	}

	@Override
	public String getCommandUsage() {
		return "command.instantkill.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params)throws CommandException {
		EntityPlayerMP player = getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class);
		
		try {player.setInstantkill(parseTrueFalse(params, 0, !player.getInstantkill()));}
		catch (IllegalArgumentException ex) {throw new CommandException("command.instantkill.failure", sender);}
		
		sender.sendLangfileMessage(player.getInstantkill() ? "command.instantkill.on" : "command.instantkill.off");
		return null;
	}
	
	@Override
	public CommandRequirement[] getRequirements() {
		return new CommandRequirement[] {CommandRequirement.PATCH_ENTITYPLAYERMP};
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
		return isSenderOfEntityType(sender, EntityPlayerMP.class);
	}
}
