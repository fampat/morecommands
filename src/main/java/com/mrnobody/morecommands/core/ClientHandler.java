package com.mrnobody.morecommands.core;

import com.google.common.collect.Maps;
import com.mrnobody.morecommands.network.PacketHandlerClient;
import com.mrnobody.morecommands.patch.PatchList;
import com.mrnobody.morecommands.patch.PatchManager;
import com.mrnobody.morecommands.patch.PatchManager.AppliedPatches;
import com.mrnobody.morecommands.settings.ClientPlayerSettings;
import com.mrnobody.morecommands.settings.MoreCommandsConfig;
import com.mrnobody.morecommands.settings.PlayerSettings;
import com.mrnobody.morecommands.settings.SettingsProperty;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;

/**
 * This class handles everything associated to
 * certain {@link Event}s for the client side.
 * 
 * @author MrNobody98
 */
public class ClientHandler extends CommonHandler {
	private long ticksExisted = 0;
	
	@SubscribeEvent
	public void tick(ClientTickEvent event) {
		if (this.ticksExisted % 10 == 0) 
			PacketHandlerClient.removeOldPendingRemoteCommands();
		
		this.ticksExisted++;
	}
	
	@SubscribeEvent
	public void entityConstruct(EntityConstructing event) {
		if (event.entity instanceof EntityPlayerSP)
			ClientPlayerSettings.getInstance((EntityPlayerSP) event.entity);   //init ClientPlayerSettings
	}
	
	/**
	 * Updates the {@link ClientPlayerSettings} every time
	 * a {@link EntityPlayerSP} joins a world (e.g. on respawns or dimension changes)
	 */
	@SubscribeEvent
	public void updateSettings(EntityJoinWorldEvent event) {
		if (event.world.isRemote && event.entity instanceof EntityPlayerSP) {
	    	ClientPlayerSettings settings = MoreCommands.getEntityProperties(ClientPlayerSettings.class, PlayerSettings.MORECOMMANDS_IDENTIFIER, (EntityPlayerSP) event.entity);
	    	
	    	if (settings == null) {
	    		settings = ClientPlayerSettings.getInstance((EntityPlayerSP) event.entity);
	    		event.entity.registerExtendedProperties(PlayerSettings.MORECOMMANDS_IDENTIFIER, settings);
	    	}
			
			settings.updateSettingsProperties(SettingsProperty.getPropertyMap((EntityPlayerSP) event.entity));
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
	 * Invoked when the player joins a server. Starts the startup commands execution thread if
	 * the server is not the integrated server.
	 */
	@SubscribeEvent
	public void playerConnect(ClientConnectedToServerEvent event) {
		PacketHandlerClient.runStartupCommandsThread();
	}
	
	/**
	 * Does cleanup stuff on disconnect from a server
	 */
	@SubscribeEvent
	public void playerDisconnect(ClientDisconnectionFromServerEvent event) {
		AppliedPatches patches = PatchManager.instance().getGlobalAppliedPatches();
		
		patches.setPatchSuccessfullyApplied(PatchList.SERVER_MODDED, false);
		patches.setPatchSuccessfullyApplied(PatchList.HANDSHAKE_FINISHED, false);
		PacketHandlerClient.reregisterAndClearRemovedCmds();
		
		MoreCommands.getProxy().playerNotified = false;
		
		MoreCommandsConfig.enablePlayerAliases = MoreCommandsConfig.enablePlayerAliasesOriginal;
		MoreCommandsConfig.enablePlayerVars = MoreCommandsConfig.enablePlayerVarsOriginal;
	}
}
