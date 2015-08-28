package com.mrnobody.morecommands.network;

import java.util.UUID;

import com.google.common.base.Charsets;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.util.Reference;
import com.mrnobody.morecommands.util.ServerPlayerSettings;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public final class PacketDispatcher {
	private static final byte C00HANDSHAKE            = 0x00;
	private static final byte C01CLIENTCOMMAND        = 0x01;
	private static final byte C02FINISHHANDSHAKE      = 0x02;
	private static final byte C03KEYINPUT             = 0x03;
	private static final byte C04OUTPUT               = 0x04;
	private static final byte C05WORLD                = 0x05;
	private static final byte S00HANDSHAKE            = 0x06;
	private static final byte S01CLIENTCOMMAND        = 0x07;
	private static final byte S02HANDSHAKEFINISHED    = 0x08;
	private static final byte S03CLIMB                = 0x09;
	private static final byte S04FREECAM              = 0x0A;
	private static final byte S05FREEZECAM            = 0x0B;
	private static final byte S06XRAY                 = 0x0C;
	private static final byte S07NOCLIP               = 0x0D;
	private static final byte S08LIGHT                = 0x0E;
	private static final byte S09REACH                = 0x0F;
	private static final byte S10EXECUTECLIENTCOMMAND = 0x10;
	private static final byte S11GRAVITY              = 0x11;
	private static final byte S12STEPHEIGHT           = 0x12;
	
	private static final byte XRAY_SHOWCONFIG       = 0;
	private static final byte XRAY_CHANGESETTINGS   = 1;
	private static final byte XRAY_LOADSAVESETTINGS = 2;
	
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
			case C02FINISHHANDSHAKE:      processC02FinishHandshake(payload); break;
			case C03KEYINPUT:             processC03KeyInput(payload); break;
			case C04OUTPUT:               processC04Output(payload); break;
			case C05WORLD:                processC05World(payload); break;
			case S00HANDSHAKE:            processS00Handshake(payload); break;
			case S01CLIENTCOMMAND:        processS01ClientCommand(payload); break;
			case S02HANDSHAKEFINISHED:    processS02HandshakeFinished(payload); break;
			case S03CLIMB:                processS03Climb(payload); break;
			case S04FREECAM:              processS04Freecam(payload); break;
			case S05FREEZECAM:            processS05Freezecam(payload); break;
			case S06XRAY:                 processS06Xray(payload); break;
			case S07NOCLIP:               processS07Noclip(payload); break;
			case S08LIGHT:                processS08Light(payload); break;
			case S09REACH:                processS09Reach(payload); break;
			case S10EXECUTECLIENTCOMMAND: processS10ExecuteClientCommand(payload); break;
			case S11GRAVITY:              processS11Gravity(payload); break;
			case S12STEPHEIGHT:           processS12Stepheight(payload); break;
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
	
	private void processC02FinishHandshake(ByteBuf payload) {
		UUID uuid = readUUID(payload);
		
		this.packetHandlerServer.finishHandshake(uuid);
	}
	
	private void processC03KeyInput(ByteBuf payload) {
		int key = payload.readInt();
		UUID uuid = readUUID(payload);
		
		this.packetHandlerServer.input(uuid, key);
	}
	
	private void processC04Output(ByteBuf payload) {
		boolean output = payload.readBoolean();
		UUID uuid = readUUID(payload);
		
		this.packetHandlerServer.output(uuid, output);
	}
	
	private void processC05World(ByteBuf payload) {
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
	
	private void processS02HandshakeFinished(ByteBuf payload) {
		this.packetHandlerClient.handshakeFinished();
	}
	
	private void processS03Climb(ByteBuf payload) {
		boolean allowClimb = payload.readBoolean();
		this.packetHandlerClient.handleClimb(allowClimb);
	}
	
	private void processS04Freecam(ByteBuf payload) {
		this.packetHandlerClient.handleFreecam();
	}
	
	private void processS05Freezecam(ByteBuf payload) {
		this.packetHandlerClient.handleFreezeCam();
	}
	
	private void processS06Xray(ByteBuf payload) {
		byte id = readID(payload);
		
		if (id == XRAY_SHOWCONFIG) this.packetHandlerClient.handleXray();
		else if (id == XRAY_CHANGESETTINGS) {
			boolean enableXray = payload.readBoolean();
			int radius = payload.readInt();
			
			this.packetHandlerClient.handleXray(enableXray, radius);
		}
		else if (id == XRAY_LOADSAVESETTINGS) {
			boolean load = payload.readBoolean();
			String setting = readString(payload);
			
			this.packetHandlerClient.handleXray(load, setting);
		}
	}
	
	private void processS07Noclip(ByteBuf payload) {
		boolean allowNoclip = payload.readBoolean();
		this.packetHandlerClient.handleNoclip(allowNoclip);
	}
	
	private void processS08Light(ByteBuf payload) {
		this.packetHandlerClient.handleLight();
	}
	
	private void processS09Reach(ByteBuf payload) {
		float reachDistance = payload.readFloat();
		this.packetHandlerClient.handleReach(reachDistance);
	}
	
	private void processS10ExecuteClientCommand(ByteBuf payload) {
		String command = readString(payload);
		this.packetHandlerClient.executeClientCommand(command);
	}
	
	private void processS11Gravity(ByteBuf payload) {
		double gravity = payload.readDouble();
		this.packetHandlerClient.setGravity(gravity);
	}
	
	private void processS12Stepheight(ByteBuf payload) {
		float stepheight = payload.readFloat();
		this.packetHandlerClient.setStepheight(stepheight);
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
	
	public void sendC02FinishHandshake() {
		PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
		writeID(payload, C02FINISHHANDSHAKE);
		
		writeUUID(payload);
		this.channel.sendToServer(new FMLProxyPacket(payload, Reference.CHANNEL));
	}
	
	public void sendC03KeyInput(int key) {
		PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
		writeID(payload, C03KEYINPUT);
		
		payload.writeInt(key);
		writeUUID(payload);
		this.channel.sendToServer(new FMLProxyPacket(payload, Reference.CHANNEL));
	}
	
	public void sendC04Output(boolean output) {
		PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
		writeID(payload, C04OUTPUT);
		
		payload.writeBoolean(output);
		writeUUID(payload);
		this.channel.sendToServer(new FMLProxyPacket(payload, Reference.CHANNEL));
	}
	
	public void sendC05World(String params) {
		PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
		writeID(payload, C05WORLD);
		
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
	
	public void sendS02HandshakeFinished(EntityPlayerMP player) {
		PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
		writeID(payload, S02HANDSHAKEFINISHED);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	public void sendS03Climb(EntityPlayerMP player, boolean climb) {
		PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
		writeID(payload, S03CLIMB);
		
		payload.writeBoolean(climb);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	public void sendS04Freecam(EntityPlayerMP player) {
		PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
		writeID(payload, S04FREECAM);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	public void sendS05Freezecam(EntityPlayerMP player) {
		PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
		writeID(payload, S05FREEZECAM);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	public void sendS06Xray(EntityPlayerMP player) {
		PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
	    writeID(payload, S06XRAY);
	    writeID(payload, XRAY_SHOWCONFIG);
	    
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	public void sendS06Xray(EntityPlayerMP player, boolean xrayEnabled, int blockRadius) {
		PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
	    writeID(payload, S06XRAY);
	    writeID(payload, XRAY_CHANGESETTINGS);
	    
	    payload.writeBoolean(xrayEnabled);
	    payload.writeInt(blockRadius);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	public void sendS06Xray(EntityPlayerMP player, boolean load, String setting) {
		PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
	    writeID(payload, S06XRAY);
	    writeID(payload, XRAY_LOADSAVESETTINGS);
	    
	    payload.writeBoolean(load);
	    writeString(setting, payload);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	public void sendS07Noclip(EntityPlayerMP player, boolean noclip) {
		PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
		writeID(payload, S07NOCLIP);
		
		payload.writeBoolean(noclip);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	public void sendS08Light(EntityPlayerMP player) {
		PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
		writeID(payload, S08LIGHT);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	public void sendS09Reach(EntityPlayerMP player, float reachDistance) {
		PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
		writeID(payload, S09REACH);
		
		payload.writeFloat(reachDistance);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	public void sendS10ExecuteClientCommand(EntityPlayerMP player, String command) {
		command = replaceVars(command, ServerPlayerSettings.getPlayerSettings(player));
		
		PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
		writeID(payload, S10EXECUTECLIENTCOMMAND);
		
		writeString(command, payload);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	public void sendS11Gravity(EntityPlayerMP player, double gravity) {
		PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
		writeID(payload, S11GRAVITY);
		
		payload.writeDouble(gravity);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	public void sendS12Stepheight(EntityPlayerMP player, float stepheight) {
		PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
		writeID(payload, S12STEPHEIGHT);
		
		payload.writeFloat(stepheight);
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
		return new String(string, Charsets.UTF_8);
	}
	
	private void writeString(String string, ByteBuf buffer) {
		byte[] bytes = string.getBytes(Charsets.UTF_8);
		buffer.writeInt(bytes.length);
		buffer.writeBytes(bytes);
	}
	
	private String replaceVars(String string, ServerPlayerSettings settings) {
		String varIdentifier = "";
		String newString = "";
		boolean isReadingVarIdentifier = false;
		
		for (char ch : string.toCharArray()) {
			if (ch == '%') {
				if (isReadingVarIdentifier) {
					isReadingVarIdentifier = false;
					
					if (varIdentifier.isEmpty()) newString += "%";
					else {
						if (!settings.varMapping.containsKey(varIdentifier))
							newString += "%" + varIdentifier + "%";
						else
							newString += settings.varMapping.get(varIdentifier);
					}
					
					varIdentifier = "";
				}
				else isReadingVarIdentifier = true;
			}
			else {
				if (isReadingVarIdentifier) varIdentifier += ch;
				else newString += ch;
			}
		}
		
		return newString;
	}
}
