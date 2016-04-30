package com.mrnobody.morecommands.event;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.event.Listeners.EventListener;
import com.mrnobody.morecommands.event.Listeners.TwoEventListener;

import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerOpenContainerEvent;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * A generic event handler class for all events extending {@link Event} <br>
 * Each handler represents one event. <br>
 * Use the register methods to register an event listener
 * 
 * @author MrNobody98
 */
public class EventHandler<T extends Event> {
	public static final ForgeEventHandler<LivingAttackEvent>           ATTACK = new ForgeEventHandler<LivingAttackEvent>(MinecraftForge.EVENT_BUS, LivingAttackEvent.class, false);
	public static final ForgeEventHandler<TickEvent>                   TICK = new ForgeEventHandler<TickEvent>(MinecraftForge.EVENT_BUS, TickEvent.class, false);
	public static final ForgeEventHandler<LivingSetAttackTargetEvent>  SET_TARGET = new ForgeEventHandler<LivingSetAttackTargetEvent>(MinecraftForge.EVENT_BUS, LivingSetAttackTargetEvent.class, false);
	public static final ForgeEventHandler<PlaceEvent>                  PLACE = new ForgeEventHandler<PlaceEvent>(MinecraftForge.EVENT_BUS, PlaceEvent.class, false);
	public static final ForgeEventHandler<BreakSpeed>                  BREAKSPEED = new ForgeEventHandler<BreakSpeed>(MinecraftForge.EVENT_BUS, BreakSpeed.class, false);
	public static final ForgeEventHandler<CommandEvent>                COMMAND = new ForgeEventHandler<CommandEvent>(MinecraftForge.EVENT_BUS, CommandEvent.class, false);
	public static final ForgeEventHandler<EntityJoinWorldEvent>        ENTITYJOIN = new ForgeEventHandler<EntityJoinWorldEvent>(MinecraftForge.EVENT_BUS, EntityJoinWorldEvent.class, false);
	public static final ForgeEventHandler<ExplosionEvent>              EXPLOSION = new ForgeEventHandler<ExplosionEvent>(MinecraftForge.EVENT_BUS, ExplosionEvent.class, false);
	public static final ForgeEventHandler<LivingFallEvent>             FALL = new ForgeEventHandler<LivingFallEvent>(MinecraftForge.EVENT_BUS, LivingFallEvent.class, false);
	public static final ForgeEventHandler<PlayerDestroyItemEvent>      ITEM_DESTROY = new ForgeEventHandler<PlayerDestroyItemEvent>(MinecraftForge.EVENT_BUS, PlayerDestroyItemEvent.class, false);
	public static final ForgeEventHandler<KeyInputEvent>               KEYINPUT = new ForgeEventHandler<KeyInputEvent>(MinecraftForge.EVENT_BUS, KeyInputEvent.class, true);
	public static final ForgeEventHandler<AttackEntityEvent>           PLAYER_ATTACK = new ForgeEventHandler<AttackEntityEvent>(MinecraftForge.EVENT_BUS, AttackEntityEvent.class, false);
	public static final ForgeEventHandler<RenderWorldLastEvent>        RENDERWORLD = new ForgeEventHandler<RenderWorldLastEvent>(MinecraftForge.EVENT_BUS, RenderWorldLastEvent.class, true);
	public static final ForgeEventHandler<PlaySoundEvent>              SOUND = new ForgeEventHandler<PlaySoundEvent>(MinecraftForge.EVENT_BUS, PlaySoundEvent.class, true);
	public static final ForgeEventHandler<EntityItemPickupEvent>       PICKUP = new ForgeEventHandler<EntityItemPickupEvent>(MinecraftForge.EVENT_BUS, EntityItemPickupEvent.class, false);
	public static final ForgeEventHandler<WorldEvent.Load>             LOAD_WORLD = new ForgeEventHandler<WorldEvent.Load>(MinecraftForge.EVENT_BUS, WorldEvent.Load.class, false);
	public static final ForgeEventHandler<WorldEvent.Unload>           UNLOAD_WORLD = new ForgeEventHandler<WorldEvent.Unload>(MinecraftForge.EVENT_BUS, WorldEvent.Unload.class, false);
	public static final ForgeEventHandler<LivingDeathEvent>            DEATH = new ForgeEventHandler<LivingDeathEvent>(MinecraftForge.EVENT_BUS, LivingDeathEvent.class, false);
	public static final ForgeEventHandler<EnderTeleportEvent>          ENDER_TELEPORT = new ForgeEventHandler<EnderTeleportEvent>(MinecraftForge.EVENT_BUS, EnderTeleportEvent.class, false);
	public static final ForgeEventHandler<PlayerOpenContainerEvent>    OPEN_CONTAINER = new ForgeEventHandler<PlayerOpenContainerEvent>(MinecraftForge.EVENT_BUS, PlayerOpenContainerEvent.class, false);
	
	public static final EventHandler<ItemStackChangeSizeEvent> ITEMSTACK_CHANGE_SIZE = new EventHandler<ItemStackChangeSizeEvent>(ItemStackChangeSizeEvent.class, false);
	public static final EventHandler<DamageItemEvent>          DAMAGE_ITEM = new EventHandler<DamageItemEvent>(DamageItemEvent.class, false);
	
	private static final EventHandler[] defaultHandlers;
	private static EventHandler[] allHandlers;
	
	/**
	 * returns all event handlers which were created by the newXXXHandler methods in this class
	 * 
	 * @return all created event handlers
	 */
	public static EventHandler[] getAllCreatedEventHandlers() {
		return Arrays.copyOf(allHandlers, allHandlers.length);
	}
	
	static {
		defaultHandlers = allHandlers = getDefaultHandlers();
	}
	
	/**
	 * finds and returns all EventHandlers defined by "public static final" fields defined in this class
	 * 
	 * @return the event handlers defined in this class
	 */
	private static EventHandler[] getDefaultHandlers() {
		List<EventHandler> handlers = new ArrayList<EventHandler>();
		
		try {
			for (Field f : EventHandler.class.getFields()) {
				if (Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers()) && f.get(null) instanceof EventHandler) {
					handlers.add((EventHandler) f.get(null));
				}
			}
			
			return handlers.toArray(new EventHandler[handlers.size()]);
		}
		catch (Exception ex) {return new EventHandler[0];}
	}
	
	private static void putEventHandlers(EventHandler... newHandlers) {
		EventHandler[] oldHandlers = allHandlers;
		allHandlers = new EventHandler[oldHandlers.length + newHandlers.length];
		System.arraycopy(oldHandlers, 0, allHandlers, 0, oldHandlers.length);
		System.arraycopy(newHandlers, 0, allHandlers, oldHandlers.length, newHandlers.length);
	}
	
	/**
	 * registers all "public static final ForgeEventHandler" fields defined in this class
	 * to their corresponding event bus
	 * 
	 * @param mod the mod container, needed by the event bus to register the event <br>
	 * (retrievable via {@link net.minecraftforge.fml.common.Loader#activeModContainer()} or {@link net.minecraftforge.fml.common.Loader#getReversedModObjectList()})
	 * @param allowClientSide whether to allow to register event handlers which are only intended to use client side {@link #isClientOnly()}
	 */
	public static void registerDefaultForgeHandlers(ModContainer mod, boolean allowClientSide) {
		for (EventHandler handler : defaultHandlers)
			if (handler instanceof ForgeEventHandler && (allowClientSide || !handler.isClientOnly()))
				if (!ForgeEventHandler.registerForgeHandler((ForgeEventHandler) handler, mod)) 
					MoreCommands.INSTANCE.getLogger().warn("Could not register EventHandler for Event " + handler.getEventClass().getName());
	}
	
	/**
	 * Creates a new event handler
	 * 
	 * @param <T> the event type
	 * @param eventClass the event class
	 * @param clientOnly whether this event is for client use only
	 * @return the event handler
	 */
	public static <T extends Event> EventHandler<T> newEventHandler(Class<T> eventClass, boolean clientOnly) {
		EventHandler<T> handler = new EventHandler<T>(eventClass, clientOnly);
		putEventHandlers(handler);
		return handler;
	}
	
	/**
	 * Creates a new forge event handler without registering it to the event bus. 
	 * Use {@link ForgeEventHandler#registerForgeHandler(ForgeEventHandler, ModContainer)} to do that
	 * 
	 * @param <T> the event type
	 * @param bus the event bus corresponding to the event
	 * @param eventClass the event class
	 * @param clientOnly whether this event is for client use only
	 * @return the forge event handler
	 */
	public static <T extends Event> ForgeEventHandler<T> newForgeEventHandler(EventBus bus, Class<T> eventClass, boolean clientOnly) {
		ForgeEventHandler<T> handler = new ForgeEventHandler<T>(bus, eventClass, clientOnly);
		putEventHandlers(handler);
		return handler;
	}
	
	/**
	 * Creates a new forge event handler and registers it immediately to the event bus
	 * 
	 * @param <T> the event type
	 * @param bus the event bus corresponding to the event
	 * @param eventClass the event class
	 * @param clientOnly whether this event is for client use only
	 * @param mod the mod container, needed by the event bus to register the event <br>
	 * (retrievable via {@link net.minecraftforge.fml.common.Loader#activeModContainer()} or {@link net.minecraftforge.fml.common.Loader#getReversedModObjectList()})
	 * @return the forge event handler
	 */
	public static <T extends Event> ForgeEventHandler<T> newForgeEventHandler(EventBus bus, Class<T> eventClass, boolean clientOnly, ModContainer mod) {
		ForgeEventHandler<T> handler = newForgeEventHandler(bus, eventClass, clientOnly);
		ForgeEventHandler.registerForgeHandler(handler, mod);
		return handler;
	}
	
	private Map<TwoEventListener<? extends Event, ? extends Event>, EventListener<T>> doubleListeners = new HashMap<TwoEventListener<? extends Event, ? extends Event>, EventListener<T>>();
	private Set<EventListener<T>> listeners = Collections.synchronizedSet(new HashSet<EventListener<T>>());
	private Class<T> eventClass;
	private boolean clientOnly;
	
	private EventHandler(Class<T> eventClass, boolean clientOnly) {
		this.eventClass = eventClass;
		this.clientOnly = clientOnly;
	}
	
	/**
	 * @return Whether this handler is for use on client side only
	 */
	public final boolean isClientOnly() {
		return this.clientOnly;
	}
	
	/**
	 * @return The event class
	 */
	public final Class<T> getEventClass() {
		return this.eventClass;
	}
	
	/**
	 * Posts an event
	 * 
	 * @param event the event to post to all listeners
	 * @return whether the event was canceled
	 */
	public boolean post(T event) {
		synchronized (this.listeners) {
			Iterator<EventListener<T>> listenerIterator = this.listeners.iterator();
			
			while (listenerIterator.hasNext()) {
				listenerIterator.next().onEvent(event);
			}
		}
		
		return event.isCancelable() ? event.isCanceled() : false;
	}

	/**
	 * Registers a listener to the handler
	 */
	public final void register(EventListener<T> listener) {
		if (!this.listeners.contains(listener)) this.listeners.add(listener);
	}
	
	/**
	 * Registers a listener for two events. The {@link TwoEventListener#onEvent1(Event)} will be invoked.
	 */
	public final void registerFirst(final TwoEventListener<? super T, ? extends Event> listener) {
		if (!this.doubleListeners.containsKey(listener)) {
			EventListener<T> l = new EventListener<T>()
			{
				public void onEvent(T event)
				{
					listener.onEvent1(event);
				}
			};
	        
			this.doubleListeners.put(listener, l);
			this.listeners.add(l);
		}
	}
	
	/**
	 * Registers a listener for two events. The {@link TwoEventListener#onEvent2(Event)} will be invoked.
	 */
	public final void registerSecond(final TwoEventListener<? extends Event, ? super T> listener) {
		if (!this.doubleListeners.containsKey(listener)) {
			EventListener<T> l = new EventListener<T>()
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
	public final void unregister(EventListener<T> listener) {
		if (this.listeners.contains(listener)) this.listeners.remove(listener);
	}
	
	/**
	 * Unregisters a listener for two events from the handler
	 */
	public final void unregister(TwoEventListener<? super Event, ? super Event> listener) {
		if (this.doubleListeners.containsKey(listener)) {
			this.listeners.remove(this.doubleListeners.get(listener));
			this.doubleListeners.remove(listener);
		}
	}
	
	/**
	 * @return whether the event listener is registered
	 */
	public final boolean isRegistered(EventListener<T> listener) {
		return this.listeners.contains(listener);
	}
	
	/**
	 * @return whether the double event listener is registered
	 */
	public final boolean isRegistered(TwoEventListener<? super Event, ? super Event> listener) {
		return this.doubleListeners.containsKey(listener);
	}
	
	/**
	 * @return A copy of the listener set
	 */
	public final Set<EventListener<T>> getListeners() {
		return new HashSet<EventListener<T>>(this.listeners);
	}
	
	/**
	 * @return A copy of the double listener set
	 */
	public final Set<TwoEventListener<? extends Event, ? extends Event>> getDoubleListeners() {
		return new HashSet<TwoEventListener<? extends Event, ? extends Event>>(this.doubleListeners.keySet());
	}
	
	/**
	 * An extension of {@link EventHandler} for all forge events which are posted
	 * on a forge {@link EventBus}
	 * 
	 * @author MrNobody98
	 */
	public static final class ForgeEventHandler<T extends Event> extends EventHandler<T> {
		private EventBus bus;
		
		private ForgeEventHandler(EventBus bus, Class<T> eventClass, boolean clientOnly) {
			super(eventClass, clientOnly);
			this.bus = bus;
		}
		
		/**
		 * @return The bus on which the handler shall be registered
		 */
		public final EventBus getBus() {
			return this.bus;
		}
		
		/**
		 * Invoked when the event is fired
		 * @return whether the event was canceled
		 */
		@SubscribeEvent
		public final boolean post(T event) {
			return super.post(event);
		}
		
		/**
		 * The private "internal" register method for event handlers which is used by forge
		 * after the "normal" register method ({@link EventBus#register(Object)}) got
		 * the method which handles an event, the event class, etc.
		 */
		private static final Method register = getRegisterMethod();
		
		/**
		 * The "post" method of a {@link ForgeEventHandler}
		 */
		private static final Method post = getPostMethod();
		
		/**
		 * Gets the private "internal" register method of the {@link EventBus}
		 * @see ForgeEventHandler#register
		 */
		private static Method getRegisterMethod() {
			try {
				Method register = EventBus.class.getDeclaredMethod("register", Class.class, Object.class, Method.class, ModContainer.class);
				register.setAccessible(true);
				return register;
			}
			catch (Exception ex) {ex.printStackTrace(); return null;}
		}
		
		/**
		 * Gets the "post" method of the {@link ForgeEventHandler}
		 * @see ForgeEventHandler#post(Event)
		 */
		private static Method getPostMethod() {
			try {
				for (Method m : ForgeEventHandler.class.getMethods()) {
					if (m.getName().equals("post") && m.isAnnotationPresent(SubscribeEvent.class)) return m;
				}
				return null;
			}
			catch (Exception ex) {ex.printStackTrace(); return null;}
		}
		
		/**
		 * Registers a handler to its {@link net.minecraftforge.fml.common.eventhandler.EventBus}. <br>
		 * The regular {@link net.minecraftforge.fml.common.eventhandler.EventBus#register(Object)} method can't
		 * be used, because it doesn't work with generic types.
		 * 
		 * @param handler the forge event handler
		 * @param mod the mod container, needed by the event bus to register the event <br>
		 * (retrievable via {@link net.minecraftforge.fml.common.Loader#activeModContainer()} or {@link net.minecraftforge.fml.common.Loader#getReversedModObjectList()})
		 * @return whether the forge event handler was successfully registered to its bus
		 */
		public static boolean registerForgeHandler(ForgeEventHandler handler, ModContainer mod) {
			if (handler.getBus() != null && handler.getEventClass() != null && post != null && register != null && mod != null) {
				try {ForgeEventHandler.register.invoke(handler.getBus(), handler.getEventClass(), handler, post, mod);}
				catch (Exception ex) {ex.printStackTrace(); return false;}
				return true;
			}
			return false;
		}
	}
}
