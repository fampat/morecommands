package com.mrnobody.morecommands.event;

import net.minecraftforge.fml.common.eventhandler.Event;

public final class Listeners {
	private Listeners() {}
	
	/**
	 * A generic interface for handling an event extending {@link Event}
	 * 
	 * @author MrNobody98
	 */
	public static interface EventListener<T extends Event> {
		/**
		 * Called when the event is fired
		 */
		public void onEvent(T event);
	}
	
	/**
	 * A generic interface for handling two events extending {@link Event}
	 * 
	 * @author MrNobody98
	 */
	public static interface TwoEventListener<T1 extends Event, T2 extends Event>{
		/**
		 * Called when event 1 is fired
		 */
		public void onEvent1(T1 event);
    
		/**
		 * Called when event 2 is fired
		 */
		public void onEvent2(T2 event);
	}
}
