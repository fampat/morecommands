package com.mrnobody.morecommands.command.server;

import java.util.List;
import java.util.ArrayList;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.command.CommandBase.Requirement;
import com.mrnobody.morecommands.command.CommandBase.ServerType;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Player;

import cpw.mods.fml.relauncher.Side;

@Command(
		name = "give",
		description = "command.give.description",
		example = "command.give.example",
		syntax = "command.give.syntax",
		videoURL = "command.give.videoURL"
		)
public class CommandGive extends ServerCommand {

	@Override
	public String getCommandName() {
		return "give";
	}

	@Override
	public String getUsage() {
		return "command.give.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params)throws CommandException {
		if (params.length > 1) {
			Player player = params[0].toLowerCase().equals("me") ? sender.toPlayer() : new Player(sender, params[0]);
			
			Item item = (Item)Item.itemRegistry.getObject(params[1].toLowerCase().startsWith("minecraft:") ? params[1].toLowerCase() : "minecraft:" + params[1].toLowerCase());
			
			if (item == null) {
				try {item = Item.getItemById(Integer.parseInt(params[1]));}
				catch (NumberFormatException e) {}
			}
			
			if (item != null) {
				if (params.length > 2) {
					if (params.length > 3) {
						if (item.getHasSubtypes()) {
							try {player.givePlayerItem(item, Integer.parseInt(params[2]), Integer.parseInt(params[3]));}
							catch(NumberFormatException e) {sender.sendLangfileMessageToPlayer("command.give.notFound", new Object[0]);}
						}
						else {sender.sendLangfileMessageToPlayer("command.give.noMeta", new Object[0]);}
					}
					else {
						try {player.givePlayerItem(item, Integer.parseInt(params[2])); sender.sendLangfileMessageToPlayer("command.give.success", new Object[0]);}
						catch (NumberFormatException e) {sender.sendLangfileMessageToPlayer("command.give.notFound", new Object[0]);}
					}
				}
				else {player.givePlayerItem(item); sender.sendLangfileMessageToPlayer("command.give.success", new Object[0]);}
			}
			else {sender.sendLangfileMessageToPlayer("command.give.notFound", new Object[0]);}
		}
		else {
			sender.sendLangfileMessageToPlayer("command.give.invalidUsage", new Object[0]);
		}
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
