package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.patch.EntityPlayerMP;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;

@Command(
		name = "criticalhit",
		description = "command.criticalhit.description",
		example = "command.criticalhit.example",
		syntax = "command.criticalhit.syntax",
		videoURL = "command.criticalhit.videoURL"
		)
public class CommandCriticalhit extends ServerCommand {

	@Override
	public String getName() {
		return "criticalhit";
	}

	@Override
	public String getUsage() {
		return "command.criticalhit.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
	    EntityPlayerMP player = (EntityPlayerMP) sender.getMinecraftISender();
    	
		try {player.setCriticalhit(parseTrueFalse(params, 0, player.getCriticalHit()));}
		catch (IllegalArgumentException ex) {throw new CommandException("command.criticalhit.failure", sender);}
		
		sender.sendLangfileMessage(player.getCriticalHit() ? "command.criticalhit.on" : "command.criticalhit.off");
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
