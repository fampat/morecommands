package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.Potion;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
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
	public String getCommandName() {
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
    				sender.sendLangfileMessage("command.effect.more");
    			}
    			else throw new CommandException("command.effect.invalidUsage", sender);
    		}
    		
    		else if (params[0].equals("remove")) {
    			if (params.length > 1) {
    				boolean found = false;
    				
    				for (Potion p : Potion.potionTypes) {
    					if (p != null) {
    						if (params[1].equalsIgnoreCase(p.getName().substring(7)) || String.valueOf(p.getId()).equals(params[1])) {
    							player.removePotionEffect(p.getId()); sender.sendLangfileMessage("command.effect.removeSuccess"); found = true; break;
    						}
    					}
    				}
    				if (!found) throw new CommandException("command.effect.removeFailure", sender);
    			}
    			else throw new CommandException("command.effect.invalidUsage", sender);
    		}
    		
    		else if (params[0].equals("removeAll")) {player.removeAllPotionEffects(); sender.sendLangfileMessage("command.effect.removeAllSuccess");}
    		
    		else if (params[0].equals("add")) {
    			if (params.length > 3) {
    				boolean found = false;
    				
    				for (Potion p : Potion.potionTypes) {
    					if (p != null) {
    						if (params[1].equalsIgnoreCase(p.getName().substring(7)) || String.valueOf(p.getId()).equals(params[1])) {
    							try {player.addPotionEffect(p.getId(), Integer.parseInt(params[2]), Integer.parseInt(params[3])); found = true; sender.sendLangfileMessage("command.effect.addSuccess");}
    							catch (NumberFormatException e) {sender.sendLangfileMessage("command.effect.NAN"); found = true;}
    							break;
    						}
    					}
    				}
    				if (!found) throw new CommandException("command.effect.notFound", sender);
    			}
    			else throw new CommandException("command.effect.invalidUsage", sender);
    		}
    		else throw new CommandException("command.effect.invalidUsage", sender);
		}
		else throw new CommandException("command.effect.invalidUsage", sender);
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
