package com.mrnobody.morecommands.packet.client;

import io.netty.buffer.ByteBuf;

import java.util.UUID;

import com.google.common.base.Charsets;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.network.PacketHandlerServer;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

/**
 * The client handshake packet
 * 
 * @author MrNobody98
 *
 */
public class C00PacketHandshake implements IMessage, IMessageHandler<C00PacketHandshake, IMessage> {	
	public boolean clientPlayerPatched = false;
	public UUID playerUUID;
	
	@Override
	public void fromBytes(ByteBuf data) {
		this.clientPlayerPatched = data.readBoolean();
		int length = data.readInt();
		byte[] string = data.readBytes(length).array();
		this.playerUUID = UUID.fromString(new String(string));
	}

	@Override
	public void toBytes(ByteBuf data) {
		data.writeBoolean(this.clientPlayerPatched);
		byte[] string = this.playerUUID.toString().getBytes(Charsets.UTF_8);
		int length = string.length;
		data.writeInt(length);
		data.writeBytes(string);
	}
	
	@Override
	public IMessage onMessage(C00PacketHandshake message, MessageContext ctx) {
		PacketHandlerServer.INSTANCE.handshake(message.playerUUID, message.clientPlayerPatched);
		
		return null;
	}

	/**
	 * Registers the packet to the network system
	 */
	public static void register(int discriminator) {
		MoreCommands.getNetwork().registerMessage(C00PacketHandshake.class, C00PacketHandshake.class, discriminator, Side.SERVER);
	}
}
