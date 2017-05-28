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
	description = "command.sprint.description",
	example = "command.sprint.example",
	name = "sprint",
	syntax = "command.sprint.syntax",
	videoURL = "command.sprint.videoURL"
	)
public class CommandSprint extends StandardCommand implements ServerCommandProperties {
	@Override
	public String getCommandName() {
		return "sprint";
	}

	@Override
	public String getCommandUsage() {
		return "command.sprint.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		EntityPlayerMP player = getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class);
		boolean sprint;
		
		try {sprint = parseTrueFalse(params, 0, !player.getInfiniteSprinting());}
		catch (IllegalArgumentException ex) {throw new CommandException("command.sprint.failure", sender);}
		
		sender.sendLangfileMessage(sprint ? "command.sprint.on" : "command.sprint.off");
		
		player.setInfiniteSprinting(sprint);
		player.setSprinting(sprint);
		
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
