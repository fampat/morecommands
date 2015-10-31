package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.core.MoreCommands;
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
public class CommandFreezecam extends ServerCommand {
	@Override
	public String getCommandName() {
		return "freezecam";
	}

	@Override
	public String getUsage() {
		return "command.freezecam.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		ServerPlayerSettings settings = ServerPlayerSettings.getPlayerSettings((EntityPlayerMP) sender.getMinecraftISender());
		
		if (settings.freeezecam) {
			settings.freeezecam = false;
			sender.sendLangfileMessage("command.freezecam.off");
		}
		else {
			settings.freeezecam = true;
			sender.sendLangfileMessage("command.freezecam.on");
		}
		
		MoreCommands.getMoreCommands().getPacketDispatcher().sendS05Freezecam((EntityPlayerMP) sender.getMinecraftISender());
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[] {Requirement.MODDED_CLIENT, Requirement.PATCH_ENTITYCLIENTPLAYERMP};
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
