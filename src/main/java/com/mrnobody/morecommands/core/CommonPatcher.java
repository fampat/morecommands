package com.mrnobody.morecommands.core;

import java.lang.reflect.Field;

import com.mrnobody.morecommands.command.server.CommandAlias;
import com.mrnobody.morecommands.core.AppliedPatches.PlayerPatches;
import com.mrnobody.morecommands.network.PacketHandlerServer;
import com.mrnobody.morecommands.patch.ServerCommandManager;
import com.mrnobody.morecommands.patch.ServerConfigurationManagerDedicated;
import com.mrnobody.morecommands.util.GlobalSettings;
import com.mrnobody.morecommands.util.Reference;
import com.mrnobody.morecommands.util.ReflectionHelper;
import com.mrnobody.morecommands.util.ServerPlayerSettings;

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
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;

/**
 * The common Patcher class
 * 
 * @author MrNobody98
 *
 */
public class CommonPatcher {
	protected MoreCommands mod;
	
	public CommonPatcher() {
		this.mod = MoreCommands.getMoreCommands();
	}
	
	/**
	 * Registers the Patcher to the event buses to receive events determining when patches shall be applied
	 */
	private void loadEventPatches() {
		MinecraftForge.EVENT_BUS.register(this);
		FMLCommonHandler.instance().bus().register(this);
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
		Field commandManager = ReflectionHelper.getField(MinecraftServer.class, "commandManager");
		
		if (commandManager != null) {
			try {
				commandManager.set(MinecraftServer.getServer(), new ServerCommandManager(MinecraftServer.getServer().getCommandManager()));
				this.mod.getLogger().info("Command Manager Patches applied");
				AppliedPatches.setServerCommandManagerPatched(true);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		if (this.applyServerConfigManagerPatch(event.getServer())) {
			this.mod.getLogger().info("Server Configuration Manager Patches applied");
			AppliedPatches.setServerConfigManagerPatched(true);
		}
	}
	
	protected boolean applyServerConfigManagerPatch(MinecraftServer server){
		if (server instanceof DedicatedServer) {
			//must create new instance via reflection because "new" creates bytecode but the "ServerConfigurationManagerDedicated" class
			//is not available on the client so it will cause a NoClassDefFoundError, reflection creates the new instance dynamically
			try {server.setConfigManager(ServerConfigurationManagerDedicated.class.getConstructor(DedicatedServer.class).newInstance(server));}
			catch (Exception ex) {ex.printStackTrace(); return false;}
			return true;
		}
		return false;
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
			
			if (!AppliedPatches.playerPatchMapping.containsKey(player)) patches = new PlayerPatches();
			else patches = AppliedPatches.playerPatchMapping.get(player);
			
			if (player.playerNetServerHandler.playerEntity == event.entity && !(player.playerNetServerHandler instanceof com.mrnobody.morecommands.patch.NetHandlerPlayServer)) {
				NetHandlerPlayServer handler = player.playerNetServerHandler;
				player.playerNetServerHandler = new com.mrnobody.morecommands.patch.NetHandlerPlayServer(MinecraftServer.getServer(), handler.netManager, handler.playerEntity);
				this.mod.getLogger().info("Server Play Handler Patches applied for Player " + player.getName());
				patches.setServerPlayHandlerPatched(true);
			}
			
			AppliedPatches.playerPatchMapping.put(player, patches);
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
		
		if (!AppliedPatches.playerPatchMapping.containsKey(player)) 
			AppliedPatches.playerPatchMapping.put(player, new PlayerPatches());
		
		ServerPlayerSettings.storePlayerSettings(player, ServerPlayerSettings.readPlayerSettings(player));
		CommandAlias.registerAliases(player);
		
		this.mod.getLogger().info("Requesting Client Handshake for Player '" + player.getName() + "'");
		this.mod.getPacketDispatcher().sendS00Handshake(player);
		
		if (GlobalSettings.retryHandshake)
			PacketHandlerServer.addPlayerToRetries(player);
		
		if (GlobalSettings.welcome_message)
			event.player.addChatMessage((new ChatComponentText("More Commands (v" + Reference.VERSION + ") loaded")).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.DARK_AQUA)));
	}
	
	/**
	 * Saves the settings for a player logging out and resets the patches applied for this player
	 */
	@SubscribeEvent
	public void playerLogout(PlayerLoggedOutEvent event) {
		if (event.player instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP) event.player;
			
			if (AppliedPatches.playerPatchMapping.containsKey(player))
				AppliedPatches.playerPatchMapping.remove(player);
			
			if (ServerPlayerSettings.containsSettingsForPlayer(player)) {
				ServerPlayerSettings.getPlayerSettings(player).saveSettings();
				ServerPlayerSettings.removePlayerSettings(player);
			}
		}
	}
}
