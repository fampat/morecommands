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
		EntityPlayerMP player = (EntityPlayerMP) sender.getMinecraftISender();
		
		ServerPlayerSettings settings = ServerPlayerSettings.getPlayerSettings((EntityPlayerMP) sender.getMinecraftISender());
    	settings.lightWorld = !settings.lightWorld;
    		
    	MoreCommands.getMoreCommands().getPacketDispatcher().sendS08Light(player);
    		
    	if (!settings.lightWorld) sender.sendLangfileMessage("command.light.restore");
    	else sender.sendLangfileMessage("command.light.lightup");
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
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return sender instanceof EntityPlayerMP;
	}
}
