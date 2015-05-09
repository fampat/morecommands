package com.mrnobody.morecommands.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.mrnobody.morecommands.handler.Listeners.Listener;
import com.mrnobody.morecommands.handler.Listeners.TwoEventListener;

import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * A generic Handler class for every event extending {@link Event}
 * 
 * @author MrNobody98
 */
public class Handler<T extends Event> {
	private Map<TwoEventListener<T, T>, Listener<T>> doubleListener = new HashMap<TwoEventListener<T, T>, Listener<T>>();
	private List<Listener<T>> listener = new ArrayList<Listener<T>>();
	private Class<T> eventClass;
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
	public Class<T> getEventClass() {
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
	 * Registers a listener for two events to the handler
	 * 
	 * @param firstEvent true to invoke {@link TwoEventListener#onEvent1(Event)}, <br> false to invoke {@link TwoEventListener#onEvent2(Event)}
	 */
	public void register(final TwoEventListener<T, T> listener, boolean firstEvent) {
		if (!this.doubleListener.containsKey(listener)) {
			Listener<T> l = firstEvent ? new Listener<T>()
			{
				public void onEvent(T event)
				{
					listener.onEvent1(event);
				}
			} : new Listener<T>()
			{
				public void onEvent(T event)
				{
					listener.onEvent2(event);
				}
			};
	        
			this.doubleListener.put(listener, l);
			this.listener.add(l);
		}
	}
	
	/**
	 * Unregisters a listener from the handler
	 */
	public void unregister(Listener<T> listener) {
		if (this.listener.contains(listener)) this.listener.remove(listener);
	}
	
	/**
	 * Unregisters a listener for two events from the handler
	 */
	public void unregister(TwoEventListener<T, T> listener) {
		if (this.doubleListener.containsKey(listener)) {
			this.listener.remove(this.doubleListener.get(listener));
			this.doubleListener.remove(listener);
		}
	}
	
	/**
	 * @return Whether this listener is already registered
	 */
	public boolean isRegistered(Listener<T> listener) {
		if (this.listener.contains(listener)) return true;
		else return false;
	}
	
	/**
	 * @return Whether this two event listener is already registered
	 */
	public boolean isRegistered(TwoEventListener<T, T> listener) {
    	return this.doubleListener.containsKey(listener);
	}
}