package com.mrnobody.morecommands.command;

import com.mrnobody.morecommands.core.AppliedPatches;
import com.mrnobody.morecommands.core.AppliedPatches.PlayerPatches;
import com.mrnobody.morecommands.patch.ClientCommandManager;
import com.mrnobody.morecommands.patch.EntityClientPlayerMP;
import com.mrnobody.morecommands.patch.EntityPlayerMP;
import com.mrnobody.morecommands.util.GlobalSettings;

import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.client.ClientCommandHandler;

/**
 * An enum of requirements for a command to be executed properly
 * 
 * @author MrNobody98
 */
public enum CommandRequirement {
	/**
	 * Checks whether the players {@link net.minecraft.entity.player.EntityPlayerMP#playerNetServerHandler} was replaced with
	 * {@link com.mrnobody.morecommands.patch.NetHandlerPlayServer}
	 */
	PATCH_NETHANDLERPLAYSERVER("command.generic.netServerPlayHandlerNotPatched") {
		@Override public boolean isSatisfied(ICommandSender sender, PlayerPatches playerPatches, Side side) {
			return side.isServer() ? playerPatches.serverPlayHandlerPatched() : false;                          //server side only
		}
	},
	/**
	 * Checks whether {@link Minecraft#thePlayer} is of type {@link com.mrnobody.morecommands.patch.EntityClientPlayerMP}
	 */
	PATCH_ENTITYCLIENTPLAYERMP("command.generic.clientPlayerNotPatched") {
		@Override public boolean isSatisfied(ICommandSender sender, PlayerPatches playerPatches, Side side) {
			return side.isServer() ? playerPatches.clientPlayerPatched() : Minecraft.getMinecraft().thePlayer instanceof EntityClientPlayerMP;
		}
	},
	/**
	 * Checks whether the type of a server player object is of type {@link com.mrnobody.morecommands.patch.EntityPlayerMP}
	 */
	PATCH_ENTITYPLAYERMP("command.generic.serverPlayerNotPatched") {
		@Override public boolean isSatisfied(ICommandSender sender, PlayerPatches playerPatches, Side side) {
			return side.isServer() ? sender instanceof EntityPlayerMP : false;                                  //server side only
		}
	},
	/**
	 * Checks whether the client has installed MoreCommands
	 */
	MODDED_CLIENT("command.generic.clientNotModded") {
		@Override public boolean isSatisfied(ICommandSender sender, PlayerPatches playerPatches, Side side) {
			return side.isServer() ? playerPatches.clientModded() : true;                                       //server side only, always true client side
		}
	},
	/**
	 * Checks whether {@link net.minecraft.server.MinecraftServer#getConfigurationManager()} is either of type
	 * {@link com.mrnobody.morecommands.patch.ServerConfigurationManagerIntegrated} or of type
	 * {@link com.mrnobody.morecommands.patch.ServerConfigurationManagerDedicated}
	 */
	PATCH_SERVERCONFIGMANAGER("command.generic.serverConfigManagerNotPatched") {
		@Override public boolean isSatisfied(ICommandSender sender, PlayerPatches playerPatches, Side side) {
			return side.isServer() ? AppliedPatches.serverConfigManagerPatched() : false;                       //server side only
		}
	},
	/**
	 * Checks whether {@link ClientCommandHandler#instance} is of type {@link ClientCommandManager}
	 */
	PATCH_CLIENTCOMMANDHANDLER("command.generic.clientCommandHandlerNotPatched") {
		@Override public boolean isSatisfied(ICommandSender sender, PlayerPatches playerPatches, Side side) {
			return side.isClient() ? ClientCommandHandler.instance instanceof ClientCommandManager : false;     //client side only
		}
	},
	/**
	 * Checks whether {@link net.minecraft.server.MinecraftServer#getCommandManager()} is of type
	 * {@link com.mrnobody.morecommands.patch.ServerCommandManager}
	 */
	PATCH_SERVERCOMMANDHANDLER("command.generic.serverCommandManagerNotPatched") {
		@Override public boolean isSatisfied(ICommandSender sender, PlayerPatches playerPatches, Side side) {
			return side.isServer() ? AppliedPatches.serverCommandManagerPatched() : false;                       //server side only
		}
	},
	/**
	 * Checks whether the other side is required to have MoreCommands installed, otherwise it's optional
	 */
	OTHER_SIDE_MUST_BE_MODDED("command.generic.clientsDontHaveToBeModded", "command.generic.serverDoesntHaveToBeModded") {
		@Override public boolean isSatisfied(ICommandSender sender, PlayerPatches playerPatches, Side side) {
			return side.isClient() ? GlobalSettings.serverMustHaveMod : GlobalSettings.clientMustHaveMod;
		}
	};
	
	private final String langfileMsg, langFileMsgServer, langFileMsgClient;
	
	/**
	 * Constructs a {@link CommandRequirement} using a common language file entry for both sides
	 * 
	 * @param langfileMsg the language file entry that is contains the error message if the requirement is not satisfied
	 */
	private CommandRequirement(final String langfileMsg) {
		this.langfileMsg = langfileMsg;
		this.langFileMsgClient = null;
		this.langFileMsgServer = null;
	}
	
	/**
	 * Constructs a {@link CommandRequirement} using a different language file entry for server and client side
	 * 
	 * @param langfileMsgServer the language file entry that is contains the error message if the requirement is not satisfied on server side
	 * @param langFileMsgClient the language file entry that is contains the error message if the requirement is not satisfied on client side
	 */
	private CommandRequirement(final String langfileMsgServer, final String langFileMsgClient) {
		this.langFileMsgServer = langfileMsgServer;
		this.langFileMsgClient = langFileMsgClient;
		this.langfileMsg = null;
	}
	
	/**
	 * Checks whether this requirement is satisfied
	 * 
	 * @param sender the command sender
	 * @param playerPatches the {@link PlayerPatches} object corresponding to the sender
	 * @param side the side for which this requirement should be checked
	 * @return whether this requirement is satisfied
	 */
	public abstract boolean isSatisfied(ICommandSender sender, PlayerPatches playerPatches, Side side);
	
	/**
	 * Returns the error message corresponding to this requirement
	 * 
	 * @param side the side for which the error message shall be retrieved
	 * @return the error message (may be a language file entry)
	 */
	public String getLangfileMsg(Side side) {
		return this.langfileMsg == null ? side.isClient() ? this.langFileMsgClient : this.langFileMsgServer : this.langfileMsg;
	}
}
