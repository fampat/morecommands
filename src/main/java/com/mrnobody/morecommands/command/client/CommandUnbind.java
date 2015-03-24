package com.mrnobody.morecommands.command.client;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;

import com.mrnobody.morecommands.command.ClientCommand;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.core.ClientProxy;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.util.ClientPlayerSettings;
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
public class CommandUnbind extends ClientCommand {
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
		if (params.length > 0) {
			int keyid;
			
			try {
				keyid = Integer.parseInt(params[0]);
			}
			catch (NumberFormatException e) {
				keyid = Keyboard.getKeyIndex(params[0].toUpperCase());
			}
			
			if (keyid != Keyboard.KEY_NONE && ClientPlayerSettings.keybindMapping.containsKey(keyid)) {
				ClientPlayerSettings.keybindMapping.remove(keyid);
				ClientPlayerSettings.saveSettings();
				sender.sendLangfileMessageToPlayer("command.unbind.success", new Object[0]);
			}
			else if (params[0].equalsIgnoreCase("all")) {
				ClientPlayerSettings.keybindMapping.clear();
				ClientPlayerSettings.saveSettings();
				sender.sendLangfileMessageToPlayer("command.unbind.success", new Object[0]);
			}
			else if (!ClientPlayerSettings.keybindMapping.containsKey(keyid)) {
				sender.sendLangfileMessageToPlayer("command.unbind.bindingNotFound", new Object[0]);
				return;
			}
		}
		else {sender.sendLangfileMessageToPlayer("command.unbind.invalidUsage", new Object[0]);}
	}

	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}
	
	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	@Override
	public boolean registerIfServerModded() {
		return false;
	}
	
	@Override
	public int getPermissionLevel() {
		return 0;
	}
}
