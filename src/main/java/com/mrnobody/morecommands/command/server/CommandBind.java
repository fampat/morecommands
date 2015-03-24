package com.mrnobody.morecommands.command.server;

import net.minecraft.command.CommandHandler;
import net.minecraft.server.MinecraftServer;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.handler.Listener;
import com.mrnobody.morecommands.handler.PacketHandler;
import com.mrnobody.morecommands.packet.server.S09PacketExecuteClientCommand;
import com.mrnobody.morecommands.util.KeyEvent;
import com.mrnobody.morecommands.util.Keyboard;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "bind",
		description = "command.bind.description",
		example = "command.bind.example",
		syntax = "command.bind.syntax",
		videoURL = "command.bind.videoURL"
		)
public class CommandBind extends ServerCommand implements Listener<KeyEvent> {
	private final CommandHandler commandHandler = (CommandHandler) MinecraftServer.getServer().getCommandManager();
	
	public CommandBind() {
		PacketHandler.KEYINPUT.getHandler().register(this);
	}
	
	public void onEvent(KeyEvent event) {
		if (!MinecraftServer.getServer().isServerRunning()) return;
		
		ServerPlayerSettings settings = ServerPlayerSettings.playerSettingsMapping.get(event.player);
		if (settings.serverKeybindMapping.containsKey(event.key)) this.commandHandler.executeCommand(event.player, settings.serverKeybindMapping.get(event.key));
		else if (settings.clientKeybindMapping.containsKey(event.key)) {
			S09PacketExecuteClientCommand packet = new S09PacketExecuteClientCommand();
			packet.command = settings.clientKeybindMapping.get(event.key);
			MoreCommands.getNetwork().sendTo(packet, event.player);
		}
	}

	@Override
	public String getCommandName() {
		return "bind";
	}

	@Override
	public String getUsage() {
		return "command.bind.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		ServerPlayerSettings settings = ServerPlayerSettings.playerSettingsMapping.get(sender.getMinecraftISender());
		
		if (params.length > 1) {
			String keycode = params[0].toUpperCase();
			String command = params[1];
			
			if (!this.commandHandler.getCommands().containsKey(command) && !settings.clientCommands.contains(command)) {
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
			
			if (Keyboard.getKeyIndex(keycode) != Keyboard.KEY_NONE) {
				if (settings.clientCommands.contains(command)) settings.clientKeybindMapping.put(Keyboard.getKeyIndex(keycode), command);
				else settings.serverKeybindMapping.put(Keyboard.getKeyIndex(keycode), command);
				
				settings.saveSettings();
				
				sender.sendLangfileMessageToPlayer("command.bind.success", new Object[0]);
			}
			else {sender.sendLangfileMessageToPlayer("command.bind.invalidChar", new Object[0]);}
		}
		else {sender.sendLangfileMessageToPlayer("command.bind.invalidUsage", new Object[0]);}
	}

	@Override
	public Requirement[] getRequirements() {
		return new Requirement[] {Requirement.MODDED_CLIENT};
	}
	
	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	public void unregisterFromHandler() {
		PacketHandler.KEYINPUT.getHandler().unregister(this);
	}
	
	@Override
	public int getPermissionLevel() {
		return 0;
	}
}
