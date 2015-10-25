package com.mrnobody.morecommands.core;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.mrnobody.morecommands.command.CommandBase.ServerType;
import com.mrnobody.morecommands.command.client.CommandAlias;
import com.mrnobody.morecommands.network.PacketHandlerClient;
import com.mrnobody.morecommands.patch.ClientCommandManager;
import com.mrnobody.morecommands.patch.ServerConfigurationManagerIntegrated;
import com.mrnobody.morecommands.util.ClientPlayerSettings;
import com.mrnobody.morecommands.util.ReflectionHelper;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLStateEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraftforge.client.ClientCommandHandler;

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
		Field instance = ReflectionHelper.getField(ClientCommandHandler.class, "instance");
		Field modifiers = ReflectionHelper.getField(Field.class, "modifiers");
		
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
			
			Field guiScreenField = ReflectionHelper.getField(clientPlayHandler.getClass(), "guiScreenServer");
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
	 * Reads the client player settings when the player connects to a server
	 * and registers client aliases
	 */
	@SubscribeEvent
	public void playerConnect(ClientConnectedToServerEvent event) {
		if (this.mod.getRunningServer() != ServerType.INTEGRATED) 
			PacketHandlerClient.runStartupThread(event.manager.getSocketAddress().toString());
		ClientPlayerSettings.readSettings(event.manager.getSocketAddress().toString());
		CommandAlias.registerAliases();
	}
	
	/**
	 * Does cleanup stuff on disconnect from a server
	 */
	@SubscribeEvent
	public void playerDisconnect(ClientDisconnectionFromServerEvent event) {
		this.mod.setPlayerUUID(null);
		AppliedPatches.setServerModded(false);
		AppliedPatches.setHandshakeFinished(false);
		PacketHandlerClient.reregisterAndClearRemovedCmds();
		this.clientNetHandlerPatchApplied = false;
		MoreCommands.getProxy().playerNotified = false;
	}
}
