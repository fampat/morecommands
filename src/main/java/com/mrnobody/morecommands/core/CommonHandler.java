package com.mrnobody.morecommands.core;

import com.google.common.collect.Maps;
import com.mrnobody.morecommands.network.PacketHandlerServer;
import com.mrnobody.morecommands.patch.PatchManager;
import com.mrnobody.morecommands.patch.PatchManager.AppliedPatches;
import com.mrnobody.morecommands.settings.MoreCommandsConfig;
import com.mrnobody.morecommands.settings.PlayerSettings;
import com.mrnobody.morecommands.settings.ServerPlayerSettings;
import com.mrnobody.morecommands.settings.SettingsProperty;
import com.mrnobody.morecommands.util.ChatChannel;
import com.mrnobody.morecommands.util.Reference;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerConnectionFromClientEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerDisconnectionFromClientEvent;

/**
 * This class handles everything associated to
 * certain {@link Event}s for both, client and server side.
 * 
 * @author MrNobody98
 */
public class CommonHandler {
	public CommonHandler() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public void onStateEvent(FMLStateEvent event) {
		PatchManager.instance().fireStateEvent(event);
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
		
		AppliedPatches pp1 = event.getOriginal().getCapability(AppliedPatches.PATCHES_CAPABILITY, null);
		AppliedPatches pp2 = event.getEntityPlayer().getCapability(AppliedPatches.PATCHES_CAPABILITY, null);

		if (settings2 != null) //Should never be null
			settings2.init((EntityPlayerMP) event.getEntityPlayer(), settings);
		
		if (settings != null)
			for (ChatChannel channel : settings.chatChannels)
				channel.replaceRespawnedPlayer((EntityPlayerMP) event.getOriginal(), (EntityPlayerMP) event.getEntityPlayer());
		
		if (pp1 != pp2)
			pp2.copyFrom(pp1);
		
		MoreCommands.INSTANCE.getPacketDispatcher().sendS14RemoteWorld((EntityPlayerMP) event.getEntityPlayer(), event.getEntityPlayer().world.getSaveHandler().getWorldDirectory().getName());
	}
	
	/**
	 * Attaches capabilities to an entity
	 */
	@SubscribeEvent
	public void attachCapabilities(AttachCapabilitiesEvent event) {
		if (event.getObject() instanceof EntityPlayerMP) {
			event.addCapability(AppliedPatches.PATCHES_IDENTIFIER, AppliedPatches.PATCHES_CAPABILITY.getDefaultInstance());
			event.addCapability(PlayerSettings.SETTINGS_IDENTIFIER, PlayerSettings.SETTINGS_CAP_SERVER.getDefaultInstance());
		}
	}
	
	/**
	 * Invoked when a client connects to the server. Loads player settings
	 * and sends a handshake to the client.
	 */
	@SubscribeEvent
	public void clientConnect(ServerConnectionFromClientEvent event) {
		EntityPlayerMP player = ((NetHandlerPlayServer) event.getHandler()).player;
		ServerPlayerSettings settings = player.getCapability(PlayerSettings.SETTINGS_CAP_SERVER, null);
		
		if (settings != null)  //Should never be null
			settings.init(player, new ServerPlayerSettings(player));
		
		//Packets are not intended to be sent at this point but is required here
		//To prevent a NPE in OutboundTarget.selectNetworks(), we have to set the NetHandlerPlayServer
		player.connection = (NetHandlerPlayServer) event.getHandler();
		
		MoreCommands.INSTANCE.getLogger().info("Requesting Client Handshake for Player '" + player.getName() + "'");
		MoreCommands.INSTANCE.getPacketDispatcher().sendS00Handshake(player);
		MoreCommands.INSTANCE.getPacketDispatcher().sendS14RemoteWorld(player, player.world.getSaveHandler().getWorldDirectory().getName());
		
		//To prevent a NPE because Minecraft.thePlayer is not set at this point, reset connection to null
		player.connection = null;
		
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
			ITextComponent icc1 = (new TextComponentString("MoreCommands (v" + Reference.VERSION + ") loaded")).setStyle((new Style()).setColor(TextFormatting.DARK_AQUA));
			ITextComponent icc2 = (new TextComponentString(Reference.WEBSITE)).setStyle((new Style()).setColor(TextFormatting.YELLOW).setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Reference.WEBSITE)));
			ITextComponent icc3 = (new TextComponentString(" - ")).setStyle((new Style()).setColor(TextFormatting.DARK_GRAY));
			
			event.player.sendMessage(icc1.appendSibling(icc3).appendSibling(icc2));
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
