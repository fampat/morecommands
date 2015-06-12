package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.network.PacketDispatcher;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "xray",
		description = "command.xray.description",
		example = "command.xray.example",
		syntax = "command.xray.syntax",
		videoURL = "command.xray.videoURL"
		)
public class CommandXray extends ServerCommand {

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
		EntityPlayerMP player = (EntityPlayerMP) sender.getMinecraftISender();
		ServerPlayerSettings ability = ServerPlayerSettings.playerSettingsMapping.get(sender.getMinecraftISender());
		
		boolean showGUI = false;
		int blockRadius = ability.xrayBlockRadius;
		boolean enable = ability.xrayEnabled;
		
		if (params.length > 0) {
			if (params[0].equalsIgnoreCase("config")) {showGUI = true;}
			else if (params[0].equalsIgnoreCase("radius") && params.length > 1) {
				try {blockRadius = Integer.parseInt(params[1]);}
				catch (NumberFormatException nfe) {sender.sendLangfileMessage("command.xray.invalidUsage", new Object[0]);}
			}
			else if (params[0].equalsIgnoreCase("enable") || params[0].equalsIgnoreCase("on") || params[0].equalsIgnoreCase("1")) {
				enable = true; 
				sender.sendLangfileMessage("command.xray.enabled", new Object[0]);
			}
			else if (params[0].equalsIgnoreCase("disable") || params[0].equalsIgnoreCase("off") || params[0].equalsIgnoreCase("0")) {
				enable = false; 
				sender.sendLangfileMessage("command.xray.disabled", new Object[0]);
			}
			else {sender.sendLangfileMessage("command.xray.invalidUsage", new Object[0]);}
		}
		else {
			enable = !enable; 
			sender.sendLangfileMessage(enable ? "command.xray.enabled" : "command.xray.disabled", new Object[0]);
		}
		
		ability.xrayBlockRadius = blockRadius;
		ability.xrayEnabled = enable;
		MoreCommands.getMoreCommands().getPacketDispatcher().sendS05Xray(player, showGUI, enable, blockRadius);
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