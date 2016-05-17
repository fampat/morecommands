package com.mrnobody.morecommands.command.server;

import java.util.ArrayList;
import java.util.List;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.GlobalSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.EntityLivingBase;

import net.minecraft.command.ICommandSender;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.EnumChatFormatting;

@Command(
		name = "enchant",
		description = "command.enchant.description",
		example = "command.enchant.example",
		syntax = "command.enchant.syntax",
		videoURL = "command.enchant.videoURL"
		)
public class CommandEnchant extends StandardCommand implements ServerCommandProperties {
	private static final int PAGE_MAX = 15;
	
	@Override
	public String getName() {
		return "enchant";
	}

	@Override
	public String getUsage() {
		return "command.enchant.syntax";
	}
	
	private Enchantment[] getEnchantments(String[] names) {
		List<Enchantment> list = new ArrayList<Enchantment>(names.length);
		for (String name : names) {
			Enchantment e = Enchantment.getEnchantmentByLocation(name);
			if (e != null) list.add(e);
		}
		return list.toArray(new Enchantment[list.size()]);
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		EntityLivingBase entity = new EntityLivingBase(getSenderAsEntity(sender.getMinecraftISender(), net.minecraft.entity.EntityLivingBase.class));
		
		if (params.length > 0) {
    		if(params[0].equals("list")) {
    			int page = 0;
    			String[] enchantments = Enchantment.getLocationNames();
    			
    			if (params.length > 1) {
    				try {
    					page = Integer.parseInt(params[1]) - 1; 
    					if (page < 0) page = 0;
    					else if (page * PAGE_MAX > enchantments.length) page = enchantments.length / PAGE_MAX;
    				}
    				catch (NumberFormatException e) {throw new CommandException("command.enchant.NAN", sender);}
    			}
    			
    			final int stop = (page + 1) * PAGE_MAX;;
    			for (int i = page * PAGE_MAX; i < stop && i < enchantments.length; i++)
    				sender.sendStringMessage(" - '" + enchantments[i] + "' (ID " + Enchantment.getEnchantmentByLocation(enchantments[i]).effectId + ")");
    			
    			sender.sendLangfileMessage("command.enchant.more", EnumChatFormatting.RED);
    		}
    		else if (params[0].equals("remove")) {
    			if (params.length <= 1 || (params.length > 1 && params[1].equalsIgnoreCase("*"))) {
    				entity.removeEnchantments();
    				sender.sendLangfileMessage("command.enchant.removeAllSuccess");
    			}
    			else if (params.length > 1) {
    				Enchantment e = getEnchantment(params[1]);
    				
    				if (e != null) entity.removeEnchantment(e);
    				else throw new CommandException("command.enchant.notFound", sender);
    				
    				sender.sendLangfileMessage("command.enchant.removeSuccess");
    			}
    		}
    		else if (params[0].equals("add") && params.length > 1) {
 				int level = 1;
				
				if (params.length > 2) {
					try {level = Integer.parseInt(params[2]);}
					catch (NumberFormatException e) {throw new CommandException("command.enchant.NAN", sender);}
				}
				
				Enchantment e = getEnchantment(params[1]);
				
				if (e != null) {
					if (!entity.addEnchantment(e, level, GlobalSettings.strictEnchanting))
						throw new CommandException("command.enchant.cantApply", sender);
				}
				else throw new CommandException("command.enchant.notFound", sender);
				
				sender.sendLangfileMessage("command.enchant.addSuccess");
    		}
    		else throw new CommandException("command.generic.invalidUsage", sender, this.getName());
		}
		else throw new CommandException("command.generic.invalidUsage", sender, this.getName());
	}
	
	@Override
	public CommandRequirement[] getRequirements() {
		return new CommandRequirement[0];
	}
	
	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	@Override
	public int getDefaultPermissionLevel() {
		return 2;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return isSenderOfEntityType(sender, net.minecraft.entity.EntityLivingBase.class);
	}
}
