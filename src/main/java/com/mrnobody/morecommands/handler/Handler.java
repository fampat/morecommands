package com.mrnobody.morecommands.handler;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.mrnobody.morecommands.handler.Listeners.EventListener;
import com.mrnobody.morecommands.handler.Listeners.TwoEventListener;

import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * A generic Handler class for every event extending {@link Event}
 * 
 * @author MrNobody98
 */
public class Handler<T extends Event> {
	private Map<TwoEventListener<? extends Event, ? extends Event>, EventListener<T>> doubleListeners = new HashMap<TwoEventListener<? extends Event, ? extends Event>, EventListener<T>>();
	private Set<EventListener<T>> listeners = Collections.synchronizedSet(new HashSet<EventListener<T>>());
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
		synchronized (this.listeners) {
			Iterator<EventListener<T>> listenerIterator = this.listeners.iterator();
			
			while (listenerIterator.hasNext()) {
				listenerIterator.next().onEvent(event);
			}
		}
	}

	/**
	 * Registers a listener to the handler
	 */
	public void register(EventListener<T> listener) {
		if (!this.listeners.contains(listener)) this.listeners.add(listener);
	}
	
	/**
	 * Registers a listener for two events to the handler
	 * 
	 * @param firstEvent true to invoke {@link TwoEventListener#onEvent1(Event)}, <br> false to invoke {@link TwoEventListener#onEvent2(Event)}
	 */
	public void register(final TwoEventListener<? super Event, ? super Event> listener, boolean firstEvent) {
		if (!this.doubleListeners.containsKey(listener)) {
			EventListener<T> l = firstEvent ? new EventListener<T>()
			{
				public void onEvent(T event)
				{
					listener.onEvent1(event);
				}
			} : new EventListener<T>()
			{
				public void onEvent(T event)
				{
					listener.onEvent2(event);
				}
			};
	        
			this.doubleListeners.put(listener, l);
			this.listeners.add(l);
		}
	}
	
	/**
	 * Unregisters a listener from the handler
	 */
	public void unregister(EventListener<T> listener) {
		if (this.listeners.contains(listener)) this.listeners.remove(listener);
	}
	
	/**
	 * Unregisters a listener for two events from the handler
	 */
	public void unregister(TwoEventListener<? super Event, ? super Event> listener) {
		if (this.doubleListeners.containsKey(listener)) {
			this.listeners.remove(this.doubleListeners.get(listener));
			this.doubleListeners.remove(listener);
		}
	}
	
	/**
	 * @return whether the event listener is registered
	 */
	public boolean isRegistered(EventListener<T> listener) {
		return this.listeners.contains(listener);
	}
	
	/**
	 * @return whether the double event listener is registered
	 */
	public boolean isRegistered(TwoEventListener<? super Event, ? super Event> listener) {
		return this.doubleListeners.containsKey(listener);
	}
	
	/**
	 * @return A copy of the listener set
	 */
	public Set<EventListener<T>> getListeners() {
		return new HashSet<EventListener<T>>(this.listeners);
	}
	
	/**
	 * @return A copy of the double listener set
	 */
	public Set<TwoEventListener<? extends Event, ? extends Event>> getDoubleListeners() {
		return new HashSet<TwoEventListener<? extends Event, ? extends Event>>(this.doubleListeners.keySet());
	}
}