package com.mrnobody.morecommands.packet.client;

import io.netty.buffer.ByteBuf;

import java.util.UUID;

import com.google.common.base.Charsets;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.network.PacketHandlerServer;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * A packet to let the server know that a key was pressed
 * 
 * @author MrNobody98
 *
 */
public class C02PacketKeyInput implements IMessage, IMessageHandler<C02PacketKeyInput, IMessage> {	
	public int key = -1;
	public UUID playerUUID;
	
	@Override
	public void fromBytes(ByteBuf data) {
		this.key = data.readInt();
		int length = data.readInt();
		byte[] string = data.readBytes(length).array();
		this.playerUUID = UUID.fromString(new String(string));
	}

	@Override
	public void toBytes(ByteBuf data) {
		data.writeInt(this.key);
		byte[] string = this.playerUUID.toString().getBytes(Charsets.UTF_8);
		int length = string.length;
		data.writeInt(length);
		data.writeBytes(string);
	}
	
	@Override
	public IMessage onMessage(C02PacketKeyInput message, MessageContext ctx) {
		PacketHandlerServer.INSTANCE.input(message.playerUUID, message.key);
		
		return null;
	}
	
	/**
	 * Registers the packet to the network system
	 */
	public static void register(int discriminator) {
		MoreCommands.getMoreCommands().getNetwork().registerMessage(C02PacketKeyInput.class, C02PacketKeyInput.class, discriminator, Side.SERVER);
	}
}
