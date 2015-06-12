package com.mrnobody.morecommands.command.server;

import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.util.Keyboard;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "bindid",
		description = "command.bindid.description",
		example = "command.bindid.example",
		syntax = "command.bindid.syntax",
		videoURL = "command.bindid.videoURL"
		)
public class CommandBindID extends ServerCommand {
	private final CommandHandler commandHandler = (CommandHandler) MinecraftServer.getServer().getCommandManager();

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
		ServerPlayerSettings settings = ServerPlayerSettings.playerSettingsMapping.get(sender.getMinecraftISender());
		
		if (params.length > 1) {
			try {
				int keyid = Integer.parseInt(params[0]);
				String command = params[1];
				
				if (!this.commandHandler.getCommands().containsKey(command) && !settings.clientCommands.contains(command)) {
					sender.sendLangfileMessage("command.generic.notFound", new Object[0]);
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
					if (settings.clientCommands.contains(command)) settings.clientKeybindMapping.put(keyid, command);
					else settings.serverKeybindMapping.put(keyid, command);
					
					settings.saveSettings();
					
					sender.sendLangfileMessage("command.bindid.success", new Object[0]);
				}
				else {sender.sendLangfileMessage("command.bindid.invalidChar", new Object[0]);}
			} catch (NumberFormatException e) {sender.sendLangfileMessage("command.bind.invalidID", new Object[0]);}
		}
		else {sender.sendLangfileMessage("command.bindid.invalidUsage", new Object[0]);}
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[] {Requirement.MODDED_CLIENT};
	}
	
	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}

	@Override
	public void unregisterFromHandler() {}
	
	@Override
	public int getPermissionLevel() {
		return 0;
	}
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return sender instanceof EntityPlayerMP;
	}
}
