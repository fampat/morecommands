package com.mrnobody.morecommands.command.server;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listener;
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
		if (!(event.entityPlayer instanceof EntityPlayerMP)) return;
		
		ServerPlayerSettings settings = ServerPlayerSettings.playerSettingsMapping.get(event.entityPlayer);
		event.newSpeed = settings.breakSpeedEnabled ? settings.breakspeed : event.originalSpeed;
	}

	@Override
	public String getCommandName() {
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
			if (params[0].toLowerCase().equals("reset")) {enable = false;}
			else if (params[0].toLowerCase().equals("max")) {speed = Float.MAX_VALUE; enable = true; setspeed = true;}
			else if (params[0].toLowerCase().equals("min")) {speed = 0.0F; enable = true; setspeed = true;}
			else {
				try {speed = Float.parseFloat(params[0]); enable = true; setspeed = true;}
				catch (NumberFormatException e) {sender.sendLangfileMessageToPlayer("command.breakspeed.invalidArg", new Object[0]);}
			}
		}
		else {sender.sendLangfileMessageToPlayer("command.breakspeed.invalidUsage", new Object[0]);}
		
		if (enable) {
			settings.breakSpeedEnabled = true;
			if (setspeed) {
				settings.breakspeed = speed;
				sender.sendLangfileMessageToPlayer("command.breakspeed.setto", new Object[] {speed});
			}
		}
		else {
			settings.breakSpeedEnabled = false;
			sender.sendLangfileMessageToPlayer("command.breakspeed.reset", new Object[0]);
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
}
