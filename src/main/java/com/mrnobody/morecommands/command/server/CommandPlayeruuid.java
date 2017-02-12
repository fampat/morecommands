package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

@Command(
		name = "playeruuid",
		description = "command.playeruuid.description",
		example = "command.playeruuid.example",
		syntax = "command.playeruuid.syntax",
		videoURL = "command.playeruuid.videoURL"
		)
public class CommandPlayeruuid extends StandardCommand implements ServerCommandProperties {

	@Override
	public String getCommandName() {
		return "playeruuid";
	}

	@Override
	public String getCommandUsage() {
		return "command.playeruuid.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		sender.sendLangfileMessage("command.playeruuid.uuid", getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class).getUniqueID().toString());
		return getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class).getUniqueID().toString();
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
