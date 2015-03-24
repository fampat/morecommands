package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.command.CommandBase.Requirement;
import com.mrnobody.morecommands.command.CommandBase.ServerType;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Coordinate;
import com.mrnobody.morecommands.wrapper.Player;
import com.mrnobody.morecommands.wrapper.World;

import cpw.mods.fml.relauncher.Side;

@Command(
		name = "weather",
		description = "command.weather.description",
		example = "command.weather.example",
		syntax = "command.weather.syntax",
		videoURL = "command.weather.videoURL"
		)
public class CommandWeather extends ServerCommand {

	@Override
	public String getCommandName() {
		return "weather";
	}

	@Override
	public String getUsage() {
		return "command.weather.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Player player = sender.toPlayer();
		World world = player.getWorld();
		
		if (params.length > 0) {
			boolean state = false;
			boolean entered = false;
			
	    	if (params.length > 1) {
	    		if (params[1].toLowerCase().equals("true")) {state = true; entered = true;}
	    		else if (params[1].toLowerCase().equals("false")) {state = false; entered = true;}
	    		else if (params[1].toLowerCase().equals("0")) {state = false; entered = true;}
	    		else if (params[1].toLowerCase().equals("1")) {state = true; entered = true;}
	    		else if (params[1].toLowerCase().equals("on")) {state = true; entered = true;}
	    		else if (params[1].toLowerCase().equals("off")) {state = false; entered = true;}
	    	}
	    	
	    	if (params[0].toLowerCase().equals("rain")) {
	    		if (entered) world.setRaining(state);
	    		else world.setRaining(!world.isRaining());
	    		sender.sendLangfileMessageToPlayer("command.weather.rainSuccess", new Object[0]);
	    	}
	    	
	    	if (params[0].toLowerCase().equals("thunder")) {
	    		if (entered) world.setThunder(state);
	    		else world.setThunder(!world.isThunder());
	    		sender.sendLangfileMessageToPlayer("command.weather.thunderSuccess", new Object[0]);
	    	}
	    	
	    	if (params[0].toLowerCase().equals("lightning")) {
	    		Coordinate hit = player.trace(128);
	    		if (hit != null) {world.useLightning(hit); sender.sendLangfileMessageToPlayer("command.weather.lightningSuccess", new Object[0]);}
	    		else {sender.sendLangfileMessageToPlayer("command.weather.notInSight", new Object[0]);}
	    	}
		}
		else {sender.sendLangfileMessageToPlayer("command.weather.invalidUsage", new Object[0]);}
	}

	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}
	
	@Override
	public void unregisterFromHandler() {}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	@Override
	public int getPermissionLevel() {
		return 2;
	}
}
