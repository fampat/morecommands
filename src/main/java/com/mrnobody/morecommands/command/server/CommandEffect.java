package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.potion.Potion;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.patch.EntityPlayerMP;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Player;

@Command(
		name = "effect",
		description = "command.effect.description",
		example = "command.effect.example",
		syntax = "command.effect.syntax",
		videoURL = "command.effect.videoURL"
		)
public class CommandEffect extends ServerCommand {

	@Override
	public String getName() {
		return "effect";
	}

	@Override
	public String getUsage() {
		return "command.effect.syntax";
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
    				int to = PAGE_MAX * page <= Potion.potionTypes.length ? PAGE_MAX * page : Potion.potionTypes.length;
    				int from = to - PAGE_MAX;
    				
    				for (int index = from; index < to; index++) {
    					if (Potion.potionTypes[index] != null) sender.sendStringMessage(" - '" + Potion.potionTypes[index].getName().substring(7) + "' (" + String.valueOf(Potion.potionTypes[index].getId()) + ")");
    				}
    				sender.sendLangfileMessage("command.effect.more", new Object[0]);
    			}
    			else {sender.sendLangfileMessage("command.effect.invalidUsage", new Object[0]);}
    		}
    		
    		else if (params[0].equals("remove")) {
    			if (params.length > 1) {
    				boolean broken = false;
    				
    				for (Potion p : Potion.potionTypes) {
    					if (p != null) {
    						if (params[1].equalsIgnoreCase(p.getName().substring(7)) || String.valueOf(p.getId()).equals(params[1])) {
    							player.removePotionEffect(p.getId()); sender.sendLangfileMessage("command.effect.removeSuccess", new Object[0]); broken = true; break;
    						}
    					}
    				}
    				if (!broken) sender.sendLangfileMessage("command.effect.removeFailure", new Object[0]);
    			}
    			else {sender.sendLangfileMessage("command.effect.invalidUsage", new Object[0]);}
    		}
    		
    		else if (params[0].equals("removeAll")) {player.removeAllPotionEffects(); sender.sendLangfileMessage("command.effect.removeAllSuccess", new Object[0]);}
    		
    		else if (params[0].equals("add")) {
    			if (params.length > 2) {
    				boolean broken = false;
    				
    				for (Potion p : Potion.potionTypes) {
    					if (p != null) {
    						if (params[1].toLowerCase().equals(p.getName().substring(7).toLowerCase()) || String.valueOf(p.getId()).equals(params[1])) {
    							try {player.addPotionEffect(p.getId(), Integer.parseInt(params[2]), params.length > 3 ? Integer.parseInt(params[3]) : 0); broken = true; sender.sendLangfileMessage("command.effect.addSuccess", new Object[0]);}
    							catch (NumberFormatException e) {sender.sendLangfileMessage("command.effect.NAN", new Object[0]); broken = true;}
    							break;
    						}
    					}
    				}
    				if (!broken) sender.sendLangfileMessage("command.effect.notFound", new Object[0]);
    			}
    			else {sender.sendLangfileMessage("command.effect.invalidUsage", new Object[0]);}
    		}
    		else {sender.sendLangfileMessage("command.effect.invalidUsage", new Object[0]);}
		}
		else {sender.sendLangfileMessage("command.effect.invalidUsage", new Object[0]);}
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
