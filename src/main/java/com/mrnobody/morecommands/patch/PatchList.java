package com.mrnobody.morecommands.patch;

import com.mrnobody.morecommands.core.MoreCommands;

/**
 * A list of patches to classes
 * 
 * @author MrNobody98
 */
public final class PatchList {
	private PatchList() {}
	
	/** The patch to the chat gui. See {@link PatchChatGui} */
	public static final String PATCH_CHATGUI              = "PATCH_CHATGUI";
	/** The patch to the client side Command Manager. See {@link PatchClientCommandManager} */
	public static final String PATCH_CL_CMD_MANAGER       = "PATCH_CLIENT_COMMAND_MANAGER";
	/** The patch to the EntityPlayerMP class. See {@link PatchEntityPlayerMP} */
	public static final String PATCH_ENTITYPLAYERMP       = "PATCH_ENTITYPLAYERMP";
	/** The patch to the PlayerList class. See {@link PatchEntityPlayerMP} */
	public static final String PATCH_PLAYERLIST           =  PATCH_ENTITYPLAYERMP;
	/** The patch to the EntityPlayerSP class. See {@link PatchEntityPlayerSP} */
	public static final String PATCH_ENTITYPLAYERSP       = "PATCH_ENTITYPLAYERSP";
	/** The patch to the NetHandlerPlayServer. See {@link PatchNetHandlerPlayServer} */
	public static final String PATCH_NETHANDLERPLAYSERVER = "PATCH_NETHANDLERPAYSERVER";
	/** The patch to the global renderer. See {@link PatchRenderGlobal} */
	public static final String PATCH_RENDERGLOBAL         = "PATCH_RENDERGLOBAL";
	/** The patch to the server side Command Manager. See {@link PatchServerCommandManager} */
	public static final String PATCH_SV_CMD_MANAGER       = "PATCH_SERVER_COMMAND_MANAGER";
	/** Not a patch. Just a marker to keep track whether the client has MoreCommands installed */
	public static final String CLIENT_MODDED              = "CLIENT_MODDED";
	/** Not a patch. Just a marker to keep track whether the server has MoreCommands installed */
	public static final String SERVER_MODDED              = "SERVER_MODDED";
	/** Not a patch. Just a marker to keep track whether the handshake was completed */
	public static final String HANDSHAKE_FINISHED         = "HANDSHAKE_FINISHED";
	
	private static boolean registered = false;
	
	/**
	 * Registers all patches to the {@link PatchManager}
	 */
	public static void registerPatches() {
		if (registered) return;
		
		PatchManager.instance().registerPatch(new PatchEntityPlayerMP(PATCH_ENTITYPLAYERMP));
		PatchManager.instance().registerPatch(new PatchNetHandlerPlayServer(PATCH_NETHANDLERPLAYSERVER));
		PatchManager.instance().registerPatch(new PatchServerCommandManager(PATCH_SV_CMD_MANAGER));
		
		if (MoreCommands.isClientSide()) {
			PatchManager.instance().registerPatch(new PatchChatGui(PATCH_CHATGUI));
			PatchManager.instance().registerPatch(new PatchClientCommandManager(PATCH_CL_CMD_MANAGER));
			PatchManager.instance().registerPatch(new PatchEntityPlayerSP(PATCH_ENTITYPLAYERSP));
			PatchManager.instance().registerPatch(new PatchRenderGlobal(PATCH_RENDERGLOBAL));
		}
		
		registered = true;
	}
}
