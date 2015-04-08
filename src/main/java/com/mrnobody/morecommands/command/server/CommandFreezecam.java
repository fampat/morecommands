package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
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
	public String getCommandName() {
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
    		MoreCommands.getMoreCommands().getNetwork().sendTo(packet, (EntityPlayerMP) sender.getMinecraftISender());
			
			ability.freeezecam = false;
			sender.sendLangfileMessage("command.freezecam.off", new Object[0]);
		}
		else {
    		S04PacketFreezecam packet = new S04PacketFreezecam();
    		MoreCommands.getMoreCommands().getNetwork().sendTo(packet, (EntityPlayerMP) sender.getMinecraftISender());
			
			ability.freeezecam = true;
			sender.sendLangfileMessage("command.freezecam.on", new Object[0]);
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
