package com.mrnobody.morecommands.command;

import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.patch.PatchClientCommandManager;
import com.mrnobody.morecommands.patch.PatchEntityPlayerMP;
import com.mrnobody.morecommands.patch.PatchEntityPlayerSP;
import com.mrnobody.morecommands.patch.PatchList;
import com.mrnobody.morecommands.patch.PatchManager;
import com.mrnobody.morecommands.patch.PatchNetHandlerPlayServer;
import com.mrnobody.morecommands.patch.PatchRenderGlobal;
import com.mrnobody.morecommands.patch.PatchServerCommandManager;
import com.mrnobody.morecommands.settings.MoreCommandsConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.client.ClientCommandHandler;

/**
 * An enum of requirements for a command to be executed properly
 * 
 * @author MrNobody98
 */
public enum CommandRequirement {
	/**
	 * Checks whether the players {@link net.minecraft.entity.player.EntityPlayerMP#playerNetServerHandler} was replaced with
	 * {@link com.mrnobody.morecommands.patch.PatchNetHandlerPlayServer.NetHandlerPlayServer}
	 */
	PATCH_NETHANDLERPLAYSERVER("command.generic.netServerPlayHandlerNotPatched") {
		@Override public boolean isSatisfied(ICommandSender sender) {
			EntityPlayerMP player = getAsEntityOrNull(sender, EntityPlayerMP.class);
			return player == null ? false : player.connection instanceof PatchNetHandlerPlayServer.NetHandlerPlayServer;
		}
	},
	/**
	 * Checks whether {@link Minecraft#thePlayer} is of type {@link com.mrnobody.morecommands.patch.PatchEntityPlayerSP.EntityPlayerSP}
	 */
	PATCH_ENTITYPLAYERSP("command.generic.clientPlayerNotPatched") {
		@Override public boolean isSatisfied(ICommandSender sender) {
			if (MoreCommands.isClientSide()) {
				PatchEntityPlayerSP.EntityPlayerSP player = getAsEntityOrNull(Minecraft.getMinecraft().player, PatchEntityPlayerSP.EntityPlayerSP.class);
				return player != null;
			}
			else {
				EntityPlayerMP player = getAsEntityOrNull(sender, EntityPlayerMP.class);
				return player == null ? false : PatchManager.instance().getAppliedPatchesForPlayer(player).wasPatchSuccessfullyApplied(PatchList.PATCH_ENTITYPLAYERSP);
			}
		}
	},
	/**
	 * Checks whether {@link Minecraft#renderGlobal} is of type {@link com.mrnobody.morecommands.patch.RenderGlobal}
	 */
	PATCH_RENDERGLOBAL("command.generic.renderGlobalNotPatched") {
		@Override public boolean isSatisfied(ICommandSender sender) {
			if (MoreCommands.isClientSide()) {
				return Minecraft.getMinecraft().renderGlobal instanceof PatchRenderGlobal.RenderGlobal;
			}
			else {
				EntityPlayerMP player = getAsEntityOrNull(sender, EntityPlayerMP.class);
				return player == null ? false : PatchManager.instance().getAppliedPatchesForPlayer(player).wasPatchSuccessfullyApplied(PatchList.PATCH_RENDERGLOBAL);
			}
		}
	},
	/**
	 * Checks whether the type of a server player object is of type {@link com.mrnobody.morecommands.patch.EntityPlayerMP}
	 */
	PATCH_ENTITYPLAYERMP("command.generic.serverPlayerNotPatched") {
		@Override public boolean isSatisfied(ICommandSender sender) {
			PatchEntityPlayerMP.EntityPlayerMP player = getAsEntityOrNull(sender, PatchEntityPlayerMP.EntityPlayerMP.class);
			return player != null;
		}
	},
	/**
	 * Checks whether the client has installed MoreCommands
	 */
	MODDED_CLIENT("command.generic.clientNotModded") {
		@Override public boolean isSatisfied(ICommandSender sender) {
			EntityPlayerMP player = getAsEntityOrNull(sender, EntityPlayerMP.class);
			return player == null ? false : PatchManager.instance().getAppliedPatchesForPlayer(player).wasPatchSuccessfullyApplied(PatchList.CLIENT_MODDED);
		}
	},
	/**
	 * Checks whether {@link net.minecraft.server.MinecraftServer#getConfigurationManager()} is either of type
	 * {@link com.mrnobody.morecommands.patch.PatchEntityPlayerMP.ServerConfigurationManagerIntegrated} or of type
	 * {@link com.mrnobody.morecommands.patch.PatchEntityPlayerMP.ServerConfigurationManagerDedicated}
	 */
	PATCH_SERVERCONFIGMANAGER("command.generic.serverConfigManagerNotPatched") {
		@Override public boolean isSatisfied(ICommandSender sender) {
			MinecraftServer mcServer = sender.getServer();
			return mcServer == null ? false : mcServer.getPlayerList().getClass().getName().startsWith(PatchEntityPlayerMP.class.getName());
		}
	},
	/**
	 * Checks whether {@link ClientCommandHandler#instance} is of type {@link PatchClientCommandManager.ClientCommandManager}
	 */
	PATCH_CLIENTCOMMANDHANDLER("command.generic.clientCommandHandlerNotPatched") {
		@Override public boolean isSatisfied(ICommandSender sender) {
			if (!MoreCommands.isClientSide()) return false;
			else return ClientCommandHandler.instance instanceof PatchClientCommandManager.ClientCommandManager;
		}
	},
	/**
	 * Checks whether {@link net.minecraft.server.MinecraftServer#getCommandManager()} is of type
	 * {@link com.mrnobody.morecommands.patch.PatchServerCommandManager.ServerCommandManager}
	 */
	PATCH_SERVERCOMMANDHANDLER("command.generic.serverCommandManagerNotPatched") {
		@Override public boolean isSatisfied(ICommandSender sender) {
			MinecraftServer mcServer = sender.getServer();
			return mcServer == null ? false : mcServer.getCommandManager() instanceof PatchServerCommandManager.ServerCommandManager;
		}
	},
	/**
	 * Checks whether ALL clients are required to have MoreCommands installed, otherwise it's optional
	 */
	ALL_CLIENTS_MUST_BE_MODDED("command.generic.clientsDontHaveToBeModded") {
		@Override public boolean isSatisfied(ICommandSender sender) {
			return MoreCommandsConfig.clientMustHaveMod;
		}
	},
	/**
	 * Checks whether the server is required to have MoreCommands installed, otherwise it's optional
	 */
	SERVER_MUST_BE_MODDED("command.generic.serverDoesntHaveToBeModded") {
		@Override public boolean isSatisfied(ICommandSender sender) {
			return MoreCommandsConfig.clientMustHaveMod;
		}
	};
	
	private static <T extends Entity> T getAsEntityOrNull(ICommandSender sender, Class<T> entityType) {
		if (!AbstractCommand.isSenderOfEntityType(sender, entityType)) return null;
		else return AbstractCommand.getSenderAsEntity(sender, entityType);
	}
	
	private final String langfileMsg;
	
	/**
	 * Constructs a {@link CommandRequirement} using a common language file entry for both sides
	 * 
	 * @param langfileMsg the language file entry that is contains the error message if the requirement is not satisfied
	 */
	private CommandRequirement(final String langfileMsg) {
		this.langfileMsg = langfileMsg;
	}
	
	/**
	 * Checks whether this requirement is satisfied
	 * 
	 * @param sender the command sender
	 * @return whether this requirement is satisfied
	 */
	public abstract boolean isSatisfied(ICommandSender sender);
	
	/**
	 * Returns the error message corresponding to this requirement
	 * 
	 * @return the error message (may be a language file entry)
	 */
	public String getLangfileMsg() {
		return this.langfileMsg;
	}
}
