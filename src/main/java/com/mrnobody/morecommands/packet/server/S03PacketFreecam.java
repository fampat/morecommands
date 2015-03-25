package com.mrnobody.morecommands.packet.server;

import io.netty.buffer.ByteBuf;

import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.network.PacketHandlerClient;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

/**
 * A packet to toggle freecam mode on/off
 * 
 * @author MrNobody98
 *
 */
public class S03PacketFreecam implements IMessage, IMessageHandler<S03PacketFreecam, IMessage> {	
	@Override
	public void fromBytes(ByteBuf data) {
	}

	@Override
	public void toBytes(ByteBuf data) {
	}
	
	@Override
	public IMessage onMessage(S03PacketFreecam message, MessageContext ctx) {
		PacketHandlerClient.INSTANCE.handleFreecam();
		
		return null;
	}
	
	/**
	 * Registers the packet to the network system
	 */
	public static void register(int discriminator) {
		MoreCommands.getMoreCommands().getNetwork().registerMessage(S03PacketFreecam.class, S03PacketFreecam.class, discriminator, Side.CLIENT);
	}
}
