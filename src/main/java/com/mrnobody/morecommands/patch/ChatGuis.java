package com.mrnobody.morecommands.patch;

import com.mrnobody.morecommands.core.AppliedPatches;
import com.mrnobody.morecommands.settings.ClientPlayerSettings;
import com.mrnobody.morecommands.settings.MoreCommandsConfig;
import com.mrnobody.morecommands.settings.PlayerSettings;
import com.mrnobody.morecommands.util.Variables;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;

/**
 * This class contains two subclasses of {@link net.minecraft.client.gui.GuiChat}
 * and  {@link net.minecraft.client.gui.GuiSleepMP} because these classes
 * are where chat messages are sent to the server. These subclasses do some
 * pre-processing before a message is sent to the server, e.g. replacing variables.
 * 
 * @author MrNobody98
 */
public final class ChatGuis {
	private ChatGuis() {}
	
	/**
	 * @author MrNobody98
	 * @see ChatGuis
	 */
	public static final class GuiChat extends net.minecraft.client.gui.GuiChat {
		public GuiChat() {}
		
		public GuiChat(String prefilledText) {
			super(prefilledText);
		}
		
		@Override
		public void sendChatMessage(String msg, boolean addToChat) {
			processChat(this.mc, msg, addToChat);
		}
	}
	
	/**
	 * @author MrNobody98
	 * @see ChatGuis
	 */
	public static final class GuiSleepMP extends net.minecraft.client.gui.GuiSleepMP {
		public GuiSleepMP() {}
		
		@Override
		public void sendChatMessage(String msg, boolean addToChat) {
			processChat(this.mc, msg, addToChat);
		}
	}
	
	/**
	 * Replaces client side variables and the send the message to the server
	 * (if it is not a client side command)
	 */
	private static void processChat(Minecraft mc, String chat, boolean addToChat) {
		if (addToChat) mc.ingameGUI.getChatGUI().addToSentMessages(chat);
		
		if (MoreCommandsConfig.enablePlayerVars) {
			ClientPlayerSettings settings = mc.thePlayer.getCapability(PlayerSettings.SETTINGS_CAP_CLIENT, null);
			
			if (settings != null) {
				boolean replaceIgnored;
				
				if (chat.length() > 1 && chat.charAt(0) == '%') {
					int end = chat.indexOf('%', 1);
					String val = end > 0 ? settings.variables.get(chat.substring(1, end)) : null;
					
					replaceIgnored = val != null && val.startsWith("/") &&
									(chat.length() - 1 == end || chat.charAt(end + 1) == ' ') &&
									ClientCommandHandler.instance.getCommands().containsKey(val.substring(1)) 
							? false : !AppliedPatches.serverModded();
				}
				else replaceIgnored = chat.startsWith("/") && ClientCommandHandler.instance.getCommands().containsKey(chat.substring(1).split(" ")[0]) ?
										false : !AppliedPatches.serverModded();
				
				try {chat = Variables.replaceVars(chat, replaceIgnored, settings.variables);}
				catch (Variables.VariablesCouldNotBeResolvedException e) {chat = e.getNewString();}
			}
		}
		
		if (chat.trim().startsWith("/") && ClientCommandHandler.instance.executeCommand(mc.thePlayer, chat) != 0) return;
		else mc.thePlayer.sendChatMessage(chat);
	}
}
