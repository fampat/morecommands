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
		name = "light",
		description = "command.light.description",
		example = "command.light.example",
		syntax = "command.light.syntax",
		videoURL = "command.light.videoURL"
		)
public class CommandLight extends StandardCommand implements ServerCommandProperties {
	@Override
	public String getCommandName() {
		return "light";
	}

	@Override
	public String getCommandUsage() {
		return "command.light.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		EntityPlayerMP player = getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class);
		
		ServerPlayerSettings settings = getPlayerSettings(player);
    	settings.lightWorld = !settings.lightWorld;
    		
    	MoreCommands.INSTANCE.getPacketDispatcher().sendS07Light(player);
    		
    	if (!settings.lightWorld) sender.sendLangfileMessage("command.light.restore");
    	else sender.sendLangfileMessage("command.light.lightup");
	}
	
	@Override
	public CommandRequirement[] getRequirements() {
		return new CommandRequirement[] {CommandRequirement.MODDED_CLIENT};
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
