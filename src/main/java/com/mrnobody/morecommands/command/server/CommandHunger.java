package com.mrnobody.morecommands.command.server;

import java.lang.reflect.Field;

import net.minecraft.util.FoodStats;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.util.ReflectionHelper;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Player;

@Command(
		name = "hunger",
		description = "command.hunger.description",
		example = "command.hunger.example",
		syntax = "command.hunger.syntax",
		videoURL = "command.hunger.videoURL"
		)
public class CommandHunger extends ServerCommand {

	@Override
	public String getCommandName() {
		return "hunger";
	}

	@Override
	public String getUsage() {
		return "command.hunger.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Player player = sender.toPlayer();
		int foodLevel;
		
		if (params.length > 0) {
			try {foodLevel = Integer.parseInt(params[0]);}
			catch (NumberFormatException e) {
				if (params[0].toLowerCase().equals("min")) {foodLevel = 0;}
				else if (params[0].toLowerCase().equals("max")) {foodLevel = 20;}
				else if (params[0].toLowerCase().equals("get")) {sender.sendLangfileMessageToPlayer("command.hunger.get", new Object[] {player.getHunger()}); return;}
				else {sender.sendLangfileMessageToPlayer("command.hunger.invalidParam", new Object[0]); return;}
			}
			
			Field foodStats = ReflectionHelper.getField(FoodStats.class, "foodLevel");
			
			if (foodStats != null) {
				try {
					foodStats.setInt(player.getMinecraftPlayer().getFoodStats(), foodLevel);
					sender.sendLangfileMessageToPlayer("command.hunger.success", new Object[0]);
				}
				catch (Exception ex) {
					sender.sendLangfileMessageToPlayer("command.hunger.error", new Object[0]);
				}
			}
			else sender.sendLangfileMessageToPlayer("command.hunger.error", new Object[0]);
		}
		else {sender.sendLangfileMessageToPlayer("command.hunger.invalidUsage", new Object[0]);}
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
