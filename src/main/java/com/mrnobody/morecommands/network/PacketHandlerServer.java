package com.mrnobody.morecommands.network;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.command.server.CommandWorld;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.core.Patcher;
import com.mrnobody.morecommands.handler.PacketHandler;
import com.mrnobody.morecommands.util.KeyEvent;
import com.mrnobody.morecommands.util.ServerPlayerSettings;

/**
 * This class handles all incoming packets from the clients
 * 
 * @author MrNobody98
 *
 */
public class PacketHandlerServer {
	/**
	 * Is called if the server receives a handshake packet
	 */
	public void handshake(UUID uuid, boolean patched) {
		MoreCommands.getMoreCommands().getLogger().info("Client handshake received");
		
		Patcher.playerPatchMapping.get(ServerPlayerSettings.playerUUIDMapping.get(uuid)).setClientModded(true);
		Patcher.playerPatchMapping.get(ServerPlayerSettings.playerUUIDMapping.get(uuid)).setClientPlayerPatched(patched);
		
		MoreCommands.getMoreCommands().getPacketDispatcher().sendS01ClientCommand(ServerPlayerSettings.playerUUIDMapping.get(uuid));
	}

	/**
	 * Called if the client pressed a key
	 */
	public void input(UUID playerUUID, int key) {
		EntityPlayerMP player = ServerPlayerSettings.playerUUIDMapping.get(playerUUID);
		PacketHandler.KEYINPUT.getHandler().onEvent(new KeyEvent(player, key));
	}

	/**
	 * Called if the client wants to disable chat output
	 */
	public void output(UUID playerUUID, boolean output) {
		EntityPlayerMP player = ServerPlayerSettings.playerUUIDMapping.get(playerUUID);
		
		if (player != null) {
			ServerPlayerSettings settings = ServerPlayerSettings.playerSettingsMapping.get(player);
			if (settings != null) settings.output = output;
		}
	}

	/**
	 * Called if the client sends a client commands
	 */
	public void clientCommand(UUID uuid, String command) {
		ServerPlayerSettings.playerSettingsMapping.get(ServerPlayerSettings.playerUUIDMapping.get(uuid)).clientCommands.add(command);
		MoreCommands.getMoreCommands().getLogger().info("Server took note of client Command '" + command + "'");
	}

	/**
	 * Called if the client wants to know the seed or the world name of a world on the integrated server
	 */
	public void handleWorld(UUID uuid, String params) {
		EntityPlayerMP player = ServerPlayerSettings.playerUUIDMapping.get(uuid);
		if (player != null && MinecraftServer.getServer() != null && MinecraftServer.getServer().getCommandManager() != null) {
			Object command = MinecraftServer.getServer().getCommandManager().getCommands().get("world");
			
			if (command instanceof CommandWorld) {
				((ServerCommand) command).processCommand(player, params.split(" "));
			}
		}
	}
}
