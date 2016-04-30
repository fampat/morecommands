package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;

@Command(
		name = "invisible",
		description = "command.invisible.description",
		example = "command.invisible.example",
		syntax = "command.invisible.syntax",
		videoURL = "command.invisible.videoURL"
		)
public class CommandInvisible extends StandardCommand implements ServerCommandProperties {
	@Override
	public String getName() {
		return "invisible";
	}

	@Override
	public String getUsage() {
		return "command.invisible.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Entity entity = getSenderAsEntity(sender.getMinecraftISender(), Entity.class);
		
		try {entity.setInvisible(parseTrueFalse(params, 0, entity.isInvisible()));}
		catch (IllegalArgumentException ex) {throw new CommandException("command.invisible.failure", sender);}
		
		sender.sendLangfileMessage(entity.isInvisible() ? "command.invisible.on" : "command.invisible.off");
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
		return isSenderOfEntityType(sender, Entity.class);
	}
}
