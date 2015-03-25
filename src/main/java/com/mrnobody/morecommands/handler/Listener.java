package com.mrnobody.morecommands.handler;

import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * A generic interface for every event extending {@link Event}
 * 
 * @author MrNobody98
 */
public interface Listener<T extends Event> {
	/**
	 * Called when the event is fired
	 */
	public void onEvent(T event);
}
