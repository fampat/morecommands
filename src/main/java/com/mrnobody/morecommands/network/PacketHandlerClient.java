package com.mrnobody.morecommands.network;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.ClientCommandHandler;

import com.mrnobody.morecommands.command.ClientCommand;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.core.Patcher;
import com.mrnobody.morecommands.patch.EntityPlayerSP;
import com.mrnobody.morecommands.patch.PlayerControllerMP;
import com.mrnobody.morecommands.util.Reference;
import com.mrnobody.morecommands.util.XrayHelper;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.EntityCamera;
import com.mrnobody.morecommands.wrapper.World;

/**
 * This class handles all incoming packets from the server
 * 
 * @author MrNobody98
 *
 */
public class PacketHandlerClient {
	public static List<ClientCommand> removedCmds = new ArrayList<ClientCommand>();
	
	//Data for the freecam command
	public EntityPlayerSP freecamOriginalPlayer;
	public boolean prevIsFlying;
	public boolean prevAllowFlying;
	public float prevFlySpeed;
	public  float prevWalkSpeed;
	public boolean prevNoclip;
	
	//Data for the freezecam command
	public EntityPlayerSP freezecamOriginalPlayer;
	
	//Data for the light command
	public boolean isEnlightened = false;
	public int lightenedWorld = 0;
	
	/**
	 * Is called if the client receives a handshake packet
	 */
	public void handshake(UUID uuid) {
		MoreCommands.getMoreCommands().setPlayerUUID(uuid);
		Patcher.setServerModded(true);
		
		//Remove commands, which shall be removed if the server side version shall be used
		List<String> remove = new ArrayList<String>();
		for (Object command : ClientCommandHandler.instance.getCommands().keySet()) {
			if (ClientCommandHandler.instance.getCommands().get((String) command) instanceof ClientCommand) {
				ClientCommand cmd = (ClientCommand) ClientCommandHandler.instance.getCommands().get((String) command);
				if (!cmd.registerIfServerModded()) {remove.add(cmd.getName()); PacketHandlerClient.removedCmds.add(cmd);}
			}
		}
		
		for (String rem : remove) {
			ClientCommandHandler.instance.getCommands().remove(rem);
			MoreCommands.getMoreCommands().getLogger().info("Unregistered client command '" + rem + "' because server side version of this command is used");
		}
		
		//Let the server know that the mod is installed client side
		MoreCommands.getMoreCommands().getLogger().info("Sending client handshake");
		MoreCommands.getMoreCommands().getPacketDispatcher().sendC00Handshake(
				Minecraft.getMinecraft().thePlayer instanceof EntityPlayerSP,
				Minecraft.getMinecraft().renderGlobal instanceof com.mrnobody.morecommands.patch.RenderGlobal);
		
		//Execute commands specified in the startup.cfg
		try {
			File startup = new File(Reference.getModDir(), "startup.cfg");
			if (!startup.exists() || !startup.isFile()) startup.createNewFile();
			
			BufferedReader br = new BufferedReader(new FileReader(startup));
			String line;
			while ((line = br.readLine()) != null) {
				if (ClientCommandHandler.instance.executeCommand(Minecraft.getMinecraft().thePlayer, line) == 0)
					Minecraft.getMinecraft().thePlayer.sendChatMessage(line.startsWith("/") ? line : "/" + line);
				MoreCommands.getMoreCommands().getLogger().info("Executed startup command '" + line + "'");
			}
			br.close();
		}
		catch (IOException ex) {ex.printStackTrace(); MoreCommands.getMoreCommands().getLogger().info("Startup commands couldn't be executed");}
	}
	
	/**
	 * Enables climbing on every wall
	 */
	public void handleClimb(boolean climb) {
		if (Minecraft.getMinecraft().thePlayer instanceof EntityPlayerSP) {
			((EntityPlayerSP) Minecraft.getMinecraft().thePlayer).OverrideOnLadder(climb);
		}
	}
	
	/**
	 * Enables/Disables to freecam around the map
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
	 * Enables/Disables a frozen camera
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
	 * Enables/Disables xray
	 */
	public void handleXray(boolean showConfig, boolean xrayEnabled, int blockRadius) {
		if (showConfig) XrayHelper.getInstance().showConfig();
		else XrayHelper.getInstance().changeSettings(blockRadius, xrayEnabled);
	}

	/**
	 * Enables/Disables noclip
	 */
	public void handleNoclip(boolean allowNoclip) {
		if (Minecraft.getMinecraft().thePlayer instanceof EntityPlayerSP) 
			((EntityPlayerSP) Minecraft.getMinecraft().thePlayer).setOverrideNoclip(allowNoclip);
	}

	/**
	 * Lightens the world or reverses lighting
	 */
	public void handleLight() {
		World clientWorld = new World(Minecraft.getMinecraft().thePlayer.worldObj);
			
		if(clientWorld.getMinecraftWorld().hashCode() != lightenedWorld) {
			isEnlightened = false;
		}
			
		if (!isEnlightened) {
			float[] lightBrightnessTable = clientWorld.getMinecraftWorld().provider.getLightBrightnessTable();
			
			for (int i = 0; i < lightBrightnessTable.length; i++) {
				lightBrightnessTable[i] = 1.0F;
			}
				
			lightenedWorld = clientWorld.getMinecraftWorld().hashCode();
		}
		else {
			clientWorld.getMinecraftWorld().provider.registerWorld(clientWorld.getMinecraftWorld());
		}
		isEnlightened = !isEnlightened;
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
				MoreCommands.getMoreCommands().getPacketDispatcher().sendC01ClientCommand(((ClientCommand) command).getName());
			}
		}
	}

	/**
	 * sets the jump height
	 */
	public void setGravity(double gravity) {
		if (Minecraft.getMinecraft().thePlayer instanceof EntityPlayerSP) {
			((EntityPlayerSP) Minecraft.getMinecraft().thePlayer).setGravity(gravity);
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
		Entity hit = (new com.mrnobody.morecommands.wrapper.Entity(player)).traceEntity(128.0D);
		
		if (player.ridingEntity != null) {
			sender.sendLangfileMessage("command.ride.dismounted", new Object[0]);
			player.mountEntity(null);
			return;
		}
		
		if (hit != null) {
			if (hit instanceof EntityLiving) {
				player.mountEntity(hit);
				sender.sendLangfileMessage("command.ride.mounted", new Object[0]);
			}
			else sender.sendLangfileMessage("command.ride.notLiving", new Object[0]);
		}
		else sender.sendLangfileMessage("command.ride.notFound", new Object[0]);
	}
}
