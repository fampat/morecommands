package com.mrnobody.morecommands.command.server;

import net.minecraft.entity.player.EntityPlayerMP;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.util.Keyboard;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "unbind",
		description = "command.unbind.description",
		example = "command.unbind.example",
		syntax = "command.unbind.syntax",
		videoURL = "command.unbind.videoURL"
		)
public class CommandUnbind extends ServerCommand {
	@Override
	public String getCommandName() {
		return "unbind";
	}

	@Override
	public String getUsage() {
		return "command.unbind.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		ServerPlayerSettings settings = ServerPlayerSettings.playerSettingsMapping.get(sender.getMinecraftISender());
		
		if (params.length > 0) {
			int keyid;
			
			try {
				keyid = Integer.parseInt(params[0]);
			}
			catch (NumberFormatException e) {
				keyid = Keyboard.getKeyIndex(params[0].toUpperCase());
			}
			
			if (keyid != Keyboard.KEY_NONE) {
				settings.clientKeybindMapping.remove(keyid);
				settings.serverKeybindMapping.remove(keyid);
				settings.saveSettings();
				sender.sendLangfileMessageToPlayer("command.unbind.success", new Object[0]);
			}
			else if (params[0].toLowerCase().equals("all")) {
				settings.serverKeybindMapping.clear();
				settings.clientKeybindMapping.clear();
				settings.saveSettings();
				sender.sendLangfileMessageToPlayer("command.unbind.success", new Object[0]);
			}
			else {sender.sendLangfileMessageToPlayer("command.unbind.bindingNotFound", new Object[0]);}
		}
		else {sender.sendLangfileMessageToPlayer("command.unbind.invalidUsage", new Object[0]);}
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
		return 0;
	}
}
