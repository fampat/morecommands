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
		name = "xray",
		description = "command.xray.description",
		example = "command.xray.example",
		syntax = "command.xray.syntax",
		videoURL = "command.xray.videoURL"
		)
public class CommandXray extends StandardCommand implements ServerCommandProperties {

	@Override
	public String getCommandName() {
		return "xray";
	}

	@Override
	public String getUsage() {
		return "command.xray.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		EntityPlayerMP player = getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class);
		ServerPlayerSettings settings = getPlayerSettings(player);
		
		if (params.length > 0) {
			if (params[0].equalsIgnoreCase("config")) 
				MoreCommands.INSTANCE.getPacketDispatcher().sendS05Xray(player);
			else if (params[0].equalsIgnoreCase("radius") && params.length > 1) {
				try {settings.xrayBlockRadius = Integer.parseInt(params[1]);}
				catch (NumberFormatException nfe) {throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());}
				
				settings.xrayBlockRadius = settings.xrayBlockRadius > 100 ? 100 : settings.xrayBlockRadius < 0 ? 0 : settings.xrayBlockRadius;
				MoreCommands.INSTANCE.getPacketDispatcher().sendS05Xray(player, settings.xrayEnabled, settings.xrayBlockRadius);
			}
			else if (params[0].equalsIgnoreCase("enable") || params[0].equalsIgnoreCase("on") || params[0].equalsIgnoreCase("1") || params[0].equalsIgnoreCase("true")) {
				settings.xrayEnabled = true;
				MoreCommands.INSTANCE.getPacketDispatcher().sendS05Xray(player, true, settings.xrayBlockRadius);
				sender.sendLangfileMessage("command.xray.enabled");
			}
			else if (params[0].equalsIgnoreCase("disable") || params[0].equalsIgnoreCase("off") || params[0].equalsIgnoreCase("0") || params[0].equalsIgnoreCase("false")) {
				settings.xrayEnabled = false;
				MoreCommands.INSTANCE.getPacketDispatcher().sendS05Xray(player, false, settings.xrayBlockRadius);
				sender.sendLangfileMessage("command.xray.disabled");
			}
			else if (params[0].equalsIgnoreCase("load") && params.length > 1)
				MoreCommands.INSTANCE.getPacketDispatcher().sendS05Xray(player, true, params[1]);
			else if (params[0].equalsIgnoreCase("save") && params.length > 1)
				MoreCommands.INSTANCE.getPacketDispatcher().sendS05Xray(player, false, params[1]);
			else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
		}
		else {
			settings.xrayEnabled = !settings.xrayEnabled;
			MoreCommands.INSTANCE.getPacketDispatcher().sendS05Xray(player, settings.xrayEnabled, settings.xrayBlockRadius);
			sender.sendLangfileMessage(settings.xrayEnabled ? "command.xray.enabled" : "command.xray.disabled");
		}
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
