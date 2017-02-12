package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.EntityUtils;
import com.mrnobody.morecommands.util.WorldUtils;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

@Command(
		name = "weather",
		description = "command.weather.description",
		example = "command.weather.example",
		syntax = "command.weather.syntax",
		videoURL = "command.weather.videoURL"
		)
public class CommandWeather extends StandardCommand implements ServerCommandProperties {

	@Override
	public String getCommandName() {
		return "weather";
	}

	@Override
	public String getCommandUsage() {
		return "command.weather.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
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
	    		if (entered) WorldUtils.setRaining(world, state);
	    		else WorldUtils.setRaining(world, !WorldUtils.isRaining(world));
	    		sender.sendLangfileMessage("command.weather.rainSuccess");
	    	}
	    	
	    	if (params[0].equalsIgnoreCase("thunder")) {
	    		if (entered) WorldUtils.setThunder(world, state);
	    		else WorldUtils.setThunder(world, !WorldUtils.isThunder(world));
	    		sender.sendLangfileMessage("command.weather.thunderSuccess");
	    	}
	    	
	    	if (params[0].equalsIgnoreCase("lightning")) {
	    		BlockPos hit = params.length > 3 ? getCoordFromParams(sender.getMinecraftISender(), params, 1) : 
	    				isSenderOfEntityType(sender.getMinecraftISender(), net.minecraft.entity.Entity.class) ?
	    				EntityUtils.traceBlock(getSenderAsEntity(sender.getMinecraftISender(), Entity.class), 128) :
	    				sender.getPosition();
	    		if (hit != null) {WorldUtils.useLightning(world, hit); sender.sendLangfileMessage("command.weather.lightningSuccess");}
	    		else throw new CommandException("command.weather.notInSight", sender);
	    	}
		}
		else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
		
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
	public int getDefaultPermissionLevel(String[] args) {
		return 2;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return true;
	}
}
