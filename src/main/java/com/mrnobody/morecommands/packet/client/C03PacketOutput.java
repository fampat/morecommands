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
 * A packet to let the server know that the player want's to disable chat output
 * 
 * @author MrNobody98
 *
 */
public class C03PacketOutput implements IMessage, IMessageHandler<C03PacketOutput, IMessage> {	
	public boolean output;
	public UUID playerUUID;
	
	@Override
	public void fromBytes(ByteBuf data) {
		this.output = data.readBoolean();
		int length = data.readInt();
		byte[] string = data.readBytes(length).array();
		this.playerUUID = UUID.fromString(new String(string));
	}

	@Override
	public void toBytes(ByteBuf data) {
		data.writeBoolean(this.output);
		byte[] string = this.playerUUID.toString().getBytes(Charsets.UTF_8);
		int length = string.length;
		data.writeInt(length);
		data.writeBytes(string);
	}
	
	@Override
	public IMessage onMessage(C03PacketOutput message, MessageContext ctx) {
		PacketHandlerServer.INSTANCE.output(message.playerUUID, message.output);
		
		return null;
	}
	
	/**
	 * Registers the packet to the network system
	 */
	public static void register(int discriminator) {
		MoreCommands.getNetwork().registerMessage(C03PacketOutput.class, C03PacketOutput.class, discriminator, Side.SERVER);
	}
}
