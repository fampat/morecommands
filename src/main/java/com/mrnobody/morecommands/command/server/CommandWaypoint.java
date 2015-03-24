package com.mrnobody.morecommands.command.server;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.command.CommandBase.Requirement;
import com.mrnobody.morecommands.command.CommandBase.ServerType;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Coordinate;
import com.mrnobody.morecommands.wrapper.Player;

import cpw.mods.fml.relauncher.Side;

@Command(
		name = "waypoint",
		description = "command.waypoint.description",
		example = "command.waypoint.example",
		syntax = "command.waypoint.syntax",
		videoURL = "command.waypoint.videoURL"
		)
public class CommandWaypoint extends ServerCommand {
	private class NotFoundException extends Exception {}
	
	@Override
	public String getCommandName() {
		return "waypoint";
	}

	@Override
	public String getUsage() {
		return "command.waypoint.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		ServerPlayerSettings settings = ServerPlayerSettings.playerSettingsMapping.get(sender.getMinecraftISender());
		
		if (params.length > 1) {
			Player player = sender.toPlayer();
			
			if (params[0].equalsIgnoreCase("set")) {
				double x = player.getPosition().getX();
				double y = player.getPosition().getY();
				double z = player.getPosition().getZ();
					
				if (params.length > 4) {
					try {
						x = Double.parseDouble(params[2]);
						y = Double.parseDouble(params[3]);
						z = Double.parseDouble(params[4]);
					}
					catch (NumberFormatException nfe) {sender.sendLangfileMessageToPlayer("command.waypoint.NAN", new Object[0]); return;}
				}
				String name = params[1];
				double[] data = new double[] {x, y, z, (double) player.getYaw(), (double) player.getPitch()};
				this.setWaypoint(settings.waypoints, name, data);
				settings.saveSettings();
				DecimalFormat f = new DecimalFormat("#.##");
					
				sender.sendStringMessageToPlayer("Waypoint '" + name + "' successfully set at: "
						+ " X = " + f.format(x)
						+ "; Y = " + f.format(y)
						+ "; Z = " + f.format(z));
			}
			else if (params[0].equalsIgnoreCase("rem") || params[0].equalsIgnoreCase("remove") || params[0].equalsIgnoreCase("del") || params[0].equalsIgnoreCase("delete")) {
				try {this.deleteWaypoint(settings.waypoints, params[1]); settings.saveSettings(); sender.sendLangfileMessageToPlayer("command.waypoint.removed", new Object[] {params[1]});}
				catch (NotFoundException nfe) {sender.sendLangfileMessageToPlayer("command.waypoint.notFound", new Object[] {params[1]});}
			}
			else if (params[0].equalsIgnoreCase("goto")) {
				double[] data;
				try {data = this.getWaypoint(settings.waypoints, params[1]);}
				catch (NotFoundException nfe) {sender.sendLangfileMessageToPlayer("command.waypoint.notFound", new Object[] {params[1]}); return;}
				
				player.setPosition(new Coordinate(data[0], data[1], data[2]));
				player.setYaw((float) data[3]);
				player.setPitch((float) data[4]);
				
				sender.sendLangfileMessageToPlayer("command.waypoint.teleported", new Object[] {params[1]});
			}
			else {sender.sendLangfileMessageToPlayer("command.waypoint.invalidArgs", new Object[0]);}
		}
		else if (params.length > 0 && params[0].equalsIgnoreCase("list")) {
			if (settings.waypoints == null) return;
			
			Iterator<String> names = settings.waypoints.keySet().iterator();
			DecimalFormat f = new DecimalFormat("#.##");
			
			while (names.hasNext()) {
				String name = names.next();
				double[] data = settings.waypoints.get(name);
				
				sender.sendStringMessageToPlayer("- '" + name + "' (X = " + f.format(data[0]) + "; Y = " + f.format(data[1]) + "; Z = " + f.format(data[2]) + ")");
			}
		}
		else {sender.sendLangfileMessageToPlayer("command.waypoint.invalidArgs", new Object[0]);}
	}

	private void setWaypoint(Map<String, double[]> waypoints, String name, double[] data) {
		waypoints.put(name, data);
	}
	
	private void deleteWaypoint(Map<String, double[]> waypoints, String name) throws NotFoundException {
		if (!waypoints.containsKey(name)) throw new NotFoundException();
		waypoints.remove(name);
	}
	
	private double[] getWaypoint(Map<String, double[]> waypoints, String name) throws NotFoundException {
		if (!waypoints.containsKey(name)) throw new NotFoundException();
		return waypoints.get(name);
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
