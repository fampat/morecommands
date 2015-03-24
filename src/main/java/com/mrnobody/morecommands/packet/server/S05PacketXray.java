package com.mrnobody.morecommands.packet.server;

import io.netty.buffer.ByteBuf;

import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.network.PacketHandlerClient;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

/**
 * A packet to toggle xray on/off
 * 
 * @author MrNobody98
 *
 */
public class S05PacketXray implements IMessage, IMessageHandler<S05PacketXray, IMessage> {	
	public boolean showConfig;
	
	public boolean xrayEnabled;
	public int blockRadius;
	
	@Override
	public void fromBytes(ByteBuf data) {
		this.showConfig = data.readBoolean();
		this.xrayEnabled = data.readBoolean();
		this.blockRadius = data.readInt();
	}

	@Override
	public void toBytes(ByteBuf data) {
		data.writeBoolean(this.showConfig);
		data.writeBoolean(this.xrayEnabled);
		data.writeInt(this.blockRadius);
	}
	
	@Override
	public IMessage onMessage(S05PacketXray message, MessageContext ctx) {
		PacketHandlerClient.INSTANCE.handleXray(message.showConfig, message.showConfig ? false : message.xrayEnabled, message.showConfig ? 0 : message.blockRadius);
		
		return null;
	}
	
	/**
	 * Registers the packet to the network system
	 */
	public static void register(int discriminator) {
		MoreCommands.getNetwork().registerMessage(S05PacketXray.class, S05PacketXray.class, discriminator, Side.CLIENT);
	}
}
