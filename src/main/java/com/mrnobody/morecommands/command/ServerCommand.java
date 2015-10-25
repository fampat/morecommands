package com.mrnobody.morecommands.command;

import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.core.AppliedPatches;
import com.mrnobody.morecommands.util.LanguageManager;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

/**
 * Base class for all server commands
 * 
 * @author MrNobody98
 */
public abstract class ServerCommand extends CommandBase {
    /**
     * @return Whether the command sender can use the command
     */
	public abstract boolean canSenderUse(ICommandSender sender);
    
    public final void execute(ICommandSender sender, String[] params) {
    	if (MoreCommands.isModEnabled() && this.isEnabled(sender)) {
        	try {
        		this.execute(new CommandSender(sender), params);
        	}
        	catch (CommandException e) {
        		sender.addChatMessage(new ChatComponentText(e.getMessage()));
        	}
    	}
    	else {
    		if (!MoreCommands.isModEnabled())
    			sender.addChatMessage(new ChatComponentText(LanguageManager.getTranslation(MoreCommands.getMoreCommands().getCurrentLang(sender), "command.generic.notEnabled", new Object[0])));
    		else if (!(sender instanceof EntityPlayerMP))
    			sender.addChatMessage(new ChatComponentText(LanguageManager.getTranslation(MoreCommands.getMoreCommands().getCurrentLang(sender), "command.generic.notServer", new Object[0])));
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
    	
    	AppliedPatches.PlayerPatches clientInfo = AppliedPatches.playerPatchMapping.get(sender);
    	
    	Requirement[] requierements = this.getRequirements();
    	if (clientInfo == null && requierements.length > 0) return false;
    	else if (clientInfo == null && requierements.length <= 0) return true;
    	
    	for (Requirement requierement : requierements) {
    		if (requierement == Requirement.PATCH_SERVERCONFIGMANAGER) {
    			if (!AppliedPatches.serverConfigManagerPatched()) {
    				sendChatMsg(sender, LanguageManager.getTranslation(lang, "command.generic.serverConfigManagerNotPatched"));
    	    		return false;
    			}
    		}
    		
    		if (requierement == Requirement.PATCH_SERVERCOMMANDHANDLER) {
    			if (!AppliedPatches.serverCommandManagerPatched()) {
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
    		
    		if (requierement == Requirement.HANDSHAKE_FINISHED) {
    			if (!clientInfo.handshakeFinished()) {
    				sendChatMsg(sender, LanguageManager.getTranslation(lang, "command.generic.handshakeNotFinished"));
    	    		return false;
    			}
    		}
    		
    		if (requierement == Requirement.HANDSHAKE_FINISHED_IF_CLIENT_MODDED) {
    			if (clientInfo.clientModded() && !clientInfo.handshakeFinished()) {
    				sendChatMsg(sender, LanguageManager.getTranslation(lang, "command.generic.handshakeNotFinished"));
    	    		return false;
    			}
    		}
    		
    		if (requierement == Requirement.PATCH_RENDERGLOBAL) {
    			if (!clientInfo.renderGlobalPatched()) {
    				sendChatMsg(sender, LanguageManager.getTranslation(lang, "command.generic.renderGlobalNotPatched"));
    	    		return false;
    			}
    		}
    		
    		if (requierement == Requirement.PATCH_ENTITYPLAYERSP) {
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
