package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
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
public class CommandKeepinventory extends StandardCommand implements ServerCommandProperties {

	@Override
	public String getName() {
		return "keepinventory";
	}

	@Override
	public String getUsage() {
		return "command.keepinventory.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params)throws CommandException {
		EntityPlayerMP player = getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class);;
		ServerPlayerSettings settings = getPlayerSettings(player);
		boolean keepinventory;
		
		try {keepinventory = parseTrueFalse(params, 0, player.getKeepInventory());}
		catch (IllegalArgumentException ex) {throw new CommandException("command.keepinventory.failure", sender);}
		
		sender.sendLangfileMessage(keepinventory ? "command.keepinventory.on" : "command.keepinventory.off");
        
    	settings.keepinventory = keepinventory;
    	player.setKeepInventory(keepinventory);
	}
	
	@Override
	public CommandRequirement[] getRequirements() {
		return new CommandRequirement[] {CommandRequirement.PATCH_ENTITYPLAYERMP, CommandRequirement.PATCH_SERVERCONFIGMANAGER};
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
