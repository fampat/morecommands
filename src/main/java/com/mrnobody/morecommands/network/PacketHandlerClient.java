package com.mrnobody.morecommands.network;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import com.mrnobody.morecommands.command.ClientCommand;
import com.mrnobody.morecommands.core.ClientProxy;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.core.Patcher;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listener;
import com.mrnobody.morecommands.packet.client.C01PacketClientCommand;
import com.mrnobody.morecommands.patch.EntityClientPlayerMP;
import com.mrnobody.morecommands.patch.PlayerControllerMP;
import com.mrnobody.morecommands.util.XrayClientTick;
import com.mrnobody.morecommands.util.Reference;
import com.mrnobody.morecommands.util.XrayRenderTick;
import com.mrnobody.morecommands.util.XrayConfGui;
import com.mrnobody.morecommands.util.XrayHelper;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.EntityCamera;
import com.mrnobody.morecommands.wrapper.Player;
import com.mrnobody.morecommands.wrapper.World;

import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.registry.GameData;

/**
 * This class handles all incoming packets from the server
 * 
 * @author MrNobody98
 *
 */
public class PacketHandlerClient {
	public static final PacketHandlerClient INSTANCE = new PacketHandlerClient();
	public static List<ClientCommand> removedCmds = new ArrayList<ClientCommand>();
	
	//Data for the freecam command
	public EntityClientPlayerMP freecamOriginalPlayer;
	public boolean prevIsFlying;
	public boolean prevAllowFlying;
	public float prevFlySpeed;
	public  float prevWalkSpeed;
	public boolean prevNoclip;
	
	//Data for the freezecam command
	public EntityClientPlayerMP freezecamOriginalPlayer;
	
	//Data for the light command
	public boolean isEnlightened = false;
	public int lightenedWorld = 0;
	
	//The xray helper singleton, which will be set from the client proxy
	public XrayHelper xrayHelper;
	
	/**
	 * Is called if the client receives a handshake packet
	 */
	public void handshake(UUID uuid) {
		MoreCommands.setPlayerUUID(uuid);
		Patcher.setServerModded(true);
		
		//Remove commands, which shall be removed if the server side version shall be used
		List<String> remove = new ArrayList<String>();
		for (Object command : ClientCommandHandler.instance.getCommands().keySet()) {
			if (ClientCommandHandler.instance.getCommands().get((String) command) instanceof ClientCommand) {
				ClientCommand cmd = (ClientCommand) ClientCommandHandler.instance.getCommands().get((String) command);
				if (!cmd.registerIfServerModded()) {remove.add(cmd.getCommandName()); PacketHandlerClient.removedCmds.add(cmd);}
			}
		}
		
		for (String rem : remove) {
			ClientCommandHandler.instance.getCommands().remove(rem);
			MoreCommands.getLogger().info("Unregistered client command '" + rem + "' because server side version of this command is used");
		}
		
		//Let the server know that the mod is installed client side
		MoreCommands.getLogger().info("Sending client handshake");
		
		if (MoreCommands.getProxy() instanceof ClientProxy) {
			((ClientProxy) MoreCommands.getProxy()).sendHandshake(uuid);
		}
		else {
			MoreCommands.getLogger().info("Couldn't send handshake, the proxy is not the client proxy. This shouldn't happend");
		}
		
		//Execute commands specified in the startup.cfg
		try {
			File startup = new File(Reference.getModDir(), "startup.cfg");
			if (!startup.exists() || !startup.isFile()) startup.createNewFile();
			
			BufferedReader br = new BufferedReader(new FileReader(startup));
			String line;
			while ((line = br.readLine()) != null) {
				if (ClientCommandHandler.instance.executeCommand(Minecraft.getMinecraft().thePlayer, line) == 0)
					Minecraft.getMinecraft().thePlayer.sendChatMessage(line.startsWith("/") ? line : "/" + line);
				MoreCommands.getLogger().info("Executed startup command '" + line + "'");
			}
			br.close();
		}
		catch (IOException ex) {ex.printStackTrace(); MoreCommands.getLogger().info("Startup commands couldn't be executed");}
	}
	
	/**
	 * Enables climbing on every wall
	 */
	public void handleClimb(boolean climb) {
		if (Minecraft.getMinecraft().thePlayer instanceof EntityClientPlayerMP) {
			((EntityClientPlayerMP) Minecraft.getMinecraft().thePlayer).OverrideOnLadder(climb);
		}
	}
	
	/**
	 * Enables/Disables to freecam around the map
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
            Minecraft.getMinecraft().renderViewEntity = new EntityCamera(Minecraft.getMinecraft(), Minecraft.getMinecraft().theWorld, Minecraft.getMinecraft().getSession(), this.freecamOriginalPlayer.getHandler(), this.freecamOriginalPlayer.getWriter(), this.freecamOriginalPlayer.movementInput);
            Minecraft.getMinecraft().renderViewEntity.setPositionAndRotation(this.freecamOriginalPlayer.posX, this.freecamOriginalPlayer.posY, this.freecamOriginalPlayer.posZ, this.freecamOriginalPlayer.rotationYaw, this.freecamOriginalPlayer.rotationPitch);
		}
	}
	
	
	/**
	 * Enables/Disables a frozen camera
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
			EntityCamera camera = new EntityCamera(Minecraft.getMinecraft(), Minecraft.getMinecraft().theWorld, Minecraft.getMinecraft().getSession(), this.freezecamOriginalPlayer.getHandler(), this.freezecamOriginalPlayer.getWriter(), this.freezecamOriginalPlayer.movementInput);
			camera.setPositionAndRotation(this.freezecamOriginalPlayer.posX, this.freezecamOriginalPlayer.posY, this.freezecamOriginalPlayer.posZ, this.freezecamOriginalPlayer.rotationYaw, this.freezecamOriginalPlayer.rotationPitch);
			camera.setFreezeCamera(0, 0, 0, this.freezecamOriginalPlayer.rotationYaw, this.freezecamOriginalPlayer.rotationPitch);
			this.freezecamOriginalPlayer.setFreezeCamYawAndPitch(this.freezecamOriginalPlayer.rotationYaw, this.freezecamOriginalPlayer.rotationPitch);
			this.freezecamOriginalPlayer.setFreezeCamera(true);
			Minecraft.getMinecraft().renderViewEntity = camera;
		}
	}

	
	/**
	 * Enables/Disables xray
	 */
	public void handleXray(boolean showConfig, boolean xrayEnabled, int blockRadius) {
		if (showConfig) this.xrayHelper.showConfig();
		else this.xrayHelper.changeSettings(blockRadius, xrayEnabled);
	}

	/**
	 * Enables/Disables noclip
	 */
	public void handleNoclip(boolean allowNoclip) {
		Minecraft.getMinecraft().thePlayer.noClip = allowNoclip;
	}
	
	/**
	 * Lightens the world or reverses lighting
	 */
	public void handleLight() {
		World clientWorld = new World(Minecraft.getMinecraft().thePlayer.worldObj);
			
		if(clientWorld.getMinecraftWorld().hashCode() != this.lightenedWorld) {
			this.isEnlightened = false;
		}
			
		if (!this.isEnlightened) {
			float[] lightBrightnessTable = clientWorld.getMinecraftWorld().provider.lightBrightnessTable;
			
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
	 */
	public void handleReach(float reachDistance) {
		if (Minecraft.getMinecraft().playerController instanceof PlayerControllerMP) {
			((PlayerControllerMP)Minecraft.getMinecraft().playerController).setBlockReachDistance(reachDistance);
		}
	}

	/**
	 * Executes a client command
	 */
	public void executeClientCommand(String command) {
		ClientCommandHandler.instance.executeCommand(Minecraft.getMinecraft().thePlayer, command);
	}

	/**
	 * sends all client commands to the server (e.g. for key bindings)
	 */
	public void sendClientCommands() {
		for (Object command : ClientCommandHandler.instance.getCommands().values()) {
			if (command instanceof ClientCommand) {
				C01PacketClientCommand packet = new C01PacketClientCommand();
				packet.playerUUID = MoreCommands.getPlayerUUID();
				packet.command = ((ClientCommand) command).getCommandName();
				MoreCommands.getNetwork().sendToServer(packet);
			}
		}
	}

	/**
	 * sets the jump height
	 */
	public void setGravity(double gravity) {
		if (Minecraft.getMinecraft().thePlayer instanceof EntityClientPlayerMP) {
			((EntityClientPlayerMP) Minecraft.getMinecraft().thePlayer).setGravity(gravity);
		}
	}

	/**
	 * sets the step height
	 */
	public void setStepheight(float stepheight) {
		Minecraft.getMinecraft().thePlayer.stepHeight = stepheight;
	}

	/**
	 * mounts/dismounts an entity
	 */
	public void ride() {
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		CommandSender sender = new CommandSender(player);
		Entity hit = (new Player(player)).traceEntity(128.0D);
		
		if (player.ridingEntity != null) {
			sender.sendLangfileMessageToPlayer("command.ride.dismounted", new Object[0]);
			player.mountEntity(null);
			return;
		}
		
		if (hit != null) {
			if (hit instanceof EntityLiving) {
				player.mountEntity(hit);
				sender.sendLangfileMessageToPlayer("command.ride.mounted", new Object[0]);
			}
			else sender.sendLangfileMessageToPlayer("command.ride.notLiving", new Object[0]);
		}
		else sender.sendLangfileMessageToPlayer("command.ride.notFound", new Object[0]);
	}
}
