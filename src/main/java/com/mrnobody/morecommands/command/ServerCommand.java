package com.mrnobody.morecommands.command;

import java.util.UUID;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

import com.mrnobody.morecommands.command.server.CommandFly;
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
     * Invoked when the server stops to unregister commands from handlers
     */
	public abstract void unregisterFromHandler();
	
    /**
     * @return Whether the command sender can use the command
     */
	public abstract boolean canSenderUse(ICommandSender sender);
    
    public final void execute(ICommandSender sender, String[] params) {
    	System.out.println("sender: " + sender.getClass().getName());
    	if (MoreCommands.isModEnabled() && this.isEnabled(sender)) {
        	try{
        		if (!ServerPlayerSettings.playerSettingsMapping.containsKey(sender) && sender instanceof EntityPlayerMP)
        			ServerPlayerSettings.playerSettingsMapping.put((EntityPlayerMP) sender, ServerPlayerSettings.getPlayerSettings((EntityPlayerMP) sender));
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
    	MoreCommands mod = MoreCommands.getMoreCommands();
    	
    	if (!this.canSenderUse(sender)) {
    		sender.addChatMessage(new ChatComponentText(LanguageManager.getTranslation(mod.getCurrentLang(sender), "command.generic.notServer", new Object[0])));
    		return false;
    	}
    	
    	if (!(this.getAllowedServerType() == ServerType.ALL || this.getAllowedServerType() == mod.getRunningServer())) {
    		if (this.getAllowedServerType() == ServerType.INTEGRATED) sender.addChatMessage(new ChatComponentText(LanguageManager.getTranslation(mod.getCurrentLang(sender), "command.generic.notIntegrated", new Object[0])));
    		if (this.getAllowedServerType() == ServerType.DEDICATED) sender.addChatMessage(new ChatComponentText(LanguageManager.getTranslation(mod.getCurrentLang(sender), "command.generic.notDedicated", new Object[0])));
    		return false;
    	}
    	
    	Patcher.PlayerPatches clientInfo = Patcher.playerPatchMapping.get(sender);
    	
    	Requirement[] requierements = this.getRequirements();
    	if (clientInfo == null && requierements.length > 0) return false;
    	else if (clientInfo == null && requierements.length <= 0) return true;
    	
    	for (Requirement requierement : requierements) {
    		if (requierement == Requirement.PATCH_SERVERCONFIGMANAGER) {
    			if (!Patcher.serverConfigManagerPatched()) {
    				sender.addChatMessage(new ChatComponentText(LanguageManager.getTranslation(mod.getCurrentLang(sender), "command.generic.serverConfigManagerNotPatched", new Object[0])));
    	    		return false;
    			}
    		}
    		
    		if (requierement == Requirement.MODDED_CLIENT) {
    			if (!clientInfo.clientModded()) {
    				sender.addChatMessage(new ChatComponentText(LanguageManager.getTranslation(mod.getCurrentLang(sender), "command.generic.clientNotModded", new Object[0])));
    	    		return false;
    			}
    		}
    		
    		if (requierement == Requirement.PATCH_RENDERGLOBAL) {
    			if (!clientInfo.renderGlobalPatched()) {
    				sender.addChatMessage(new ChatComponentText(LanguageManager.getTranslation(mod.getCurrentLang(sender), "command.generic.renderGlobalNotPatched", new Object[0])));
    	    		return false;
    			}
    		}
    		
    		if (requierement == Requirement.PATCH_ENTITYPLAYERSP) {
    			if (!clientInfo.clientPlayerPatched()) {
    				sender.addChatMessage(new ChatComponentText(LanguageManager.getTranslation(mod.getCurrentLang(sender), "command.generic.clientPlayerNotPatched", new Object[0])));
    	    		return false;
    			}
    		}
    		
    		if (requierement == Requirement.PATCH_ENTITYPLAYERMP) {
    			if (!(sender instanceof com.mrnobody.morecommands.patch.EntityPlayerMP)) {
    				sender.addChatMessage(new ChatComponentText(LanguageManager.getTranslation(mod.getCurrentLang(sender), "command.generic.serverPlayerNotPatched", new Object[0])));
    	    		return false;
    			}
    		}
    		
      		if (requierement == Requirement.PATCH_NETHANDLERPLAYSERVER) {
    			if (!clientInfo.serverPlayHandlerPatched()) {
    				sender.addChatMessage(new ChatComponentText(LanguageManager.getTranslation(mod.getCurrentLang(sender), "command.generic.netServerPlayHandlerNotPatched", new Object[0])));
    	    		return false;
    			}
    		}
    	}
    	
    	return true;
    }
    
    @Override
    public boolean isUsernameIndex(String[] args, int index) {
    	return index == args.length - 1;
    }
}
