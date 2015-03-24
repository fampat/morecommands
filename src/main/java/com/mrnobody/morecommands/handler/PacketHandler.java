package com.mrnobody.morecommands.handler;

import com.mrnobody.morecommands.util.KeyEvent;

/**
 * An enumeration of handlers used for sending events if a packet is received
 * 
 * @author MrNobody98
 */
public enum PacketHandler {
	KEYINPUT(new Handler<KeyEvent>(KeyEvent.class, false));
	
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
}
