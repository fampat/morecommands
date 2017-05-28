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
import net.minecraft.event.ClickEvent;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.fml.common.FMLCommonHandler;
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
		FMLCommonHandler.instance().bus().register(this);
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
		if (!(event.entityPlayer instanceof EntityPlayerMP) || !(event.original instanceof EntityPlayerMP)) return;
		ServerPlayerSettings settings = MoreCommands.getEntityProperties(ServerPlayerSettings.class, PlayerSettings.MORECOMMANDS_IDENTIFIER, event.original);
		ServerPlayerSettings settings2 = MoreCommands.getEntityProperties(ServerPlayerSettings.class, PlayerSettings.MORECOMMANDS_IDENTIFIER, event.entityPlayer);
		
		AppliedPatches pp1 = MoreCommands.getEntityProperties(AppliedPatches.class, AppliedPatches.PATCHES_IDENTIFIER, event.original);
		AppliedPatches pp2 = MoreCommands.getEntityProperties(AppliedPatches.class, AppliedPatches.PATCHES_IDENTIFIER, event.entityPlayer);
		
		if (settings2 == null)
			event.entityPlayer.registerExtendedProperties(PlayerSettings.MORECOMMANDS_IDENTIFIER, settings2 = new ServerPlayerSettings((EntityPlayerMP) event.entityPlayer, settings));
		
		if (pp2 == null)
			event.entityPlayer.registerExtendedProperties(AppliedPatches.PATCHES_IDENTIFIER, pp2 = new AppliedPatches());
		
		if (settings != null)
			for (ChatChannel channel : settings.chatChannels)
				channel.replaceRespawnedPlayer((EntityPlayerMP) event.original, (EntityPlayerMP) event.entityPlayer);
		
		if (pp1 != pp2)
			pp2.copyFrom(pp1);
		
		MoreCommands.INSTANCE.getPacketDispatcher().sendS14RemoteWorld((EntityPlayerMP) event.entityPlayer, event.entityPlayer.worldObj.getSaveHandler().getWorldDirectoryName());
	}
	
	/**
	 * Invoked when a client connects to the server. Loads player settings
	 * and sends a handshake to the client.
	 */
	@SubscribeEvent
	public void clientConnect(ServerConnectionFromClientEvent event) {
		EntityPlayerMP player = ((NetHandlerPlayServer) event.handler).playerEntity;
		
		if (MoreCommands.getEntityProperties(ServerPlayerSettings.class, PlayerSettings.MORECOMMANDS_IDENTIFIER, player) == null) {
			ServerPlayerSettings settings = new ServerPlayerSettings(player);
			settings.init(player, player.worldObj);
			player.registerExtendedProperties(PlayerSettings.MORECOMMANDS_IDENTIFIER, settings);
		}
		
		if (MoreCommands.getEntityProperties(AppliedPatches.class, AppliedPatches.PATCHES_IDENTIFIER, player) == null)
			player.registerExtendedProperties(AppliedPatches.PATCHES_IDENTIFIER, new AppliedPatches());
		
		//Packets are not intended to be sent at this point but is required here
		//To prevent a NPE in OutboundTarget.selectNetworks(), we have to set the NetHandlerPlayServer
		player.playerNetServerHandler = (NetHandlerPlayServer) event.handler;
		
		MoreCommands.INSTANCE.getLogger().info("Requesting Client Handshake for Player '" + player.getName() + "'");
		MoreCommands.INSTANCE.getPacketDispatcher().sendS00Handshake(player);
		MoreCommands.INSTANCE.getPacketDispatcher().sendS14RemoteWorld(player, player.worldObj.getSaveHandler().getWorldDirectoryName());
		
		//To prevent a NPE because Minecraft.thePlayer is not set at this point, reset connection to null
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
		
		ServerPlayerSettings settings = MoreCommands.getEntityProperties(ServerPlayerSettings.class, PlayerSettings.MORECOMMANDS_IDENTIFIER, player);
		if (settings == null) player.registerExtendedProperties(PlayerSettings.MORECOMMANDS_IDENTIFIER, settings = new ServerPlayerSettings(player));
		
		if (MoreCommands.getEntityProperties(AppliedPatches.class, AppliedPatches.PATCHES_IDENTIFIER, player) == null) 
			player.registerExtendedProperties(AppliedPatches.PATCHES_IDENTIFIER, new AppliedPatches());
		
		settings.updateSettingsProperties(SettingsProperty.getPropertyMap(player));
		
		if (MoreCommandsConfig.welcome_message) {
			IChatComponent icc1 = (new ChatComponentText("MoreCommands (v" + Reference.VERSION + ") loaded")).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.DARK_AQUA));
			IChatComponent icc2 = (new ChatComponentText(Reference.WEBSITE)).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.YELLOW).setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Reference.WEBSITE)));
			IChatComponent icc3 = (new ChatComponentText(" - ")).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.DARK_GRAY));
			
			event.player.addChatMessage(icc1.appendSibling(icc3).appendSibling(icc2));
		}	
	}
	
	/**
	 * Invoked when a player logs out. Used to update and save the player's settings
	 */
	@SubscribeEvent
	public void playerLogout(PlayerLoggedOutEvent event) {
		if (!(event.player instanceof EntityPlayerMP)) return;
		ServerPlayerSettings settings = MoreCommands.getEntityProperties(ServerPlayerSettings.class, PlayerSettings.MORECOMMANDS_IDENTIFIER, (EntityPlayerMP) event.player);
		
		if (settings!= null) {
			settings.captureChannelsAndLeaveForLogout();
			settings.resetSettingsProperties(Maps.<SettingsProperty, String>newEnumMap(SettingsProperty.class));
			settings.getManager().saveSettings();
		}
	}
}
