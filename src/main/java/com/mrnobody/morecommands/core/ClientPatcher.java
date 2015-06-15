package com.mrnobody.morecommands.core;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;

import com.mrnobody.morecommands.command.ClientCommand;
import com.mrnobody.morecommands.command.client.CommandAlias;
import com.mrnobody.morecommands.network.PacketHandlerClient;
import com.mrnobody.morecommands.patch.ClientCommandManager;
import com.mrnobody.morecommands.patch.RenderGlobal;
import com.mrnobody.morecommands.patch.ServerCommandManager;
import com.mrnobody.morecommands.patch.ServerConfigurationManagerIntegrated;
import com.mrnobody.morecommands.util.ClientPlayerSettings;
import com.mrnobody.morecommands.util.GlobalSettings;
import com.mrnobody.morecommands.util.Reference;
import com.mrnobody.morecommands.util.ReflectionHelper;
import com.mrnobody.morecommands.util.ServerPlayerSettings;

/**
 * The Patcher used by the Client proxy
 * 
 * @author MrNobody98
 *
 */
public class ClientPatcher extends Patcher {
	private MoreCommands mod;
	
	public ClientPatcher() {
		this.mod = MoreCommands.getMoreCommands();
	}
	
	/**
	 * Registers the Patcher to the event buses to receive events determining when patches shall be applied
	 */
	private void loadEventPatches() {
		MinecraftForge.EVENT_BUS.register(this);
		FMLCommonHandler.instance().bus().register(this);
	}
	
	public void applyModStatePatch(FMLStateEvent stateEvent) {
		if (stateEvent instanceof FMLInitializationEvent) {
			this.applyInitPatches();
			this.loadEventPatches();
		}
		else if (stateEvent instanceof FMLServerAboutToStartEvent) {
			this.applyServerStartPatches((FMLServerAboutToStartEvent) stateEvent);
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
			Field reloadListenerList = ReflectionHelper.getField(SimpleReloadableResourceManager.class, "reloadListeners");
			
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
		Field instance = ReflectionHelper.getField(ClientCommandHandler.class, "instance");
		Field modifiers = ReflectionHelper.getField(Field.class, "modifiers");
		
		try {
			modifiers.setInt(instance, instance.getModifiers() & ~Modifier.FINAL);
			instance.set(null, new ClientCommandManager());
			
			this.mod.getLogger().info("Client Command Manager Patches applied");
			Patcher.setClientCommandManagerPatched(true);
		}
		catch (Exception ex)  {ex.printStackTrace();}
	}

	/**
	 * Applies patches before the server starts, which are patches for: <br>
	 * {@link net.minecraft.command.ServerCommandManager} and {@link ServerConfigurationManager}
	 */
	private void applyServerStartPatches(FMLServerAboutToStartEvent event) {
		Field commandManager = ReflectionHelper.getField(MinecraftServer.class, "commandManager");
		
		if (commandManager != null) {
			try {
				commandManager.set(MinecraftServer.getServer(), new ServerCommandManager());
				this.mod.getLogger().info("Server Command Manager Patches applied");
				Patcher.setServerCommandManagerPatched(true);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		if (event.getServer() instanceof IntegratedServer) {
			event.getServer().setConfigManager(new ServerConfigurationManagerIntegrated((IntegratedServer) event.getServer()));
			this.mod.getLogger().info("Server Configuration Manager Patches applied");
			Patcher.setServerConfigManagerPatched(true);
		}
	}
	
	private boolean clientNetHandlerPatchApplied = false;
	
	/**
	 * Applies patches for a player joining a world, which is currently only the patch for: <br>
	 * {@link NetHandlerPlayServer}
	 */
	@SubscribeEvent
	public void onJoin(EntityJoinWorldEvent event) {
		if (event.entity instanceof EntityPlayerMP) {
			PlayerPatches patches;
			EntityPlayerMP player = (EntityPlayerMP) event.entity;
			
			if (!Patcher.playerPatchMapping.containsKey(player)) patches = new PlayerPatches();
			else patches = Patcher.playerPatchMapping.get(player);
			
			if (player.playerNetServerHandler.playerEntity == event.entity) {
				NetHandlerPlayServer handler = player.playerNetServerHandler;
				player.playerNetServerHandler = new com.mrnobody.morecommands.patch.NetHandlerPlayServer(MinecraftServer.getServer(), handler.netManager, handler.playerEntity);
				this.mod.getLogger().info("Server Play Handler Patches applied for Player " + player.getName());
				patches.setServerPlayHandlerPatched(true);
			}
			
			Patcher.playerPatchMapping.put(player, patches);
		}
	}
	
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
					FMLClientHandler.instance().setPlayClient(new com.mrnobody.morecommands.patch.NetHandlerPlayClient(Minecraft.getMinecraft(), guiScreen, manager, clientPlayHandler.getGameProfile()));
					this.clientNetHandlerPatchApplied = true;
					this.mod.getLogger().info("Client Play Handler Patches applied");
				}
			}
		}
	}
	
	/**
	 * Called on a player login. Sends a request for a handshake to the client,
	 * loads the players settings and displays the welcome message if enabled.
	 * Also registers server aliases set by this player.
	 */
	@SubscribeEvent
	public void playerLogin(PlayerLoggedInEvent event) {
		if (!(event.player instanceof EntityPlayerMP)) return;
		EntityPlayerMP player = (EntityPlayerMP) event.player;
		
		this.mod.getLogger().info("Requesting Client Handshake");
		if (!Patcher.playerPatchMapping.containsKey(player))
			Patcher.playerPatchMapping.put(player, new PlayerPatches());
		ServerPlayerSettings.playerUUIDMapping.put(event.player.getUniqueID(), player);
		this.mod.getPacketDispatcher().sendS00Handshake(player);
		
		if (GlobalSettings.welcome_message)
			player.addChatMessage((new ChatComponentText("MrNobody Commands Mod (v" + Reference.VERSION + ") loaded")).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.DARK_AQUA)));
		ServerPlayerSettings.playerSettingsMapping.put(player, ServerPlayerSettings.getPlayerSettings(player));
		com.mrnobody.morecommands.command.server.CommandAlias.registerAliases(player);
	}
	
	/**
	 * Saves the settings for a player logging out and resets the patches applied for this player
	 */
	@SubscribeEvent
	public void playerLogout(PlayerLoggedOutEvent event) {
		if (event.player instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP) event.player;
			
			if (Patcher.playerPatchMapping.containsKey(player)) 
				Patcher.playerPatchMapping.remove(player);
		}
		
		if (ServerPlayerSettings.playerSettingsMapping.containsKey(event.player)) {
			ServerPlayerSettings.playerSettingsMapping.get(event.player).saveSettings();
			ServerPlayerSettings.playerSettingsMapping.remove(event.player);
		}
	}
	
	/**
	 * Reads the client player settings when the player connects to a server
	 * and registers client aliases
	 */
	@SubscribeEvent
	public void playerConnect(ClientConnectedToServerEvent event) {
		ClientPlayerSettings.readSettings(event.manager.getRemoteAddress().toString());
		CommandAlias.registerAliases();
	}
	
	/**
	 * Does cleanup stuff on disconnect from a server
	 */
	@SubscribeEvent
	public void playerDisconnect(ClientDisconnectionFromServerEvent event) {
		this.mod.setPlayerUUID(null);
		Patcher.setServerModded(false);
		for (ClientCommand cmd : PacketHandlerClient.removedCmds) ClientCommandHandler.instance.registerCommand(cmd);
		PacketHandlerClient.removedCmds.clear();
		this.clientNetHandlerPatchApplied = false;
	}
}
