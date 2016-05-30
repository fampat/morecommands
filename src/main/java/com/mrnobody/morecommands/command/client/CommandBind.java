package com.mrnobody.morecommands.command.client;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Sets;
import com.mrnobody.morecommands.command.ClientCommandProperties;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.MultipleCommands;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.event.EventHandler;
import com.mrnobody.morecommands.event.Listeners.EventListener;
import com.mrnobody.morecommands.util.ClientPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;

@Command.MultipleCommand(
		name = {"bind", "bindid", "unbind", "unbindid"},
		description = {"command.bind.description", "command.bind.id.description", "command.unbind.description", "command.unbind.id.description"},
		example = {"command.bind.example", "command.bind.id.example", "command.unbind.example", "command.unbind.id.example"},
		syntax = {"command.bind.syntax", "command.bind.id.syntax", "command.unbind.syntax", "command.unbind.id.syntax"},
		videoURL = {"command.bind.videoURL", "command.bind.id.videoURL", "command.unbind.videoURL", "command.unbind.id.videoURL"}
		)
public class CommandBind extends MultipleCommands implements ClientCommandProperties, EventListener<KeyInputEvent> {
	@Override
	public String[] getNames() {
		return new String[] {"bind", "bindid", "unbind", "unbindid"};
	}

	@Override
	public String[] getUsages() {
		return new String[] {"command.bind.syntax", "command.bind.id.syntax", "command.unbind.syntax", "command.unbind.id.syntax"};
	}
	
	public CommandBind(int typeIndex) {
		super(typeIndex);
	}
	
	public CommandBind() {
		super();
		EventHandler.KEYINPUT.register(this);
	}
	
	@Override
	public void onEvent(KeyInputEvent event) {
		if (!Keyboard.isKeyDown(Keyboard.getEventKey()) || Minecraft.getMinecraft().thePlayer == null) return;
		ClientPlayerSettings settings = getPlayerSettings(Minecraft.getMinecraft().thePlayer);
		
		if (settings.bindings.containsKey(org.lwjgl.input.Keyboard.getEventKey()))
			executeCommand(settings.bindings.get(Keyboard.getEventKey()));
	}
	
    private void executeCommand(String command) {
    	if (!command.startsWith("/")) command = "/" + command;
        if (ClientCommandHandler.instance.executeCommand(Minecraft.getMinecraft().thePlayer, command.trim()) != 0) return;
        Minecraft.getMinecraft().thePlayer.sendChatMessage(command);
	}
    
	@Override
	public void execute(String commandName, CommandSender sender, String[] params) throws CommandException {
		boolean byId = commandName.endsWith("id");
		boolean unbind = commandName.startsWith("unbind");
		
		if (params.length <= 0)
			throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
		
		if (!isSenderOfEntityType(sender.getMinecraftISender(), EntityPlayerSP.class))
			throw new CommandException("command.generic.notAPlayer", sender);
		
		ClientPlayerSettings settings = getPlayerSettings(getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerSP.class));
		int key; try {key = byId ? Integer.parseInt(params[0]) : Keyboard.getKeyIndex(params[0].toUpperCase());}
		catch (NumberFormatException nfe) {throw new CommandException("command.generic.NaN", sender, "KEYID");}
		
		if (unbind) {
			if (key != Keyboard.KEY_NONE && settings.bindings.containsKey(key)) {
				removeOrPutAndUpdateBinding(settings, key, null);
				sender.sendLangfileMessage("command.unbind.success");
			}
			else if (params[0].equalsIgnoreCase("all")) {
				removeOrPutAndUpdateBinding(settings, -1, null);
				sender.sendLangfileMessage("command.unbind.success");
			}
			else if (!settings.bindings.containsKey(key))
				throw new CommandException("command.unbind.bindingNotFound", sender);
		}
		else {
			if (params.length <= 1) throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
			String command = params[1];
			
			if (params.length > 2)
				command = rejoinParams(Arrays.copyOfRange(params, 1, params.length));
			
			if (key != Keyboard.KEY_NONE) {
				removeOrPutAndUpdateBinding(settings, key, command);
				sender.sendLangfileMessage("command.bind.success");
			}
			else throw new CommandException("command.bind.invalidKey", sender);
		}
	}
	
	private void removeOrPutAndUpdateBinding(ClientPlayerSettings settings, int key, String value) {
		Map<String, String> bindings;
		
		if (key == -1) {
			Set<String> keys = Sets.newHashSetWithExpectedSize(settings.bindings.size());
			for (int key2 : settings.bindings.keySet()) keys.add(Keyboard.getKeyName(key2));
			bindings = settings.removeAndUpdate("bindings", keys, String.class, true, true);
		}
		else if (value == null) bindings = settings.removeAndUpdate("bindings", Keyboard.getKeyName(key), String.class, true);
		else bindings = settings.putAndUpdate("bindings", Keyboard.getKeyName(key), value, String.class, true);
		
		settings.bindings = new HashMap<Integer, String>(bindings.size());
		
		for (Map.Entry<String, String> entry : bindings.entrySet()) {
			key = Keyboard.getKeyIndex(entry.getKey());
			if (key != Keyboard.KEY_NONE) settings.bindings.put(key, entry.getValue());
		}
	}
	
	@Override
	public CommandRequirement[] getRequirements() {
		return new CommandRequirement[0];
	}
	
	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}

	@Override
	public boolean registerIfServerModded() {
		return true;
	}
	
	@Override
	public int getDefaultPermissionLevel() {
		return 0;
	}
}
