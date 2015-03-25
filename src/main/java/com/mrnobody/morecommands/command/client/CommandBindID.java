package com.mrnobody.morecommands.command.client;

import net.minecraft.command.CommandHandler;
import net.minecraftforge.client.ClientCommandHandler;

import com.mrnobody.morecommands.command.ClientCommand;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.util.ClientPlayerSettings;
import com.mrnobody.morecommands.util.Keyboard;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "bindid",
		description = "command.bindid.description",
		example = "command.bindid.example",
		syntax = "command.bindid.syntax",
		videoURL = "command.bindid.videoURL"
		)
public class CommandBindID extends ClientCommand {
	private final CommandHandler commandHandler = ClientCommandHandler.instance;
	
	@Override
	public String getName() {
		return "bindid";
	}

	@Override
	public String getUsage() {
		return "command.bindid.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		if (params.length > 1) {
			try {
				int keyid = Integer.parseInt(params[0]);
				String command = params[1];
				
				if (this.commandHandler.getCommands().get(command) == null) {
					sender.sendLangfileMessageToPlayer("command.generic.notFound", new Object[0]);
					return;
				}
				
				if (params.length > 2) {
					int index = 0;
					String parameters = "";
					
					for (String param : params) {
						if (index > 1) {parameters += " " + param;}
						index++;
					}
					
					command += parameters;
				}
			
				if (Keyboard.getKeyName(keyid) != null) {
					ClientPlayerSettings.keybindMapping.put(keyid, command);
					ClientPlayerSettings.saveSettings();
					
					sender.sendLangfileMessageToPlayer("command.bindid.success", new Object[0]);
				}
				else {sender.sendLangfileMessageToPlayer("command.bindid.invalidChar", new Object[0]);}
			} catch (NumberFormatException e) {sender.sendLangfileMessageToPlayer("command.bindid.invalidID", new Object[0]);}
		}
		else {sender.sendLangfileMessageToPlayer("command.bindid.invalidUsage", new Object[0]);}
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
