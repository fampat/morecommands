package com.mrnobody.morecommands.packet.server;

import io.netty.buffer.ByteBuf;

import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.network.PacketHandlerClient;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

/**
 * A packet to request the client sending all client commands
 * 
 * @author MrNobody98
 *
 */
public class S01PacketClientCommand implements IMessage, IMessageHandler<S01PacketClientCommand, IMessage> {	
	@Override
	public void fromBytes(ByteBuf data) {}

	@Override
	public void toBytes(ByteBuf data) {}
	
	@Override
	public IMessage onMessage(S01PacketClientCommand message, MessageContext ctx) {
		PacketHandlerClient.INSTANCE.sendClientCommands();
		
		return null;
	}
	
	/**
	 * Registers the packet to the network system
	 */
	public static void register(int discriminator) {
		MoreCommands.getMoreCommands().getNetwork().registerMessage(S01PacketClientCommand.class, S01PacketClientCommand.class, discriminator, Side.CLIENT);
	}
}
