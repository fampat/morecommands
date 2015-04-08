package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.packet.server.S03PacketFreecam;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

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
		ServerPlayerSettings ability = ServerPlayerSettings.playerSettingsMapping.get(sender.getMinecraftISender());
		
		if (ability.freecam) {
    		S03PacketFreecam packet = new S03PacketFreecam();
    		MoreCommands.getMoreCommands().getNetwork().sendTo(packet, (EntityPlayerMP) sender.getMinecraftISender());
			
			ability.freecam = false;
			sender.sendLangfileMessage("command.freecam.off", new Object[0]);
		}
		else {
    		S03PacketFreecam packet = new S03PacketFreecam();
    		MoreCommands.getMoreCommands().getNetwork().sendTo(packet, (EntityPlayerMP) sender.getMinecraftISender());
			
			ability.freecam = true;
            sender.sendLangfileMessage("command.freecam.on", new Object[0]);
		}
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[] {Requirement.MODDED_CLIENT, Requirement.PATCH_ENTITYCLIENTPLAYERMP};
	}
	
	@Override
	public void unregisterFromHandler() {}

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
