package com.mrnobody.morecommands.packet.server;

import io.netty.buffer.ByteBuf;

import com.google.common.base.Charsets;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.network.PacketHandlerClient;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

/**
 * A packet to set the jump height
 * 
 * @author MrNobody98
 *
 */
public class S10PacketGravity implements IMessage, IMessageHandler<S10PacketGravity, IMessage> {
	public double gravity;
	
	@Override
	public void fromBytes(ByteBuf data) {
		this.gravity = data.readDouble();
	}

	@Override
	public void toBytes(ByteBuf data) {
		data.writeDouble(this.gravity);
	}
	
	@Override
	public IMessage onMessage(S10PacketGravity message, MessageContext ctx) {
		PacketHandlerClient.INSTANCE.setGravity(message.gravity);
		
		return null;
	}
	
	/**
	 * Registers the packet to the network system
	 */
	public static void register(int discriminator) {
		MoreCommands.getNetwork().registerMessage(S10PacketGravity.class, S10PacketGravity.class, discriminator, Side.CLIENT);
	}
}
