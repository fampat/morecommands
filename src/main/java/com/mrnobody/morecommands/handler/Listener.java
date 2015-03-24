package com.mrnobody.morecommands.handler;

import cpw.mods.fml.common.eventhandler.Event;

/**
 * A generic interface for every event extending {@link Event}
 * 
 * @author MrNobody98
 */
public interface Listener<T extends Event> {
	public void onEvent(T event);
}
