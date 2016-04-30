package com.mrnobody.morecommands.core;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.network.PacketHandlerClient;
import com.mrnobody.morecommands.patch.ClientCommandManager;
import com.mrnobody.morecommands.patch.IntegratedPlayerList;
import com.mrnobody.morecommands.patch.RenderGlobal;
import com.mrnobody.morecommands.util.ClientPlayerSettings;
import com.mrnobody.morecommands.util.GlobalSettings;
import com.mrnobody.morecommands.util.ObfuscatedNames.ObfuscatedField;
import com.mrnobody.morecommands.util.PlayerSettings;
import com.mrnobody.morecommands.util.ReflectionHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;

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
		
		if (stateEvent instanceof FMLInitializationEvent) {
			this.applyInitPatches();
		}
		else if (stateEvent instanceof FMLPostInitializationEvent) {
			this.applyPostInitPatches();
		}
	}
	
	/**
	 * Applies patches during initialization, which is currently only the patch to:
	 * {@link net.minecraft.client.renderer.RenderGlobal}
	 */
	private void applyInitPatches() {
		try {
			SimpleReloadableResourceManager resourceManager = (SimpleReloadableResourceManager) Minecraft.getMinecraft().getResourceManager();
			Field reloadListenerList = ReflectionHelper.getField(ObfuscatedField.SimpleReloadableResourceManager_reloadListeners);
			
			List reloadListeners = (List) reloadListenerList.get(resourceManager);
			reloadListeners.remove(Minecraft.getMinecraft().renderGlobal);
			
			Minecraft.getMinecraft().renderGlobal = new RenderGlobal(Minecraft.getMinecraft());
			resourceManager.registerReloadListener(Minecraft.getMinecraft().renderGlobal);
			
			this.mod.getLogger().info("RenderGlobal Patches applied");
		}
		catch (Exception ex) {
			this.mod.getLogger().info("Error applying RenderGlobal Patches");
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
			server.setPlayerList(new IntegratedPlayerList((IntegratedServer) server));
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
					FMLClientHandler.instance().setPlayClient(new com.mrnobody.morecommands.patch.NetHandlerPlayClient(Minecraft.getMinecraft(), guiScreen, manager, clientPlayHandler.getGameProfile()));
					this.clientNetHandlerPatchApplied = true;
					this.mod.getLogger().info("Client Play Handler Patches applied");
				}
			}
		}
	}
	
	/**
	 * Attaches capabilities to an entity
	 */
	@Override
	@SubscribeEvent
	public void attachCapabilities(AttachCapabilitiesEvent.Entity event) {
		if (event.getEntity() instanceof EntityPlayerSP) {
			event.addCapability(PlayerSettings.SETTINGS_IDENTIFIER, ClientPlayerSettings.getInstance((EntityPlayerSP) event.getEntity()));
		}
		
		super.attachCapabilities(event);
	}
	
	/**
	 * Updates the {@link ClientPlayerSettings} every time
	 * a {@link EntityClientPlayerMP} joins a world (e.g. on respawns or dimension changes)
	 */
	@SubscribeEvent
	public void updateSettings(EntityJoinWorldEvent event) {
		if (event.getWorld().isRemote && event.getEntity() instanceof EntityPlayerSP) {
	    	ClientPlayerSettings settings = event.getEntity().getCapability(PlayerSettings.SETTINGS_CAP_CLIENT, null);
			
	    	if (settings != null) settings.updateSettings(MoreCommands.getProxy().getCurrentServerNetAddress(), 
			MoreCommands.getProxy().getCurrentWorld(), event.getWorld().provider.getDimensionType().getName());
		}
	}
	
	/**
	 * Updates the {@link ClientPlayerSettings} of {@link Minecraft#thePlayer}
	 * every time a client world is unloaded
	 */
	@SubscribeEvent
	public void updateSettings(WorldEvent.Unload event) {
		if (event.getWorld() == Minecraft.getMinecraft().theWorld) {
	    	ClientPlayerSettings settings = Minecraft.getMinecraft().thePlayer.getCapability(PlayerSettings.SETTINGS_CAP_CLIENT, null);
			if (settings != null) settings.updateSettings(null, null, null);
		}
	}
	
	/**
	 * Invoked when the player joins a server. Starts the startup commands execution thread if
	 * the server is not the integrated server.
	 */
	@SubscribeEvent
	public void playerConnect(ClientConnectedToServerEvent event) {
		if (MoreCommands.getServerType() != ServerType.INTEGRATED)
			PacketHandlerClient.runStartupThread(event.getManager().getRemoteAddress().toString());
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
