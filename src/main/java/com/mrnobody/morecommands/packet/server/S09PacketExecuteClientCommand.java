package com.mrnobody.morecommands.packet.server;

import io.netty.buffer.ByteBuf;

import com.google.common.base.Charsets;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.network.PacketHandlerClient;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * A packet to let the client execute a client command
 * 
 * @author MrNobody98
 *
 */
public class S09PacketExecuteClientCommand implements IMessage, IMessageHandler<S09PacketExecuteClientCommand, IMessage> {	
	public String command;
	
	@Override
	public void fromBytes(ByteBuf data) {
		int length = data.readInt();
		byte[] string = data.readBytes(length).array();
		this.command = new String(string);
	}

	@Override
	public void toBytes(ByteBuf data) {
		byte[] string = this.command.getBytes(Charsets.UTF_8);
		int length = string.length;
		data.writeInt(length);
		data.writeBytes(string);
	}
	
	@Override
	public IMessage onMessage(S09PacketExecuteClientCommand message, MessageContext ctx) {
		PacketHandlerClient.INSTANCE.executeClientCommand(message.command);
		
		return null;
	}
	
	/**
	 * Registers the packet to the network system
	 */
	public static void register(int discriminator) {
		MoreCommands.getMoreCommands().getNetwork().registerMessage(S09PacketExecuteClientCommand.class, S09PacketExecuteClientCommand.class, discriminator, Side.CLIENT);
	}
}
