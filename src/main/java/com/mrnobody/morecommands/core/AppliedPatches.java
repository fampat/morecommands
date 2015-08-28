package com.mrnobody.morecommands.core;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayerMP;

/**
 * Class containing Information to the patches applied to players, server and client
 * 
 * @author MrNobody98
 *
 */
public final class AppliedPatches {
	private AppliedPatches() {}
	
	private static volatile boolean serverModded = false;
	private static volatile boolean handshakeFinished = false;
	private static boolean serverConfigManagerPatched = false;
	private static boolean serverCommandManagerPatched = false;
	private static boolean clientCommandManagerPatched = false;
	
	/**
	 * A map containing which patches were applied for each player
	 */
	public static final Map<EntityPlayerMP, PlayerPatches> playerPatchMapping = new HashMap<EntityPlayerMP, PlayerPatches>();
	
	/**
	 * A class containing information which patches were applied for a player
	 * 
	 * @author MrNobody98
	 *
	 */
	public static class PlayerPatches {
		private boolean handshakeFinished = false;
		private boolean clientModded = false;
		private boolean clientPlayerPatched = false;
		private boolean serverPlayHandlerPatched = false;
		private boolean renderGlobalPatched = false;
		
		/**
		 * @return Whether the player has installed the mod client side
		 */
		public boolean clientModded() {
			return this.clientModded;
		}
		
		/**
		 * @return Whether the players {@link net.minecraft.client.entity.EntityPlayerSP} was patched
		 */
		public boolean clientPlayerPatched() {
			return this.clientPlayerPatched;
		}
		
		/**
		 * @return Whether the players {@link net.minecraft.network.NetHandlerPlayServer} was patched
		 */
		public boolean serverPlayHandlerPatched() {
			return this.serverPlayHandlerPatched;
		}
		
		/**
		 * @return Whether global renderer {@link net.minecraft.client.renderer.RenderGlobal} was patched
		 */
		public boolean renderGlobalPatched() {
			return this.renderGlobalPatched;
		}
		
		/**
		 * @return Whether the server completed a full handshake with the client
		 */
		public boolean handshakeFinished() {
			return this.handshakeFinished;
		}
		
		/**
		 * Sets whether the client has this mod installed
		 */
		public void setClientModded(boolean modded) {
			this.clientModded = modded;
		}
		
		/**
		 * Sets whether the players {@link net.minecraft.client.entity.EntityClientPlayerMP} was patched
		 */
		public void setClientPlayerPatched(boolean patched) {
			this.clientPlayerPatched = patched;
		}
		
		/**
		 * Sets whether the players {@link net.minecraft.network.NetHandlerPlayServer} was patched
		 */
		public void setServerPlayHandlerPatched(boolean patched) {
			this.serverPlayHandlerPatched = patched;
		}

		/**
		 * Sets whether the players {@link net.minecraft.client.renderer.RenderGlobal} was patched
		 */
		public void setRenderGlobalPatched(boolean patched) {
			this.renderGlobalPatched = patched;
		}
		
		/**
		 * Sets whether the server completed a full handshake with the client
		 */
		public void setHandshakeFinished(boolean finished) {
			this.handshakeFinished = finished;
		}
	}
	
	/**
	 * @return Whether the servers {@link net.minecraft.server.management.ServerConfigurationManager} was patched
	 */
	public static boolean serverConfigManagerPatched() {
		return AppliedPatches.serverConfigManagerPatched;
	}
	
	/**
	 * @return Whether the clients Command Handler ({@link net.minecraftforge.client.ClientCommandHandler}) was patched
	 */
	public static boolean clientCommandManagerPatched() {
		return AppliedPatches.clientCommandManagerPatched;
	}
	
	/**
	 * @return Whether the servers Command Handler ({@link net.minecraft.command.ServerCommandManager}) was patched
	 */
	public static boolean serverCommandManagerPatched() {
		return AppliedPatches.serverCommandManagerPatched;
	}
	
	/**
	 * @return Whether the server has this mod installed
	 */
	public static boolean serverModded() {
		return AppliedPatches.serverModded;
	}
	
	/**
	 * @return Whether the handshake was finished
	 */
	public static boolean handshakeFinished() {
		return AppliedPatches.handshakeFinished;
	}
	
	/**
	 * Sets whether the server has this mod installed
	 */
	public static void setServerModded(boolean modded) {
		AppliedPatches.serverModded = modded;
	}
	
	/**
	 * Sets whether the handshake is finished
	 */
	public static void setHandshakeFinished(boolean finished) {
		AppliedPatches.handshakeFinished = finished;
	}
	
	/**
	 * Sets whether the servers {@link net.minecraft.server.management.ServerConfigurationManager} was patched
	 */
	public static void setServerConfigManagerPatched(boolean patched) {
		AppliedPatches.serverConfigManagerPatched = patched;
	}
	
	/**
	 * Sets whether the clients Command Handler ({@link net.minecraftforge.client.ClientCommandHandler}) was patched
	 */
	public static void setClientCommandManagerPatched(boolean patched) {
		AppliedPatches.clientCommandManagerPatched = patched;
	}
	
	/**
	 * Sets whether the servers Command Handler ({@link net.minecraft.command.ServerCommandManager}) was patched
	 */
	public static void setServerCommandManagerPatched(boolean patched) {
		AppliedPatches.serverCommandManagerPatched = patched;
	}
}
