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
	public String getCommandName() {
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
				
				if (this.commandHandler.getCommands().get(command) == null)
					throw new CommandException("command.generic.notFound", sender);
				
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
					
					sender.sendLangfileMessage("command.bindid.success");
				}
				else throw new CommandException("command.bindid.invalidChar", sender);
			}
			catch (NumberFormatException e) {throw new CommandException("command.bindid.invalidID", sender);}
		}
		else throw new CommandException("command.bindid.invalidUsage", sender);
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
