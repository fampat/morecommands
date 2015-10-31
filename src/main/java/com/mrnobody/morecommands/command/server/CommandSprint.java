package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.patch.EntityPlayerMP;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;

@Command(
	description = "command.sprint.description",
	example = "command.sprint.example",
	name = "sprint",
	syntax = "command.sprint.syntax",
	videoURL = "command.sprint.videoURL"
		)
public class CommandSprint extends ServerCommand {
	@Override
	public String getName() {
		return "sprint";
	}

	@Override
	public String getUsage() {
		return "command.sprint.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		EntityPlayerMP player = (EntityPlayerMP) sender.getMinecraftISender();
		boolean sprint;
		
		try {sprint = parseTrueFalse(params, 0, player.getInfniteSprinting());}
		catch (IllegalArgumentException ex) {throw new CommandException("command.sprint.failure", sender);}
		
		sender.sendLangfileMessage(sprint ? "command.sprint.on" : "command.sprint.off");
		
		player.setInfniteSprinting(sprint);
		player.setSprinting(sprint);
	}

	@Override
	public Requirement[] getRequirements() {
		return new Requirement[] {Requirement.PATCH_ENTITYPLAYERMP};
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
