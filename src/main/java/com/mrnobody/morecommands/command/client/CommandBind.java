package com.mrnobody.morecommands.command.client;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandHandler;
import net.minecraftforge.client.ClientCommandHandler;

import com.mrnobody.morecommands.command.ClientCommand;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.core.AppliedPatches;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listeners.EventListener;
import com.mrnobody.morecommands.network.PacketDispatcher;
import com.mrnobody.morecommands.util.ClientPlayerSettings;
import com.mrnobody.morecommands.util.Keyboard;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;

@Command(
		name = "bind",
		description = "command.bind.description",
		example = "command.bind.example",
		syntax = "command.bind.syntax",
		videoURL = "command.bind.videoURL"
		)
public class CommandBind extends ClientCommand implements EventListener<KeyInputEvent> {
	private final CommandHandler commandHandler = ClientCommandHandler.instance;
	
	@Override
	public String getCommandName() {
		return "bind";
	}

	@Override
	public String getUsage() {
		return "command.bind.syntax";
	}
	
	public CommandBind() {
		EventHandler.KEYINPUT.getHandler().register(this);
	}
	
	@Override
	public void onEvent(KeyInputEvent event) {
		if (!org.lwjgl.input.Keyboard.isKeyDown(org.lwjgl.input.Keyboard.getEventKey())) return;
		
		if (AppliedPatches.serverModded())
			MoreCommands.getMoreCommands().getPacketDispatcher().sendC03KeyInput(org.lwjgl.input.Keyboard.getEventKey());
		else {
			if (ClientPlayerSettings.keybindMapping.containsKey(org.lwjgl.input.Keyboard.getEventKey()))
				this.commandHandler.executeCommand(Minecraft.getMinecraft().thePlayer, ClientPlayerSettings.keybindMapping.get(org.lwjgl.input.Keyboard.getEventKey()));
		}
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		if (params.length > 1) {
			String keycode = params[0].toUpperCase();
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
			
			if (Keyboard.getKeyIndex(keycode) != Keyboard.KEY_NONE) {
				ClientPlayerSettings.keybindMapping.put(Keyboard.getKeyIndex(keycode), command);
				ClientPlayerSettings.saveSettings();
				
				sender.sendLangfileMessage("command.bind.success");
			}
			else throw new CommandException("command.bind.invalidChar", sender);
		}
		else throw new CommandException("command.bind.invalidUsage", sender);
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
