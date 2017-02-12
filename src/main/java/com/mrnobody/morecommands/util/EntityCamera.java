package com.mrnobody.morecommands.util;

import com.mrnobody.morecommands.patch.EntityPlayerSP;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.stats.StatFileWriter;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovementInput;
import net.minecraft.world.World;

/**
 * The camera used for the free- and freezecam commands
 * 
 * @author MrNobody98
 */
public class EntityCamera extends EntityPlayerSP {

    public EntityCamera(Minecraft mcIn, World worldIn, NetHandlerPlayClient p_i46278_3_, StatFileWriter p_i46278_4_, MovementInput input) {
        super(mcIn, worldIn, p_i46278_3_, p_i46278_4_);
		this.movementInput = input;
	}
	
	@Override
	public boolean canBePushed() {
		return false;
	}
	
	@Override
	public void onEntityUpdate() {}

	
	@Override
	public void onUpdate() {}
	
	@Override
	public void onDeath(DamageSource source) {}
	
	@Override
	public boolean isEntityAlive() {
		return true;
	}
	
	/**
	 * Sets the freezecam data
	 */
	public void setFreezeCamera(double motionX, double motionY, double motionZ, float yaw, float pitch) {
		this.lastTickPosX = this.posX;
		this.lastTickPosY = this.posY;
		this.lastTickPosZ = this.posZ;
		this.posX += motionX;
		this.posY += motionY;
		this.posZ += motionZ;
		this.prevRotationYaw = this.rotationYaw;
		this.prevRotationPitch = this.rotationPitch;
		this.rotationYaw = yaw;
		this.rotationPitch = pitch;
	}
	
	/**
	 * Sets the freecam data
	 */
	public void setFreeCamera(double motionX, double motionY, double motionZ, float yaw, float pitch) {
		if (this.movementInput.sneak && this.movementInput.jump) this.motionY = 0;
		else if (this.movementInput.jump && this.motionY < this.capabilities.getFlySpeed()) this.motionY += 0.30D;
		else if (this.movementInput.sneak && this.motionY > -this.capabilities.getFlySpeed()) this.motionY -= 0.30D;
		else if (!this.movementInput.jump && !this.movementInput.sneak) this.motionY = this.motionY > 0 ? this.motionY - 0.30D : this.motionY < 0 ? this.motionY + 0.30D : this.motionY;
		
		this.lastTickPosX = this.posX;
		this.lastTickPosY = this.posY;
		this.lastTickPosZ = this.posZ;
		this.posX += motionX;
		this.posY += this.motionY;
		this.posZ += motionZ;
		this.prevRotationYaw = this.rotationYaw;
		this.prevRotationPitch = this.rotationPitch;
		this.rotationYaw = yaw;
		this.rotationPitch = pitch;
	}
}
