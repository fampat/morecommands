package com.mrnobody.morecommands.command.server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.GlobalSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;
import net.minecraft.item.Item;

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
	public String getName() {
		return "itemdamage";
	}

	@Override
	public String getUsage() {
		return "command.itemdamage.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		try {GlobalSettings.itemdamage = parseTrueFalse(params, 0, GlobalSettings.itemdamage);}
		catch (IllegalArgumentException ex) {throw new CommandException("command.itemdamage.failure", sender);}
		
		sender.sendLangfileMessage(GlobalSettings.itemdamage ? "command.itemdamage.on" : "command.itemdamage.off");
        
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
