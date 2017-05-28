package com.mrnobody.morecommands.patch;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Sets;
import com.mrnobody.morecommands.patch.PatchEntityPlayerSP.EntityPlayerSP;
import com.mrnobody.morecommands.util.ObfuscatedNames.ObfuscatedField;
import com.mrnobody.morecommands.util.ReflectionHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLStateEvent;

/**
 * The patcher for {@link net.minecraft.client.renderer.RenderGlobal} <br>
 * When using noclip the terrain isn't rendered if the player is not spectator.
 * The patch of this class overrides the spectator check.
 * 
 * @author MrNobody98
 *
 */
public class PatchRenderGlobal implements PatchManager.StateEventBasedPatch {
	private String displayName;
	
	PatchRenderGlobal(String displayName) {
		this.displayName = displayName;
	}
	
	@Override
	public Collection<Class<? extends FMLStateEvent>> stateEventClasses() {
		return Sets.<Class<? extends FMLStateEvent>>newHashSet(FMLInitializationEvent.class);
	}
	
	@Override
	public <T extends FMLStateEvent> boolean applyStateEventPatch(T event) {
		try {
			SimpleReloadableResourceManager resourceManager = (SimpleReloadableResourceManager) Minecraft.getMinecraft().getResourceManager();
			Field reloadListenerList = ReflectionHelper.getField(ObfuscatedField.SimpleReloadableResourceManager_reloadListeners);
			
			List<?> reloadListeners = (List<?>) reloadListenerList.get(resourceManager);
			reloadListeners.remove(Minecraft.getMinecraft().renderGlobal);
			
			Minecraft.getMinecraft().renderGlobal = new RenderGlobal(Minecraft.getMinecraft());
			resourceManager.registerReloadListener(Minecraft.getMinecraft().renderGlobal);
			
			PatchManager.instance().getGlobalAppliedPatches().setPatchSuccessfullyApplied(this.displayName, true);
			return true;
		}
		catch (Exception ex) {
			PatchManager.instance().getGlobalAppliedPatches().setPatchSuccessfullyApplied(this.displayName, false);
			return false;
		}
	}

	@Override
	public <T extends FMLStateEvent> boolean needsToBeApplied(T event) {
		return true;
	}
	
	@Override
	public <T extends FMLStateEvent> boolean printLogFor(T event) {
		return true;
	}

	@Override
	public String getDisplayName() {
		return this.displayName;
	}

	@Override
	public String getFailureConsequences() {
		return "Disables Noclip";
	}
	
	public static class RenderGlobal extends net.minecraft.client.renderer.RenderGlobal {
		RenderGlobal(Minecraft mcIn) {
			super(mcIn);
		}
		
		@Override
		public void setupTerrain(Entity viewEntity, double partialTicks, ICamera camera, int frameCount, boolean playerSpectator) {
			if (Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().player instanceof EntityPlayerSP) 
				super.setupTerrain(viewEntity, partialTicks, camera, frameCount, playerSpectator || ((EntityPlayerSP) Minecraft.getMinecraft().player).getOverrideNoclip());
			else 
				super.setupTerrain(viewEntity, partialTicks, camera, frameCount, playerSpectator);
		}
	}
}
