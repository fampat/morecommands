package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.enchantment.Enchantment;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.patch.EntityPlayerMP;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Player;

@Command(
		name = "enchant",
		description = "command.enchant.description",
		example = "command.enchant.example",
		syntax = "command.enchant.syntax",
		videoURL = "command.enchant.videoURL"
		)
public class CommandEnchant extends ServerCommand {

	@Override
	public String getName() {
		return "enchant";
	}

	@Override
	public String getUsage() {
		return "command.enchant.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Player player = new Player((EntityPlayerMP) sender.getMinecraftISender());
		
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
    				int to = PAGE_MAX * page <= Enchantment.enchantmentsBookList.length ? PAGE_MAX * page : Enchantment.enchantmentsBookList.length;
    				int from = to - PAGE_MAX;
    				
    				for (int index = from; index < to; index++) {
    					if (Enchantment.enchantmentsBookList[index] != null) sender.sendStringMessage(" - '" + Enchantment.enchantmentsBookList[index].getName().substring(12) + "' (" + String.valueOf(Enchantment.enchantmentsBookList[index].effectId) + ")");
    				}
    				sender.sendLangfileMessage("command.enchant.more");
    			}
    			else throw new CommandException("command.enchant.invalidUsage", sender);
    		}
    		
    		else if (params[0].equals("remove")) {
    			player.removeEnchantment(); 
    			sender.sendLangfileMessage("command.enchant.removeSuccess");
    		}
		
    		else if (params[0].equals("add")) {
    			if (params.length > 2) {
    				boolean found = false;
    				
    				for (Enchantment e : Enchantment.enchantmentsBookList) {
    					if (e != null) {
    						if (params[1].equalsIgnoreCase(e.getName().substring(12)) || String.valueOf(e.effectId).equals(params[1])) {
    							try {player.addEnchantment(e, Integer.parseInt(params[2])); found = true; sender.sendLangfileMessage("command.enchant.addSuccess");}
    							catch (NumberFormatException ex) {sender.sendLangfileMessage("command.enchant.NAN"); found = true;}
    							break;
    						}
    					}
    				}
    				if (!found) throw new CommandException("command.enchant.notFound", sender);
    			}
    			else throw new CommandException("command.enchant.invalidUsage", sender);
    		}
    		else throw new CommandException("command.enchant.invalidUsage", sender);
		}
		else throw new CommandException("command.enchant.invalidUsage", sender);
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
