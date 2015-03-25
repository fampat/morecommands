package com.mrnobody.morecommands.packet.server;

import io.netty.buffer.ByteBuf;

import java.util.UUID;

import com.google.common.base.Charsets;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.network.PacketHandlerClient;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

/**
 * The server handshake packet
 * 
 * @author MrNobody98
 *
 */
public class S00PacketHandshake implements IMessage, IMessageHandler<S00PacketHandshake, IMessage> {	
	public UUID playerUUID;
	
	@Override
	public void fromBytes(ByteBuf data) {
		int length = data.readInt();
		byte[] string = data.readBytes(length).array();
		this.playerUUID = UUID.fromString(new String(string));
	}

	@Override
	public void toBytes(ByteBuf data) {
		byte[] string = this.playerUUID.toString().getBytes(Charsets.UTF_8);
		int length = string.length;
		data.writeInt(length);
		data.writeBytes(string);
	}
	
	@Override
	public IMessage onMessage(S00PacketHandshake message, MessageContext ctx) {
		PacketHandlerClient.INSTANCE.handshake(message.playerUUID);
		
		return null;
	}
	
	/**
	 * Registers the packet to the network system
	 */
	public static void register(int discriminator) {
		MoreCommands.getMoreCommands().getNetwork().registerMessage(S00PacketHandshake.class, S00PacketHandshake.class, discriminator, Side.CLIENT);
	}
}
