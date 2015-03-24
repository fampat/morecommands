package com.mrnobody.morecommands.command.server;

import net.minecraft.enchantment.Enchantment;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.command.CommandBase.Requirement;
import com.mrnobody.morecommands.command.CommandBase.ServerType;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Player;

import cpw.mods.fml.relauncher.Side;

@Command(
		name = "enchant",
		description = "command.enchant.description",
		example = "command.enchant.example",
		syntax = "command.enchant.syntax",
		videoURL = "command.enchant.videoURL"
		)
public class CommandEnchant extends ServerCommand {

	@Override
	public String getCommandName() {
		return "enchant";
	}

	@Override
	public String getUsage() {
		return "command.enchant.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Player player = sender.toPlayer();
		
		if (params.length > 0) {
    		if(params[0].equals("list")) {
    			int page = 1;
    			int PAGE_MAX = 15;
    			boolean validParam = true;
    			
    			if (params.length > 1) {
    				try {page = Integer.parseInt(params[1]);} 
    				catch (NumberFormatException e) {validParam = false;}
    			}
    			
    			if (validParam) {
    				int to = PAGE_MAX * page <= Enchantment.enchantmentsList.length ? PAGE_MAX * page : Enchantment.enchantmentsList.length;
    				int from = to - PAGE_MAX;
    				
    				for (int index = from; index < to; index++) {
    					if (Enchantment.enchantmentsList[index] != null) sender.sendStringMessageToPlayer(" - '" + Enchantment.enchantmentsList[index].getName().substring(12) + "' (" + String.valueOf(Enchantment.enchantmentsList[index].effectId) + ")");
    				}
    				sender.sendLangfileMessageToPlayer("command.enchant.more", new Object[0]);
    			}
    			else {sender.sendLangfileMessageToPlayer("command.enchant.invalidUsage", new Object[0]);}
    		}
    		
    		else if (params[0].equals("remove")) {
    			player.removeEnchantment(); 
    			sender.sendLangfileMessageToPlayer("command.enchant.removeSuccess", new Object[0]);
    		}
		
    		else if (params[0].equals("add")) {
    			if (params.length > 2) {
    				boolean broken = false;
    				
    				for (Enchantment e : Enchantment.enchantmentsList) {
    					if (e != null) {
    						if (params[1].toLowerCase().equals(e.getName().substring(12).toLowerCase()) || String.valueOf(e.effectId).equals(params[1])) {
    							try {player.addEnchantment(e, Integer.parseInt(params[2])); broken = true; sender.sendLangfileMessageToPlayer("command.enchant.addSuccess", new Object[0]);}
    							catch (NumberFormatException ex) {sender.sendLangfileMessageToPlayer("command.enchant.NAN", new Object[0]); broken = true;}
    							break;
    						}
    					}
    				}
    				if (!broken) sender.sendLangfileMessageToPlayer("command.enchant.notFound", new Object[0]);
    			}
    			else {sender.sendLangfileMessageToPlayer("command.enchant.invalidUsage", new Object[0]);}
    		}
    		else {sender.sendLangfileMessageToPlayer("command.enchant.invalidUsage", new Object[0]);}
		}
		else {sender.sendLangfileMessageToPlayer("command.enchant.invalidUsage", new Object[0]);}
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
