package com.mrnobody.morecommands.core;

import java.lang.reflect.Field;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

import com.mrnobody.morecommands.command.server.CommandAlias;
import com.mrnobody.morecommands.core.Patcher.PlayerPatches;
import com.mrnobody.morecommands.packet.server.S00PacketHandshake;
import com.mrnobody.morecommands.patch.ServerCommandManager;
import com.mrnobody.morecommands.patch.ServerConfigurationManagerDedicated;
import com.mrnobody.morecommands.util.GlobalSettings;
import com.mrnobody.morecommands.util.Reference;
import com.mrnobody.morecommands.util.ReflectionHelper;
import com.mrnobody.morecommands.util.ServerPlayerSettings;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLStateEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;

public class ServerPatcher extends Patcher {
	private MoreCommands mod;
	
	public ServerPatcher() {
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
		Field commandManager = ReflectionHelper.getField(MinecraftServer.class, "commandManager");
		
		if (commandManager != null) {
			try {
				commandManager.set(MinecraftServer.getServer(), new ServerCommandManager());
				this.mod.getLogger().info("Command Manager Patches applied");
				Patcher.setServerCommandManagerPatched(true);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		if (event.getServer() instanceof DedicatedServer) {
			event.getServer().func_152361_a(new ServerConfigurationManagerDedicated((DedicatedServer) event.getServer()));
			this.mod.getLogger().info("Server Configuration Manager Patches applied");
			Patcher.setServerConfigManagerPatched(true);
		}
	}
	
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
				this.mod.getLogger().info("Server Play Handler Patches applied for Player " + player.getCommandSenderName());
				patches.setServerPlayHandlerPatched(true);
			}
			
			Patcher.playerPatchMapping.put(player, patches);
		}
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
	 * Called on a player login. Sends a request for a handshake to the client,
	 * loads the players settings and displays the welcome message if enabled.
	 * Also loads aliases set by this player.
	 */
	@SubscribeEvent
	public void playerLogin(PlayerLoggedInEvent event) {
		if (!(event.player instanceof EntityPlayerMP)) return;
		EntityPlayerMP player = (EntityPlayerMP) event.player;
		
		this.mod.getLogger().info("Requesting Client Handshake");
		if (!Patcher.playerPatchMapping.containsKey(player)) 
			Patcher.playerPatchMapping.put(player, new PlayerPatches());
		ServerPlayerSettings.playerUUIDMapping.put(event.player.getUniqueID(), player);
		S00PacketHandshake packet = new S00PacketHandshake();
		packet.playerUUID = player.getUniqueID();
		this.mod.getNetwork().sendTo(packet, player);
		
		if (GlobalSettings.welcome_message)
			event.player.addChatMessage((new ChatComponentText("More Commands Mod (v" + Reference.VERSION + ") loaded")).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.DARK_AQUA)));
		ServerPlayerSettings.playerSettingsMapping.put(player, ServerPlayerSettings.getPlayerSettings(player));
		CommandAlias.registerAliases(player);
	}
}
