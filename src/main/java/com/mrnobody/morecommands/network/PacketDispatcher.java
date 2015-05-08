package com.mrnobody.morecommands.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayerMP;

import com.google.common.base.Charsets;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.util.Reference;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;

public final class PacketDispatcher {
	private static final byte C00HANDSHAKE = 0;
	private static final byte C01CLIENTCOMMAND = 1;
	private static final byte C02KEYINPUT = 2;
	private static final byte C03OUTPUT = 3;
	private static final byte C04WORLD = 4;
	private static final byte S00HANDSHAKE = 5;
	private static final byte S01CLIENTCOMMAND = 6;
	private static final byte S02CLIMB = 7;
	private static final byte S03FREECAM = 8;
	private static final byte S04FREEZECAM = 9;
	private static final byte S05XRAY = 10;
	private static final byte S06NOCLIP = 11;
	private static final byte S07LIGHT = 12;
	private static final byte S08REACH = 13;
	private static final byte S09EXECUTECLIENTCOMMAND = 14;
	private static final byte S10GRAVITY = 15;
	private static final byte S11STEPHEIGHT = 16;
	private static final byte S12RIDE = 17;
	
	private FMLEventChannel channel;
	private PacketHandlerClient packetHandlerClient;
	private PacketHandlerServer packetHandlerServer;
	
	public PacketDispatcher() {
		this.channel = NetworkRegistry.INSTANCE.newEventDrivenChannel(Reference.CHANNEL);
		this.channel.register(this);
		if (MoreCommands.isClientSide())
			this.packetHandlerClient = new PacketHandlerClient();
		this.packetHandlerServer = new PacketHandlerServer();
	}
	
	@SubscribeEvent
	public void onServerPacketData(ClientCustomPacketEvent event) {
		if (!event.packet.channel().equals(Reference.CHANNEL)) return;
		try {handlePacket(event.packet);}
		catch (Exception ex) {
			ex.printStackTrace(); 
			MoreCommands.getMoreCommands().getLogger().warn("Got a Server packet although running server side. Should be impossible");
		}
	}
	
	@SubscribeEvent
	public void onClientPacketData(ServerCustomPacketEvent event) {
		if (!event.packet.channel().equals(Reference.CHANNEL)) return;
		try {handlePacket(event.packet);}
		catch (Exception ex) {
			ex.printStackTrace(); 
			MoreCommands.getMoreCommands().getLogger().warn("Got a Server packet although running server side. Should be impossible");
		}
	}
	
	private void handlePacket(FMLProxyPacket packet) throws Exception {
		byte id = packet.payload().readByte();
		ByteBuf payload = packet.payload();
		
		switch (id) {
			case C00HANDSHAKE: processC00Handshake(payload); break;
			case C01CLIENTCOMMAND: processC01ClientCommand(payload); break;
			case C02KEYINPUT: processC02KeyInput(payload); break;
			case C03OUTPUT: processC03Output(payload); break;
			case C04WORLD: processC04World(payload); break;
			case S00HANDSHAKE: processS00Handshake(payload); break;
			case S01CLIENTCOMMAND: processS01ClientCommand(payload); break;
			case S02CLIMB: processS02Climb(payload); break;
			case S03FREECAM: processS03Freecam(payload); break;
			case S04FREEZECAM: processS04Freezecam(payload); break;
			case S05XRAY: processS05Xray(payload); break;
			case S06NOCLIP: processS06Noclip(payload); break;
			case S07LIGHT: processS07Light(payload); break;
			case S08REACH: processS08Reach(payload); break;
			case S09EXECUTECLIENTCOMMAND: processS09ExecuteClientCommand(payload); break;
			case S10GRAVITY: processS10Gravity(payload); break;
			case S11STEPHEIGHT: processS11Stepheight(payload); break;
			case S12RIDE: processS12Ride(payload); break;
			default: break;
		}
	}
	
	private void processC00Handshake(ByteBuf payload) {
		boolean patched = payload.readBoolean();
		int length = payload.readInt();
		byte[] string = payload.readBytes(length).array();
		UUID uuid = UUID.fromString(new String(string));
		
		this.packetHandlerServer.handshake(uuid, patched);
	}
	
	private void processC01ClientCommand(ByteBuf payload) {
		int length1 = payload.readInt();
		byte[] string1 = payload.readBytes(length1).array();
		String command = new String(string1);
		
		int length2 = payload.readInt();
		byte[] string2 = payload.readBytes(length2).array();
		UUID uuid = UUID.fromString(new String(string2));
		
		this.packetHandlerServer.clientCommand(uuid, command);
	}
	
	private void processC02KeyInput(ByteBuf payload) {
		int key = payload.readInt();
		int length = payload.readInt();
		byte[] string = payload.readBytes(length).array();
		UUID uuid = UUID.fromString(new String(string));
		
		this.packetHandlerServer.input(uuid, key);
	}
	
	private void processC03Output(ByteBuf payload) {
		boolean output = payload.readBoolean();
		int length = payload.readInt();
		byte[] string = payload.readBytes(length).array();
		UUID uuid = UUID.fromString(new String(string));
		
		this.packetHandlerServer.output(uuid, output);
	}
	
	private void processC04World(ByteBuf payload) {
		int length0 = payload.readInt();
		byte[] string0 = payload.readBytes(length0).array();
		String params = new String(string0);
		int length = payload.readInt();
		byte[] string = payload.readBytes(length).array();
		UUID uuid = UUID.fromString(new String(string));
		
		this.packetHandlerServer.handleWorld(uuid, params);
	}
	
	
	private void processS00Handshake(ByteBuf payload) {
		int length = payload.readInt();
		byte[] string = payload.readBytes(length).array();
		UUID uuid = UUID.fromString(new String(string));
		
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
		
		this.packetHandlerClient.handleNoclip(allowNoclip);;
	}
	
	private void processS07Light(ByteBuf payload) {
		this.packetHandlerClient.handleLight();
	}
	
	private void processS08Reach(ByteBuf payload) {
		float reachDistance = payload.readFloat();
		
		this.packetHandlerClient.handleReach(reachDistance);
	}
	
	private void processS09ExecuteClientCommand(ByteBuf payload) {
		int length = payload.readInt();
		byte[] string = payload.readBytes(length).array();
		String command = new String(string);
		
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
	
	public void sendC00Handshake(boolean clientPlayerPatched) {
		ByteBuf payload = Unpooled.buffer();
		payload.writeByte(C00HANDSHAKE);
		payload.writeBoolean(clientPlayerPatched);
		writePlayerUUID(payload);
		this.channel.sendToServer(new FMLProxyPacket(payload, Reference.CHANNEL));
	}
	
	public void sendC01ClientCommand(String command) {
		ByteBuf payload = Unpooled.buffer();
		payload.writeByte(C01CLIENTCOMMAND);
		
		byte[] string1 = command.getBytes(Charsets.UTF_8);
		int length1 = string1.length;
		payload.writeInt(length1);
		payload.writeBytes(string1);
		
		writePlayerUUID(payload);
		this.channel.sendToServer(new FMLProxyPacket(payload, Reference.CHANNEL));
	}
	
	public void sendC02KeyInput(int key) {
		ByteBuf payload = Unpooled.buffer();
		payload.writeByte(C02KEYINPUT);
		payload.writeInt(key);
		writePlayerUUID(payload);
		this.channel.sendToServer(new FMLProxyPacket(payload, Reference.CHANNEL));
	}
	
	public void sendC03Output(boolean output) {
		ByteBuf payload = Unpooled.buffer();
		payload.writeByte(C03OUTPUT);
		payload.writeBoolean(output);
		writePlayerUUID(payload);
		this.channel.sendToServer(new FMLProxyPacket(payload, Reference.CHANNEL));
	}
	
	public void sendC04World(String params) {
		ByteBuf payload = Unpooled.buffer();
		payload.writeByte(C04WORLD);
		
		byte[] string = params.getBytes(Charsets.UTF_8);
		int length = string.length;
		payload.writeInt(length);
		payload.writeBytes(string);
		
		writePlayerUUID(payload);
		this.channel.sendToServer(new FMLProxyPacket(payload, Reference.CHANNEL));
	}
	
	public void sendS00Handshake(EntityPlayerMP player) {
		ByteBuf payload = Unpooled.buffer();
		payload.writeByte(S00HANDSHAKE);
		writePlayerUUID(player.getUniqueID(), payload);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	public void sendS01ClientCommand(EntityPlayerMP player) {
		ByteBuf payload = Unpooled.buffer();
		payload.writeByte(S01CLIENTCOMMAND);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	public void sendS02Climb(EntityPlayerMP player, boolean climb) {
		ByteBuf payload = Unpooled.buffer();
		payload.writeByte(S02CLIMB);
		payload.writeBoolean(climb);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	public void sendS03Freecam(EntityPlayerMP player) {
		ByteBuf payload = Unpooled.buffer();
		payload.writeByte(S03FREECAM);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	public void sendS04Freezecam(EntityPlayerMP player) {
		ByteBuf payload = Unpooled.buffer();
		payload.writeByte(S04FREEZECAM);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	public void sendS05Xray(EntityPlayerMP player, boolean showConfig, boolean xrayEnabled, int blockRadius) {
		ByteBuf payload = Unpooled.buffer();
		payload.writeByte(S05XRAY);
		payload.writeBoolean(showConfig);
		payload.writeBoolean(xrayEnabled);
		payload.writeInt(blockRadius);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	public void sendS06Noclip(EntityPlayerMP player, boolean noclip) {
		ByteBuf payload = Unpooled.buffer();
		payload.writeByte(S06NOCLIP);
		payload.writeBoolean(noclip);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	public void sendS07Light(EntityPlayerMP player) {
		ByteBuf payload = Unpooled.buffer();
		payload.writeByte(S07LIGHT);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	public void sendS08Reach(EntityPlayerMP player, float reachDistance) {
		ByteBuf payload = Unpooled.buffer();
		payload.writeByte(S08REACH);
		payload.writeFloat(reachDistance);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	public void sendS09ExecuteClientCommand(EntityPlayerMP player, String command) {
		ByteBuf payload = Unpooled.buffer();
		payload.writeByte(S09EXECUTECLIENTCOMMAND);
		byte[] string = command.getBytes(Charsets.UTF_8);
		int length = string.length;
		payload.writeInt(length);
		payload.writeBytes(string);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	public void sendS10Gravity(EntityPlayerMP player, double gravity) {
		ByteBuf payload = Unpooled.buffer();
		payload.writeByte(S10GRAVITY);
		payload.writeDouble(gravity);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	public void sendS11Stepheight(EntityPlayerMP player, float stepheight) {
		ByteBuf payload = Unpooled.buffer();
		payload.writeByte(S11STEPHEIGHT);
		payload.writeFloat(stepheight);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	public void sendS12Ride(EntityPlayerMP player) {
		ByteBuf payload = Unpooled.buffer();
		payload.writeByte(S12RIDE);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	private void writePlayerUUID(ByteBuf buffer) {
		writePlayerUUID(MoreCommands.getMoreCommands().getPlayerUUID(), buffer);
	}
	
	private void writePlayerUUID(UUID uuid, ByteBuf buffer) {
		byte[] string = uuid.toString().getBytes(Charsets.UTF_8);
		int length = string.length;
		buffer.writeInt(length);
		buffer.writeBytes(string);
	}
}
