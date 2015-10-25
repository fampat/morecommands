package com.mrnobody.morecommands.command.server;

import java.lang.reflect.Field;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listeners.EventListener;
import com.mrnobody.morecommands.util.ReflectionHelper;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Player;

import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.FoodStats;

@Command(
		name = "hunger",
		description = "command.hunger.description",
		example = "command.hunger.example",
		syntax = "command.hunger.syntax",
		videoURL = "command.hunger.videoURL"
		)
public class CommandHunger extends ServerCommand implements EventListener<TickEvent> {
	private final Field foodLevel = ReflectionHelper.getField(FoodStats.class, "foodLevel");
	
	public CommandHunger() {
		EventHandler.TICK.getHandler().register(this);
	}

	@Override
	public void onEvent(TickEvent ev) {
		if (this.foodLevel != null && ev instanceof TickEvent.PlayerTickEvent) {
			TickEvent.PlayerTickEvent event = (TickEvent.PlayerTickEvent) ev;
			
			if (event.player instanceof EntityPlayerMP && event.phase == TickEvent.Phase.END) {
				ServerPlayerSettings settings = ServerPlayerSettings.getPlayerSettings((EntityPlayerMP) event.player);
				if (!settings.hunger) {
					try {this.foodLevel.setInt(event.player.getFoodStats(), 20);}
					catch (Exception ex) {ex.printStackTrace();}
				}
			}
		}
	}
	
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
		ServerPlayerSettings settings = ServerPlayerSettings.getPlayerSettings((EntityPlayerMP) sender.getMinecraftISender());
		int foodLevel;
		
		if (params.length > 0) {
			try {foodLevel = Integer.parseInt(params[0]);}
			catch (NumberFormatException e) {
				if (params[0].equalsIgnoreCase("min")) {foodLevel = 0;}
				else if (params[0].equalsIgnoreCase("max")) {foodLevel = 20;}
				else if (params[0].equalsIgnoreCase("get")) {sender.sendLangfileMessage("command.hunger.get", player.getHunger()); return;}
				else if (params[0].equalsIgnoreCase("enable") || params[0].equalsIgnoreCase("1")
						|| params[0].equalsIgnoreCase("on") || params[0].equalsIgnoreCase("true")) {
						settings.hunger = true;
						sender.sendLangfileMessage("command.hunger.on"); return;
				}
				else if (params[0].equalsIgnoreCase("disable") || params[0].equalsIgnoreCase("0")
						|| params[0].equalsIgnoreCase("off") || params[0].equalsIgnoreCase("false")) {
						settings.hunger = false;
						sender.sendLangfileMessage("command.hunger.off"); return;
				}
				else throw new CommandException("command.hunger.invalidParam", sender);
			}
			
			if (this.foodLevel != null) {
				try {
					this.foodLevel.setInt(player.getMinecraftPlayer().getFoodStats(), foodLevel < 0 ? 0 : foodLevel > 20 ? 20 : foodLevel);
					sender.sendLangfileMessage("command.hunger.success");
				}
				catch (Exception ex) {throw new CommandException("command.hunger.error", sender);}
			}
			else throw new CommandException("command.hunger.error", sender);
		}
        else {
        	settings.hunger = !settings.hunger;
        	sender.sendLangfileMessage(settings.hunger ? "command.hunger.on" : "command.hunger.off");
        }
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
		return sender instanceof EntityPlayerMP;
	}
}
