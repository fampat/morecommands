package com.mrnobody.morecommands.command.client;

import com.mrnobody.morecommands.command.ClientCommand;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.util.ClientPlayerSettings;
import com.mrnobody.morecommands.util.Keyboard;
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
				sender.sendLangfileMessage("command.unbind.success");
			}
			else if (params[0].equalsIgnoreCase("all")) {
				ClientPlayerSettings.keybindMapping.clear();
				ClientPlayerSettings.saveSettings();
				sender.sendLangfileMessage("command.unbind.success");
			}
			else if (!ClientPlayerSettings.keybindMapping.containsKey(keyid))
				throw new CommandException("command.unbind.bindingNotFound", sender);
		}
		else throw new CommandException("command.unbind.invalidUsage", sender);
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
