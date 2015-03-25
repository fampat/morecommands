package com.mrnobody.morecommands.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;

import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.core.Patcher;
import com.mrnobody.morecommands.util.LanguageManager;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import cpw.mods.fml.common.eventhandler.Event;

/**
 * Base class for all server commands
 * 
 * @author MrNobody98
 */
public abstract class ServerCommand extends CommandBase {
    /**
     * Called when the server stops to unregister commands from handlers
     */
	public abstract void unregisterFromHandler();
    
    public final void processCommand(ICommandSender sender, String[] params) {
    	MoreCommands mod = MoreCommands.getMoreCommands();
    	
    	if (mod.isModEnabled() && sender instanceof EntityPlayerMP && this.isEnabled((EntityPlayerMP) sender)) {
        	try{
        		if (!ServerPlayerSettings.playerSettingsMapping.containsKey(sender))
        			ServerPlayerSettings.playerSettingsMapping.put((EntityPlayerMP) sender, ServerPlayerSettings.getPlayerSettings((EntityPlayerMP) sender));
        		this.execute(new CommandSender(sender), params);
        	}
        	catch (CommandException e) {
        		sender.addChatMessage(new ChatComponentText(e.getMessage()));
        	}
    	}
    	else {
    		if (!mod.isModEnabled())
    			sender.addChatMessage(new ChatComponentText(LanguageManager.getTranslation(mod.getCurrentLang(sender), "command.generic.notEnabled", new Object[0])));
    		else if (!(sender instanceof EntityPlayerMP))
    			sender.addChatMessage(new ChatComponentText(LanguageManager.getTranslation(mod.getCurrentLang(sender), "command.generic.notServer", new Object[0])));
    	}
    }
    
    public final boolean isEnabled(EntityPlayer player) {
    	MoreCommands mod = MoreCommands.getMoreCommands();
    	
    	if (!(player instanceof net.minecraft.entity.player.EntityPlayerMP)) {
    		player.addChatMessage(new ChatComponentText(LanguageManager.getTranslation(mod.getCurrentLang(player), "command.generic.notServer", new Object[0])));
    		return false;
    	}
    	
    	if (!(this.getAllowedServerType() == ServerType.ALL || this.getAllowedServerType() == mod.getRunningServer())) {
    		if (this.getAllowedServerType() == ServerType.INTEGRATED) player.addChatMessage(new ChatComponentText(LanguageManager.getTranslation(mod.getCurrentLang(player), "command.generic.notIntegrated", new Object[0])));
    		if (this.getAllowedServerType() == ServerType.DEDICATED) player.addChatMessage(new ChatComponentText(LanguageManager.getTranslation(mod.getCurrentLang(player), "command.generic.notDedicated", new Object[0])));
    		return false;
    	}
    	
    	Patcher.PlayerPatches clientInfo = Patcher.playerPatchMapping.get(player);
    	
    	if (clientInfo == null) {
    		player.addChatMessage(new ChatComponentText(LanguageManager.getTranslation(mod.getCurrentLang(player), "command.generic.noPlayerInfoAvailable", new Object[0])));
    		return false;
    	}
    	
    	Requirement[] requierements = this.getRequirements();
    	
    	for (Requirement requierement : requierements) {
    		if (requierement == Requirement.PATCH_SERVERCONFIGMANAGER) {
    			if (!Patcher.serverConfigManagerPatched()) {
    				player.addChatMessage(new ChatComponentText(LanguageManager.getTranslation(mod.getCurrentLang(player), "command.generic.serverConfigManagerNotPatched", new Object[0])));
    	    		return false;
    			}
    		}
    		
    		if (requierement == Requirement.MODDED_CLIENT) {
    			if (!clientInfo.clientModded()) {
    				player.addChatMessage(new ChatComponentText(LanguageManager.getTranslation(mod.getCurrentLang(player), "command.generic.clientNotModded", new Object[0])));
    	    		return false;
    			}
    		}
    		
    		if (requierement == Requirement.PATCH_ENTITYCLIENTPLAYERMP) {
    			if (!clientInfo.clientPlayerPatched()) {
    				player.addChatMessage(new ChatComponentText(LanguageManager.getTranslation(mod.getCurrentLang(player), "command.generic.clientPlayerNotPatched", new Object[0])));
    	    		return false;
    			}
    		}
    		
    		if (requierement == Requirement.PATCH_ENTITYPLAYERMP) {
    			if (!(player instanceof com.mrnobody.morecommands.patch.EntityPlayerMP)) {
    				player.addChatMessage(new ChatComponentText(LanguageManager.getTranslation(mod.getCurrentLang(player), "command.generic.serverPlayerNotPatched", new Object[0])));
    	    		return false;
    			}
    		}
    		
      		if (requierement == Requirement.PATCH_NETHANDLERPLAYSERVER) {
    			if (!clientInfo.serverPlayHandlerPatched()) {
    				player.addChatMessage(new ChatComponentText(LanguageManager.getTranslation(mod.getCurrentLang(player), "command.generic.netServerPlayHandlerNotPatched", new Object[0])));
    	    		return false;
    			}
    		}
    	}
    	
    	return true;
    }
}
