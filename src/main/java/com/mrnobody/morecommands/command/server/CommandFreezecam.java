package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

@Command(
		name = "freezecam",
		description = "command.freezecam.description",
		example = "command.freezecam.example",
		syntax = "command.freezecam.syntax",
		videoURL = "command.freezecam.videoURL"
		)
public class CommandFreezecam extends StandardCommand implements ServerCommandProperties {
	@Override
	public String getCommandName() {
		return "freezecam";
	}

	@Override
	public String getCommandUsage() {
		return "command.freezecam.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		ServerPlayerSettings settings = getPlayerSettings(getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class));
		
		if (settings.freeezecam) {
			settings.freeezecam = false;
			sender.sendLangfileMessage("command.freezecam.off");
		}
		else {
			settings.freeezecam = true;
			sender.sendLangfileMessage("command.freezecam.on");
		}
		
		MoreCommands.INSTANCE.getPacketDispatcher().sendS04Freezecam(getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class));
	}
	
	@Override
	public CommandRequirement[] getRequirements() {
		return new CommandRequirement[] {CommandRequirement.MODDED_CLIENT, CommandRequirement.PATCH_ENTITYPLAYERSP};
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