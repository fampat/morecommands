package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.patch.EntityPlayerMP;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;

@Command(
		name = "instantmine",
		description = "command.instantmine.description",
		example = "command.instantmine.example",
		syntax = "command.instantmine.syntax",
		videoURL = "command.instantmine.videoURL"
		)
public class CommandInstantmine extends StandardCommand implements ServerCommandProperties {
	@Override
	public String getCommandName() {
		return "instantmine";
	}

	@Override
	public String getCommandUsage() {
		return "command.instantmine.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		EntityPlayerMP player = getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class);
	    
		try {player.setInstantmine(parseTrueFalse(params, 0, player.getInstantmine()));}
		catch (IllegalArgumentException ex) {throw new CommandException("command.instantmine.failure", sender);}
		
		sender.sendLangfileMessage(player.getInstantmine() ? "command.instantmine.on" : "command.instantmine.off");
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
	public int getDefaultPermissionLevel() {
		return 2;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return isSenderOfEntityType(sender, EntityPlayerMP.class);
	}
}
