package com.mrnobody.morecommands.patch;

import java.lang.reflect.Field;

import com.google.common.base.Charsets;
import com.mrnobody.morecommands.util.ObfuscatedNames.ObfuscatedField;
import com.mrnobody.morecommands.util.ReflectionHelper;

import io.netty.util.concurrent.GenericFutureListener;
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
	private final Field clientWorldController = ReflectionHelper.getField(ObfuscatedField.NetHandlerPlayClient_clientWorldController);
	private Minecraft mc;
	
	public NetHandlerPlayClient(Minecraft mc, GuiScreen screen, NetworkManager manager) {
		super(mc, screen, manager);
		this.mc = mc;
		this.getNetworkManager().setNetHandler(this);
	}
	
	@Override
    public void handleJoinGame(S01PacketJoinGame p_147282_1_)
    {
		if (this.clientWorldController == null) super.handleJoinGame(p_147282_1_);
        this.mc.playerController = new com.mrnobody.morecommands.patch.PlayerControllerMP(this.mc, this); //Replaces the playerController with my own patched PlayerControllerMP
        ReflectionHelper.set(ObfuscatedField.NetHandlerPlayClient_clientWorldController, this.clientWorldController, this, new WorldClient(this, new WorldSettings(0L, p_147282_1_.func_149198_e(), false, p_147282_1_.func_149195_d(), p_147282_1_.func_149196_i()), p_147282_1_.func_149194_f(), p_147282_1_.func_149192_g(), this.mc.mcProfiler));
        ReflectionHelper.get(ObfuscatedField.NetHandlerPlayClient_clientWorldController, this.clientWorldController, this).isRemote = true;
        this.mc.loadWorld(ReflectionHelper.get(ObfuscatedField.NetHandlerPlayClient_clientWorldController, this.clientWorldController, this));
        this.mc.thePlayer.dimension = p_147282_1_.func_149194_f();
        this.mc.displayGuiScreen(new GuiDownloadTerrain(this));
        this.mc.thePlayer.setEntityId(p_147282_1_.func_149197_c());
        this.currentServerMaxPlayers = p_147282_1_.func_149193_h();
        this.mc.playerController.setGameType(p_147282_1_.func_149198_e());
        this.mc.gameSettings.sendSettingsToServer();
        this.getNetworkManager().scheduleOutboundPacket(new C17PacketCustomPayload("MC|Brand", ClientBrandRetriever.getClientModName().getBytes(Charsets.UTF_8)), new GenericFutureListener[0]);
    }
}
