package com.mrnobody.morecommands.command;

import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.patch.EntityClientPlayerMP;
import com.mrnobody.morecommands.util.LanguageManager;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.ClientCommandHandler;

/**
 * Base Class for all client commands
 * 
 * @author MrNobody98
 */
public abstract class ClientCommand extends CommandBase {
	public final void processCommand(ICommandSender sender, String[] params) {
    	if (MoreCommands.isModEnabled() && this.isEnabled(sender)) {
        	try{
        		this.execute(new CommandSender(sender), params);
        	}
        	catch (CommandException e) {
        		ChatComponentText text = new ChatComponentText(e.getMessage());
        		text.getChatStyle().setColor(EnumChatFormatting.RED);
        		if (!(sender instanceof EntityPlayerMP)) {if (CommandSender.output) sender.addChatMessage(text);}
        		else if (CommandSender.output && !ServerPlayerSettings.containsSettingsForPlayer((EntityPlayerMP) sender)) sender.addChatMessage(text);
        		else if (CommandSender.output && ServerPlayerSettings.getPlayerSettings((EntityPlayerMP) sender).output) sender.addChatMessage(text);
        	}
    	}
    	else {
    		if (!MoreCommands.isModEnabled()) {
        		ChatComponentText text = new ChatComponentText(LanguageManager.translate(MoreCommands.getMoreCommands().getCurrentLang(sender), "command.generic.notEnabled"));
        		text.getChatStyle().setColor(EnumChatFormatting.RED);
        		sender.addChatMessage(text);
    		}
    	}
    }
	
    @Override
    public final boolean isEnabled(ICommandSender sender) {
		String lang = MoreCommands.getMoreCommands().getCurrentLang(sender);
		
    	if (!(sender instanceof net.minecraft.client.entity.EntityClientPlayerMP)) {
    		sendChatMsg(sender, LanguageManager.translate(lang, "command.generic.notClient"));
    		return false;
    	}
    	
    	if (!(this.getAllowedServerType() == ServerType.ALL || this.getAllowedServerType() == MoreCommands.getMoreCommands().getRunningServer())) {
    		if (this.getAllowedServerType() == ServerType.INTEGRATED) 
    			sendChatMsg(sender, LanguageManager.translate(lang, "command.generic.notIntegrated"));
    		if (this.getAllowedServerType() == ServerType.DEDICATED)
    			sendChatMsg(sender, LanguageManager.translate(lang, "command.generic.notDedicated"));
    		return false;
    	}
    	
    	Requirement[] requierements = this.getRequirements();
    	
    	for (Requirement requierement : requierements) {
    		if (requierement == Requirement.PATCH_ENTITYCLIENTPLAYERMP) {
    			if (!(Minecraft.getMinecraft().thePlayer instanceof EntityClientPlayerMP)) {
    				sendChatMsg(sender, LanguageManager.translate(lang, "command.generic.clientPlayerNotPatched"));
    	    		return false;
    			}
    		}
    		
    		if (requierement == Requirement.PATCH_CLIENTCOMMANDHANDLER) {
    			if (!(ClientCommandHandler.instance instanceof com.mrnobody.morecommands.patch.ClientCommandManager)) {
    				sendChatMsg(sender, LanguageManager.translate(lang, "command.generic.clientCommandHandlerNotPatched"));
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
    
    /**
     * @return Whether this command shall be registered if the server has this mod installed
     */
    public abstract boolean registerIfServerModded();
}
