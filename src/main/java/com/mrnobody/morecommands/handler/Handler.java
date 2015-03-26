package com.mrnobody.morecommands.handler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/**
 * A generic Handler class for every event extending {@link Event}
 * 
 * @author MrNobody98
 */
public class Handler<T extends Event> {
	private List<Listener<T>> listener = new ArrayList<Listener<T>>();
	private Class<? extends Event> eventClass;
	private boolean clientOnly;
	
	public Handler(Class<T> eventClass, boolean clientOnly) {
		this.eventClass = eventClass;
		this.clientOnly = clientOnly;
	}
	
	/**
	 * @return Whether this handler is for use on client side only
	 */
	public boolean isClientOnly() {
		return this.clientOnly;
	}
	
	/**
	 * @return The event class
	 */
	public Class<? extends Event> getEventClass() {
		return this.eventClass;
	}
	
	/**
	 * Called when the event is fired
	 */
	@SubscribeEvent
	public final void onEvent(T event) {
		Iterator<Listener<T>> listenerIterator = this.listener.iterator();
		
		while (listenerIterator.hasNext()) {
			listenerIterator.next().onEvent(event);
		}
	}

	/**
	 * Registers a listener to the handler
	 */
	public void register(Listener<T> listener) {
		if (!this.listener.contains(listener)) this.listener.add(listener);
	}
	
	/**
	 * Unregisters a listener from the handler
	 */
	public void unregister(Listener<T> listener) {
		if (this.listener.contains(listener)) this.listener.remove(listener);
	}
	
	/**
	 * @return Whether this listener is already registered
	 */
	public boolean isRegistered(Listener<T> listener) {
		if (this.listener.contains(listener)) return true;
		else return false;
	}
}
