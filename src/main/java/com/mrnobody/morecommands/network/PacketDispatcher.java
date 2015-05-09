package com.mrnobody.morecommands.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

import com.google.common.base.Charsets;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.util.Reference;

public final class PacketDispatcher {
	private static final byte C00HANDSHAKE            = 0x00;
	private static final byte C01CLIENTCOMMAND        = 0x01;
	private static final byte C02KEYINPUT             = 0x02;
	private static final byte C03OUTPUT               = 0x03;
	private static final byte C04WORLD                = 0x04;
	private static final byte S00HANDSHAKE            = 0x05;
	private static final byte S01CLIENTCOMMAND        = 0x06;
	private static final byte S02CLIMB                = 0x07;
	private static final byte S03FREECAM              = 0x08;
	private static final byte S04FREEZECAM            = 0x09;
	private static final byte S05XRAY                 = 0x0A;
	private static final byte S06NOCLIP               = 0x0B;
	private static final byte S07LIGHT                = 0x0C;
	private static final byte S08REACH                = 0x0D;
	private static final byte S09EXECUTECLIENTCOMMAND = 0x0E;
	private static final byte S10GRAVITY              = 0x0F;
	private static final byte S11STEPHEIGHT           = 0x10;
	private static final byte S12RIDE                 = 0x11;
	
	private FMLEventChannel channel;
	private PacketHandlerClient packetHandlerClient;
	private PacketHandlerServer packetHandlerServer;
	
	public PacketDispatcher() {
		this.channel = NetworkRegistry.INSTANCE.newEventDrivenChannel(Reference.CHANNEL);
		this.channel.register(this);
		if (MoreCommands.isClientSide()) this.packetHandlerClient = new PacketHandlerClient();
		this.packetHandlerServer = new PacketHandlerServer();
	}
	
	@SubscribeEvent
	public void onServerPacketData(ClientCustomPacketEvent event) {
		if (!event.packet.channel().equals(Reference.CHANNEL)) return;
		try {handlePacket(event.packet);}
		catch (Exception ex) {
			ex.printStackTrace(); 
			MoreCommands.getMoreCommands().getLogger().warn("Error handling packet");
		}
	}
	
	@SubscribeEvent
	public void onClientPacketData(ServerCustomPacketEvent event) {
		if (!event.packet.channel().equals(Reference.CHANNEL)) return;
		try {handlePacket(event.packet);}
		catch (Exception ex) {
			ex.printStackTrace(); 
			MoreCommands.getMoreCommands().getLogger().warn("Error handling packet");
		}
	}
	
	private void handlePacket(FMLProxyPacket packet) throws Exception {
		byte id = readID(packet.payload());
		ByteBuf payload = packet.payload();
		
		switch (id) {
			case C00HANDSHAKE:            processC00Handshake(payload); break;
			case C01CLIENTCOMMAND:        processC01ClientCommand(payload); break;
			case C02KEYINPUT:             processC02KeyInput(payload); break;
			case C03OUTPUT:               processC03Output(payload); break;
			case C04WORLD:                processC04World(payload); break;
			case S00HANDSHAKE:            processS00Handshake(payload); break;
			case S01CLIENTCOMMAND:        processS01ClientCommand(payload); break;
			case S02CLIMB:                processS02Climb(payload); break;
			case S03FREECAM:              processS03Freecam(payload); break;
			case S04FREEZECAM:            processS04Freezecam(payload); break;
			case S05XRAY:                 processS05Xray(payload); break;
			case S06NOCLIP:               processS06Noclip(payload); break;
			case S07LIGHT:                processS07Light(payload); break;
			case S08REACH:                processS08Reach(payload); break;
			case S09EXECUTECLIENTCOMMAND: processS09ExecuteClientCommand(payload); break;
			case S10GRAVITY:              processS10Gravity(payload); break;
			case S11STEPHEIGHT:           processS11Stepheight(payload); break;
			case S12RIDE:                 processS12Ride(payload); break;
			default:                      break;
		}
	}
	
	private void processC00Handshake(ByteBuf payload) {
		boolean patched = payload.readBoolean();
		boolean renderGlobalPatched = payload.readBoolean();
		UUID uuid = readUUID(payload);
		
		this.packetHandlerServer.handshake(uuid, patched, renderGlobalPatched);
	}
	
	private void processC01ClientCommand(ByteBuf payload) {
		String command = readString(payload);
		UUID uuid = readUUID(payload);
		
		this.packetHandlerServer.clientCommand(uuid, command);
	}
	
	private void processC02KeyInput(ByteBuf payload) {
		int key = payload.readInt();
		UUID uuid = readUUID(payload);
		
		this.packetHandlerServer.input(uuid, key);
	}
	
	private void processC03Output(ByteBuf payload) {
		boolean output = payload.readBoolean();
		UUID uuid = readUUID(payload);
		
		this.packetHandlerServer.output(uuid, output);
	}
	
	private void processC04World(ByteBuf payload) {
		String params = readString(payload);
		UUID uuid = readUUID(payload);
		
		this.packetHandlerServer.handleWorld(uuid, params);
	}
	
	private void processS00Handshake(ByteBuf payload) {
		UUID uuid = readUUID(payload);
		this.packetHandlerClient.handshake(uuid);
	}
	
	private void processS01ClientCommand(ByteBuf payload) {
		this.packetHandlerClient.sendClientCommands();
	}
	
	private void processS02Climb(ByteBuf payload) {
		boolean allowClimb = payload.readBoolean();
		this.packetHandlerClient.handleClimb(allowClimb);
	}
	
	private void processS03Freecam(ByteBuf payload) {
		this.packetHandlerClient.handleFreecam();
	}
	
	private void processS04Freezecam(ByteBuf payload) {
		this.packetHandlerClient.handleFreezeCam();
	}
	
	private void processS05Xray(ByteBuf payload) {
		boolean showConfig = payload.readBoolean();
		boolean xrayEnabled = payload.readBoolean();
		int blockRadius = payload.readInt();
		
		this.packetHandlerClient.handleXray(showConfig, xrayEnabled, blockRadius);
	}
	
	private void processS06Noclip(ByteBuf payload) {
		boolean allowNoclip = payload.readBoolean();
		this.packetHandlerClient.handleNoclip(allowNoclip);
	}
	
	private void processS07Light(ByteBuf payload) {
		this.packetHandlerClient.handleLight();
	}
	
	private void processS08Reach(ByteBuf payload) {
		float reachDistance = payload.readFloat();
		this.packetHandlerClient.handleReach(reachDistance);
	}
	
	private void processS09ExecuteClientCommand(ByteBuf payload) {
		String command = readString(payload);
		this.packetHandlerClient.executeClientCommand(command);
	}
	
	private void processS10Gravity(ByteBuf payload) {
		double gravity = payload.readDouble();
		this.packetHandlerClient.setGravity(gravity);
	}
	
	private void processS11Stepheight(ByteBuf payload) {
		float stepheight = payload.readFloat();
		this.packetHandlerClient.setStepheight(stepheight);
	}
	
	private void processS12Ride(ByteBuf payload) {
		this.packetHandlerClient.ride();
	}
	
	public void sendC00Handshake(boolean clientPlayerPatched, boolean renderGlobalPatched) {
		PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
		writeID(payload, C00HANDSHAKE);
		
		payload.writeBoolean(clientPlayerPatched);
		payload.writeBoolean(renderGlobalPatched);
		writeUUID(payload);
		this.channel.sendToServer(new FMLProxyPacket(payload, Reference.CHANNEL));
	}
	
	public void sendC01ClientCommand(String command) {
		PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
		writeID(payload, C01CLIENTCOMMAND);
		
		writeString(command, payload);
		writeUUID(payload);
		this.channel.sendToServer(new FMLProxyPacket(payload, Reference.CHANNEL));
	}
	
	public void sendC02KeyInput(int key) {
		PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
		writeID(payload, C02KEYINPUT);
		
		payload.writeInt(key);
		writeUUID(payload);
		this.channel.sendToServer(new FMLProxyPacket(payload, Reference.CHANNEL));
	}
	
	public void sendC03Output(boolean output) {
		PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
		writeID(payload, C03OUTPUT);
		
		payload.writeBoolean(output);
		writeUUID(payload);
		this.channel.sendToServer(new FMLProxyPacket(payload, Reference.CHANNEL));
	}
	
	public void sendC04World(String params) {
		PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
		writeID(payload, C04WORLD);
		
		writeString(params, payload);
		writeUUID(payload);
		this.channel.sendToServer(new FMLProxyPacket(payload, Reference.CHANNEL));
	}
	
	public void sendS00Handshake(EntityPlayerMP player) {
		PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
		writeID(payload, S00HANDSHAKE);
		
		writeUUID(player.getUniqueID(), payload);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	public void sendS01ClientCommand(EntityPlayerMP player) {
		PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
		writeID(payload, S01CLIENTCOMMAND);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	public void sendS02Climb(EntityPlayerMP player, boolean climb) {
		PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
		writeID(payload, S02CLIMB);
		
		payload.writeBoolean(climb);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	public void sendS03Freecam(EntityPlayerMP player) {
		PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
		writeID(payload, S03FREECAM);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	public void sendS04Freezecam(EntityPlayerMP player) {
		PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
		writeID(payload, S04FREEZECAM);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	public void sendS05Xray(EntityPlayerMP player, boolean showConfig, boolean xrayEnabled, int blockRadius) {
		PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
		writeID(payload, S05XRAY);
		
		payload.writeBoolean(showConfig);
		payload.writeBoolean(xrayEnabled);
		payload.writeInt(blockRadius);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	public void sendS06Noclip(EntityPlayerMP player, boolean noclip) {
		PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
		writeID(payload, S06NOCLIP);
		
		payload.writeBoolean(noclip);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	public void sendS07Light(EntityPlayerMP player) {
		PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
		writeID(payload, S07LIGHT);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	public void sendS08Reach(EntityPlayerMP player, float reachDistance) {
		PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
		writeID(payload, S08REACH);
		
		payload.writeFloat(reachDistance);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	public void sendS09ExecuteClientCommand(EntityPlayerMP player, String command) {
		PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
		writeID(payload, S09EXECUTECLIENTCOMMAND);
		
		writeString(command, payload);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	public void sendS10Gravity(EntityPlayerMP player, double gravity) {
		PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
		writeID(payload, S10GRAVITY);
		
		payload.writeDouble(gravity);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	public void sendS11Stepheight(EntityPlayerMP player, float stepheight) {
		PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
		writeID(payload, S11STEPHEIGHT);
		
		payload.writeFloat(stepheight);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	public void sendS12Ride(EntityPlayerMP player) {
		PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
		writeID(payload, S12RIDE);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	private void writeID(ByteBuf buffer, byte id) {
		buffer.writeByte(id);
	}
	
	private byte readID(ByteBuf buffer) {
		return buffer.readByte();
	}
	
	private void writeUUID(ByteBuf buffer) {
		writeUUID(MoreCommands.getMoreCommands().getPlayerUUID(), buffer);
	}
	
	private void writeUUID(UUID uuid, ByteBuf buffer) {
		writeString(uuid.toString(), buffer);
	}
	
	private UUID readUUID(ByteBuf buffer) {
		return UUID.fromString(readString(buffer));
	}
	
	private String readString(ByteBuf buffer) {
		int length = buffer.readInt();
		byte[] string = buffer.readBytes(length).array();
		return new String(string);
	}
	
	private void writeString(String string, ByteBuf buffer) {
		byte[] bytes = string.getBytes(Charsets.UTF_8);
		buffer.writeInt(bytes.length);
		buffer.writeBytes(bytes);
	}
}
