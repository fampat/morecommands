package com.mrnobody.morecommands.packet.server;

import io.netty.buffer.ByteBuf;

import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.network.PacketHandlerClient;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

/**
 * A packet to advise the client to (dis)mount an entity
 * 
 * @author MrNobody98
 *
 */
public class S12PacketRide implements IMessage, IMessageHandler<S12PacketRide, IMessage> {
	@Override
	public void fromBytes(ByteBuf data) {}

	@Override
	public void toBytes(ByteBuf data) {}
	
	@Override
	public IMessage onMessage(S12PacketRide message, MessageContext ctx) {
		PacketHandlerClient.INSTANCE.ride();
		
		return null;
	}
	
	/**
	 * Registers the packet to the network system
	 */
	public static void register(int discriminator) {
		MoreCommands.getNetwork().registerMessage(S12PacketRide.class, S12PacketRide.class, discriminator, Side.CLIENT);
	}
}