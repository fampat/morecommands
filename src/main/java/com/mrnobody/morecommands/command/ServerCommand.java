package com.mrnobody.morecommands.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.core.Patcher;
import com.mrnobody.morecommands.util.LanguageManager;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

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
	
    /**
     * @return Whether the command sender can use the command
     */
	public abstract boolean canSenderUse(ICommandSender sender);
    
    public final void processCommand(ICommandSender sender, String[] params) {
    	if (MoreCommands.isModEnabled() && this.isEnabled(sender)) {
        	try{
        		if (!ServerPlayerSettings.playerSettingsMapping.containsKey(sender) && sender instanceof EntityPlayerMP)
        			ServerPlayerSettings.playerSettingsMapping.put((EntityPlayerMP) sender, ServerPlayerSettings.getPlayerSettings((EntityPlayerMP) sender));
        		this.execute(new CommandSender(sender), params);
        	}
        	catch (CommandException e) {
        		ChatComponentText text = new ChatComponentText(e.getMessage());
        		text.getChatStyle().setColor(EnumChatFormatting.RED);
        		sender.addChatMessage(text);
        	}
    	}
    	else {
    		if (!MoreCommands.isModEnabled()) {
        		ChatComponentText text = new ChatComponentText(LanguageManager.getTranslation(MoreCommands.getMoreCommands().getCurrentLang(sender), "command.generic.notEnabled"));
        		text.getChatStyle().setColor(EnumChatFormatting.RED);
        		sender.addChatMessage(text);
    		}
    		else if (!(sender instanceof EntityPlayerMP)) {
        		ChatComponentText text = new ChatComponentText(LanguageManager.getTranslation(MoreCommands.getMoreCommands().getCurrentLang(sender), "command.generic.notServer"));
        		text.getChatStyle().setColor(EnumChatFormatting.RED);
        		sender.addChatMessage(text);
    		}
    	}
    }
    
    public final boolean isEnabled(ICommandSender sender) {
    	String lang = MoreCommands.getMoreCommands().getCurrentLang(sender);
    	
    	if (!this.canSenderUse(sender)) {
    		sendChatMsg(sender, LanguageManager.getTranslation(lang, "command.generic.notServer"));
    		return false;
    	}
    	
    	if (!(this.getAllowedServerType() == ServerType.ALL || this.getAllowedServerType() == MoreCommands.getMoreCommands().getRunningServer())) {
    		if (this.getAllowedServerType() == ServerType.INTEGRATED)
    			sendChatMsg(sender, LanguageManager.getTranslation(lang, "command.generic.notIntegrated"));
    		if (this.getAllowedServerType() == ServerType.DEDICATED) 
    			sendChatMsg(sender, LanguageManager.getTranslation(lang, "command.generic.notDedicated"));
    		return false;
    	}
    	
    	Patcher.PlayerPatches clientInfo = Patcher.playerPatchMapping.get(sender);
    	
    	Requirement[] requierements = this.getRequirements();
    	if (clientInfo == null && requierements.length > 0) return false;
    	else if (clientInfo == null && requierements.length <= 0) return true;
    	
    	for (Requirement requierement : requierements) {
    		if (requierement == Requirement.PATCH_SERVERCONFIGMANAGER) {
    			if (!Patcher.serverConfigManagerPatched()) {
    				sendChatMsg(sender, LanguageManager.getTranslation(lang, "command.generic.serverConfigManagerNotPatched"));
    	    		return false;
    			}
    		}
    		
    		if (requierement == Requirement.PATCH_SERVERCOMMANDHANDLER) {
    			if (!Patcher.serverCommandManagerPatched()) {
    				sendChatMsg(sender, LanguageManager.getTranslation(lang, "command.generic.serverCommandManagerNotPatched"));
    	    		return false;
    			}
    		}
    		
    		if (requierement == Requirement.MODDED_CLIENT) {
    			if (!clientInfo.clientModded()) {
    				sendChatMsg(sender, LanguageManager.getTranslation(lang, "command.generic.clientNotModded"));
    	    		return false;
    			}
    		}
    		
    		if (requierement == Requirement.PATCH_ENTITYCLIENTPLAYERMP) {
    			if (!clientInfo.clientPlayerPatched()) {
    				sendChatMsg(sender, LanguageManager.getTranslation(lang, "command.generic.clientPlayerNotPatched"));
    	    		return false;
    			}
    		}
    		
    		if (requierement == Requirement.PATCH_ENTITYPLAYERMP) {
    			if (!(sender instanceof com.mrnobody.morecommands.patch.EntityPlayerMP)) {
    				sendChatMsg(sender, LanguageManager.getTranslation(lang, "command.generic.serverPlayerNotPatched"));
    	    		return false;
    			}
    		}
    		
      		if (requierement == Requirement.PATCH_NETHANDLERPLAYSERVER) {
    			if (!clientInfo.serverPlayHandlerPatched()) {
    				sendChatMsg(sender, LanguageManager.getTranslation(lang, "command.generic.netServerPlayHandlerNotPatched"));
    	    		return false;
    			}
    		}
    	}
    	
    	return true;
    }
    
    private final void sendChatMsg(ICommandSender sender, String msg) {
    	ChatComponentText text = new ChatComponentText(msg);
    	text.getChatStyle().setColor(EnumChatFormatting.RED);
    	sender.addChatMessage(text);
    }
}
