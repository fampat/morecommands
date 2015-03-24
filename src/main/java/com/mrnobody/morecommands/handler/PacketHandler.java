package com.mrnobody.morecommands.handler;

import com.mrnobody.morecommands.util.KeyEvent;

/**
 * An enumeration of handlers used for sending events if a packet is received
 * 
 * @author MrNobody98
 */
public enum PacketHandler {
	KEYINPUT(new Handler<KeyEvent>(KeyEvent.class));
	
	private Handler handler;
	
	PacketHandler(Handler handler) {
		this.handler = handler;
	}
	
	/**
	 * @return The handler handling the received packet
	 */
	public Handler getHandler() {
		return this.handler;
	}
	
	/**
	 * @return whether this handler is intended to be used only on client side
	 */
	public static boolean isClientOnly(PacketHandler handler) {
		return false;
	}
}
