package com.mrnobody.morecommands.command.server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.command.ICommandSender;
import net.minecraft.item.Item;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.util.GlobalSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "itemdamage",
		description = "command.itemdamage.description",
		example = "command.itemdamage.example",
		syntax = "command.itemdamage.syntax",
		videoURL = "command.itemdamage.videoURL"
		)
public class CommandItemdamage extends ServerCommand {
	private final Map<Item, Integer> damageValues = new HashMap<Item, Integer>();
	
	public CommandItemdamage() {
		Iterator<Item> items = Item.itemRegistry.iterator();
		
		while (items.hasNext()) {
			Item item = items.next();
			this.damageValues.put(item, item.getMaxDamage());
		}
	}
	
	
	@Override
	public String getCommandName() {
		return "itemdamage";
	}

	@Override
	public String getUsage() {
		return "command.itemdamage.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
        if (params.length > 0) {
        	if (params[0].equalsIgnoreCase("enable") || params[0].equalsIgnoreCase("1")
            	|| params[0].equalsIgnoreCase("on") || params[0].equalsIgnoreCase("true")) {
        		GlobalSettings.itemdamage = true;
            	sender.sendLangfileMessage("command.itemdamage.on");
            }
            else if (params[0].equalsIgnoreCase("disable") || params[0].equalsIgnoreCase("0")
            		|| params[0].equalsIgnoreCase("off") || params[0].equalsIgnoreCase("false")) {
            	GlobalSettings.itemdamage = false;
            	sender.sendLangfileMessage("command.itemdamage.off");
            }
            else throw new CommandException("command.itemdamage.failure", sender);
        }
        else {
        	GlobalSettings.itemdamage = !GlobalSettings.itemdamage;
        	sender.sendLangfileMessage(GlobalSettings.itemdamage ? "command.itemdamage.on" : "command.itemdamage.off");
        }
        
        if (GlobalSettings.itemdamage) {
			Iterator<Item> items = this.damageValues.keySet().iterator();
			
			while (items.hasNext()) {
				Item item = items.next();
				
				item.setMaxDamage(this.damageValues.get(item));
			}
        }
        else {
			Iterator<Item> items = this.damageValues.keySet().iterator();
			
			while (items.hasNext()) items.next().setMaxDamage(-1);
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
		return true;
	}
}
