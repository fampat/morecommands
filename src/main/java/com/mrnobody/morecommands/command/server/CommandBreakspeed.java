package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listeners.Listener;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "breakspeed",
		description = "command.breakspeed.description",
		example = "command.breakspeed.example",
		syntax = "command.breakspeed.syntax",
		videoURL = "command.breakspeed.videoURL"
		)
public class CommandBreakspeed extends ServerCommand implements Listener<BreakSpeed> {
	public CommandBreakspeed() {EventHandler.BREAKSPEED.getHandler().register(this);}
	
	@Override
	public void onEvent(BreakSpeed event) {
		if (!ServerPlayerSettings.playerSettingsMapping.containsKey(event.entityPlayer)) return;
		
		ServerPlayerSettings settings = ServerPlayerSettings.playerSettingsMapping.get(event.entityPlayer);
		event.newSpeed = settings.breakSpeedEnabled ? settings.breakspeed : event.originalSpeed;
	}

	@Override
	public String getName() {
		return "breakspeed";
	}

	@Override
	public String getUsage() {
		return "command.breakspeed.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		ServerPlayerSettings settings = ServerPlayerSettings.playerSettingsMapping.get(sender.getMinecraftISender());
		
		boolean enable = true;
		boolean setspeed = false;
		float speed = 0.0F;
		
		if (params.length > 0) {
			if (params[0].equalsIgnoreCase("reset")) {enable = false;}
			else if (params[0].equalsIgnoreCase("max")) {speed = Float.MAX_VALUE; enable = true; setspeed = true;}
			else if (params[0].equalsIgnoreCase("min")) {speed = 0.0F; enable = true; setspeed = true;}
			else {
				try {speed = Float.parseFloat(params[0]); enable = true; setspeed = true;}
				catch (NumberFormatException e) {throw new CommandException("command.breakspeed.invalidArg", sender);}
			}
		}
		else throw new CommandException("command.breakspeed.invalidUsage", sender);
		
		if (enable) {
			settings.breakSpeedEnabled = true;
			if (setspeed) {
				settings.breakspeed = speed;
				sender.sendLangfileMessage("command.breakspeed.setto", speed);
			}
		}
		else {
			settings.breakSpeedEnabled = false;
			sender.sendLangfileMessage("command.breakspeed.reset");
		}
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}
	
	@Override
	public void unregisterFromHandler() {
		EventHandler.BREAKSPEED.getHandler().unregister(this);
	}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	@Override
	public int getPermissionLevel() {
		return 2;
	}
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return sender instanceof EntityPlayerMP;
	}
}
