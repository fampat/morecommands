package com.mrnobody.morecommands.network;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.command.server.CommandWorld;
import com.mrnobody.morecommands.core.AppliedPatches;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.handler.PacketHandler;
import com.mrnobody.morecommands.util.GlobalSettings;
import com.mrnobody.morecommands.util.KeyEvent;
import com.mrnobody.morecommands.util.ServerPlayerSettings;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

/**
 * This class handles all incoming packets from the clients
 * 
 * @author MrNobody98
 *
 */
public class PacketHandlerServer {
	/**
	 * a class containing information when <br>
	 * and how often to retry a handshake
	 * 
	 * @author MrNobody98
	 */
	private static final class HandshakeRetry {
		private int timeout, remainingTime, retries;
		
		public HandshakeRetry(int timeout, int retries) {
			this.timeout = this.remainingTime = timeout;
			this.retries = retries;
		}
	}
	
	private static Thread retryThread;
	private static final Map<EntityPlayerMP, HandshakeRetry> handshakeRetries = new ConcurrentHashMap<EntityPlayerMP, HandshakeRetry>();
	
	/**
	 * Adds a player to the retry handshake thread
	 */
	public static void addPlayerToRetries(EntityPlayerMP player) {
		handshakeRetries.put(player, new HandshakeRetry(
				GlobalSettings.handshakeTimeout < 0 ? 3 : GlobalSettings.handshakeTimeout > 10 ? 10 : GlobalSettings.handshakeTimeout,
				GlobalSettings.handshakeRetries < 0 ? 3 : GlobalSettings.handshakeRetries > 10 ? 10 : GlobalSettings.handshakeRetries));
	}
	
	/**
	 * starts a thread which will retry the handshake for the players added via {@link #addPlayerToRetries}
	 */
	public static void startHandshakeRetryThread() {
		retryThread = new Thread(new Runnable() {
			@Override
			public void run() {
				long t = System.currentTimeMillis() + 1000;
				List<EntityPlayerMP> removeRetries = new ArrayList<EntityPlayerMP>();
				
				while (!retryThread.isInterrupted()) {
					if (System.currentTimeMillis() > t) {
						for (Map.Entry<EntityPlayerMP, HandshakeRetry> retry : handshakeRetries.entrySet()) {
							if (retry.getValue().retries == 0) {
								removeRetries.add(retry.getKey());
								MoreCommands.getMoreCommands().getLogger().info("Handhsake failed for player '" + retry.getKey().getName() + "'");
							}
							else if (retry.getValue().remainingTime == 0) {
								MoreCommands.getMoreCommands().getLogger().info("Retrying handshake for player '" + retry.getKey().getName() + "'");
								MoreCommands.getMoreCommands().getPacketDispatcher().sendS00Handshake(retry.getKey());
								retry.getValue().retries--; retry.getValue().remainingTime = retry.getValue().timeout;
							}
							else retry.getValue().remainingTime--;
						}
						
						for (EntityPlayerMP player : removeRetries)
							handshakeRetries.remove(player);
						
						removeRetries.clear();
						
						t += 1000;
					}
					
				}
			}
		});
		
		retryThread.setPriority(Thread.MIN_PRIORITY);
		retryThread.start();
	}
	
	/**
	 * stops the retry handshake thread
	 */
	public static void stopHandshakeRetryThread() {
		retryThread.interrupt();
	}
	
	public static void executeStartupCommands() {
		for (String command : MoreCommands.getMoreCommands().getStartupCommands()) {
			MinecraftServer.getServer().getCommandManager().executeCommand(MinecraftServer.getServer(), command);
			MoreCommands.getMoreCommands().getLogger().info("Executed startup command '" + command + "'");
		}
	}
	
	/**
	 * Is called if the server receives a handshake packet
	 */
	public void handshake(UUID uuid, boolean patched, boolean renderGlobalPatched) {
		MoreCommands.getMoreCommands().getLogger().info("Client handshake received for player '" + MinecraftServer.getServer().getConfigurationManager().getPlayerByUUID(uuid).getName() + "'");
		
		AppliedPatches.playerPatchMapping.get(MinecraftServer.getServer().getConfigurationManager().getPlayerByUUID(uuid)).setClientModded(true);
		AppliedPatches.playerPatchMapping.get(MinecraftServer.getServer().getConfigurationManager().getPlayerByUUID(uuid)).setClientPlayerPatched(patched);
		AppliedPatches.playerPatchMapping.get(MinecraftServer.getServer().getConfigurationManager().getPlayerByUUID(uuid)).setRenderGlobalPatched(renderGlobalPatched);
		
		MoreCommands.getMoreCommands().getLogger().info("Receiving client commands for player '" + MinecraftServer.getServer().getConfigurationManager().getPlayerByUUID(uuid).getName() + "'");
		MoreCommands.getMoreCommands().getPacketDispatcher().sendS01ClientCommand(MinecraftServer.getServer().getConfigurationManager().getPlayerByUUID(uuid));
	}
	
	/**
	 * Called when the handshake is finished
	 */
	public void finishHandshake(UUID uuid) {
		handshakeRetries.remove(MinecraftServer.getServer().getConfigurationManager().getPlayerByUUID(uuid));
		AppliedPatches.playerPatchMapping.get(MinecraftServer.getServer().getConfigurationManager().getPlayerByUUID(uuid)).setHandshakeFinished(true);
		MoreCommands.getMoreCommands().getLogger().info("Received the following client commands for player '" + MinecraftServer.getServer().getConfigurationManager().getPlayerByUUID(uuid).getName() + "':\n" + 
														ServerPlayerSettings.getPlayerSettings(MinecraftServer.getServer().getConfigurationManager().getPlayerByUUID(uuid)).clientCommands);
		MoreCommands.getMoreCommands().getLogger().info("Handshake completed for player '" + MinecraftServer.getServer().getConfigurationManager().getPlayerByUUID(uuid).getName() + "'");
		MoreCommands.getMoreCommands().getPacketDispatcher().sendS02HandshakeFinished(MinecraftServer.getServer().getConfigurationManager().getPlayerByUUID(uuid));
	}
	
	/**
	 * Called if the client sends a client commands
	 */
	public void clientCommand(UUID uuid, String command) {
		ServerPlayerSettings.getPlayerSettings(MinecraftServer.getServer().getConfigurationManager().getPlayerByUUID(uuid)).clientCommands.add(command);
	}

	/**
	 * Called if the client pressed a key
	 */
	public void input(UUID playerUUID, int key) {
		EntityPlayerMP player = MinecraftServer.getServer().getConfigurationManager().getPlayerByUUID(playerUUID);
		PacketHandler.KEYINPUT.getHandler().onEvent(new KeyEvent(player, key));
	}

	/**
	 * Called if the client wants to disable chat output
	 */
	public void output(UUID playerUUID, boolean output) {
		EntityPlayerMP player = MinecraftServer.getServer().getConfigurationManager().getPlayerByUUID(playerUUID);
		
		if (player != null) {
			ServerPlayerSettings settings = ServerPlayerSettings.getPlayerSettings(player);
			if (settings != null) settings.output = output;
		}
	}

	/**
	 * Called if the client wants to know the seed or the world name of a world on the integrated server
	 */
	public void handleWorld(UUID uuid, String params) {
		EntityPlayerMP player = MinecraftServer.getServer().getConfigurationManager().getPlayerByUUID(uuid);
		if (player != null && MinecraftServer.getServer() != null && MinecraftServer.getServer().getCommandManager() != null) {
			Object command = MinecraftServer.getServer().getCommandManager().getCommands().get("world");
			
			if (command instanceof CommandWorld) {
				((ServerCommand) command).execute(player, params.split(" "));
			}
		}
	}
}
