package com.mrnobody.morecommands.patch;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.event.EventHandler;
import com.mrnobody.morecommands.util.Reference;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * A manager for {@link PatchManager#Patch}es.
 * 
 * @author MrNobody98
 */
public final class PatchManager {
	private static final Method FIRE_FORGE_EVENT = getFireForgeEventMethod();
	private static PatchManager instance;
	
	//Used to fetch the fireForgeEvent() method to register it to the event bus.
	private static Method getFireForgeEventMethod() {
		try {return PatchManager.class.getMethod("fireForgeEvent", Event.class);}
		catch (Exception ex) {
			MoreCommands.INSTANCE.getLogger().warn("Failed to fetch PatchManager's fireForgeEvent method. "
					+ "This will probably cause several features not to work");
			
			return null;
		}
	}
	
	/**
	 * @return the PatchManager singleton
	 */
	public static PatchManager instance() {
		if (instance == null) instance = new PatchManager();
		return instance;
	}
	
	/**
	 * This interface represents a "Patch". A patch
	 * is simply meant to be a modification to existing
	 * classes to modify their behaviour.
	 * 
	 * @author MrNobody98
	 */
	public static interface Patch {
		/**
		 * @return the name of this patch (e.g. for logging)
		 */
		String getDisplayName();
		
		/**
		 * @return a brief description what happens when this patch fails
		 */
		String getFailureConsequences();
	}
	
	/**
	 * A Patch which is applied when receiving a {@link FMLStateEvent}
	 * 
	 * @author MrNobody98
	 */
	public static interface StateEventBasedPatch extends Patch {
		/**
		 * @return the {@link FMLStateEvent} subclasses for which the apply method if this patch should be invoked
		 */
		Collection<Class<? extends FMLStateEvent>> stateEventClasses();
		
		/**
		 * Invoked to apply the patch
		 * 
		 * @param event an event of one of the types of {@link #stateEventClasses()}
		 * @return whether the patch could successfully be applied
		 */
		<T extends FMLStateEvent> boolean applyStateEventPatch(T event);
		
		/**
		 * Returns whether this patch needs to be applied.
		 * 
		 * @param event the {@link FMLStateEvent} which would be used to invoke the apply method
		 * @return whether this patch needs to be applied.
		 */
		<T extends FMLStateEvent> boolean needsToBeApplied(T event);
		
		/**
		 * Returns whether the PatchManager should automatically print a log message
		 * 
		 * @param event the {@link FMLStateEvent} which would be used to invoke the apply method
		 * @return whether to print a log message
		 */
		<T extends FMLStateEvent> boolean printLogFor(T event);
	}
	
	/**
	 * A Patch which is applied when receiving a Forge {@link Event}
	 * 
	 * @author MrNobody98
	 */
	public static interface ForgeEventBasedPatch extends Patch {
		/**
		 * @return the {@link Event} subclasses for which the apply method if this patch should be invoked
		 */
		Collection<Class<? extends Event>> forgeEventClasses();
		
		/**
		 * Invoked to apply the patch
		 * 
		 * @param event an event of one of the types of {@link #forgeEventClasses()}
		 * @return whether the patch could successfully be applied
		 */
		<T extends Event> boolean applyForgeEventPatch(T event);
		
		/**
		 * Returns whether this patch needs to be applied.
		 * (Can be used to filter for specific content, e.g. a certain gui in a GuiOpenEvent)
		 * 
		 * @param event the {@link Event} which would be used to invoke the apply method
		 * @return whether this patch needs to be applied.
		 */
		<T extends Event> boolean needsToBeApplied(T event);
		
		/**
		 * Returns whether the PatchManager should automatically print a log message
		 * 
		 * @param event the {@link Event} which would be used to invoke the apply method
		 * @return whether to print a log message
		 */
		<T extends Event> boolean printLogFor(T event);
	}
	
	/**
	 * A Patch which was applied using ASM.
	 * This interface should check whether the patching was successful
	 * 
	 * @author MrNobody98
	 */
	public static interface ASMBasedPatch extends Patch {
		/**
		 * @return Whether the patching via ASM was successful
		 */
		boolean check();
	}
	
	/**
	 * A simple container which keeps track of which patches
	 * were successful. This implements {@link ICapabilityProvider}
	 * and therefore allows player specific instances and can
	 * be attached to a player.
	 * 
	 * @author MrNobody98
	 */
	public static class AppliedPatches implements ICapabilityProvider {
		public static final ResourceLocation PATCHES_IDENTIFIER = new ResourceLocation(Reference.MODID, "patches");
		
		@CapabilityInject(AppliedPatches.class)
		public static final Capability<AppliedPatches> PATCHES_CAPABILITY = null;
		
		/**
		 * Registers this capability to the capability manager
		 */
		public static final void registerCapability() {
			CapabilityManager.INSTANCE.register(AppliedPatches.class, new Capability.IStorage<AppliedPatches>() {
				@Override public NBTBase writeNBT(Capability<AppliedPatches> capability, AppliedPatches instance, EnumFacing side) {return null;}
				@Override public void readNBT(Capability<AppliedPatches> capability, AppliedPatches instance, EnumFacing side, NBTBase nbt) {}
			}, new Callable<AppliedPatches>() {
				@Override public AppliedPatches call() throws Exception {return new AppliedPatches();}
			});
		}
		
		private Map<String, Boolean> appliedPatches = Maps.newHashMap();
		
		/**
		 * Sets whether a patch was successful.<br>
		 * 
		 * @param patch the display name of the Patch
		 * @param success whether the patch was successful
		 */
		public void setPatchSuccessfullyApplied(String patch, boolean success) {
			this.appliedPatches.put(patch, success);
		}
		
		/**
		 * Returns whether a patch was successful
		 * 
		 * @param patch the display name of the patch
		 * @return whether the patch was successful
		 */
		public boolean wasPatchSuccessfullyApplied(String patch) {
			Boolean applied = this.appliedPatches.get(patch);
			return applied == null ? false : applied.booleanValue();
		}
		
		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			return capability == PATCHES_CAPABILITY;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			return capability == PATCHES_CAPABILITY ? PATCHES_CAPABILITY.<T>cast(this) : null;
		}
		
		public void copyFrom(AppliedPatches patches) {
			this.appliedPatches = Maps.newHashMap(patches.appliedPatches);
		}
	}
	
	private Set<Class<? extends Event>> registeredEvents = Sets.newHashSet();
	private AppliedPatches appliedPatches = new AppliedPatches();
	
	private SetMultimap<Class<? extends FMLStateEvent>, StateEventBasedPatch> stateEventBasedPatches = HashMultimap.create();
	private SetMultimap<Class<? extends Event>, ForgeEventBasedPatch> forgeEventBasedPatches = HashMultimap.create();
	private List<ASMBasedPatch> asmBasedPatches = Collections.synchronizedList(Lists.<ASMBasedPatch>newArrayList());
	
	/**
	 * @return a global {@link AppliedPatches} instance (which is not bound to a certain player)
	 */
	public AppliedPatches getGlobalAppliedPatches() {
		return this.appliedPatches;
	}
	
	/**
	 * Gets the {@link AppliedPatches} for a player. If there is none,
	 * a new instance is created and return.
	 * 
	 * @param player the player
	 * @return the {@link AppliedPatches} for the player
	 */
	public AppliedPatches getAppliedPatchesForPlayer(EntityPlayerMP player) {
		AppliedPatches patches = player.getCapability(AppliedPatches.PATCHES_CAPABILITY, null);
		
		if (patches == null) 
			patches = new AppliedPatches();
		
		return patches;
	}
	
	/**
	 * Registers a patch. Note: This has no function
	 * if the patch is not one o {@link StateEventBasedPatch}, {@link ForgeEventBasedPatch}
	 * or {@link ASMBasedPatch}.
	 * 
	 * @param patch the patch
	 */
	public void registerPatch(Patch patch) {
		if (patch instanceof ASMBasedPatch) {
			synchronized (this.asmBasedPatches) {
				this.asmBasedPatches.add((ASMBasedPatch) patch);
			}
		}
		
		if (patch instanceof StateEventBasedPatch) {
			StateEventBasedPatch sePatch = (StateEventBasedPatch) patch;
			
			synchronized (this.stateEventBasedPatches) {
				for (Class<? extends FMLStateEvent> eventClass : Sets.newHashSet(sePatch.stateEventClasses()))
					this.stateEventBasedPatches.put(eventClass, sePatch);
			}
		}
		
		if (patch instanceof ForgeEventBasedPatch) {
			ForgeEventBasedPatch fePatch = (ForgeEventBasedPatch) patch;
			
			synchronized (this.forgeEventBasedPatches) {
				for (Class<? extends Event> eventClass : Sets.newHashSet(fePatch.forgeEventClasses()))
					this.forgeEventBasedPatches.put(eventClass, fePatch);
			}
			
			for (Class<? extends Event> eventClass : Sets.difference(Sets.newHashSet(fePatch.forgeEventClasses()), this.registeredEvents))
				registerEventClass(patch.toString(), eventClass);
		}
	}
	
	private void registerEventClass(String patchName, Class<? extends Event> eventClass) {
		if (!EventHandler.registerMethodToEventBus(MinecraftForge.EVENT_BUS, eventClass, this, FIRE_FORGE_EVENT, Loader.instance().activeModContainer()))
			MoreCommands.INSTANCE.getLogger().warn("Failed to register PatchManager fireForgeEvent method to the event bus "
					+ "for patch " + patchName);
	}
	
	/**
	 * Fires an {@link FMLStateEvent} and applies all patches associated with it
	 * 
	 * @param event the {@link FMLStateEvent}
	 */
	public <T extends FMLStateEvent> void fireStateEvent(T event) {
		synchronized (this.stateEventBasedPatches) {
			Iterator<StateEventBasedPatch> itr = this.stateEventBasedPatches.get(event.getClass()).iterator();
			
			while (itr.hasNext()) {
				StateEventBasedPatch patch = itr.next();
				if (!patch.needsToBeApplied(event)) continue;
				
				if (patch.printLogFor(event)) {
					if (!patch.applyStateEventPatch(event))
						handleApplyFailure(patch);
					else
						handlyApplySuccess(patch);
				}
				else patch.applyStateEventPatch(event);
			}
		}
	}
	

	/**
	 * Fires an {@link Event} and applies all patches associated with it
	 * 
	 * @param event the {@link Event}
	 */
	@SubscribeEvent
	public <T extends Event> void fireForgeEvent(T event) {
		synchronized (this.forgeEventBasedPatches) {
			Iterator<ForgeEventBasedPatch> itr = this.forgeEventBasedPatches.get(event.getClass()).iterator();
			
			while (itr.hasNext()) {
				ForgeEventBasedPatch patch = itr.next();
				if (!patch.needsToBeApplied(event)) continue;
				
				if (patch.printLogFor(event)) {
					if (!patch.applyForgeEventPatch(event))
						handleApplyFailure(patch);
					else
						handlyApplySuccess(patch);
				}
				else patch.applyForgeEventPatch(event);
			}
		}
	}
	
	/**
	 * Checks all {@link ASMBasedPatch}es whether they were successful
	 */
	public void checkASMBasedPatches() {
		synchronized (this.asmBasedPatches) {
			Iterator<ASMBasedPatch> itr = this.asmBasedPatches.iterator();
			
			while (itr.hasNext()) {
				ASMBasedPatch patch = itr.next();
				
				if (!patch.check())
					handleApplyFailure(patch);
				else
					handlyApplySuccess(patch);
			}
		}
	}
	
	private void handleApplyFailure(Patch patch) {
		MoreCommands.INSTANCE.getLogger().warn(String.format("Patch %s failed to apply with consequence: %s", 
												patch.getDisplayName(), patch.getFailureConsequences()));
	}
	
	private void handlyApplySuccess(Patch patch) {
		MoreCommands.INSTANCE.getLogger().info(String.format("Patch %s successfully applied", patch.getDisplayName()));
	}
}
