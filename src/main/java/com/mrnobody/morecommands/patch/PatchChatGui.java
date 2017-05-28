package com.mrnobody.morecommands.patch;

import java.util.Collection;

import com.google.common.collect.Sets;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.patch.PatchManager.AppliedPatches;
import com.mrnobody.morecommands.settings.ClientPlayerSettings;
import com.mrnobody.morecommands.settings.MoreCommandsConfig;
import com.mrnobody.morecommands.settings.PlayerSettings;
import com.mrnobody.morecommands.util.ObfuscatedNames.ObfuscatedField;
import com.mrnobody.morecommands.util.ReflectionHelper;
import com.mrnobody.morecommands.util.Variables;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * This patch contains two subclasses of {@link net.minecraft.client.gui.GuiChat}
 * and {@link net.minecraft.client.gui.GuiSleepMP} because these classes
 * are where chat messages are sent to the server. These subclasses do some
 * pre-processing before a message is sent to the server, e.g. replacing variables.
 * 
 * @author MrNobody98
 */
public class PatchChatGui implements PatchManager.ForgeEventBasedPatch {
	private String displayName;
	
	PatchChatGui(String displayName) {
		this.displayName = displayName;
	}
	
	@Override
	public String getDisplayName() {
		return this.displayName;
	}

	@Override
	public String getFailureConsequences() {
		return "Issues with variable replacement on for non-command chat messages";
	}
	
	public Collection<Class<? extends Event>> forgeEventClasses() {
		return Sets.<Class<? extends Event>>newHashSet(GuiOpenEvent.class);
	}
	
	@Override
	public <T extends Event> boolean needsToBeApplied(T e) {
		return ((GuiOpenEvent) e).gui instanceof net.minecraft.client.gui.GuiChat;
	}
	
	@Override
	public <T extends Event> boolean printLogFor(T event) {
		return true;
	}
	
	@Override
	public <T extends Event> boolean applyForgeEventPatch(T e) {
		GuiOpenEvent event = (GuiOpenEvent) e;
		String prefilledText = ReflectionHelper.get(ObfuscatedField.GuiChat_defaultInputFieldText, (net.minecraft.client.gui.GuiChat) event.gui);
		
		if (event.gui instanceof net.minecraft.client.gui.GuiSleepMP) event.gui = new GuiSleepMP();
		else event.gui = prefilledText == null ? new GuiChat() : new GuiChat(prefilledText);
		
		PatchManager.instance().getGlobalAppliedPatches().setPatchSuccessfullyApplied(this.displayName, true);
		return true;
	}
	
	/**
	 * @author MrNobody98
	 * @see PatchChatGui
	 */
	public static final class GuiChat extends net.minecraft.client.gui.GuiChat {
		GuiChat() {}
		
		GuiChat(String prefilledText) {
			super(prefilledText);
		}
		
		@Override
		public void sendChatMessage(String msg, boolean addToChat) {
			processChat(this.mc, msg, addToChat);
		}
	}
	
	/**
	 * @author MrNobody98
	 * @see PatchChatGui
	 */
	public static final class GuiSleepMP extends net.minecraft.client.gui.GuiSleepMP {
		GuiSleepMP() {}
		
		@Override
		public void sendChatMessage(String msg, boolean addToChat) {
			processChat(this.mc, msg, addToChat);
		}
	}
	
	/**
	 * Replaces client side variables and then sends the message to the server
	 * (if it is not a client side command)
	 */
	private static void processChat(Minecraft mc, String chat, boolean addToChat) {
		if (addToChat) mc.ingameGUI.getChatGUI().addToSentMessages(chat);
		
		if (MoreCommandsConfig.enablePlayerVars) {
			ClientPlayerSettings settings = MoreCommands.getEntityProperties(ClientPlayerSettings.class, PlayerSettings.MORECOMMANDS_IDENTIFIER, mc.thePlayer);
			
			if (settings != null) {
				boolean replaceIgnored;
				AppliedPatches patches = PatchManager.instance().getGlobalAppliedPatches();
				
				if (chat.length() > 1 && chat.charAt(0) == '%') {
					int end = chat.indexOf('%', 1);
					String val = end > 0 ? settings.variables.get(chat.substring(1, end)) : null;
					
					replaceIgnored = val != null && val.startsWith("/") &&
									(chat.length() - 1 == end || chat.charAt(end + 1) == ' ') &&
									ClientCommandHandler.instance.getCommands().containsKey(val.substring(1)) 
							? false : !patches.wasPatchSuccessfullyApplied(PatchList.SERVER_MODDED);
				}
				else replaceIgnored = chat.startsWith("/") && ClientCommandHandler.instance.getCommands().containsKey(chat.substring(1).split(" ")[0]) ?
										false : !patches.wasPatchSuccessfullyApplied(PatchList.SERVER_MODDED);
				
				try {chat = Variables.replaceVars(chat, replaceIgnored, settings.variables);}
				catch (Variables.VariablesCouldNotBeResolvedException e) {chat = e.getNewString();}
			}
		}
		
		if (chat.trim().startsWith("/") && ClientCommandHandler.instance.executeCommand(mc.thePlayer, chat) != 0) return;
		else mc.thePlayer.sendChatMessage(chat);
	}
}
