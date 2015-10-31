package com.mrnobody.morecommands.network;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mrnobody.morecommands.command.ClientCommand;
import com.mrnobody.morecommands.core.AppliedPatches;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.patch.EntityPlayerSP;
import com.mrnobody.morecommands.patch.PlayerControllerMP;
import com.mrnobody.morecommands.util.ClientPlayerSettings;
import com.mrnobody.morecommands.util.GlobalSettings;
import com.mrnobody.morecommands.util.XrayHelper;
import com.mrnobody.morecommands.util.XrayHelper.BlockSettings;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.EntityCamera;
import com.mrnobody.morecommands.wrapper.World;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.ClientCommandHandler;

/**
 * This class handles all incoming packets from the server
 * 
 * @author MrNobody98
 *
 */
public class PacketHandlerClient {
	private static final List<ClientCommand> removedCmds = new ArrayList<ClientCommand>();
	
	//Data for the freecam command
	private EntityPlayerSP freecamOriginalPlayer;
	private boolean prevIsFlying;
	private boolean prevAllowFlying;
	private float prevFlySpeed;
	private float prevWalkSpeed;
	private boolean prevNoclip;
	
	//Data for the freezecam command
	private EntityPlayerSP freezecamOriginalPlayer;
	
	//Data for the light command
	private boolean isEnlightened = false;
	private int lightenedWorld = 0;
	
	/**
	 * re-registers the unregistered client commands and clears the list
	 */
	public static void reregisterAndClearRemovedCmds() {
		for (ClientCommand cmd : PacketHandlerClient.removedCmds) ClientCommandHandler.instance.registerCommand(cmd);
		PacketHandlerClient.removedCmds.clear();
	}
	
	/**
	 * Runs a thread waiting for a handshake from the server <br>
	 * to execute startup commands. If it isn't received after a <br>
	 * certain time, they are still executed
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
				for (String command : MoreCommands.getMoreCommands().getStartupCommandsMultiplayer(socketAddress)) {
					if (ClientCommandHandler.instance.executeCommand(Minecraft.getMinecraft().thePlayer, command) == 0)
						Minecraft.getMinecraft().thePlayer.sendChatMessage(command.startsWith("/") ? command : "/" + command);
					MoreCommands.getMoreCommands().getLogger().info("Executed startup command '" + command + "'");
				}
			}
		}).start();
	}
	
	public static void executeStartupCommands() {
		notifyPlayerAboutUpdate();
		
		for (String command : MoreCommands.getMoreCommands().getStartupCommands()) {
			if (ClientCommandHandler.instance.executeCommand(Minecraft.getMinecraft().thePlayer, command) == 0)
				Minecraft.getMinecraft().thePlayer.sendChatMessage(command.startsWith("/") ? command : "/" + command);
			MoreCommands.getMoreCommands().getLogger().info("Executed startup command '" + command + "'");
		}
	}
	
	private static void notifyPlayerAboutUpdate() {
		if (MoreCommands.getProxy().getUpdateText() != null && !MoreCommands.getProxy().wasPlayerNotified())
			Minecraft.getMinecraft().thePlayer.addChatMessage(MoreCommands.getProxy().getUpdateText());
	}
	
	/**
	 * Is called if the client receives a handshake packet
	 */
	public void handshake(UUID uuid) {
		MoreCommands.getMoreCommands().setPlayerUUID(uuid);
		AppliedPatches.setServerModded(true);
		
		//Remove commands, which shall be removed if the server side version shall be used
		List<String> remove = new ArrayList<String>();
		for (Object command : ClientCommandHandler.instance.getCommands().keySet()) {
			if (ClientCommandHandler.instance.getCommands().get((String) command) instanceof ClientCommand) {
				ClientCommand cmd = (ClientCommand) ClientCommandHandler.instance.getCommands().get((String) command);
				if (!cmd.registerIfServerModded()) {remove.add(cmd.getName()); PacketHandlerClient.removedCmds.add(cmd);}
			}
		}
		
		if (!remove.isEmpty()) {
			for (String rem : remove)
				ClientCommandHandler.instance.getCommands().remove(rem);
			
			MoreCommands.getMoreCommands().getLogger().info("Unregistered following client commands because server side versions are used:\n" + remove);
		}
			
		//Let the server know that the mod is installed client side
		MoreCommands.getMoreCommands().getLogger().info("Sending client handshake");
		MoreCommands.getMoreCommands().getPacketDispatcher().sendC00Handshake(
				Minecraft.getMinecraft().thePlayer instanceof EntityPlayerSP,
				Minecraft.getMinecraft().renderGlobal instanceof com.mrnobody.morecommands.patch.RenderGlobal);
	}
	
	/**
	 * Called when the handshake is finished
	 */
	public void handshakeFinished() {
		AppliedPatches.setHandshakeFinished(true);
		MoreCommands.getMoreCommands().getLogger().info("Handshake finished");
		if (MoreCommands.getMoreCommands().getRunningServer() == ServerType.INTEGRATED) PacketHandlerClient.executeStartupCommands();
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
	 * Shows the xray config
	 */
	public void handleXray() {
		XrayHelper.getInstance().showConfig();
	}
	
	/**
	 * Enables/Disables xray and sets the radius
	 */
	public void handleXray(boolean xrayEnabled, int blockRadius) {
		XrayHelper.getInstance().changeSettings(blockRadius, xrayEnabled);
	}
	
	/**
	 * loads/saves an xray setting
	 */
	public void handleXray(boolean load, String setting) {
		if (load) {
			if (ClientPlayerSettings.xrayColorMapping.containsKey(setting)
				&& ClientPlayerSettings.xrayRadiusMapping.containsKey(setting)) {
				int radius = ClientPlayerSettings.xrayRadiusMapping.get(setting);
				Map<Block, Integer> colors = ClientPlayerSettings.xrayColorMapping.get(setting);
				
				for (XrayHelper.BlockSettings bs : XrayHelper.getInstance().blockMapping.values()) bs.draw = false;
				
				for (Map.Entry<Block, Integer> entry : colors.entrySet())
					XrayHelper.getInstance().blockMapping.put(entry.getKey(), new XrayHelper.BlockSettings(entry.getKey(), new Color(entry.getValue()), true));
				
				XrayHelper.getInstance().changeSettings(radius, XrayHelper.getInstance().xrayEnabled);
				(new CommandSender(Minecraft.getMinecraft().thePlayer)).sendLangfileMessage("command.xray.loaded", setting);
			}
			else (new CommandSender(Minecraft.getMinecraft().thePlayer)).sendLangfileMessage("command.xray.notFound", EnumChatFormatting.RED, setting);
		}
		else {
			ClientPlayerSettings.xrayRadiusMapping.put(setting, XrayHelper.getInstance().blockRadius);
			Map<Block, Integer> colors = new HashMap<Block, Integer>();
			
			for (Map.Entry<Block, BlockSettings> entry : XrayHelper.getInstance().blockMapping.entrySet()) {
				if (entry.getValue().draw) {
					Color c = entry.getValue().color;
					colors.put(entry.getKey(), (c.getBlue() << 16) + (c.getGreen() << 8) + c.getRed());
				}
			}
			
			ClientPlayerSettings.xrayColorMapping.put(setting, colors);
			ClientPlayerSettings.saveSettings();
			(new CommandSender(Minecraft.getMinecraft().thePlayer)).sendLangfileMessage("command.xray.saved", setting);
		}
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
		
		MoreCommands.getMoreCommands().getPacketDispatcher().sendC02FinishHandshake();
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
	
	public void setFluidMovement(boolean fluidmovement) {
		if (Minecraft.getMinecraft().thePlayer instanceof EntityPlayerSP) {
			((EntityPlayerSP) Minecraft.getMinecraft().thePlayer).setFluidMovement(fluidmovement);
		}
	}
}
