package com.mrnobody.morecommands.packet.server;

import io.netty.buffer.ByteBuf;

import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.network.PacketHandlerClient;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * A packet to toggle climb mode on/off
 * 
 * @author MrNobody98
 *
 */
public class S02PacketClimb implements IMessage, IMessageHandler<S02PacketClimb, IMessage> {
	public boolean allowClimb;
	
	@Override
	public void fromBytes(ByteBuf data) {
		this.allowClimb = data.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf data) {
		data.writeBoolean(this.allowClimb);
	}
	
	@Override
	public IMessage onMessage(S02PacketClimb message, MessageContext ctx) {
		PacketHandlerClient.INSTANCE.handleClimb(message.allowClimb);
		
		return null;
	}
	
	/**
	 * Registers the packet to the network system
	 */
	public static void register(int discriminator) {
		MoreCommands.getMoreCommands().getNetwork().registerMessage(S02PacketClimb.class, S02PacketClimb.class, discriminator, Side.CLIENT);
	}
}
