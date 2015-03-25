package com.mrnobody.morecommands.packet.server;

import io.netty.buffer.ByteBuf;

import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.network.PacketHandlerClient;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

/**
 * A packet to toggle freezecam mode on/off
 * 
 * @author MrNobody98
 *
 */
public class S04PacketFreezecam implements IMessage, IMessageHandler<S04PacketFreezecam, IMessage> {	
	@Override
	public void fromBytes(ByteBuf data) {}

	@Override
	public void toBytes(ByteBuf data) {}
	
	@Override
	public IMessage onMessage(S04PacketFreezecam message, MessageContext ctx) {
		PacketHandlerClient.INSTANCE.handleFreezeCam();
		
		return null;
	}
	
	/**
	 * Registers the packet to the network system
	 */
	public static void register(int discriminator) {
		MoreCommands.getMoreCommands().getNetwork().registerMessage(S04PacketFreezecam.class, S04PacketFreezecam.class, discriminator, Side.CLIENT);
	}
}
