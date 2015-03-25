package com.mrnobody.morecommands.patch;

import io.netty.buffer.Unpooled;

import java.lang.reflect.Field;

import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.world.WorldSettings;

import com.mojang.authlib.GameProfile;
import com.mrnobody.morecommands.util.ReflectionHelper;

/**
 * The patched class of {@link net.minecraft.client.network.NetHandlerPlayClient} <br>
 * This class sets the {@link Minecraft#playerController} field, which again is responsible <br>
 * for setting the client player ({@link Minecraft#thePlayer}), which is the actual target
 * I want to modify. <br> By patching this class I can substitute the {@link Minecraft#playerController}
 * field and use my own patched {@link EntityClientPlayerMP}.
 * 
 * @author MrNobody98
 *
 */
public class NetHandlerPlayClient extends net.minecraft.client.network.NetHandlerPlayClient {
	private Minecraft mc;
	
	public NetHandlerPlayClient(Minecraft mc, GuiScreen screen, NetworkManager manager, GameProfile profile) {
		super(mc, screen, manager, profile);
		this.getNetworkManager().setNetHandler(this);
		this.mc = mc;
	}
	
	/**
	 * Gets the clientWorldController in {@link net.minecraft.client.network.NetHandlerPlayClient}
	 */
	private WorldClient getClientWorldController() {
		Field worldControllerField = ReflectionHelper.getField(net.minecraft.client.network.NetHandlerPlayClient.class, "clientWorldController");
		boolean error = false;
		if (worldControllerField != null) {
			WorldClient worldClient = null;
			try {
				worldClient = (WorldClient) worldControllerField.get(this);
			}
			catch (IllegalAccessException e) {error = true;}
			
			if (!error) return worldClient;
			else return null;
		}
		else return null;
	}
	
	/**
	 * Gets the clientWorldController field in {@link net.minecraft.client.network.NetHandlerPlayClient}
	 */
	private Field getClientWorldControllerField() {
		Field worldControllerField = ReflectionHelper.getField(net.minecraft.client.network.NetHandlerPlayClient.class, "clientWorldController");
		if (worldControllerField != null) return worldControllerField;
		else return null;
	}
	
	@Override
    public void handleJoinGame(S01PacketJoinGame packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.mc);
        this.mc.playerController = new PlayerControllerMP(this.mc, this); //Replaces the playerController with my own patched PlayerControllerMP
        ReflectionHelper.setField(getClientWorldControllerField(), this, new WorldClient(this, new WorldSettings(0L, packetIn.getGameType(), false, packetIn.isHardcoreMode(), packetIn.getWorldType()), packetIn.getDimension(), packetIn.getDifficulty(), this.mc.mcProfiler));
        this.mc.gameSettings.difficulty = packetIn.getDifficulty();
        this.mc.loadWorld(this.getClientWorldController());
        this.mc.thePlayer.dimension = packetIn.getDimension();
        this.mc.displayGuiScreen(new GuiDownloadTerrain(this));
        this.mc.thePlayer.setEntityId(packetIn.getEntityId());
        this.currentServerMaxPlayers = packetIn.getMaxPlayers();
        this.mc.thePlayer.setReducedDebug(packetIn.isReducedDebugInfo());
        this.mc.playerController.setGameType(packetIn.getGameType());
        this.mc.gameSettings.sendSettingsToServer();
        this.getNetworkManager().sendPacket(new C17PacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString(ClientBrandRetriever.getClientModName())));
    }
}
