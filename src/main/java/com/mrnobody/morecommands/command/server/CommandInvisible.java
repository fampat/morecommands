package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

@Command(
		name = "invisible",
		description = "command.invisible.description",
		example = "command.invisible.example",
		syntax = "command.invisible.syntax",
		videoURL = "command.invisible.videoURL"
		)
public class CommandInvisible extends ServerCommand {
	@Override
	public String getCommandName() {
		return "invisible";
	}

	@Override
	public String getUsage() {
		return "command.invisible.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		EntityPlayerMP player = (EntityPlayerMP) sender.getMinecraftISender();
		
		try {player.setInvisible(parseTrueFalse(params, 0, player.isInvisible()));}
		catch (IllegalArgumentException ex) {throw new CommandException("command.invisible.failure", sender);}
		
		sender.sendLangfileMessage(player.isInvisible() ? "command.invisible.on" : "command.invisible.off");
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
		return 2;
	}
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return sender instanceof EntityPlayerMP;
	}
}
