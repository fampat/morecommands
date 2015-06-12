package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listeners.Listener;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

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
	public String getName() {
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
        	if (params[0].equalsIgnoreCase("true")) {allowBreathe = true; success = true;}
        	else if (params[0].equalsIgnoreCase("false")) {allowBreathe = false; success = true;}
        	else if (params[0].equalsIgnoreCase("0")) {allowBreathe = false; success = true;}
        	else if (params[0].equalsIgnoreCase("1")) {allowBreathe = true; success = true;}
        	else if (params[0].equalsIgnoreCase("on")) {allowBreathe = true; success = true;}
        	else if (params[0].equalsIgnoreCase("off")) {allowBreathe = false; success = true;}
    		else if (params[0].equalsIgnoreCase("enable")) {allowBreathe = true; success = true;}
    		else if (params[0].equalsIgnoreCase("disable")) {allowBreathe = false; success = true;}
        	else {success = false;}
        }
        else {allowBreathe = !ability.scuba; success = true;}
        	
        if (success) ability.scuba = allowBreathe;
        	
        sender.sendLangfileMessage(success ? allowBreathe ? "command.scuba.on" : "command.scuba.off" : "command.scuba.failure", new Object[0]);
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
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return sender instanceof EntityPlayerMP;
	}
}
