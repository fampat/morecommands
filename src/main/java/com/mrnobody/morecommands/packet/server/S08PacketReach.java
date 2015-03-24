package com.mrnobody.morecommands.packet.server;

import io.netty.buffer.ByteBuf;

import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.network.PacketHandlerClient;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

/**
 * A packet to set the reach distance
 * 
 * @author MrNobody98
 *
 */
public class S08PacketReach implements IMessage,  IMessageHandler<S08PacketReach, IMessage> {
	public float reachDistance;
	
	@Override
	public void fromBytes(ByteBuf data) {
		this.reachDistance = data.readFloat();
	}

	@Override
	public void toBytes(ByteBuf data) {
		data.writeFloat(this.reachDistance);
	}
	
	@Override
	public IMessage onMessage(S08PacketReach message, MessageContext ctx) {
		PacketHandlerClient.INSTANCE.handleReach(message.reachDistance);
		
		return null;
	}
	
	/**
	 * Registers the packet to the network system
	 */
	public static void register(int discriminator) {
		MoreCommands.getNetwork().registerMessage(S08PacketReach.class, S08PacketReach.class, discriminator, Side.CLIENT);
	}
}
