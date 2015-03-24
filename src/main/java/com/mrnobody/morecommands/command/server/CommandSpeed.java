package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Player;

@Command(
		name = "speed",
		description = "command.speed.description",
		example = "command.speed.example",
		syntax = "command.speed.syntax",
		videoURL = "command.speed.videoURL"
		)
public class CommandSpeed extends ServerCommand {
	private final float walkSpeedDefault = 0.1F;
	private final float flySpeedDefault = 0.05F;

	@Override
	public String getCommandName() {
		return "speed";
	}

	@Override
	public String getUsage() {
		return "command.speed.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Player player = sender.toPlayer();
		
		if (params.length > 1) {
			if (params[0].equalsIgnoreCase("walk") || params[0].equalsIgnoreCase("fly")) {
				if (params[1].equalsIgnoreCase("set")) {
					if (params.length > 2) {
						float speed;
						
						try {speed = Float.parseFloat(params[2]);}
						catch (NumberFormatException nfe) {sender.sendLangfileMessageToPlayer("command.speed.NAN", new Object[0]); return;}
						
						if (params[0].equalsIgnoreCase("walk")) {
							player.getMinecraftPlayer().capabilities.setPlayerWalkSpeed(speed / 10);
							sender.sendLangfileMessageToPlayer("command.speed.walkSet", new Object[0]);
						}
						else if (params[0].equalsIgnoreCase("fly")) {
							player.getMinecraftPlayer().capabilities.setFlySpeed(speed / 10);
							sender.sendLangfileMessageToPlayer("command.speed.flySet", new Object[0]);
						}
						
						player.getMinecraftPlayer().sendPlayerAbilities();
					}
					else {sender.sendLangfileMessageToPlayer("command.speed.noArg", new Object[0]);}
				}
				else if (params[1].equalsIgnoreCase("get")) {
					if (params[0].equalsIgnoreCase("walk")) {sender.sendLangfileMessageToPlayer("command.speed.getWalk", new Object[] {(player.getMinecraftPlayer().capabilities.getWalkSpeed() * 10)});}
					else if (params[0].equalsIgnoreCase("fly")) {sender.sendLangfileMessageToPlayer("command.speed.getFly", new Object[] {(player.getMinecraftPlayer().capabilities.getFlySpeed() * 10)});}
				}
				else if (params[1].equalsIgnoreCase("reset")) {
					if (params[0].equalsIgnoreCase("walk")) {
						player.getMinecraftPlayer().capabilities.setPlayerWalkSpeed(this.walkSpeedDefault);
						sender.sendLangfileMessageToPlayer("command.speed.walkReset", new Object[0]);
					}
					else if (params[0].equalsIgnoreCase("fly")) {
						player.getMinecraftPlayer().capabilities.setFlySpeed(this.flySpeedDefault);
						sender.sendLangfileMessageToPlayer("command.speed.flyReset", new Object[0]);
					}
					
					player.getMinecraftPlayer().sendPlayerAbilities();
				}
				else {sender.sendLangfileMessageToPlayer("command.speed.invalidUsage", new Object[0]);}
			}
			else {sender.sendLangfileMessageToPlayer("command.speed.invalidUsage", new Object[0]);}
		}
		else {sender.sendLangfileMessageToPlayer("command.speed.invalidUsage", new Object[0]);}
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
