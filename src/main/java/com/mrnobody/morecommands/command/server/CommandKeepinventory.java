package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.patch.EntityPlayerMP;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;

@Command(
		name = "keepinventory",
		description = "command.keepinventory.description",
		example = "command.keepinventory.example",
		syntax = "command.keepinventory.syntax",
		videoURL = "command.keepinventory.videoURL"
		)
public class CommandKeepinventory extends ServerCommand {

	@Override
	public String getCommandName() {
		return "keepinventory";
	}

	@Override
	public String getUsage() {
		return "command.keepinventory.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params)throws CommandException {
		EntityPlayerMP player = (EntityPlayerMP) sender.getMinecraftISender();
		ServerPlayerSettings settings = ServerPlayerSettings.getPlayerSettings((EntityPlayerMP) sender.getMinecraftISender());
		boolean keepinventory;
		
		try {keepinventory = parseTrueFalse(params, 0, player.getKeepInventory());}
		catch (IllegalArgumentException ex) {throw new CommandException("command.keepinventory.failure", sender);}
		
		sender.sendLangfileMessage(keepinventory ? "command.keepinventory.on" : "command.keepinventory.off");
        
    	settings.keepinventory = keepinventory;
    	player.setKeepInventory(keepinventory);
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[] {Requirement.PATCH_ENTITYPLAYERMP, Requirement.PATCH_SERVERCONFIGMANAGER};
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
