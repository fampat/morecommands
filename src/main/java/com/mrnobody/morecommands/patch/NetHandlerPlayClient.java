package com.mrnobody.morecommands.patch;

import com.mrnobody.morecommands.patch.PlayerControllerMP;
import com.mrnobody.morecommands.util.ReflectionHelper;

import java.lang.reflect.Field;

import io.netty.util.concurrent.GenericFutureListener;

import com.google.common.base.Charsets;

import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.world.WorldSettings;

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
	
	public NetHandlerPlayClient(Minecraft mc, GuiScreen screen, NetworkManager manager) {
		super(mc, screen, manager);
		this.mc = mc;
		this.getNetworkManager().setNetHandler(this);
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
    public void handleJoinGame(S01PacketJoinGame p_147282_1_)
    {
        this.mc.playerController = new PlayerControllerMP(this.mc, this); //Replaces the playerController with my own patched PlayerControllerMP
        ReflectionHelper.setField(getClientWorldControllerField(), this, new WorldClient(this, new WorldSettings(0L, p_147282_1_.func_149198_e(), false, p_147282_1_.func_149195_d(), p_147282_1_.func_149196_i()), p_147282_1_.func_149194_f(), p_147282_1_.func_149192_g(), this.mc.mcProfiler));
        this.getClientWorldController().isRemote = true;
        this.mc.loadWorld(this.getClientWorldController());
        this.mc.thePlayer.dimension = p_147282_1_.func_149194_f();
        this.mc.displayGuiScreen(new GuiDownloadTerrain(this));
        this.mc.thePlayer.setEntityId(p_147282_1_.func_149197_c());
        this.currentServerMaxPlayers = p_147282_1_.func_149193_h();
        this.mc.playerController.setGameType(p_147282_1_.func_149198_e());
        this.mc.gameSettings.sendSettingsToServer();
        this.getNetworkManager().scheduleOutboundPacket(new C17PacketCustomPayload("MC|Brand", ClientBrandRetriever.getClientModName().getBytes(Charsets.UTF_8)), new GenericFutureListener[0]);
    }
}
