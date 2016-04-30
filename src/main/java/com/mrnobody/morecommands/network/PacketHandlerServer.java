package com.mrnobody.morecommands.network;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.mrnobody.morecommands.command.AbstractCommand;
import com.mrnobody.morecommands.core.AppliedPatches.PlayerPatches;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.util.GlobalSettings;
import com.mrnobody.morecommands.util.LanguageManager;
import com.mrnobody.morecommands.util.PlayerSettings;
import com.mrnobody.morecommands.util.Reference;
import com.mrnobody.morecommands.util.ServerPlayerSettings;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

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
		private final int timeout; private int remainingTime, retries;
		
		public HandshakeRetry(int timeout, int retries) {
			this.timeout = this.remainingTime = timeout;
			this.retries = retries;
		}
	}
	
	private static final Map<EntityPlayerMP, HandshakeRetry> handshakeRetries = new ConcurrentHashMap<EntityPlayerMP, HandshakeRetry>();
	private static final ScheduledExecutorService retryExecutor = Executors.newSingleThreadScheduledExecutor();
	private static ScheduledFuture<?> retryHandshake;
	
	static {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				retryExecutor.shutdown();
				
				try {retryExecutor.awaitTermination(5, TimeUnit.SECONDS);}
				catch (InterruptedException ex) {}
			}
		}));
	}
	
	/**
	 * Adds a player to the retry handshake thread
	 * @param player the player
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
		retryHandshake = retryExecutor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				Thread.currentThread().setName("MoreCommand Handshake Retry Thread");
				
				List<EntityPlayerMP> removeRetries = new ArrayList<EntityPlayerMP>();

				for (Map.Entry<EntityPlayerMP, HandshakeRetry> retry : handshakeRetries.entrySet()) {
					if (retry.getValue().retries == 0) {
						removeRetries.add(retry.getKey());
						MoreCommands.INSTANCE.getLogger().info("Handshake failed for player '" + retry.getKey().getCommandSenderName() + "'");
					}
					else if (retry.getValue().remainingTime == 0) {
						MoreCommands.INSTANCE.getLogger().info("Retrying handshake for player '" + retry.getKey().getCommandSenderName() + "'");
						MoreCommands.INSTANCE.getPacketDispatcher().sendS00Handshake(retry.getKey());
						retry.getValue().retries--; retry.getValue().remainingTime = retry.getValue().timeout;
					}
					else retry.getValue().remainingTime--;
				}
				
				for (EntityPlayerMP player : removeRetries)
					handshakeRetries.remove(player);
				
				removeRetries.clear();
			}
		}, 0, 1, TimeUnit.SECONDS);
	}
	
	/**
	 * stops the retry handshake thread
	 */
	public static void stopHandshakeRetryThread() {
		retryHandshake.cancel(true);
		handshakeRetries.clear();
	}
	
	/**
	 * Executes the startup commands that are intended to be executed on the server's startup.
	 * This method is only invoked on dedicated servers
	 */
	public static void executeStartupCommands() {
		for (String command : MoreCommands.INSTANCE.getStartupCommands()) {
			MinecraftServer.getServer().getCommandManager().executeCommand(MinecraftServer.getServer(), command);
			MoreCommands.INSTANCE.getLogger().info("Executed startup command '" + command + "'");
		}
	}
	
	/**
	 * Is called if the server receives a handshake packet
	 * @param player the player
	 * @param patched whether the client player was patched
	 * @param version the client's MoreCommands version
	 */
	public void handshake(EntityPlayerMP player, boolean patched, String version) {
		if (!Reference.VERSION.equals(version)) {
			MoreCommands.INSTANCE.getLogger().warn("Player " + player.getCommandSenderName() + " has incompatible MoreCommands version: " + version + ", version " + Reference.VERSION + " is required");
			return;
		}
		
		MoreCommands.INSTANCE.getLogger().info("Client handshake received for player '" + player.getCommandSenderName() + "'");
		
		PlayerPatches patches = MoreCommands.getEntityProperties(PlayerPatches.class, PlayerPatches.PLAYERPATCHES_IDENTIFIER, player);
		patches.setClientModded(true);
		patches.setClientPlayerPatched(patched);
		
		handshakeRetries.remove(player);
		MoreCommands.INSTANCE.getPacketDispatcher().sendS01HandshakeFinished(player);
	}
	
	/**
	 * Called if the client wants to enable/disable chat output
	 * @param player the player
	 * @param output whether to enable or disable chat output
	 */
	public void output(EntityPlayerMP player, boolean output) {
		if (player != null) {
			ServerPlayerSettings settings = MoreCommands.getEntityProperties(ServerPlayerSettings.class, PlayerSettings.MORECOMMANDS_IDENTIFIER	, player);
			if (settings != null) settings.output = output;
		}
	}

	
	/**
	 * Called if the client wants to know or modify world information (not available client side)
	 * @param player the player
	 * @param output whether to enable or disable chat output
	 */
	public void handleWorld(EntityPlayerMP player, String[] params) {
		if (MinecraftServer.getServer().getCommandManager().getCommands().get("world") instanceof AbstractCommand)
			MinecraftServer.getServer().getCommandManager().executeCommand(player, "world " + AbstractCommand.rejoinParams(params));
		else
			player.addChatMessage(new ChatComponentText(LanguageManager.translate(MoreCommands.INSTANCE.getCurrentLang(player), "command.world.notFound")));
	}
}
