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
 * A packet to let the server know about a client command
 * 
 * @author MrNobody98
 *
 */
public class C01PacketClientCommand implements IMessage, IMessageHandler<C01PacketClientCommand, IMessage> {	
	public String command;
	public UUID playerUUID;
	
	@Override
	public void fromBytes(ByteBuf data) {
		int length1 = data.readInt();
		byte[] string1 = data.readBytes(length1).array();
		this.command = new String(string1);
		
		int length2 = data.readInt();
		byte[] string2 = data.readBytes(length2).array();
		this.playerUUID = UUID.fromString(new String(string2));
		
		
	}

	@Override
	public void toBytes(ByteBuf data) {
		byte[] string1 = this.command.getBytes(Charsets.UTF_8);
		int length1 = string1.length;
		data.writeInt(length1);
		data.writeBytes(string1);
		
		byte[] string2 = this.playerUUID.toString().getBytes(Charsets.UTF_8);
		int length2 = string2.length;
		data.writeInt(length2);
		data.writeBytes(string2);
	}
	
	@Override
	public IMessage onMessage(C01PacketClientCommand message, MessageContext ctx) {
		PacketHandlerServer.INSTANCE.clientCommand(message.playerUUID, message.command);
		
		return null;
	}
	
	/**
	 * Registers the packet to the network system
	 */
	public static void register(int discriminator) {
		MoreCommands.getMoreCommands().getNetwork().registerMessage(C01PacketClientCommand.class, C01PacketClientCommand.class, discriminator, Side.SERVER);
	}
}
