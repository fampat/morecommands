package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
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
public class CommandInstantmine extends ServerCommand {
	@Override
	public String getName() {
		return "instantmine";
	}

	@Override
	public String getUsage() {
		return "command.instantmine.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		EntityPlayerMP player = (EntityPlayerMP) sender.getMinecraftISender();
	    
		try {player.setInstantmine(parseTrueFalse(params, 0, player.getInstantmine()));}
		catch (IllegalArgumentException ex) {throw new CommandException("command.instantmine.failure", sender);}
		
		sender.sendLangfileMessage(player.getInstantmine() ? "command.instantmine.on" : "command.instantmine.off");
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
