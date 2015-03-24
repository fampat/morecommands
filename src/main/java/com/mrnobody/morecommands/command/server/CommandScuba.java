package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listener;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import cpw.mods.fml.common.gameevent.TickEvent;

@Command(
		name = "scuba",
		description = "command.scuba.description",
		example = "command.scuba.example",
		syntax = "command.scuba.syntax",
		videoURL = "command.scuba.videoURL"
		)
public class CommandScuba extends ServerCommand implements Listener<TickEvent> {
	private final int AIR_MAX = 300;
	
	public CommandScuba() {EventHandler.TICK.getHandler().register(this);}
	
	@Override
	public void onEvent(TickEvent e) {
		if (e instanceof TickEvent.PlayerTickEvent) {
			TickEvent.PlayerTickEvent event = (TickEvent.PlayerTickEvent) e;
			if (!ServerPlayerSettings.playerSettingsMapping.containsKey(event.player)) return;
			if (event.player.isInWater() && ServerPlayerSettings.playerSettingsMapping.get(event.player).scuba) 
				event.player.setAir(this.AIR_MAX);
		}
	}

	@Override
	public String getCommandName() {
		return "scuba";
	}

	@Override
	public String getUsage() {
		return "command.scuba.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		ServerPlayerSettings ability = ServerPlayerSettings.playerSettingsMapping.get(sender.getMinecraftISender());
    		
        boolean allowBreathe = false;
        boolean success = false;
        	
        if (params.length >= 1) {
        	if (params[0].toLowerCase().equals("true")) {allowBreathe = true; success = true;}
        	else if (params[0].toLowerCase().equals("false")) {allowBreathe = false; success = true;}
        	else if (params[0].toLowerCase().equals("0")) {allowBreathe = false; success = true;}
        	else if (params[0].toLowerCase().equals("1")) {allowBreathe = true; success = true;}
        	else if (params[0].toLowerCase().equals("on")) {allowBreathe = true; success = true;}
        	else if (params[0].toLowerCase().equals("off")) {allowBreathe = false; success = true;}
        	else {success = false;}
        }
        else {allowBreathe = !ability.scuba; success = true;}
        	
        if (success) ability.scuba = allowBreathe;
        	
        sender.sendLangfileMessageToPlayer(success ? allowBreathe ? "command.scuba.on" : "command.scuba.off" : "command.scuba.failure", new Object[0]);
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}
	
	@Override
	public void unregisterFromHandler() {
		EventHandler.TICK.getHandler().unregister(this);
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
