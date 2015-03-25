package com.mrnobody.morecommands.packet.server;

import io.netty.buffer.ByteBuf;

import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.network.PacketHandlerClient;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * A packet to set the step height
 * 
 * @author MrNobody98
 *
 */
public class S11PacketStepheight implements IMessage, IMessageHandler<S11PacketStepheight, IMessage> {
	public float stepheight;
	
	@Override
	public void fromBytes(ByteBuf data) {
		this.stepheight = data.readFloat();
	}

	@Override
	public void toBytes(ByteBuf data) {
		data.writeFloat(this.stepheight);
	}
	
	@Override
	public IMessage onMessage(S11PacketStepheight message, MessageContext ctx) {
		PacketHandlerClient.INSTANCE.setStepheight(message.stepheight);
		
		return null;
	}
	
	/**
	 * Registers the packet to the network system
	 */
	public static void register(int discriminator) {
		MoreCommands.getMoreCommands().getNetwork().registerMessage(S11PacketStepheight.class, S11PacketStepheight.class, discriminator, Side.CLIENT);
	}
}