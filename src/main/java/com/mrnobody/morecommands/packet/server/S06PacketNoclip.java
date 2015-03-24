package com.mrnobody.morecommands.packet.server;

import io.netty.buffer.ByteBuf;

import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.network.PacketHandlerClient;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

/**
 * A packet to toggle noclip on/off
 * 
 * @author MrNobody98
 *
 */
public class S06PacketNoclip implements IMessage, IMessageHandler<S06PacketNoclip, IMessage> {	
	public boolean allowNoclip;
	
	@Override
	public void fromBytes(ByteBuf data) {
		this.allowNoclip = data.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf data) {
		data.writeBoolean(this.allowNoclip);
	}
	
	@Override
	public IMessage onMessage(S06PacketNoclip message, MessageContext ctx) {
		PacketHandlerClient.INSTANCE.handleNoclip(message.allowNoclip);
		
		return null;
	}
	
	/**
	 * Registers the packet to the network system
	 */
	public static void register(int discriminator) {
		MoreCommands.getNetwork().registerMessage(S06PacketNoclip.class, S06PacketNoclip.class, discriminator, Side.CLIENT);
	}
}
