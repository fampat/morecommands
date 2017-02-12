package com.mrnobody.morecommands.patch;

import java.lang.reflect.Field;

import com.mojang.authlib.GameProfile;
import com.mrnobody.morecommands.util.ObfuscatedNames.ObfuscatedField;
import com.mrnobody.morecommands.util.ReflectionHelper;

import io.netty.buffer.Unpooled;
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

/**
 * The patched class of {@link net.minecraft.client.network.NetHandlerPlayClient} <br>
 * This class sets the {@link Minecraft#playerController} field, which again is responsible <br>
 * for setting the client player ({@link Minecraft#thePlayer}), which is the actual target
 * to be modified. <br> By patching this class, the {@link Minecraft#playerController} field is 
 * substituted and a patched version of {@link EntityClientPlayerMP} will be used.
 * 
 * @author MrNobody98
 *
 */
public class NetHandlerPlayClient extends net.minecraft.client.network.NetHandlerPlayClient {
	private final Field clientWorldController = ReflectionHelper.getField(ObfuscatedField.NetHandlerPlayClient_clientWorldController);
	private Minecraft mc;
	
	public NetHandlerPlayClient(Minecraft mc, GuiScreen screen, NetworkManager manager, GameProfile profile) {
		super(mc, screen, manager, profile);
		this.getNetworkManager().setNetHandler(this);
		this.mc = mc;
	}
	
	@Override
    public void handleJoinGame(S01PacketJoinGame packetIn)
    {
		if (this.clientWorldController == null) super.handleJoinGame(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.mc);
        this.mc.playerController = new com.mrnobody.morecommands.patch.PlayerControllerMP(this.mc, this); //Replaces the playerController with my own patched PlayerControllerMP
        ReflectionHelper.set(ObfuscatedField.NetHandlerPlayClient_clientWorldController, this.clientWorldController, this, new WorldClient(this, new WorldSettings(0L, packetIn.getGameType(), false, packetIn.isHardcoreMode(), packetIn.getWorldType()), packetIn.getDimension(), packetIn.getDifficulty(), this.mc.mcProfiler));
        this.mc.gameSettings.difficulty = packetIn.getDifficulty();
        this.mc.loadWorld(ReflectionHelper.get(ObfuscatedField.NetHandlerPlayClient_clientWorldController, this.clientWorldController, this));
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
