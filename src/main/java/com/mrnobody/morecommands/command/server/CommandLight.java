package com.mrnobody.morecommands.command.server;

import net.minecraft.entity.player.EntityPlayerMP;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.packet.server.S07PacketLight;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "light",
		description = "command.light.description",
		example = "command.light.example",
		syntax = "command.light.syntax",
		videoURL = "command.light.videoURL"
		)
public class CommandLight extends ServerCommand {
	
	public boolean isEnlightened = false;
	public int lightenedWorld = 0;
	
	@Override
	public String getName() {
		return "light";
	}

	@Override
	public String getUsage() {
		return "command.light.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		EntityPlayerMP player = (EntityPlayerMP) sender.toPlayer().getMinecraftPlayer();
		
		ServerPlayerSettings ability = ServerPlayerSettings.playerSettingsMapping.get(sender.getMinecraftISender());
    	ability.lightWorld = !ability.lightWorld;
    		
    	S07PacketLight packet = new S07PacketLight();
    	MoreCommands.getMoreCommands().getNetwork().sendTo(packet, player);
    		
    	if (!ability.lightWorld) sender.sendLangfileMessageToPlayer("command.light.restore", new Object[0]);
    	else sender.sendLangfileMessageToPlayer("command.light.lightup", new Object[0]);
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[] {Requirement.MODDED_CLIENT};
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
