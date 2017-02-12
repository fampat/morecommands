package com.mrnobody.morecommands.command.server;

import java.util.List;
import java.util.Map;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.event.EventHandler;
import com.mrnobody.morecommands.event.Listeners.EventListener;
import com.mrnobody.morecommands.util.EntityUtils;
import com.mrnobody.morecommands.util.TargetSelector;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

@Command(
		name="ignorespawn",
		description="command.ignorespawn.description",
		example="command.ignorespawn.example",
		syntax="command.ignorespawn.syntax",
		videoURL="command.ignorespawn.videoURL"
		)
public class CommandIgnorespawn extends StandardCommand implements ServerCommandProperties, EventListener<EntityJoinWorldEvent> {
	private static final Map<Class<? extends Entity>, List<Predicate<Entity>>> ignoredEntities = Maps.newHashMap();
	
	public CommandIgnorespawn() {
		EventHandler.ENTITYJOIN.register(this);
	}
  
	@Override
	public void onEvent(EntityJoinWorldEvent event) {
		if (isEntityIgnored(event.getEntity()))
			event.setCanceled(true);
	}
  
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return true;
	}
  
	@Override
	public String getCommandName() {
		return "ignorespawn";
	}
  
	@Override
	public String getCommandUsage() {
		return "command.ignorespawn.syntax";
	}
  
	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		if (params.length > 0) {
			params = params.length > 1 ? reparseParamsWithNBTData(params) : params;
			
			Class<? extends Entity> entityClass = EntityUtils.getEntityClass(new ResourceLocation(params[0]), true);
			NBTTagCompound tag = null; boolean equalLists = false;
			
			if (entityClass == null) {
				try {entityClass = EntityUtils.getEntityClass(EntityUtils.getEntityName(Integer.parseInt(params[0])), false);}
				catch (NumberFormatException nfe) {throw new CommandException("command.ignorespawn.unknownEntity", sender);}
				
				if (entityClass == null)
					throw new CommandException("command.ignorespawn.unknownEntity", sender);
			}

			if (params.length > 1) {
				tag = getNBTFromParam(params[1]);
				equalLists = params.length > 2 && isEqualLists(params[2]);
			}
			
			Predicate<Entity> predicate = new NBTPredicate(tag, equalLists);
			List<Predicate<Entity>> predicates = ignoredEntities.get(entityClass);
			
			if (predicates == null)
				ignoredEntities.put(entityClass, predicates = Lists.newArrayList());
			
			if (!predicates.contains(predicate)) {
				predicates.add(predicate);
				sender.sendLangfileMessage("command.ignorespawn.added");
			}
			else {
				predicates.remove(predicate);
				if (predicates.isEmpty()) ignoredEntities.remove(predicates);
				sender.sendLangfileMessage("command.ignorespawn.removed");
			}
		}
		else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
		
		return null;
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
	public int getDefaultPermissionLevel(String[] args) {
		return 2;
	}
	
	private static final boolean isEntityIgnored(Entity entity) {
		List<Predicate<Entity>> predicates = ignoredEntities.get(entity.getClass());
		
		if (predicates != null)
			return Predicates.or(predicates).apply(entity);
		else
			return false;
	}
	
	private static class NBTPredicate implements Predicate<Entity> {
		private NBTTagCompound tag;
		private boolean equalLists;
		
		public NBTPredicate(NBTTagCompound tag, boolean equalLists) {
			this.tag = tag;
			this.equalLists = equalLists;
		}
		
		@Override
		public boolean apply(Entity entity) {
			if (this.tag == null) 
				return true;
			
			NBTTagCompound container = new NBTTagCompound();
			entity.writeToNBT(container);
			
			return TargetSelector.nbtContains(container, this.tag, !this.equalLists);
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == this) return true;
			else if (!(o instanceof NBTPredicate)) return false;
			
			NBTPredicate that = (NBTPredicate) o;
			return that.tag == null ? this.tag == null : that.tag.equals(this.tag);
		}
		
		@Override
		public int hashCode() {
			return this.tag == null ? 0 : this.tag.hashCode();
		}
	}
}
