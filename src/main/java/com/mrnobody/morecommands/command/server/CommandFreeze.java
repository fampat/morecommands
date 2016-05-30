package com.mrnobody.morecommands.command.server;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.event.EventHandler;
import com.mrnobody.morecommands.event.Listeners.EventListener;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Command(
		name = "freeze",
		description = "command.freeze.description",
		example = "command.freeze.example",
		syntax = "command.freeze.syntax",
		videoURL = "command.freeze.videoURL"
		)
public class CommandFreeze extends StandardCommand implements ServerCommandProperties, EventListener<TickEvent> {
	private Map<World, ClassInheritanceSet<EntityLiving>> worldsToFreeze = new WeakHashMap<World, ClassInheritanceSet<EntityLiving>>();
	
	public CommandFreeze() {
		EventHandler.TICK.register(this);
	}
	
	@Override
	public void onEvent(TickEvent ev) {
		if (!(ev instanceof TickEvent.WorldTickEvent) || ev.phase != TickEvent.Phase.END) return;
		TickEvent.WorldTickEvent event = (TickEvent.WorldTickEvent) ev;
		
		if (event.world != null && this.worldsToFreeze.containsKey(event.world)) {
			ClassInheritanceSet<EntityLiving> disallowed = this.worldsToFreeze.get(event.world);
			if (disallowed == null) return;
			
			List<Entity> loadedEntities = event.world.loadedEntityList;
			if (loadedEntities == null) return;
			
			for (int i = 0; i < loadedEntities.size(); i++) {
				if (loadedEntities.get(i) instanceof EntityLiving && !(loadedEntities.get(i) instanceof EntityPlayer) && disallowed.contains(loadedEntities.get(i))) {
					EntityLiving entity = (EntityLiving) loadedEntities.get(i);
					
					entity.setPosition(entity.prevPosX, entity.prevPosY, entity.prevPosZ);
					
					entity.motionX = 0;
					entity.motionY = 0;
					entity.motionZ = 0;
					
					entity.setAttackTarget(null);
					
					//if (entity instanceof EntityCreature) ((EntityCreature) entity).attackTime = 20;
					if (entity instanceof EntityCreeper) ((EntityCreeper) entity).setCreeperState(-1);
				}
			}
		}
	}
	
	@Override
	public String getCommandName() {
		return "freeze";
	}

	@Override
	public String getCommandUsage() {
		return "command.freeze.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		World world = sender.getWorld().getMinecraftWorld();
		String entityType = "Mob";
		
		if (params.length > 0) {
			if (com.mrnobody.morecommands.wrapper.Entity.getEntityClass(params[0]) == null) {
				try {entityType = EntityList.CLASS_TO_NAME.get(EntityList.ID_TO_CLASS.get(Integer.parseInt(params[0])));}
				catch (NumberFormatException e) {throw new CommandException("command.bring.unknownEntity", sender);}
			}
			else entityType = params[0];
		}
		
		if (entityType == null) throw new CommandException("command.freeze.unknownEntity", sender);
		Class<?> cls = com.mrnobody.morecommands.wrapper.Entity.getEntityClass(entityType);
		Class<? extends EntityLiving> entityClass;
		
		if (EntityLiving.class.isAssignableFrom(cls))
			entityClass = cls.asSubclass(EntityLiving.class);
		else 
			throw new CommandException("command.freeze.invalidEntity", sender, entityType);
		
		if (!this.worldsToFreeze.containsKey(world)) 
			this.worldsToFreeze.put(world, new ClassInheritanceSet<EntityLiving>());
		
		if (this.worldsToFreeze.get(world).contains(entityClass)) {
			this.worldsToFreeze.get(world).remove(entityClass);
			if (this.worldsToFreeze.get(world).isEmpty()) this.worldsToFreeze.remove(world);
			sender.sendLangfileMessage("command.freeze.off", entityType);
		}
		else {
			this.worldsToFreeze.get(world).add(entityClass);
			sender.sendLangfileMessage("command.freeze.on", entityType);
		}
	}
	
	private static class ClassInheritanceSet<T> extends HashSet<Class<? extends T>> {
		@Override
		public boolean add(Class<? extends T> element) {
			for (Class<? extends T> cls : this)
				if (cls.isAssignableFrom(element)) return false;
			
			return super.add(element);
		}
		
		@Override
		public boolean contains(Object o) {
			if (o instanceof Class<?>) {
				for (Class<? extends T> cls : this)
					if (cls.isAssignableFrom((Class<?>) o)) return true;
				
				return super.contains(o);
			}
			else {
				for (Class<? extends T> cls : this)
					if (cls.isInstance(o)) return true;
				
				return super.contains(o);
			}
		}
		
		@Override
		public boolean remove(Object o) {
			Set<Class<? extends T>> remove = new HashSet<Class<? extends T>>();
			
			if (o instanceof Class<?>) {
				for (Class<? extends T> cls : this)
					if (((Class<?>) o).isAssignableFrom(cls)) remove.add(cls);
				
				for (Class<? extends T> cls : remove) super.remove(cls);
				return !remove.isEmpty();
			}
			
			return super.remove(o);
		}
	}
	
	@Override
	public CommandRequirement[] getRequirements() {
		return new CommandRequirement[0];
	}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}

	@Override
	public int getDefaultPermissionLevel() {
		return 2;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return true;
	}
}
