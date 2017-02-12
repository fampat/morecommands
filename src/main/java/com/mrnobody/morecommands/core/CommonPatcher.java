package com.mrnobody.morecommands.core;

import java.lang.reflect.Field;

import com.google.common.collect.Maps;
import com.mrnobody.morecommands.core.AppliedPatches.PlayerPatches;
import com.mrnobody.morecommands.network.PacketHandlerServer;
import com.mrnobody.morecommands.patch.DedicatedPlayerList;
import com.mrnobody.morecommands.patch.ServerCommandManager;
import com.mrnobody.morecommands.settings.MoreCommandsConfig;
import com.mrnobody.morecommands.settings.PlayerSettings;
import com.mrnobody.morecommands.settings.ServerPlayerSettings;
import com.mrnobody.morecommands.settings.SettingsProperty;
import com.mrnobody.morecommands.util.ObfuscatedNames.ObfuscatedField;
import com.mrnobody.morecommands.util.Reference;
import com.mrnobody.morecommands.util.ReflectionHelper;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerConnectionFromClientEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerDisconnectionFromClientEvent;

/**
 * The common Patcher class
 * 
 * @author MrNobody98
 *
 */
public class CommonPatcher {
	protected MoreCommands mod;
	
	public CommonPatcher() {
		this.mod = MoreCommands.INSTANCE;
	}
	
	/**
	 * Registers the Patcher to the event buses to receive events determining when patches shall be applied
	 */
	private void loadEventPatches() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	/**
	 * Applies the patches corresponding to the current {@link FMLStateEvent}
	 */
	public void applyModStatePatch(FMLStateEvent stateEvent) {
		if (stateEvent instanceof FMLInitializationEvent) {
			this.loadEventPatches();
		}
		else if (stateEvent instanceof FMLServerAboutToStartEvent) {
			this.applyServerStartPatches((FMLServerAboutToStartEvent) stateEvent);
		}
	}
	
	/**
	 * Applies patches before the server starts, which are patches for: <br>
	 * {@link net.minecraft.command.ServerCommandManager} and {@link ServerConfigurationManager}
	 */
	private void applyServerStartPatches(FMLServerAboutToStartEvent event) {
		Field commandManager = ReflectionHelper.getField(ObfuscatedField.MinecraftServer_commandManager);
		
		if (commandManager != null) {
			try {
				commandManager.set(event.getServer(), new ServerCommandManager(event.getServer(), event.getServer().getCommandManager()));
				this.mod.getLogger().info("Command Manager Patches applied");
				AppliedPatches.setServerCommandManagerPatched(true);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		if (this.applyPlayerListPatch(event.getServer())) {
			this.mod.getLogger().info("Server Configuration Manager Patches applied");
			AppliedPatches.setServerConfigManagerPatched(true);
		}
	}
	
	/**
	 * Applies the patch to the {@link MinecraftServer}s {@link ServerConfigurationManager}
	 * 
	 * @param server the minecraft server
	 * @return whether the patch was applied successfully
	 */
	protected boolean applyPlayerListPatch(MinecraftServer server){
		if (server instanceof DedicatedServer) {
			//must create new instance via reflection because "new" creates bytecode but the "DedicatedPlayerList" class
			//is not available on the client so it will cause a NoClassDefFoundError, reflection creates the new instance dynamically
			try {server.setPlayerList(DedicatedPlayerList.class.getConstructor(DedicatedServer.class).newInstance(server));}
			catch (Exception ex) {ex.printStackTrace(); return false;}
			return true;
		}
		return false;
	}
	
	/**
	 * Invoked when a player is cloned. This is the case when he dies or beats the game and will be respawned
	 * The problem with that is that a new player instance will be created for the player which is bad because all settings
	 * are lost. Receiving this event allows to copy the settings to the new player object
	 */
	@SubscribeEvent
	public void clonePlayer(Clone event) {
		if (!(event.getEntityPlayer() instanceof EntityPlayerMP) || !(event.getOriginal() instanceof EntityPlayerMP)) return;
		ServerPlayerSettings settings = event.getOriginal().getCapability(PlayerSettings.SETTINGS_CAP_SERVER, null);
		ServerPlayerSettings settings2 = event.getEntityPlayer().getCapability(PlayerSettings.SETTINGS_CAP_SERVER, null);
		
		PlayerPatches pp1 = event.getOriginal().getCapability(PlayerPatches.PATCHES_CAPABILITY, null);
		PlayerPatches pp2 = event.getEntityPlayer().getCapability(PlayerPatches.PATCHES_CAPABILITY, null);

		if (settings2 != null) //Should never be null
			settings2.init((EntityPlayerMP) event.getEntityPlayer(), settings);
		
		if (pp1 != pp2) {
			pp2.setClientModded(pp1.clientModded());
			pp2.setClientPlayerPatched(pp1.clientPlayerPatched());
			pp2.setServerPlayHandlerPatched(pp1.serverPlayHandlerPatched());
		}
		
		this.mod.getPacketDispatcher().sendS14RemoteWorld((EntityPlayerMP) event.getEntityPlayer(), event.getEntityPlayer().worldObj.getSaveHandler().getWorldDirectory().getName());
	}
	
	/**
	 * Attaches capabilities to an entity
	 */
	@SubscribeEvent
	public void attachCapabilities(AttachCapabilitiesEvent.Entity event) {
		if (event.getEntity() instanceof EntityPlayerMP) {
			event.addCapability(PlayerPatches.PATCHES_IDENTIFIER, PlayerPatches.PATCHES_CAPABILITY.getDefaultInstance());
			event.addCapability(PlayerSettings.SETTINGS_IDENTIFIER, PlayerSettings.SETTINGS_CAP_SERVER.getDefaultInstance());
		}
	}
	
	/**
	 * Applies patches for a player joining a world, which is currently only the patch for: <br>
	 * {@link NetHandlerPlayServer}
	 */
	@SubscribeEvent
	public void onJoin(EntityJoinWorldEvent event) {
		if (event.getEntity() instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP) event.getEntity();
			PlayerPatches patches = player.getCapability(PlayerPatches.PATCHES_CAPABILITY, null);
			
			if (player.playerNetServerHandler.playerEntity == event.getEntity() && !(player.playerNetServerHandler instanceof com.mrnobody.morecommands.patch.NetHandlerPlayServer)) {
				NetHandlerPlayServer handler = player.playerNetServerHandler;
				player.playerNetServerHandler = new com.mrnobody.morecommands.patch.NetHandlerPlayServer(player.getServer(), handler.netManager, handler.playerEntity);
				this.mod.getLogger().info("Server Play Handler Patches applied for Player " + player.getName());
				if (patches != null) patches.setServerPlayHandlerPatched(true);
			}
		}
	}
	
	/**
	 * Invoked when a client connects to the server. Loads player settings
	 * and sends a handshake to the client.
	 */
	@SubscribeEvent
	public void clientConnect(ServerConnectionFromClientEvent event) {
		EntityPlayerMP player = ((NetHandlerPlayServer) event.getHandler()).playerEntity;
		ServerPlayerSettings settings = player.getCapability(PlayerSettings.SETTINGS_CAP_SERVER, null);
		
		if (settings != null)  //Should never be null
			settings.init(player, new ServerPlayerSettings(player));
		
		//Packets are not intended to be sent at this point but is required here
		//To prevent a NPE in OutboundTarget.selectNetworks(), we have to set the NetHandlerPlayServer
		player.playerNetServerHandler = (NetHandlerPlayServer) event.getHandler();
		
		this.mod.getLogger().info("Requesting Client Handshake for Player '" + player.getName() + "'");
		this.mod.getPacketDispatcher().sendS00Handshake(player);
		this.mod.getPacketDispatcher().sendS14RemoteWorld(player, player.worldObj.getSaveHandler().getWorldDirectory().getName());
		
		//To prevent a NPE because Minecraft.thePlayer is not set at this point, reset playerNetServerHandler to null
		player.playerNetServerHandler = null;
		
		if (MoreCommandsConfig.retryHandshake)
			PacketHandlerServer.addPlayerToRetries(player);
		
	}

	/**
	 * Invoked when a client disconnects. Currently does nothing
	 */
	@SubscribeEvent
	public void clientDisconnect(ServerDisconnectionFromClientEvent event) {}
	
	/**
	 * Called on a player login. Loads player settings if this somehow failed in
	 * clientConnect() and displays a welcome message to the player.
	 */
	@SubscribeEvent
	public void playerLogin(PlayerLoggedInEvent event) {
		if (!(event.player instanceof EntityPlayerMP)) return;
		EntityPlayerMP player = (EntityPlayerMP) event.player;
		ServerPlayerSettings settings = player.getCapability(PlayerSettings.SETTINGS_CAP_SERVER, null);
		
		if (settings != null)   //should never be null
			settings.updateSettingsProperties(SettingsProperty.getPropertyMap(player));
		
		if (MoreCommandsConfig.welcome_message) {
			ITextComponent icc1 = (new TextComponentString("MoreCommands (v" + Reference.VERSION + ") loaded")).setChatStyle((new Style()).setColor(TextFormatting.DARK_AQUA));
			ITextComponent icc2 = (new TextComponentString(Reference.WEBSITE)).setChatStyle((new Style()).setColor(TextFormatting.YELLOW).setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Reference.WEBSITE)));
			ITextComponent icc3 = (new TextComponentString(" - ")).setChatStyle((new Style()).setColor(TextFormatting.DARK_GRAY));
			
			event.player.addChatMessage(icc1.appendSibling(icc3).appendSibling(icc2));
		}	
	}
	
	/**
	 * Invoked when a player logs out. Used to update and save the player's settings
	 */
	@SubscribeEvent
	public void playerLogout(PlayerLoggedOutEvent event) {
		if (!(event.player instanceof EntityPlayerMP)) return;
		ServerPlayerSettings settings = event.player.getCapability(PlayerSettings.SETTINGS_CAP_SERVER, null);
		
		if (settings!= null) {
			settings.captureChannelsAndLeaveForLogout();
			settings.resetSettingsProperties(Maps.<SettingsProperty, String>newEnumMap(SettingsProperty.class));
			settings.getManager().saveSettings();
		}
	}
}
