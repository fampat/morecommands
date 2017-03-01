package com.mrnobody.morecommands.network;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mrnobody.morecommands.command.ClientCommand;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.core.AppliedPatches;
import com.mrnobody.morecommands.core.ClientProxy;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.event.DamageItemEvent;
import com.mrnobody.morecommands.event.EventHandler;
import com.mrnobody.morecommands.event.ItemStackChangeSizeEvent;
import com.mrnobody.morecommands.event.Listeners.EventListener;
import com.mrnobody.morecommands.network.PacketDispatcher.BlockUpdateType;
import com.mrnobody.morecommands.patch.EntityPlayerSP;
import com.mrnobody.morecommands.patch.PlayerControllerMP;
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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.init.Items;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * This class handles all incoming packets from the server
 * 
 * @author MrNobody98
 *
 */
public class PacketHandlerClient {
	/** These commands are those that get removed when the server has MoreCommands installed to execute the server side versions of them */
	private static final List<ClientCommand<?>> removedCmds = new ArrayList<ClientCommand<?>>();
	/** These commands are currently processed server side and waited for a response */
	private static final TIntObjectMap<PendingRemoteCommand> pendingRemoteCommands = new TIntObjectHashMap<PendingRemoteCommand>();
	/** A counter for pending remote commands */
	private static int pendingRemoteCommandsID = 0;
	
	private EntityPlayerSP freecamOriginalPlayer;
	private boolean prevIsFlying;
	private boolean prevAllowFlying;
	private float prevFlySpeed;
	private float prevWalkSpeed;
	private boolean prevNoclip;
	
	private EntityPlayerSP freezecamOriginalPlayer;
	
	private boolean isEnlightened = false;
	private int lightenedWorld = 0;
	
	private final EventListener<ItemStackChangeSizeEvent> onStackSizeChangeListener = new EventListener<ItemStackChangeSizeEvent>() {
		@Override
		public void onEvent(ItemStackChangeSizeEvent event) {
			if (event.getEntityPlayer() instanceof net.minecraft.client.entity.EntityPlayerSP) {
				event.newSize = event.oldSize; event.setCanceled(true);
			}
		}
	};
	
	private static final Set<Item> disableDamage = new HashSet<Item>();
	private final EventListener<DamageItemEvent> onItemDamageListener = new EventListener<DamageItemEvent>() {
		@Override
		public void onEvent(DamageItemEvent event) {
			if (event.getEntityLiving() instanceof net.minecraft.client.entity.EntityPlayerSP && disableDamage.contains(event.stack.getItem()))
				event.setCanceled(true);
		}
	};
	
	private BlockPos compassTarget = null;
	
	public PacketHandlerClient() {
		Items.COMPASS.addPropertyOverride(new ResourceLocation("angle"), new CompassAngleProperty());
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
				
				while (t > System.currentTimeMillis()) {
					if (!added && AppliedPatches.serverModded()) {t = System.currentTimeMillis() + timeout; added = true;}
					if (AppliedPatches.handshakeFinished()) break;
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
	 * @param version the server's MorCommands version
	 */
	public void handshake(String version) {
		if (!Reference.VERSION.equals(version)) {
			MoreCommands.INSTANCE.getLogger().warn("Server has incompatible MoreCommands version: " + version + ", version " + Reference.VERSION + " is required");
			return;
		}
		
		AppliedPatches.setServerModded(true);
		
		//Remove commands, which shall be removed if the server side version shall be used
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
			
		//Let the server know that the mod is installed client side
		MoreCommands.INSTANCE.getLogger().info("Sending client handshake");
		MoreCommands.INSTANCE.getPacketDispatcher().sendC00Handshake(
				Minecraft.getMinecraft().thePlayer instanceof EntityPlayerSP,
				Minecraft.getMinecraft().renderGlobal instanceof com.mrnobody.morecommands.patch.RenderGlobal);
	}
	
	/**
	 * Called when the handshake is finished
	 */
	public void handshakeFinished() {
		AppliedPatches.setHandshakeFinished(true);
		MoreCommands.INSTANCE.getLogger().info("Handshake finished");
	}
	
	/**
	 * Enables/Disables climbing on every wall
	 * @param climb whether to enable or disable climb mode
	 */
	public void handleClimb(boolean climb) {
		if (Minecraft.getMinecraft().thePlayer instanceof EntityPlayerSP) {
			((EntityPlayerSP) Minecraft.getMinecraft().thePlayer).setOverrideOnLadder(climb);
		}
	}
	
	/**
	 * Toggles freecaming around the map
	 */
	public void handleFreecam() {
		if (this.freecamOriginalPlayer != null) {
			Minecraft.getMinecraft().setRenderViewEntity(this.freecamOriginalPlayer);
			this.freecamOriginalPlayer.capabilities.allowFlying = this.prevAllowFlying;
			this.freecamOriginalPlayer.capabilities.isFlying = this.prevIsFlying;
			this.freecamOriginalPlayer.sendPlayerAbilities();
			this.freecamOriginalPlayer.setOverrideNoclip(this.prevNoclip);
			this.freecamOriginalPlayer.setFreeCam(false);
			this.freecamOriginalPlayer = null;
		}
		else if (Minecraft.getMinecraft().thePlayer instanceof EntityPlayerSP) {
			this.freecamOriginalPlayer = (EntityPlayerSP) Minecraft.getMinecraft().thePlayer;
			this.prevAllowFlying = this.freecamOriginalPlayer.capabilities.allowFlying;
			this.prevIsFlying = this.freecamOriginalPlayer.capabilities.isFlying;
			this.freecamOriginalPlayer.capabilities.allowFlying = true;
			this.freecamOriginalPlayer.capabilities.isFlying = true;
			this.freecamOriginalPlayer.sendPlayerAbilities();
			this.freecamOriginalPlayer.capabilities.allowFlying = false;
			this.freecamOriginalPlayer.capabilities.isFlying = false;
			this.prevNoclip = this.freecamOriginalPlayer.getOverrideNoclip();
            if (this.freecamOriginalPlayer.getOverrideNoclip()) this.freecamOriginalPlayer.setOverrideNoclip(false);
            this.freecamOriginalPlayer.motionX = 0;
            this.freecamOriginalPlayer.motionY = 0;
            this.freecamOriginalPlayer.motionZ = 0;
            this.freecamOriginalPlayer.setFreeCam(true);
            Minecraft.getMinecraft().setRenderViewEntity(new EntityCamera(Minecraft.getMinecraft(), Minecraft.getMinecraft().theWorld, this.freecamOriginalPlayer.getHandler(), this.freecamOriginalPlayer.getWriter(), this.freecamOriginalPlayer.movementInput));
            Minecraft.getMinecraft().getRenderViewEntity().setPositionAndRotation(this.freecamOriginalPlayer.posX, this.freecamOriginalPlayer.posY, this.freecamOriginalPlayer.posZ, this.freecamOriginalPlayer.rotationYaw, this.freecamOriginalPlayer.rotationPitch);
		}
	}
	
	/**
	 * Toggles a frozen camera
	 */
	public void handleFreezeCam() {
		if (this.freezecamOriginalPlayer != null) {
			Minecraft.getMinecraft().getRenderViewEntity().setDead();
			Minecraft.getMinecraft().setRenderViewEntity(this.freezecamOriginalPlayer);
			this.freezecamOriginalPlayer.setFreezeCamera(false);
			this.freezecamOriginalPlayer = null;
		}
		else if (Minecraft.getMinecraft().thePlayer instanceof EntityPlayerSP) {
			this.freezecamOriginalPlayer = (EntityPlayerSP) Minecraft.getMinecraft().thePlayer;
			EntityCamera camera = new EntityCamera(Minecraft.getMinecraft(), Minecraft.getMinecraft().theWorld, this.freezecamOriginalPlayer.getHandler(), this.freezecamOriginalPlayer.getWriter(), this.freezecamOriginalPlayer.movementInput);
			camera.setPositionAndRotation(this.freezecamOriginalPlayer.posX, this.freezecamOriginalPlayer.posY, this.freezecamOriginalPlayer.posZ, this.freezecamOriginalPlayer.rotationYaw, this.freezecamOriginalPlayer.rotationPitch);
			camera.setFreezeCamera(0, 0, 0, this.freezecamOriginalPlayer.rotationYaw, this.freezecamOriginalPlayer.rotationPitch);
			this.freezecamOriginalPlayer.setFreezeCamYawAndPitch(this.freezecamOriginalPlayer.rotationYaw, this.freezecamOriginalPlayer.rotationPitch);
			this.freezecamOriginalPlayer.setFreezeCamera(true);
			Minecraft.getMinecraft().setRenderViewEntity(camera);
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
	 * @param xrayEnabled whether to enable or disable xray
	 * @param blockRadius the xray block radius
	 */
	public void handleXray(boolean xrayEnabled, int blockRadius) {
		Xray.getXray().changeXraySettings(blockRadius, xrayEnabled);
	}
	
	/**
	 * loads/saves an xray setting
	 * @param load true to load a setting, false to save a setting
	 * @param setting the setting name to load/save
	 */
	public void handleXray(boolean load, String setting) {
		ClientPlayerSettings settings = Minecraft.getMinecraft().thePlayer.getCapability(PlayerSettings.SETTINGS_CAP_CLIENT, null);
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
			else (new CommandSender(Minecraft.getMinecraft().thePlayer)).sendLangfileMessage("command.xray.notFound", TextFormatting.RED, setting);
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
	 * @param allowNoclip whether to enable or disable noclip
	 */
	public void handleNoclip(boolean allowNoclip) {
		if (!(Minecraft.getMinecraft().thePlayer instanceof EntityPlayerSP)) return;
		Minecraft.getMinecraft().thePlayer.noClip = allowNoclip;
		((EntityPlayerSP) Minecraft.getMinecraft().thePlayer).setOverrideNoclip(allowNoclip);
	}
	
	/**
	 * Lightens the world or reverses world lighting
	 */
	public void handleLight() {
		World clientWorld = Minecraft.getMinecraft().thePlayer.worldObj;
			
		if (clientWorld.hashCode() != this.lightenedWorld) {
			this.isEnlightened = false;
		}
			
		if (!this.isEnlightened) {
			float[] lightBrightnessTable = clientWorld.provider.getLightBrightnessTable();
			
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
	 * @param reachDistance the reach distance
	 */
	public void handleReach(float reachDistance) {
		if (Minecraft.getMinecraft().playerController instanceof PlayerControllerMP) {
			((PlayerControllerMP)Minecraft.getMinecraft().playerController).setBlockReachDistance(reachDistance);
		}
	}

	/**
	 * sets the jump height
	 * @param gravity the jump height
	 */
	public void setGravity(float gravity) {
		if (Minecraft.getMinecraft().thePlayer instanceof EntityPlayerSP) {
			((EntityPlayerSP) Minecraft.getMinecraft().thePlayer).setGravity(gravity);
		}
	}

	/**
	 * sets the step height
	 * @param stepheight the step height
	 */
	public void setStepheight(float stepheight) {
		Minecraft.getMinecraft().thePlayer.stepHeight = stepheight;
	}
	
	/**
	 * Disables or enables fluid movement
	 * @param whether to enable or disable fluid movement handling
	 */
	public void setFluidMovement(boolean fluidmovement) {
		if (Minecraft.getMinecraft().thePlayer instanceof EntityPlayerSP) {
			((EntityPlayerSP) Minecraft.getMinecraft().thePlayer).setFluidMovement(fluidmovement);
		}
	}
	
	/**
	 * Disables or enables infiniteitems
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
	 * @param itemdamage whether to enable or disable item damage
	 */
	public void setItemDamage(Item item, boolean itemdamage) {
		if (itemdamage) disableDamage.add(item);
		else disableDamage.remove(item);
	}
	
	/**
	 * sets the compass target
	 * @param x the x coordinate of the target
	 * @param z the z coordinate of the target
	 */
	public void setCompassTarget(int x, int z) {
		this.compassTarget = new BlockPos(x, 0, z);
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
	 * This is basically a copy of net.minecraft.item.ItemCompass$1. This
	 * {@link IItemPropertyGetter} determines the angle of the compass needle
	 * This class is used to override the default target of a compass which
	 * is the spawn point.
	 * 
	 * @author MrNobody98
	 */
	private final class CompassAngleProperty implements IItemPropertyGetter {
        @SideOnly(Side.CLIENT) double rotation;
        @SideOnly(Side.CLIENT) double rota;
        @SideOnly(Side.CLIENT) long lastUpdateTick;
        
        @SideOnly(Side.CLIENT)
		public float apply(ItemStack stack, World worldIn, EntityLivingBase entityIn) {
        	 if (entityIn == null && !stack.isOnItemFrame()) return 0.0F;
             else {
                 boolean flag = entityIn != null;
                 Entity entity = (Entity)(flag ? entityIn : stack.getItemFrame());

                 if (worldIn == null) worldIn = entity.worldObj;
                 double d0;

                 if (worldIn.provider.isSurfaceWorld()) {
                	 double d1 = flag ? (double)entity.rotationYaw : this.getFrameRotation((EntityItemFrame)entity);
                     d1 = MathHelper.func_191273_b(d1 / 360.0D, 1.0D);
                     double d2 = this.getSpawnToAngle(worldIn, entity) / (Math.PI * 2D);
                     d0 = 0.5D - (d1 - 0.25D - d2);
                 }
                 else  d0 = Math.random();

                 if (flag)
                	 d0 = this.wobble(worldIn, d0);
                 
                 return MathHelper.positiveModulo((float)d0, 1.0F);
             }
		}
        
        @SideOnly(Side.CLIENT)
        private double wobble(World worldIn, double p_185093_2_) {
            if (worldIn.getTotalWorldTime() != this.lastUpdateTick) {
                this.lastUpdateTick = worldIn.getTotalWorldTime();
                double d0 = p_185093_2_ - this.rotation;
                d0 = MathHelper.func_191273_b(d0 + 0.5D, 1.0D) - 0.5D;
                this.rota += d0 * 0.1D;
                this.rota *= 0.8D;
                this.rotation = MathHelper.func_191273_b(this.rotation + this.rota, 1.0D);
            }

            return this.rotation;
        }
		
        @SideOnly(Side.CLIENT)
        private double getFrameRotation(EntityItemFrame p_185094_1_) {
        	return (double)MathHelper.clampAngle(180 + p_185094_1_.facingDirection.getHorizontalIndex() * 90);
        }
        
        @SideOnly(Side.CLIENT)
        private double getSpawnToAngle(World p_185092_1_, Entity p_185092_2_) {
            BlockPos blockpos = PacketHandlerClient.this.compassTarget == null ? p_185092_1_.getSpawnPoint() : PacketHandlerClient.this.compassTarget;
            return Math.atan2((double)blockpos.getZ() - p_185092_2_.posZ, (double)blockpos.getX() - p_185092_2_.posX);
        }
	};
	

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
