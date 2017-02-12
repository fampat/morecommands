package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.patch.EntityPlayerMP;

import net.minecraft.command.ICommandSender;

@Command(
		name = "criticalhit",
		description = "command.criticalhit.description",
		example = "command.criticalhit.example",
		syntax = "command.criticalhit.syntax",
		videoURL = "command.criticalhit.videoURL"
		)
public class CommandCriticalhit extends StandardCommand implements ServerCommandProperties {

	@Override
	public String getCommandName() {
		return "criticalhit";
	}

	@Override
	public String getCommandUsage() {
		return "command.criticalhit.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
	    EntityPlayerMP player = getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class);
	    
		try {player.setCriticalhit(parseTrueFalse(params, 0, !player.getCriticalHit()));}
		catch (IllegalArgumentException ex) {throw new CommandException("command.criticalhit.failure", sender);}
		
		sender.sendLangfileMessage(player.getCriticalHit() ? "command.criticalhit.on" : "command.criticalhit.off");
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
