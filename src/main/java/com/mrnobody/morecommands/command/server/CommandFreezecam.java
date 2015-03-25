package com.mrnobody.morecommands.command.server;

import net.minecraft.entity.player.EntityPlayerMP;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.packet.server.S04PacketFreezecam;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "freezecam",
		description = "command.freezecam.description",
		example = "command.freezecam.example",
		syntax = "command.freezecam.syntax",
		videoURL = "command.freezecam.videoURL"
		)
public class CommandFreezecam extends ServerCommand {
	@Override
	public String getName() {
		return "freezecam";
	}

	@Override
	public String getUsage() {
		return "command.freezecam.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		ServerPlayerSettings ability = ServerPlayerSettings.playerSettingsMapping.get(sender.getMinecraftISender());
		
		if (ability.freeezecam) {
    		S04PacketFreezecam packet = new S04PacketFreezecam();
    		MoreCommands.getMoreCommands().getNetwork().sendTo(packet, (EntityPlayerMP) sender.toPlayer().getMinecraftPlayer());
			
			ability.freeezecam = false;
			sender.sendLangfileMessageToPlayer("command.freezecam.off", new Object[0]);
		}
		else {
    		S04PacketFreezecam packet = new S04PacketFreezecam();
    		MoreCommands.getMoreCommands().getNetwork().sendTo(packet, (EntityPlayerMP) sender.toPlayer().getMinecraftPlayer());
			
			ability.freeezecam = true;
			sender.sendLangfileMessageToPlayer("command.freezecam.on", new Object[0]);
		}
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[] {Requirement.MODDED_CLIENT, Requirement.PATCH_ENTITYPLAYERSP};
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
}
