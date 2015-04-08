package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraftforge.event.world.ExplosionEvent;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listener;
import com.mrnobody.morecommands.util.GlobalSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "explosions",
		description = "command.explosions.description",
		example = "command.explosions.example",
		syntax = "command.explosions.syntax",
		videoURL = "command.explosions.videoURL"
		)
public class CommandExplosions extends ServerCommand implements Listener<ExplosionEvent> {
	public CommandExplosions() {
		EventHandler.EXPLOSION.getHandler().register(this);
	}

	@Override
	public void onEvent(ExplosionEvent event) {
		if (!GlobalSettings.explosions) event.setCanceled(true);
	}
	
	@Override
	public String getCommandName() {
		return "explosions";
	}

	@Override
	public String getUsage() {
		return "command.explosions.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params)throws CommandException {
    	boolean explosions = true;
    	boolean success = false;
    	
    	if (params.length >= 1) {
    		if (params[0].equalsIgnoreCase("true")) {explosions = true; success = true;}
    		else if (params[0].equalsIgnoreCase("false")) {explosions = false; success = true;}
    		else if (params[0].equalsIgnoreCase("0")) {explosions = false; success = true;}
    		else if (params[0].equalsIgnoreCase("1")) {explosions = true; success = true;}
    		else if (params[0].equalsIgnoreCase("on")) {explosions = true; success = true;}
    		else if (params[0].equalsIgnoreCase("off")) {explosions = false; success = true;}
    		else if (params[0].equalsIgnoreCase("enable")) {explosions = true; success = true;}
    		else if (params[0].equalsIgnoreCase("disable")) {explosions = false; success = true;}
    		else {success = false;}
    	}
    	else {explosions = !GlobalSettings.explosions; success = true;}
    	
    	if (success) GlobalSettings.explosions = explosions;
    	
    	sender.sendLangfileMessage(success ? explosions ? "command.explosions.on" : "command.explosions.off" : "command.explosions.failure", new Object[0]);
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}
	
	@Override
	public void unregisterFromHandler() {
		EventHandler.EXPLOSION.getHandler().unregister(this);
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
		return true;
	}
}
