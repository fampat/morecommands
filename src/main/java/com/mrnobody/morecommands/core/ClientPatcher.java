package com.mrnobody.morecommands.core;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.google.common.collect.Maps;
import com.mrnobody.morecommands.network.PacketHandlerClient;
import com.mrnobody.morecommands.patch.ChatGuis;
import com.mrnobody.morecommands.patch.ClientCommandManager;
import com.mrnobody.morecommands.patch.ServerConfigurationManagerIntegrated;
import com.mrnobody.morecommands.settings.ClientPlayerSettings;
import com.mrnobody.morecommands.settings.MoreCommandsConfig;
import com.mrnobody.morecommands.settings.PlayerSettings;
import com.mrnobody.morecommands.settings.SettingsProperty;
import com.mrnobody.morecommands.util.ObfuscatedNames.ObfuscatedField;
import com.mrnobody.morecommands.util.ReflectionHelper;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLStateEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSleepMP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;

/**
 * The Patcher used by the Client proxy
 * 
 * @author MrNobody98
 *
 */
public class ClientPatcher extends CommonPatcher {
	private boolean clientNetHandlerPatchApplied = false;
	private long ticksExisted = 0;
	
	@Override
	public void applyModStatePatch(FMLStateEvent stateEvent) {
		super.applyModStatePatch(stateEvent);
		
		if (stateEvent instanceof FMLPostInitializationEvent) {
			this.applyPostInitPatches();
		}
	}
	
	/**
	 * Applies patches after initialization was done, which is currently only the patch to:
	 * {@link net.minecraftforge.client.ClientCommandHandler}
	 */
	private void applyPostInitPatches() {
		Field instance = ReflectionHelper.getField(ObfuscatedField.ClientCommandHandler_instance);
		Field modifiers = ReflectionHelper.getField(ObfuscatedField.Field_modifiers);
		
		try {
			modifiers.setInt(instance, instance.getModifiers() & ~Modifier.FINAL);
			instance.set(null, new ClientCommandManager((ClientCommandHandler) instance.get(null)));
			
			this.mod.getLogger().info("Client Command Manager Patches applied");
			AppliedPatches.setClientCommandManagerPatched(true);
		}
		catch (Exception ex)  {ex.printStackTrace();}
	}
	
	@Override
	protected boolean applyServerConfigManagerPatch(MinecraftServer server) {
		if (server instanceof IntegratedServer) {
			server.func_152361_a(new ServerConfigurationManagerIntegrated((IntegratedServer) server));
			return true;
		}
		 return false;
	}
	
	/**
	 * Invoked every client tick to pass the right time to apply the following patch: 
	 * {@link net.minecraft.client.network.NetHandlerPlayClient}<br>
	 * Also executes {@link PacketHandlerClient#removeOldPendingRemoteCommands()} every 10 ticks
	 */
	@SubscribeEvent
	public void tick(ClientTickEvent event) {
		if (this.ticksExisted % 10 == 0) PacketHandlerClient.removeOldPendingRemoteCommands();
		
		if (!this.clientNetHandlerPatchApplied && FMLClientHandler.instance().getClientPlayHandler() != null && !(FMLClientHandler.instance().getClientPlayHandler() instanceof com.mrnobody.morecommands.patch.NetHandlerPlayClient)) {
			NetHandlerPlayClient clientPlayHandler = (NetHandlerPlayClient) FMLClientHandler.instance().getClientPlayHandler();
			
			Field guiScreenField = ReflectionHelper.getField(ObfuscatedField.NetHandlerPlayClient_guiScreenServer);
			boolean error = false;
			
			if (guiScreenField != null) {
				GuiScreen guiScreen = null;
						
				try {
					guiScreen = (GuiScreen) guiScreenField.get(clientPlayHandler);
				}
				catch (IllegalAccessException e) {error = true;}
				
				if (!error) {
					NetworkManager manager = clientPlayHandler.getNetworkManager();
					FMLClientHandler.instance().setPlayClient(new com.mrnobody.morecommands.patch.NetHandlerPlayClient(Minecraft.getMinecraft(), guiScreen, manager));
					this.clientNetHandlerPatchApplied = true;
					this.mod.getLogger().info("Client Play Handler Patches applied");
				}
			}
		}
		
		this.ticksExisted++;
	}
	
	/**
	 * Updates the {@link ClientPlayerSettings} every time
	 * a {@link EntityClientPlayerMP} joins a world (e.g. on respawns or dimension changes)
	 */
	@SubscribeEvent
	public void updateSettings(EntityJoinWorldEvent event) {
		if (event.world.isRemote && event.entity instanceof EntityClientPlayerMP) {
	    	ClientPlayerSettings settings = MoreCommands.getEntityProperties(ClientPlayerSettings.class, PlayerSettings.MORECOMMANDS_IDENTIFIER, (EntityClientPlayerMP) event.entity);
	    	
	    	if (settings == null) {
	    		settings = ClientPlayerSettings.getInstance((EntityClientPlayerMP) event.entity);
	    		event.entity.registerExtendedProperties(PlayerSettings.MORECOMMANDS_IDENTIFIER, settings);
	    	}
			
			settings.updateSettingsProperties(SettingsProperty.getPropertyMap((EntityClientPlayerMP) event.entity));
		}
	}
	
	/**
	 * Updates the {@link ClientPlayerSettings} of {@link Minecraft#thePlayer}
	 * every time a client world is unloaded
	 */
	@SubscribeEvent
	public void updateSettings(WorldEvent.Unload event) {
		if (event.world == Minecraft.getMinecraft().theWorld) {
	    	ClientPlayerSettings settings = MoreCommands.getEntityProperties(ClientPlayerSettings.class, PlayerSettings.MORECOMMANDS_IDENTIFIER, Minecraft.getMinecraft().thePlayer);
	    	
	    	if (settings == null) {
	    		settings = ClientPlayerSettings.getInstance(Minecraft.getMinecraft().thePlayer);
	    		Minecraft.getMinecraft().thePlayer.registerExtendedProperties(PlayerSettings.MORECOMMANDS_IDENTIFIER, settings);
	    	}
			
			settings.resetSettingsProperties(Maps.<SettingsProperty, String>newEnumMap(SettingsProperty.class));
		}
	}
	
	/**
	 * Invoked when a GUI is opened. Used to replace the chat gui with a modified version of it.
	 */
	@SubscribeEvent
	public void openGui(GuiOpenEvent event) {
		if (event.gui instanceof GuiChat) {
			String prefilledText = ReflectionHelper.get(ObfuscatedField.GuiChat_defaultInputFieldText, (GuiChat) event.gui);
			
			if (event.gui instanceof GuiSleepMP) event.gui = new ChatGuis.GuiSleepMP();
			else event.gui = prefilledText == null ? new ChatGuis.GuiChat() : new ChatGuis.GuiChat(prefilledText);
		}
	}
	
	/**
	 * Invoked when the player joins a server. Starts the startup commands execution thread.
	 */
	@SubscribeEvent
	public void serverConnect(ClientConnectedToServerEvent event) {
		PacketHandlerClient.runStartupCommandsThread();
	}
	
	/**
	 * Does cleanup stuff on disconnect from a server
	 */
	@SubscribeEvent
	public void serverDisconnect(ClientDisconnectionFromServerEvent event) {
		AppliedPatches.setServerModded(false);
		AppliedPatches.setHandshakeFinished(false);
		PacketHandlerClient.reregisterAndClearRemovedCmds();
		
		this.clientNetHandlerPatchApplied = false;
		MoreCommands.getProxy().playerNotified = false;
		
		MoreCommandsConfig.enablePlayerAliases = MoreCommandsConfig.enablePlayerAliasesOriginal;
		MoreCommandsConfig.enablePlayerVars = MoreCommandsConfig.enablePlayerVarsOriginal;
	}
}
