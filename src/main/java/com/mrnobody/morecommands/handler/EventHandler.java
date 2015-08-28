package com.mrnobody.morecommands.handler;

import java.lang.reflect.Method;

import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * An enumeration of handlers used for sending events if a forge event is received
 * 
 * @author MrNobody98
 */
public enum EventHandler {
	ATTACK(MinecraftForge.EVENT_BUS, new Handler<LivingAttackEvent>(LivingAttackEvent.class, false)),
	SET_TARGET(MinecraftForge.EVENT_BUS, new Handler<LivingSetAttackTargetEvent>(LivingSetAttackTargetEvent.class, false)),
	PLACE(MinecraftForge.EVENT_BUS, new Handler<PlaceEvent>(PlaceEvent.class, false)),
	BREAKSPEED(MinecraftForge.EVENT_BUS, new Handler<BreakSpeed>(BreakSpeed.class, false)),
	COMMAND(MinecraftForge.EVENT_BUS, new Handler<CommandEvent>(CommandEvent.class, false)),
	ENTITYJOIN(MinecraftForge.EVENT_BUS, new Handler<EntityJoinWorldEvent>(EntityJoinWorldEvent.class, false)),
	EXPLOSION(MinecraftForge.EVENT_BUS, new Handler<ExplosionEvent>(ExplosionEvent.class, false)),
	FALL(MinecraftForge.EVENT_BUS, new Handler<LivingFallEvent>(LivingFallEvent.class, false)),
	DESTROY(MinecraftForge.EVENT_BUS, new Handler<PlayerDestroyItemEvent>(PlayerDestroyItemEvent.class, false)),
	KEYINPUT(FMLCommonHandler.instance().bus(), new Handler<KeyInputEvent>(KeyInputEvent.class, true)),
	PLAYER_ATTACK(MinecraftForge.EVENT_BUS, new Handler<AttackEntityEvent>(AttackEntityEvent.class, false)),
	RENDERWORLD(MinecraftForge.EVENT_BUS, new Handler<RenderWorldLastEvent>(RenderWorldLastEvent.class, true)),
	TICK(FMLCommonHandler.instance().bus(), new Handler<TickEvent>(TickEvent.class, false)),
	SOUND(MinecraftForge.EVENT_BUS, new Handler<PlaySoundEvent>(PlaySoundEvent.class, true));
	
	private EventBus bus;
	private Handler handler;
	
	EventHandler(EventBus bus, Handler handler) {
		this.bus = bus;
		this.handler = handler;
	}
	
	/**
	 * @return The bus on which the handler shall be registered
	 */
	public EventBus getBus() {
		return this.bus;
	}
	
	/**
	 * @return The handler handling the received event
	 */
	public Handler getHandler() {
		return this.handler;
	}
	
	/**
	 * The private "internal" register method for event handlers which is used by forge
	 * after the "normal" register method ({@link EventBus#register(Object)}) got
	 * the method which handles an event, the event class, etc.
	 */
	private static final Method register = getRegisterMethod();
	
	/**
	 * The "onEvent" method of a {@link Handler}
	 */
	private static final Method onEvent = getOnEventMethod();
	
	/**
	 * Gets the private "internal" register method of the {@link EventBus}
	 * @see EventHandler#register
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
	 * Gets the "onEvent" method of the {@link Handler}
	 * @see Handler#onEvent(Object)
	 */
	private static Method getOnEventMethod() {
		try {
			for (Method m : Handler.class.getMethods()) {
				if (m.getName().equals("onEvent") && m.isAnnotationPresent(SubscribeEvent.class)) return m;
			}
			return null;
		}
		catch (Exception ex) {ex.printStackTrace(); return null;}
	}
	
	/**
	 * Registers a handler to a {@link cpw.mods.fml.common.eventhandler.EventBus}. <br>
	 * The regular {@link cpw.mods.fml.common.eventhandler.EventBus#register(Object)} method can't
	 * be used, because it doesn't work with generic types.
	 * 
	 * @author MrNobody98
	 */
	public static boolean register(EventHandler handler, ModContainer container) {
		if (handler.getHandler().getEventClass() != null && onEvent != null && register != null && container != null) {
			try {EventHandler.register.invoke(handler.getBus(), handler.getHandler().getEventClass(), handler.getHandler(), onEvent, container);}
			catch (Exception ex) {ex.printStackTrace(); return false;}
			return true;
		}
		return false;
	}
}