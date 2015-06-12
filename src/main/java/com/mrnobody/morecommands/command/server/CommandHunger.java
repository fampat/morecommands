package com.mrnobody.morecommands.command.server;

import java.lang.reflect.Field;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
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
		Player player = new Player((EntityPlayerMP) sender.getMinecraftISender());
		int foodLevel;
		
		if (params.length > 0) {
			try {foodLevel = Integer.parseInt(params[0]);}
			catch (NumberFormatException e) {
				if (params[0].equalsIgnoreCase("min")) {foodLevel = 0;}
				else if (params[0].equalsIgnoreCase("max")) {foodLevel = 20;}
				else if (params[0].equalsIgnoreCase("get")) {sender.sendLangfileMessage("command.hunger.get", player.getHunger()); return;}
				else throw new CommandException("command.hunger.invalidParam", sender);
			}
			
			Field foodStats = ReflectionHelper.getField(FoodStats.class, "foodLevel");
			
			if (foodStats != null) {
				try {
					foodStats.setInt(player.getMinecraftPlayer().getFoodStats(), foodLevel);
					sender.sendLangfileMessage("command.hunger.success");
				}
				catch (Exception ex) {throw new CommandException("command.hunger.error", sender);}
			}
			else throw new CommandException("command.hunger.error", sender);
		}
		else throw new CommandException("command.hunger.invalidUsage", sender);
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
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return sender instanceof EntityPlayerMP;
	}
}
