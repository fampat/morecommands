package com.mrnobody.morecommands.network;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mrnobody.morecommands.command.ClientCommand;
import com.mrnobody.morecommands.core.AppliedPatches;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.event.DamageItemEvent;
import com.mrnobody.morecommands.event.EventHandler;
import com.mrnobody.morecommands.event.ItemStackChangeSizeEvent;
import com.mrnobody.morecommands.event.Listeners.EventListener;
import com.mrnobody.morecommands.network.PacketDispatcher.BlockUpdateType;
import com.mrnobody.morecommands.patch.EntityPlayerSP;
import com.mrnobody.morecommands.patch.PlayerControllerMP;
import com.mrnobody.morecommands.util.ClientPlayerSettings;
import com.mrnobody.morecommands.util.ClientPlayerSettings.XrayInfo;
import com.mrnobody.morecommands.util.GlobalSettings;
import com.mrnobody.morecommands.util.PlayerSettings;
import com.mrnobody.morecommands.util.Reference;
import com.mrnobody.morecommands.util.Xray;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.EntityCamera;
import com.mrnobody.morecommands.wrapper.World;

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
		Items.compass.addPropertyOverride(new ResourceLocation("angle"), new CompassAngleProperty());
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
	 * certain time, they are still executed
	 * 
	 * @param socketAddress the server's socket address
	 */
	public static void runStartupThread(final String socketAddress) {
		final int timeout = GlobalSettings.startupTimeout < 0 ? 10000 : GlobalSettings.startupTimeout > 10 ? 10000 : GlobalSettings.startupTimeout * 1000;
		
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
				
				//Execute commands specified in the startup_multiplayer.cfg
				for (String command : MoreCommands.INSTANCE.getStartupCommandsMultiplayer(socketAddress)) {
					if (ClientCommandHandler.instance.executeCommand(Minecraft.getMinecraft().thePlayer, command) == 0)
						Minecraft.getMinecraft().thePlayer.sendChatMessage(command.startsWith("/") ? command : "/" + command);
					MoreCommands.INSTANCE.getLogger().info("Executed startup command '" + command + "'");
				}
			}
		}, "MoreCommands Startup Commands Thread").start();
	}
	
	/**
	 * Executes the startup commands that shall be executed on server startup.
	 * This method is invoked only for the integrated server, not for dedicated servers.
	 */
	public static void executeStartupCommands() {
		notifyPlayerAboutUpdate();
		
		for (String command : MoreCommands.INSTANCE.getStartupCommands()) {
			if (ClientCommandHandler.instance.executeCommand(Minecraft.getMinecraft().thePlayer, command) == 0)
				Minecraft.getMinecraft().thePlayer.sendChatMessage(command.startsWith("/") ? command : "/" + command);
			MoreCommands.INSTANCE.getLogger().info("Executed startup command '" + command + "'");
		}
	}
	
	/**
	 * Notifies the player that a MoreCommands update is available with a chat message
	 */
	private static void notifyPlayerAboutUpdate() {
		if (MoreCommands.getProxy().getUpdateText() != null && !MoreCommands.getProxy().wasPlayerNotified())
			Minecraft.getMinecraft().thePlayer.addChatMessage(MoreCommands.getProxy().getUpdateText());
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
		if (MoreCommands.getServerType() == ServerType.INTEGRATED) PacketHandlerClient.executeStartupCommands();
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
		World clientWorld = new World(Minecraft.getMinecraft().thePlayer.worldObj);
			
		if(clientWorld.getMinecraftWorld().hashCode() != this.lightenedWorld) {
			this.isEnlightened = false;
		}
			
		if (!this.isEnlightened) {
			float[] lightBrightnessTable = clientWorld.getMinecraftWorld().provider.getLightBrightnessTable();
			
			for (int i = 0; i < lightBrightnessTable.length; i++) {
				lightBrightnessTable[i] = 1.0F;
			}
				
			this.lightenedWorld = clientWorld.getMinecraftWorld().hashCode();
		}
		else {
			clientWorld.getMinecraftWorld().provider.registerWorld(clientWorld.getMinecraftWorld());
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
	 * Stores the server's world name on the client
	 * @param worldName the server world's name
	 */
	public void changeWorld(String worldName) {
		MoreCommands.getProxy().setCurrentWorld(worldName);
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
	 * This is basically a copy of net.minecraft.item.ItemCompass$1. This
	 * {@link IItemPropertyGetter} determines the angle of the compass needle
	 * This class is used to override the default target of a compass which
	 * is the spawn point.
	 * 
	 * @author MrNobody98
	 */
	private final class CompassAngleProperty implements IItemPropertyGetter {
		@SideOnly(Side.CLIENT) private double field_185095_a;
		@SideOnly(Side.CLIENT) private double field_185096_b;
		@SideOnly(Side.CLIENT) private long field_185097_c;
		
		@SideOnly(Side.CLIENT)
		public float apply(ItemStack stack, net.minecraft.world.World worldIn, EntityLivingBase entityIn) {
			if (entityIn == null && !stack.isOnItemFrame()) return 0.0F;
			else {
				boolean flag = entityIn != null;
				Entity entity = (Entity)(flag ? entityIn : stack.getItemFrame());
				if (worldIn == null) worldIn = entity.worldObj;
				double d0;
				
				if (worldIn.provider.isSurfaceWorld()) {
					double d1 = flag ? (double)entity.rotationYaw : this.func_185094_a((EntityItemFrame) entity);
					d1 = d1 % 360.0D;
					double d2 = this.func_185092_a(worldIn, entity);
					d0 = Math.PI - ((d1 - 90.0D) * 0.01745329238474369D - d2);
				}
				else d0 = Math.random() * (Math.PI * 2D);
				
				if (flag) d0 = this.func_185093_a(worldIn, d0);
				float f = (float)(d0 / (Math.PI * 2D));
				return MathHelper.func_188207_b(f, 1.0F);
			}
		}
		
		@SideOnly(Side.CLIENT)
		private double func_185093_a(net.minecraft.world.World p_185093_1_, double p_185093_2_) {
			if (p_185093_1_.getTotalWorldTime() != this.field_185097_c) {
				this.field_185097_c = p_185093_1_.getTotalWorldTime();
				double d0 = p_185093_2_ - this.field_185095_a;
				d0 = d0 % (Math.PI * 2D);
				d0 = MathHelper.clamp_double(d0, -1.0D, 1.0D);
				this.field_185096_b += d0 * 0.1D;
				this.field_185096_b *= 0.8D;
				this.field_185095_a += this.field_185096_b;
			}
			
			return this.field_185095_a;
		}
		
		@SideOnly(Side.CLIENT)
		private double func_185094_a(EntityItemFrame p_185094_1_) {
			return (double) MathHelper.clampAngle(180 + p_185094_1_.facingDirection.getHorizontalIndex() * 90);
		}
		
		@SideOnly(Side.CLIENT)
		private double func_185092_a(net.minecraft.world.World p_185092_1_, Entity p_185092_2_) {
			BlockPos blockpos = PacketHandlerClient.this.compassTarget == null ? p_185092_1_.getSpawnPoint() : PacketHandlerClient.this.compassTarget;
			return Math.atan2((double)blockpos.getZ() - p_185092_2_.posZ, (double)blockpos.getX() - p_185092_2_.posX);
		}
	};
}
