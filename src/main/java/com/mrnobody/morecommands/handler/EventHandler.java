package com.mrnobody.morecommands.handler;

import java.lang.reflect.Method;

import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent17;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.eventhandler.EventBus;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/**
 * An enumeration of handlers used for sending events if a forge event is received
 * 
 * @author MrNobody98
 */
public enum EventHandler {
	ATTACK(MinecraftForge.EVENT_BUS, new Handler<LivingAttackEvent>(LivingAttackEvent.class, false)),
	BLOCK_PLACEMENT(MinecraftForge.EVENT_BUS, new Handler<PlaceEvent>(PlaceEvent.class, false)),
	BREAKSPEED(MinecraftForge.EVENT_BUS, new Handler<BreakSpeed>(BreakSpeed.class, false)),
	COMMAND(MinecraftForge.EVENT_BUS, new Handler<CommandEvent>(CommandEvent.class, false)),
	ENTITYJOIN(MinecraftForge.EVENT_BUS, new Handler<EntityJoinWorldEvent>(EntityJoinWorldEvent.class, false)),
	EXPLOSION(MinecraftForge.EVENT_BUS, new Handler<ExplosionEvent>(ExplosionEvent.class, false)),
	FALL(MinecraftForge.EVENT_BUS, new Handler<LivingFallEvent>(LivingFallEvent.class, false)),
	DROPS(MinecraftForge.EVENT_BUS, new Handler<BreakEvent>(BreakEvent.class, false)),
	HURT(MinecraftForge.EVENT_BUS, new Handler<LivingHurtEvent>(LivingHurtEvent.class, false)),
	ITEM_DESTROY(MinecraftForge.EVENT_BUS, new Handler<PlayerDestroyItemEvent>(PlayerDestroyItemEvent.class, false)),
	KEYINPUT(FMLCommonHandler.instance().bus(), new Handler<KeyInputEvent>(KeyInputEvent.class, true)),
	PLAYER_ATTACK(MinecraftForge.EVENT_BUS, new Handler<AttackEntityEvent>(AttackEntityEvent.class, false)),
	INTERACT(MinecraftForge.EVENT_BUS, new Handler<PlayerInteractEvent>(PlayerInteractEvent.class, false)),
	RENDERWORLD(MinecraftForge.EVENT_BUS, new Handler<RenderWorldLastEvent>(RenderWorldLastEvent.class, true)),
	TICK(FMLCommonHandler.instance().bus(), new Handler<TickEvent>(TickEvent.class, false)),
	SOUND(MinecraftForge.EVENT_BUS, new Handler<PlaySoundEvent17>(PlaySoundEvent17.class, true));
	
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
	 * Registers a handler to a {@link cpw.mods.fml.common.eventhandler.EventBus}. <br>
	 * The regular {@link cpw.mods.fml.common.eventhandler.EventBus#register(Object)} method can't
	 * be used, because it doesn't work with generic types.
	 * 
	 * @author MrNobody98
	 */
	public static void register(EventBus bus, Handler handler, ModContainer container) {
		if (handler.getEventClass() != null) {
			Method onEvent = null;
			for (Method m : handler.getClass().getMethods()) {
				if (m.getName().equals("onEvent") && m.isAnnotationPresent(SubscribeEvent.class)) {onEvent = m; break;}
			}
			
			if (onEvent != null && EventHandler.register != null && container != null) {
				try {EventHandler.register.invoke(bus, handler.getEventClass(), handler, onEvent, container);}
				catch (Exception ex) {ex.printStackTrace();}
			}
		}
	}
}
