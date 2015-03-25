package com.mrnobody.morecommands.core;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.event.FMLStateEvent;

/**
 * Abstract base class for client and server Patcher.
 * Contains Information to the patches applied to players and server
 * 
 * @author MrNobody98
 *
 */
public abstract class Patcher {
	private static boolean serverModded = false;
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
	}
	
	/**
	 * @return Whether the servers {@link net.minecraft.server.management.ServerConfigurationManager} was patched
	 */
	public static boolean serverConfigManagerPatched() {
		return Patcher.serverConfigManagerPatched;
	}
	
	/**
	 * @return Whether the clients Command Handler ({@link net.minecraftforge.client.ClientCommandHandler}) was patched
	 */
	public static boolean clientCommandManagerPatched() {
		return Patcher.clientCommandManagerPatched;
	}
	
	/**
	 * @return Whether the servers Command Handler ({@link net.minecraft.command.ServerCommandManager}) was patched
	 */
	public static boolean serverCommandManagerPatched() {
		return Patcher.serverCommandManagerPatched;
	}
	
	/**
	 * @return Whether the server has this mod installed
	 */
	public static boolean serverModded() {
		return Patcher.serverModded;
	}
	
	/**
	 * Sets whether the server has this mod installed
	 */
	public static void setServerModded(boolean modded) {
		Patcher.serverModded = modded;
	}
	
	/**
	 * Sets whether the servers {@link net.minecraft.server.management.ServerConfigurationManager} was patched
	 */
	public static void setServerConfigManagerPatched(boolean patched) {
		Patcher.serverConfigManagerPatched = patched;
	}
	
	/**
	 * Sets whether the clients Command Handler ({@link net.minecraftforge.client.ClientCommandHandler}) was patched
	 */
	public static void setClientCommandManagerPatched(boolean patched) {
		Patcher.clientCommandManagerPatched = patched;
	}
	
	/**
	 * Sets whether the servers Command Handler ({@link net.minecraft.command.ServerCommandManager}) was patched
	 */
	public static void setServerCommandManagerPatched(boolean patched) {
		Patcher.serverCommandManagerPatched = patched;
	}
	
	/**
	 * Applies the patches corresponding to the current {@link FMLStateEvent}
	 */
	public abstract void applyModStatePatch(FMLStateEvent stateEvent);
}
