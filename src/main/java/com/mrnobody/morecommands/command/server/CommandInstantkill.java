package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.patch.EntityPlayerMP;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;

@Command(
		name = "instantkill",
		description = "command.instantkill.description",
		example = "command.instantkill.example",
		syntax = "command.instantkill.syntax",
		videoURL = "command.instantkill.videoURL"
		)
public class CommandInstantkill extends ServerCommand {
	@Override
	public String getCommandName() {
		return "instantkill";
	}

	@Override
	public String getUsage() {
		return "command.instantkill.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params)throws CommandException {
		EntityPlayerMP player = (EntityPlayerMP) sender.getMinecraftISender();
		
		try {player.setInstantkill(parseTrueFalse(params, 0, player.getInstantkill()));}
		catch (IllegalArgumentException ex) {throw new CommandException("command.instantkill.failure", sender);}
		
		sender.sendLangfileMessage(player.getInstantkill() ? "command.instantkill.on" : "command.instantkill.off");
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
