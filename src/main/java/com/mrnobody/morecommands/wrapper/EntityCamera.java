package com.mrnobody.morecommands.wrapper;

import java.util.List;

import org.lwjgl.input.Keyboard;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.stats.StatFileWriter;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovementInput;
import net.minecraft.util.ReportedException;
import net.minecraft.util.Session;
import net.minecraft.world.World;

import com.mrnobody.morecommands.patch.EntityClientPlayerMP;

/**
 * The camera used for the free- and freezecam commands
 * 
 * @author MrNobody98
 */
public class EntityCamera extends EntityClientPlayerMP {
	public EntityCamera(Minecraft minecraft, World world, Session session, NetHandlerPlayClient netClientHandler, StatFileWriter statFileWriter, MovementInput input) {
		super(minecraft, world, session, netClientHandler, statFileWriter);
		this.yOffset = 1.62F;
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
	
	//@Override
	//public void onLivingUpdate() {}
	
	//@Override
	//public void updateEntityActionState() {}
	
	//@Override
	//public void moveEntityWithHeading(float p_70612_1_, float p_70612_2_) {}
	
	//@Override
	//public void jump() {}
	
	//@Override
	//protected void updateFallState(double p_70064_1_, boolean p_70064_3_) {}
	
	//@Override
	//protected void fall(float p_70069_1_) {}
	
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
		else if (this.movementInput.sneak && this.motionY > -this.capabilities.getFlySpeed()) this.motionY -= 0.45D;
		else if (!this.movementInput.jump && !this.movementInput.sneak) this.motionY = this.motionY > 0 ? this.motionY - 0.30D : this.motionY < 0 ? this.motionY + 0.45D : this.motionY;
		
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
