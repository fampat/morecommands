package com.mrnobody.morecommands.command.client;

import java.util.Arrays;

import org.lwjgl.input.Keyboard;

import com.mrnobody.morecommands.command.ClientCommandProperties;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.MultipleCommands;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.event.EventHandler;
import com.mrnobody.morecommands.event.Listeners.EventListener;
import com.mrnobody.morecommands.settings.ClientPlayerSettings;

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
	public String[] getCommandNames() {
		return new String[] {"bind", "bindid", "unbind", "unbindid"};
	}

	@Override
	public String[] getCommandUsages() {
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
		if (!Keyboard.isKeyDown(Keyboard.getEventKey()) || Minecraft.getMinecraft().player == null) return;
		ClientPlayerSettings settings = getPlayerSettings(Minecraft.getMinecraft().player);
		
		if (settings.bindings.containsKey(Keyboard.getKeyName(Keyboard.getEventKey())))
			executeCommand(settings.bindings.get(Keyboard.getKeyName(Keyboard.getEventKey())));
		else if (settings.bindings.containsKey(Integer.toString(Keyboard.getEventKey())))
			executeCommand(settings.bindings.get(Integer.toString(Keyboard.getEventKey())));
	}
	
    private void executeCommand(String command) {
    	if (!command.startsWith("/")) command = "/" + command;
        if (ClientCommandHandler.instance.executeCommand(Minecraft.getMinecraft().player, command.trim()) != 0) return;
        Minecraft.getMinecraft().player.sendChatMessage(command);
	}
    
	@Override
	public String execute(String commandName, CommandSender sender, String[] params) throws CommandException {
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
			if (key != Keyboard.KEY_NONE && settings.bindings.containsKey(Keyboard.getKeyName(key))) {
				settings.bindings.remove(Keyboard.getKeyName(key));
				sender.sendLangfileMessage("command.unbind.success");
			}
			else if (params[0].equalsIgnoreCase("all")) {
				settings.bindings.clear();
				sender.sendLangfileMessage("command.unbind.success");
			}
			else if (!settings.bindings.containsKey(Keyboard.getKeyName(key)))
				throw new CommandException("command.unbind.bindingNotFound", sender);
		}
		else {
			if (params.length <= 1) throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
			String command = params[1];
			
			if (params.length > 2)
				command = rejoinParams(Arrays.copyOfRange(params, 1, params.length));
			
			if (key != Keyboard.KEY_NONE) {
				settings.bindings.put(Keyboard.getKeyName(key), command);
				sender.sendLangfileMessage("command.bind.success");
			}
			else throw new CommandException("command.bind.invalidKey", sender);
		}
		
		return null;
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
	public int getDefaultPermissionLevel(String[] args) {
		return 0;
	}
}
