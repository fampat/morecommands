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
 * A packet to let the server know that the player want's to know the worlds seed or the worlds name
 * 
 * @author MrNobody98
 *
 */
public class C04PacketWorld implements IMessage, IMessageHandler<C04PacketWorld, IMessage> {	
	public String params;
	public UUID playerUUID;
	
	@Override
	public void fromBytes(ByteBuf data) {
		int length0 = data.readInt();
		byte[] string0 = data.readBytes(length0).array();
		this.params = new String(string0);
		int length = data.readInt();
		byte[] string = data.readBytes(length).array();
		this.playerUUID = UUID.fromString(new String(string));
	}

	@Override
	public void toBytes(ByteBuf data) {
		byte[] string0 = this.params.getBytes(Charsets.UTF_8);
		int length0 = string0.length;
		data.writeInt(length0);
		data.writeBytes(string0);
		byte[] string = this.playerUUID.toString().getBytes(Charsets.UTF_8);
		int length = string.length;
		data.writeInt(length);
		data.writeBytes(string);
	}
	
	@Override
	public IMessage onMessage(C04PacketWorld message, MessageContext ctx) {
		PacketHandlerServer.INSTANCE.handleWorld(message.playerUUID, message.params);
		
		return null;
	}
	
	/**
	 * Registers the packet to the network system
	 */
	public static void register(int discriminator) {
		MoreCommands.getNetwork().registerMessage(C04PacketWorld.class, C04PacketWorld.class, discriminator, Side.SERVER);
	}
}
