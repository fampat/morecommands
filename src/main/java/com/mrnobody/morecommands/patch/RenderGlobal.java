package com.mrnobody.morecommands.patch;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;

/**
 * The patched class of {@link net.minecraft.client.renderer.RenderGlobal} <br>
 * When using noclip the terrain isn't rendered if the player is not spectator.
 * The patch of this class overrides the spectator check
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
		if (Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().thePlayer instanceof EntityPlayerSP) 
			super.setupTerrain(viewEntity, partialTicks, camera, frameCount, playerSpectator || ((EntityPlayerSP) Minecraft.getMinecraft().thePlayer).getOverrideNoclip());
		else super.setupTerrain(viewEntity, partialTicks, camera, frameCount, playerSpectator);
	}
}
