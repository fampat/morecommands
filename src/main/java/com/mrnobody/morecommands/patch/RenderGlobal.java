package com.mrnobody.morecommands.patch;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;

/**
 * The patched class of {@link net.minecraft.client.renderer.RenderGlobal} <br>
 * When using noclip the terrain isn't rendered if the player is not spectator.
 * The patch of this class overrides the spectator check.
 * 
 * @author MrNobody98
 *
 */
public class RenderGlobal extends net.minecraft.client.renderer.RenderGlobal {

	public RenderGlobal(Minecraft mcIn) {
		super(mcIn);
	}
	
	@Override
	public void setupTerrain(Entity viewEntity, double partialTicks, ICamera camera, int frameCount, boolean playerSpectator) {
		if (Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().player instanceof EntityPlayerSP) 
			super.setupTerrain(viewEntity, partialTicks, camera, frameCount, playerSpectator || ((EntityPlayerSP) Minecraft.getMinecraft().player).getOverrideNoclip());
		else super.setupTerrain(viewEntity, partialTicks, camera, frameCount, playerSpectator);
	}
}
