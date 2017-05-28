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

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;

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
		if (event.entity instanceof EntityClientPlayerMP)
			ClientPlayerSettings.getInstance((EntityClientPlayerMP) event.entity);   //init ClientPlayerSettings
	}
	
	/**
	 * Updates the {@link ClientPlayerSettings} every time
	 * a {@link EntityPlayerSP} joins a world (e.g. on respawns or dimension changes)
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
