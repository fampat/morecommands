package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

@Command(
		name = "freecam",
		description = "command.freecam.description",
		example = "command.freecam.example",
		syntax = "command.freecam.syntax",
		videoURL = "command.freecam.videoURL"
		)
public class CommandFreecam extends ServerCommand {
	@Override
	public String getCommandName() {
		return "freecam";
	}

	@Override
	public String getUsage() {
		return "command.freecam.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		ServerPlayerSettings settings = ServerPlayerSettings.getPlayerSettings((EntityPlayerMP) sender.getMinecraftISender());
		
		if (settings.freecam) {
			settings.freecam = false;
			sender.sendLangfileMessage("command.freecam.off");
		}
		else {
			settings.freecam = true;
            sender.sendLangfileMessage("command.freecam.on");
		}
		
		MoreCommands.getMoreCommands().getPacketDispatcher().sendS04Freecam((EntityPlayerMP) sender.getMinecraftISender());
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
