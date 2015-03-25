package com.mrnobody.morecommands.command;

import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.ClientCommandHandler;

import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.patch.EntityClientPlayerMP;
import com.mrnobody.morecommands.util.LanguageManager;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import cpw.mods.fml.common.eventhandler.Event;

/**
 * Base Class for all client commands
 * 
 * @author MrNobody98
 */
public abstract class ClientCommand extends CommandBase {
	public final void processCommand(ICommandSender sender, String[] params) {
		MoreCommands mod = MoreCommands.getMoreCommands();
		
    	if (mod.isModEnabled() && this.isEnabled(Minecraft.getMinecraft().thePlayer)) {
        	try{
        		this.execute(new CommandSender(Minecraft.getMinecraft().thePlayer), params);
        	}
        	catch (CommandException e) {
        		sender.addChatMessage(new ChatComponentText(e.getMessage()));
        	}
    	}
    	else {
    		if (!mod.isModEnabled())
    			sender.addChatMessage(new ChatComponentText(LanguageManager.getTranslation(mod.getCurrentLang(sender), "command.generic.notEnabled", new Object[0])));
    	}
    }
	
    @Override
    public final boolean isEnabled(EntityPlayer player) {
		MoreCommands mod = MoreCommands.getMoreCommands();
		
    	if (!(player instanceof net.minecraft.client.entity.EntityClientPlayerMP)) {
    		player.addChatMessage(new ChatComponentText(LanguageManager.getTranslation(mod.getCurrentLang(player), "command.generic.notClient", new Object[0])));
    		return false;
    	}
    	
    	if (!(this.getAllowedServerType() == ServerType.ALL || this.getAllowedServerType() == mod.getRunningServer())) {
    		if (this.getAllowedServerType() == ServerType.INTEGRATED) player.addChatMessage(new ChatComponentText(LanguageManager.getTranslation(mod.getCurrentLang(player), "command.generic.notIntegrated", new Object[0])));
    		if (this.getAllowedServerType() == ServerType.DEDICATED) player.addChatMessage(new ChatComponentText(LanguageManager.getTranslation(mod.getCurrentLang(player), "command.generic.notDedicated", new Object[0])));
    		return false;
    	}
    	
    	Requirement[] requierements = this.getRequirements();
    	
    	for (Requirement requierement : requierements) {
    		if (requierement == Requirement.PATCH_ENTITYCLIENTPLAYERMP) {
    			if (!(Minecraft.getMinecraft().thePlayer instanceof EntityClientPlayerMP)) {
    				player.addChatMessage(new ChatComponentText(LanguageManager.getTranslation(mod.getCurrentLang(player), "command.generic.clientPlayerNotPatched", new Object[0])));
    	    		return false;
    			}
    		}
    		
    		if (requierement == Requirement.PATCH_CLIENTCOMMANDHANDLER) {
    			if (!(ClientCommandHandler.instance instanceof com.mrnobody.morecommands.patch.ClientCommandManager)) {
    				player.addChatMessage(new ChatComponentText(LanguageManager.getTranslation(mod.getCurrentLang(player), "command.generic.clientCommandHandlerNotPatched", new Object[0])));
    	    		return false;
    			}
    		}
    	}
    	
    	return true;
    }
    
    /**
     * @return Whether this command shall be registered if the server has this mod installed
     */
    public abstract boolean registerIfServerModded();
}
