package com.mrnobody.morecommands.core;

import java.util.concurrent.Callable;

import com.mrnobody.morecommands.util.Reference;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

/**
 * Class containing Information to the patches applied to players, server and client
 * and some other state information.
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
	 * A class containing information which patches were applied for a player
	 * 
	 * @author MrNobody98
	 *
	 */
	public static class PlayerPatches implements ICapabilityProvider {
		public static final ResourceLocation PATCHES_IDENTIFIER = new ResourceLocation(Reference.MODID, "paches");
		@CapabilityInject(PlayerPatches.class)
		public static final Capability<PlayerPatches> PATCHES_CAPABILITY = null;
		
		public static final void registerCapability() {
			CapabilityManager.INSTANCE.register(PlayerPatches.class, new Capability.IStorage<PlayerPatches>() {
				@Override public NBTBase writeNBT(Capability<PlayerPatches> capability, PlayerPatches instance, EnumFacing side) {return null;}
				@Override public void readNBT(Capability<PlayerPatches> capability, PlayerPatches instance, EnumFacing side, NBTBase nbt) {}
			}, new Callable<PlayerPatches>() {
				@Override public PlayerPatches call() throws Exception {return new PlayerPatches();}
			});
		}
		
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

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			return capability == PATCHES_CAPABILITY;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			return capability == PATCHES_CAPABILITY ? PATCHES_CAPABILITY.<T>cast(this) : null;
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
