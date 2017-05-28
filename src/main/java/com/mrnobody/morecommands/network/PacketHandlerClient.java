package com.mrnobody.morecommands.network;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mrnobody.morecommands.asm.transform.TransformTextureCompass;
import com.mrnobody.morecommands.asm.transform.TransformTextureCompass.CompassTargetCallBack;
import com.mrnobody.morecommands.command.ClientCommand;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.core.ClientProxy;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.event.DamageItemEvent;
import com.mrnobody.morecommands.event.EventHandler;
import com.mrnobody.morecommands.event.ItemStackChangeSizeEvent;
import com.mrnobody.morecommands.event.Listeners.EventListener;
import com.mrnobody.morecommands.network.PacketDispatcher.BlockUpdateType;
import com.mrnobody.morecommands.patch.PatchEntityClientPlayerMP.EntityClientPlayerMP;
import com.mrnobody.morecommands.patch.PatchEntityClientPlayerMP.PlayerControllerMP;
import com.mrnobody.morecommands.patch.PatchList;
import com.mrnobody.morecommands.patch.PatchManager;
import com.mrnobody.morecommands.patch.PatchManager.AppliedPatches;
import com.mrnobody.morecommands.settings.ClientPlayerSettings;
import com.mrnobody.morecommands.settings.ClientPlayerSettings.XrayInfo;
import com.mrnobody.morecommands.settings.GlobalSettings;
import com.mrnobody.morecommands.settings.MoreCommandsConfig;
import com.mrnobody.morecommands.settings.PlayerSettings;
import com.mrnobody.morecommands.util.EntityCamera;
import com.mrnobody.morecommands.util.Reference;
import com.mrnobody.morecommands.util.Xray;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.ClientCommandHandler;

/**
 * This class handles all incoming packets from the server
 * 
 * @author MrNobody98
 *
 */
public class PacketHandlerClient {
	/** These commands will be removed when the server has MoreCommands installed to execute the server side versions of them */
	private static final List<ClientCommand<?>> removedCmds = new ArrayList<ClientCommand<?>>();
	/** These commands are currently processed server side and waited for a response */
	private static final TIntObjectMap<PendingRemoteCommand> pendingRemoteCommands = new TIntObjectHashMap<PendingRemoteCommand>();
	/** A counter for pending remote commands */
	private static int pendingRemoteCommandsID = 0;
	
	private EntityClientPlayerMP freecamOriginalPlayer;
	private boolean prevIsFlying;
	private boolean prevAllowFlying;
	private float prevFlySpeed;
	private float prevWalkSpeed;
	private boolean prevNoclip;
	
	private EntityClientPlayerMP freezecamOriginalPlayer;
	
	private boolean isEnlightened = false;
	private int lightenedWorld = 0;
	
	private static final EventListener<ItemStackChangeSizeEvent> onStackSizeChangeListener = new EventListener<ItemStackChangeSizeEvent>() {
		@Override
		public void onEvent(ItemStackChangeSizeEvent event) {
			if (event.entityPlayer instanceof net.minecraft.client.entity.EntityClientPlayerMP) {
				event.newSize = event.oldSize; event.setCanceled(true);
			}
		}
	};
	
	private static final Set<Item> disableDamage = new HashSet<Item>();
	private static final EventListener<DamageItemEvent> onItemDamageListener = new EventListener<DamageItemEvent>() {
		@Override
		public void onEvent(DamageItemEvent event) {
			if (event.entity instanceof net.minecraft.client.entity.EntityClientPlayerMP && disableDamage.contains(event.stack.getItem()))
				event.setCanceled(true);
		}
	};
	
	private ChunkCoordinates compassTarget = null;
	
	private final CompassTargetCallBack compassTargetCallback = new CompassTargetCallBack() {
		@Override
		public ChunkCoordinates getTarget(World world) {
			if (PacketHandlerClient.this.compassTarget == null) return world.getSpawnPoint();
			else return PacketHandlerClient.this.compassTarget;
		}
	};
	
	public PacketHandlerClient() {
		TransformTextureCompass.setCompassTargetCallback(this.compassTargetCallback);
		EventHandler.DAMAGE_ITEM.register(this.onItemDamageListener);
	}
	
	/**
	 * re-registers the unregistered client commands and clears the list
	 */
	public static void reregisterAndClearRemovedCmds() {
		for (ClientCommand<?> cmd : PacketHandlerClient.removedCmds) ClientCommandHandler.instance.registerCommand(cmd);
		PacketHandlerClient.removedCmds.clear();
	}
	
	/**
	 * Runs a thread waiting for a handshake from the server <br>
	 * to execute startup commands. If it isn't received after a <br>
	 * certain time, they are executed nevertheless
	 */
	public static void runStartupCommandsThread() {
		final int timeout = MoreCommandsConfig.startupTimeout < 0 ? 10000 : MoreCommandsConfig.startupTimeout > 10 ? 10000 : MoreCommandsConfig.startupTimeout * 1000;
		
		new Thread(new Runnable() {
			@Override
			public void run() {	
				long t = System.currentTimeMillis() + timeout;
				boolean added = false;
				AppliedPatches appliedPatches = PatchManager.instance().getGlobalAppliedPatches();
				
				while (t > System.currentTimeMillis()) {
					if (!added && appliedPatches.wasPatchSuccessfullyApplied(PatchList.SERVER_MODDED)) {
						t = System.currentTimeMillis() + timeout; added = true;
					}
					
					if (appliedPatches.wasPatchSuccessfullyApplied(PatchList.HANDSHAKE_FINISHED)) break;
				}
				
				if (Minecraft.getMinecraft().thePlayer == null) return;
				notifyPlayerAboutUpdate();
				
				try {Thread.sleep(MoreCommandsConfig.startupDelay * 1000);}
				catch (InterruptedException ex) {}
				
				for (String command : ClientPlayerSettings.getInstance(Minecraft.getMinecraft().thePlayer).startupCommands) {
					if (ClientCommandHandler.instance.executeCommand(Minecraft.getMinecraft().thePlayer, command) == 0)
						Minecraft.getMinecraft().thePlayer.sendChatMessage(command.startsWith("/") ? command : "/" + command);
					MoreCommands.INSTANCE.getLogger().info("Executed startup command '" + command + "'");
				}
			}
		}, "MoreCommands Startup Commands Thread (Client)").start();
	}
	
	/**
	 * Notifies the player that a MoreCommands update is available with a chat message
	 */
	private static void notifyPlayerAboutUpdate() {
		if (MoreCommands.getProxy().getUpdateText() != null && !MoreCommands.getProxy().wasPlayerNotified())
			Minecraft.getMinecraft().thePlayer.addChatMessage(MoreCommands.getProxy().getUpdateText());
	}
	
	/**
	 * Sends a command to the server to be executed. The result of the command execution (the text printed to the chat)
	 * will be accessible via a callback. Note that if the server takes longer than {@link GlobalSettings#remoteCommandsTimeout}
	 * to respond, the callback will never be invoked.
	 * 
	 * @param command the command
	 * @param callback the result callback
	 */
	public static void addPendingRemoteCommand(String command, CommandResultCallback callback) {
		synchronized (pendingRemoteCommands) {
			int id = pendingRemoteCommandsID++;
			pendingRemoteCommands.put(id, new PendingRemoteCommand(System.currentTimeMillis(), callback));
			MoreCommands.INSTANCE.getPacketDispatcher().sendC02ExecuteRemoteCommand(id, command);
		}
	}
	
	/**
	 * Removes the pending remotely executed commands for which a result hasn't been sent since
	 * {@link GlobalSettings#remoteCommandsTimeout} milliseconds
	 */
	public static void removeOldPendingRemoteCommands() {
		if (Minecraft.getMinecraft().theWorld == null) return;
		final long currentTime = System.currentTimeMillis();
		
		synchronized (pendingRemoteCommands) {
			pendingRemoteCommands.retainEntries(new TIntObjectProcedure<PendingRemoteCommand>() {
				@Override
				public boolean execute(int a, PendingRemoteCommand b) {
					if (currentTime - b.startTime > MoreCommandsConfig.remoteCommandsTimeout) {
						b.callback.timeout();
						return false;
					}
					
					return true;
				}
			});
		}
	}
	
	/**
	 * Is called if the client receives a handshake packet
	 * 
	 * @param version the server's MoreCommands version
	 */
	public void handshake(String version) {
		if (!Reference.VERSION.equals(version)) {
			MoreCommands.INSTANCE.getLogger().warn("Server has incompatible MoreCommands version: " + version + ", version " + Reference.VERSION + " is required");
			return;
		}
		
		PatchManager.instance().getGlobalAppliedPatches().setPatchSuccessfullyApplied(PatchList.SERVER_MODDED, true);
		
		List<String> remove = new ArrayList<String>();
		for (Object command : ClientCommandHandler.instance.getCommands().keySet()) {
			if (ClientCommandHandler.instance.getCommands().get((String) command) instanceof ClientCommand<?>) {
				ClientCommand<?> cmd = (ClientCommand<?>) ClientCommandHandler.instance.getCommands().get((String) command);
				if (!cmd.registerIfServerModded()) {remove.add(cmd.getCommandName()); PacketHandlerClient.removedCmds.add(cmd);}
			}
		}
		
		if (!remove.isEmpty()) {
			for (String rem : remove)
				ClientCommandHandler.instance.getCommands().remove(rem);
			
			MoreCommands.INSTANCE.getLogger().info("Unregistered following client commands because server side versions are used:\n" + remove);
		}
		
		MoreCommands.INSTANCE.getLogger().info("Sending client handshake");
		MoreCommands.INSTANCE.getPacketDispatcher().sendC00Handshake(
				PatchManager.instance().getGlobalAppliedPatches().wasPatchSuccessfullyApplied(PatchList.PATCH_ENTITYCLIENTPLAYERMP));
	}
	
	/**
	 * Called when the handshake is finished
	 */
	public void handshakeFinished() {
		PatchManager.instance().getGlobalAppliedPatches().setPatchSuccessfullyApplied(PatchList.HANDSHAKE_FINISHED, true);
		MoreCommands.INSTANCE.getLogger().info("Handshake finished");
	}
	
	/**
	 * Enables/Disables climbing on every wall
	 * 
	 * @param climb whether to enable or disable climb mode
	 */
	public void handleClimb(boolean climb) {
		if (Minecraft.getMinecraft().thePlayer instanceof EntityClientPlayerMP) {
			((EntityClientPlayerMP) Minecraft.getMinecraft().thePlayer).setOverrideOnLadder(climb);
		}
	}
	
	/**
	 * Toggles freecaming around the map
	 */
	public void handleFreecam() {
		if (this.freecamOriginalPlayer != null) {
			Minecraft.getMinecraft().renderViewEntity = this.freecamOriginalPlayer;
			this.freecamOriginalPlayer.capabilities.allowFlying = this.prevAllowFlying;
			this.freecamOriginalPlayer.capabilities.isFlying = this.prevIsFlying;
			this.freecamOriginalPlayer.sendPlayerAbilities();
			this.freecamOriginalPlayer.capabilities.setFlySpeed(this.prevFlySpeed);
			this.freecamOriginalPlayer.capabilities.setPlayerWalkSpeed(this.prevWalkSpeed);
			this.freecamOriginalPlayer.noClip = this.prevNoclip;
			this.freecamOriginalPlayer.setFreeCam(false);
			this.freecamOriginalPlayer = null;
		}
		else {
			this.freecamOriginalPlayer = (EntityClientPlayerMP) Minecraft.getMinecraft().renderViewEntity;
			this.prevAllowFlying = this.freecamOriginalPlayer.capabilities.allowFlying;
			this.prevIsFlying = this.freecamOriginalPlayer.capabilities.isFlying;
			this.freecamOriginalPlayer.capabilities.allowFlying = true;
			this.freecamOriginalPlayer.capabilities.isFlying = true;
			this.freecamOriginalPlayer.sendPlayerAbilities();
			this.prevFlySpeed = this.freecamOriginalPlayer.capabilities.getFlySpeed();
			this.prevWalkSpeed = this.freecamOriginalPlayer.capabilities.getWalkSpeed();
			this.freecamOriginalPlayer.capabilities.allowFlying = false;
			this.freecamOriginalPlayer.capabilities.isFlying = false;
			this.freecamOriginalPlayer.capabilities.setFlySpeed(50.0F);
			this.freecamOriginalPlayer.capabilities.setPlayerWalkSpeed(50.0F);
			this.prevNoclip = this.freecamOriginalPlayer.noClip;
            if (this.freecamOriginalPlayer.noClip) this.freecamOriginalPlayer.noClip = false;
            this.freecamOriginalPlayer.motionX = 0;
            this.freecamOriginalPlayer.motionY = 0;
            this.freecamOriginalPlayer.motionZ = 0;
            this.freecamOriginalPlayer.setFreeCam(true);
            Minecraft.getMinecraft().renderViewEntity = new EntityCamera(Minecraft.getMinecraft(), Minecraft.getMinecraft().theWorld, Minecraft.getMinecraft().getSession(), this.freecamOriginalPlayer.getNetHandler(), this.freecamOriginalPlayer.getStatWriter(), this.freecamOriginalPlayer.movementInput);
            Minecraft.getMinecraft().renderViewEntity.setPositionAndRotation(this.freecamOriginalPlayer.posX, this.freecamOriginalPlayer.posY, this.freecamOriginalPlayer.posZ, this.freecamOriginalPlayer.rotationYaw, this.freecamOriginalPlayer.rotationPitch);
		}
	}
	
	
	/**
	 * Toggles a frozen camera
	 */
	public void handleFreezeCam() {
		if (this.freezecamOriginalPlayer != null) {
			Minecraft.getMinecraft().renderViewEntity.setDead();
			Minecraft.getMinecraft().renderViewEntity = this.freezecamOriginalPlayer;
			this.freezecamOriginalPlayer.setFreezeCamera(false);
			this.freezecamOriginalPlayer = null;
		}
		else {
			this.freezecamOriginalPlayer = ((EntityClientPlayerMP) Minecraft.getMinecraft().renderViewEntity);
			EntityCamera camera = new EntityCamera(Minecraft.getMinecraft(), Minecraft.getMinecraft().theWorld, Minecraft.getMinecraft().getSession(), this.freezecamOriginalPlayer.getNetHandler(), this.freezecamOriginalPlayer.getStatWriter(), this.freezecamOriginalPlayer.movementInput);
			camera.setPositionAndRotation(this.freezecamOriginalPlayer.posX, this.freezecamOriginalPlayer.posY, this.freezecamOriginalPlayer.posZ, this.freezecamOriginalPlayer.rotationYaw, this.freezecamOriginalPlayer.rotationPitch);
			camera.setFreezeCamera(0, 0, 0, this.freezecamOriginalPlayer.rotationYaw, this.freezecamOriginalPlayer.rotationPitch);
			this.freezecamOriginalPlayer.setFreezeCamYawAndPitch(this.freezecamOriginalPlayer.rotationYaw, this.freezecamOriginalPlayer.rotationPitch);
			this.freezecamOriginalPlayer.setFreezeCamera(true);
			Minecraft.getMinecraft().renderViewEntity = camera;
		}
	}
	
	/**
	 * Shows the xray config
	 */
	public void handleXray() {
		Xray.getXray().showConfig();
	}
	
	/**
	 * Enables/Disables xray and sets the radius
	 * 
	 * @param xrayEnabled whether to enable or disable xray
	 * @param blockRadius the xray block radius
	 */
	public void handleXray(boolean xrayEnabled, int blockRadius) {
		Xray.getXray().changeXraySettings(blockRadius, xrayEnabled);
	}
	
	/**
	 * loads/saves an xray setting
	 * 
	 * @param load true to load a setting, false to save a setting
	 * @param setting the setting name to load/save
	 */
	public void handleXray(boolean load, String setting) {
		ClientPlayerSettings settings = MoreCommands.getEntityProperties(ClientPlayerSettings.class, PlayerSettings.MORECOMMANDS_IDENTIFIER, Minecraft.getMinecraft().thePlayer);
		if (settings == null) return;
		
		if (load) {
			if (settings.xray.containsKey(setting)) {
				int radius = settings.xray.get(setting).radius;
				Map<Block, Integer> colors = settings.xray.get(setting).colors;
				
				for (Block b : Xray.getXray().getAllBlocks()) Xray.getXray().changeBlockSettings(b, false);
				
				for (Map.Entry<Block, Integer> entry : colors.entrySet())
					Xray.getXray().changeBlockSettings(entry.getKey(), true, new Color(entry.getValue()));
				
				Xray.getXray().changeXraySettings(radius);
				(new CommandSender(Minecraft.getMinecraft().thePlayer)).sendLangfileMessage("command.xray.loaded", setting);
			}
			else (new CommandSender(Minecraft.getMinecraft().thePlayer)).sendLangfileMessage("command.xray.notFound", EnumChatFormatting.RED, setting);
		}
		else {
			Map<Block, Integer> colors = new HashMap<Block, Integer>();
			
			for (Block b : Xray.getXray().getAllBlocks()) {
				if (Xray.getXray().drawBlock(b)) {
					Color c = Xray.getXray().getColor(b);
					colors.put(b, (c.getBlue() << 16) | (c.getGreen() << 8) | c.getRed());
				}
			}
			
			settings.xray.put(setting, new XrayInfo(Xray.getXray().getRadius(), colors));
			(new CommandSender(Minecraft.getMinecraft().thePlayer)).sendLangfileMessage("command.xray.saved", setting);
		}
	}

	/**
	 * Enables/Disables noclip
	 * 
	 * @param allowNoclip whether to enable or disable noclip
	 */
	public void handleNoclip(boolean allowNoclip) {
		Minecraft.getMinecraft().thePlayer.noClip = allowNoclip;
	}
	
	/**
	 * Lightens the world or reverses world lighting
	 */
	public void handleLight() {
		World clientWorld = Minecraft.getMinecraft().thePlayer.worldObj;
		
		if(clientWorld.hashCode() != this.lightenedWorld) {
			this.isEnlightened = false;
		}
			
		if (!this.isEnlightened) {
			float[] lightBrightnessTable = clientWorld.provider.lightBrightnessTable;
			
			for (int i = 0; i < lightBrightnessTable.length; i++) {
				lightBrightnessTable[i] = 1.0F;
			}
				
			this.lightenedWorld = clientWorld.hashCode();
		}
		else {
			clientWorld.provider.registerWorld(clientWorld);
		}
		this.isEnlightened = !this.isEnlightened;
	}

	/**
	 * Sets the block reach distance
	 * 
	 * @param reachDistance the reach distance
	 */
	public void handleReach(float reachDistance) {
		if (Minecraft.getMinecraft().playerController instanceof PlayerControllerMP) {
			((PlayerControllerMP)Minecraft.getMinecraft().playerController).setBlockReachDistance(reachDistance);
		}
	}

	/**
	 * sets the jump height
	 * 
	 * @param gravity the jump height
	 */
	public void setGravity(double gravity) {
		if (Minecraft.getMinecraft().thePlayer instanceof EntityClientPlayerMP) {
			((EntityClientPlayerMP) Minecraft.getMinecraft().thePlayer).setGravity(gravity);
		}
	}

	/**
	 * sets the step height
	 * 
	 * @param stepheight the step height
	 */
	public void setStepheight(float stepheight) {
		Minecraft.getMinecraft().thePlayer.stepHeight = stepheight;
	}
	
	/**
	 * Disables or enables fluid movement
	 * 
	 * @param whether to enable or disable fluid movement handling
	 */
	public void setFluidMovement(boolean fluidmovement) {
		if (Minecraft.getMinecraft().thePlayer instanceof EntityClientPlayerMP) {
			((EntityClientPlayerMP) Minecraft.getMinecraft().thePlayer).setFluidMovement(fluidmovement);
		}
	}
	
	/**
	 * Disables or enables infiniteitems
	 * 
	 * @param infiniteitems to enable or disable infinite items
	 */
	public void setInfiniteitems(boolean infiniteitems) {
		if (infiniteitems)
			EventHandler.ITEMSTACK_CHANGE_SIZE.register(this.onStackSizeChangeListener);
		else
			EventHandler.ITEMSTACK_CHANGE_SIZE.unregister(this.onStackSizeChangeListener);
	}
	
	/**
	 * Disables or enables item damage
	 * 
	 * @param itemdamage whether to enable or disable item damage
	 */
	public void setItemDamage(Item item, boolean itemdamage) {
		if (itemdamage) disableDamage.add(item);
		else disableDamage.remove(item);
	}
	
	/**
	 * sets the compass target
	 * 
	 * @param x the x coordinate of the target
	 * @param z the z coordinate of the target
	 */
	public void setCompassTarget(int x, int z) {
		this.compassTarget = new ChunkCoordinates(x, 0, z);
	}
	
	/**
	 * resets the compass target to default (world spawn point)
	 */
	public void resetCompassTarget() {
		this.compassTarget = null;
	}
	
	/**
	 * Stores the remote (server) world name on the client
	 * 
	 * @param worldName the server world's name
	 */
	public void handleRemoteWorldName(String worldName) {
		((ClientProxy) MoreCommands.getProxy()).setRemoteWorldName(worldName);
	}
	
	/**
	 * Updates a block property
	 * 
	 * @param block the block of which the property should be updated
	 * @param type the property type to update
	 * @param value the new value
	 */
	public void updateBlock(Block block, BlockUpdateType type, int value) {
		type.update(block, value);
	}
	
	/**
	 * handles the result of a remote command execution
	 * 
	 * @param executionID the id of the executed command
	 * @param result the result of the execution
	 */
	public void handleRemoteCommandResult(int executionID, String result) {
		PendingRemoteCommand pendingCommand = null;
		
		synchronized (pendingRemoteCommands) {
			pendingCommand = pendingRemoteCommands.get(executionID);
			pendingRemoteCommands.remove(executionID);
		}
		
		if (pendingCommand != null)
			pendingCommand.callback.setCommandResult(result);
	}
	
	/**
	 * A container class for pending remote command executions
	 * 
	 * @author MrNobody98
	 */
	private static class PendingRemoteCommand {
		private long startTime; //when the command was sent to the server
		private CommandResultCallback callback; //the corresponding result callback
		
		/**
		 * @param startTime the start time (when the command was sent to the server)
		 * @param callback the corresponding result callback
		 */
		public PendingRemoteCommand(long startTime, CommandResultCallback callback) {
			this.startTime = startTime; this.callback = callback;
		}
	}
	
	/**
	 * A callback for the result of a remote command execution
	 * 
	 * @author MrNobody98
	 */
	public static interface CommandResultCallback {
		/**
		 * Sets the result of the remote command execution
		 * 
		 * @param result the result
		 */
		void setCommandResult(String result);
		
		/**
		 * Invoked when the remote command execution timed out
		 */
		void timeout();
	}
}
