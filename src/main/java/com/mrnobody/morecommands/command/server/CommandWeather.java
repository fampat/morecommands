package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Coordinate;
import com.mrnobody.morecommands.wrapper.Player;
import com.mrnobody.morecommands.wrapper.World;

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
		World world = sender.getWorld();
		
		if (params.length > 0) {
			boolean state = false;
			boolean entered = false;
			
	    	if (params.length > 1) {
	        	if (params[1].equalsIgnoreCase("enable") || params[1].equalsIgnoreCase("1")
	                    || params[1].equalsIgnoreCase("on") || params[1].equalsIgnoreCase("true")) {
	            		state = true; entered = true;
	                }
	                else if (params[0].equalsIgnoreCase("disable") || params[1].equalsIgnoreCase("0")
	                    	|| params[0].equalsIgnoreCase("off") || params[1].equalsIgnoreCase("false")) {
	                    state = false; entered = true;
	                }
	    	}
	    	
	    	if (params[0].equalsIgnoreCase("rain")) {
	    		if (entered) world.setRaining(state);
	    		else world.setRaining(!world.isRaining());
	    		sender.sendLangfileMessage("command.weather.rainSuccess");
	    	}
	    	
	    	if (params[0].equalsIgnoreCase("thunder")) {
	    		if (entered) world.setThunder(state);
	    		else world.setThunder(!world.isThunder());
	    		sender.sendLangfileMessage("command.weather.thunderSuccess");
	    	}
	    	
	    	if (params[0].equalsIgnoreCase("lightning") && sender.getMinecraftISender() instanceof EntityPlayerMP) {
	    		Coordinate hit = (new Player((EntityPlayerMP) sender.getMinecraftISender())).traceBlock(128);
	    		if (hit != null) {world.useLightning(hit); sender.sendLangfileMessage("command.weather.lightningSuccess");}
	    		else throw new CommandException("command.weather.notInSight", sender);
	    	}
		}
		else throw new CommandException("command.weather.invalidUsage", sender);
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
	public int getPermissionLevel() {
		return 2;
	}
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return true;
	}
}
