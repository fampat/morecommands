package com.mrnobody.morecommands.packet.server;

import io.netty.buffer.ByteBuf;

import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.network.PacketHandlerClient;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

/**
 * A packet to toggle world lighting on/off
 * 
 * @author MrNobody98
 *
 */
public class S07PacketLight implements IMessage, IMessageHandler<S07PacketLight, IMessage> {	
	@Override
	public void fromBytes(ByteBuf data) {}

	@Override
	public void toBytes(ByteBuf data) {}
	
	@Override
	public IMessage onMessage(S07PacketLight message, MessageContext ctx) {
		PacketHandlerClient.INSTANCE.handleLight();
		
		return null;
	}
	
	/**
	 * Registers the packet to the network system
	 */
	public static void register(int discriminator) {
		MoreCommands.getNetwork().registerMessage(S07PacketLight.class, S07PacketLight.class, discriminator, Side.CLIENT);
	}
}
