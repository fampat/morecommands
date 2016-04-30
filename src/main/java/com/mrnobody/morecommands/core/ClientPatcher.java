package com.mrnobody.morecommands.core;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.network.PacketHandlerClient;
import com.mrnobody.morecommands.patch.ClientCommandManager;
import com.mrnobody.morecommands.patch.ServerConfigurationManagerIntegrated;
import com.mrnobody.morecommands.util.ClientPlayerSettings;
import com.mrnobody.morecommands.util.GlobalSettings;
import com.mrnobody.morecommands.util.ObfuscatedNames.ObfuscatedField;
import com.mrnobody.morecommands.util.PlayerSettings;
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
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;

/**
 * The Patcher used by the Client proxy
 * 
 * @author MrNobody98
 *
 */
public class ClientPatcher extends CommonPatcher {
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
	
	private boolean clientNetHandlerPatchApplied = false;
	
	/**
	 * Called every client tick to pass the right time to apply the following patch: 
	 * {@link net.minecraft.client.network.NetHandlerPlayClient}
	 */
	@SubscribeEvent
	public void tick(ClientTickEvent event) {
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
			
			settings.updateSettings(MoreCommands.getProxy().getCurrentServerNetAddress(), MoreCommands.getProxy().getCurrentWorld(), event.world.provider.getDimensionName());
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
			
			settings.updateSettings(null, null, null);
		}
	}
	
	/**
	 * Invoked when the player joins a server. Starts the startup commands execution thread if
	 * the server is not the integrated server.
	 */
	@SubscribeEvent
	public void playerConnect(ClientConnectedToServerEvent event) {
		if (MoreCommands.getServerType() != ServerType.INTEGRATED)
			PacketHandlerClient.runStartupThread(event.manager.getSocketAddress().toString());
	}
	
	/**
	 * Does cleanup stuff on disconnect from a server
	 */
	@SubscribeEvent
	public void playerDisconnect(ClientDisconnectionFromServerEvent event) {
		AppliedPatches.setServerModded(false);
		AppliedPatches.setHandshakeFinished(false);
		PacketHandlerClient.reregisterAndClearRemovedCmds();
		this.clientNetHandlerPatchApplied = false;
		MoreCommands.getProxy().playerNotified = false;
		GlobalSettings.enablePlayerAliases = GlobalSettings.enablePlayerAliasesOriginal;
		GlobalSettings.enablePlayerVars = GlobalSettings.enablePlayerVarsOriginal;
	}
}
