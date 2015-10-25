package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.handler.Listeners.EventListener;
import com.mrnobody.morecommands.handler.PacketHandler;
import com.mrnobody.morecommands.util.KeyEvent;
import com.mrnobody.morecommands.util.Keyboard;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

@Command(
		name = "bind",
		description = "command.bind.description",
		example = "command.bind.example",
		syntax = "command.bind.syntax",
		videoURL = "command.bind.videoURL"
		)
public class CommandBind extends ServerCommand implements EventListener<KeyEvent> {
	private final CommandHandler commandHandler = (CommandHandler) MinecraftServer.getServer().getCommandManager();
	
	public CommandBind() {
		PacketHandler.KEYINPUT.getHandler().register(this);
	}
	
	public void onEvent(KeyEvent event) {
		if (!MinecraftServer.getServer().isServerRunning()) return;
		
		ServerPlayerSettings settings = ServerPlayerSettings.getPlayerSettings(event.player);
		if (settings.serverKeybindMapping.containsKey(event.key)) this.commandHandler.executeCommand(event.player, settings.serverKeybindMapping.get(event.key));
		else if (settings.clientKeybindMapping.containsKey(event.key))
			MoreCommands.getMoreCommands().getPacketDispatcher().sendS10ExecuteClientCommand(event.player, settings.clientKeybindMapping.get(event.key));
	}

	@Override
	public String getName() {
		return "bind";
	}

	@Override
	public String getUsage() {
		return "command.bind.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		ServerPlayerSettings settings = ServerPlayerSettings.getPlayerSettings((EntityPlayerMP) sender.getMinecraftISender());
		
		if (params.length > 1) {
			String keycode = params[0].toUpperCase();
			String command = params[1];
			
			if (!this.commandHandler.getCommands().containsKey(command) && !settings.clientCommands.contains(command))
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
			
			if (Keyboard.getKeyIndex(keycode) != Keyboard.KEY_NONE) {
				if (settings.clientCommands.contains(command)) settings.clientKeybindMapping.put(Keyboard.getKeyIndex(keycode), command);
				else settings.serverKeybindMapping.put(Keyboard.getKeyIndex(keycode), command);
				
				settings.saveSettings();
				
				sender.sendLangfileMessage("command.bind.success");
			}
			else throw new CommandException("command.bind.invalidChar", sender);
		}
		else throw new CommandException("command.bind.invalidUsage", sender);
	}

	@Override
	public Requirement[] getRequirements() {
		return new Requirement[] {Requirement.HANDSHAKE_FINISHED};
	}
	
	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	@Override
	public int getPermissionLevel() {
		return 0;
	}
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return sender instanceof EntityPlayerMP;
	}
}
