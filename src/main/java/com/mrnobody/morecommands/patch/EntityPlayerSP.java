package com.mrnobody.morecommands.patch;

import com.mrnobody.morecommands.wrapper.EntityCamera;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
//import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.stats.StatFileWriter;
import net.minecraft.world.World;

/**
 * The patched class of {@link net.minecraft.client.entity.EntityPlayerSP} <br>
 * This patch is needed to make several commands available and working, <br>
 * e.g. climbing on any wall, free-/freezecam, etc.
 * 
 * @author MrNobody98
 *
 */
public class EntityPlayerSP extends net.minecraft.client.entity.EntityPlayerSP {
	private boolean overrideOnLadder = false;
	private boolean freezeCam = false;
	private boolean freeCam = false;
	private boolean overrideNoclip = false;
	private boolean fluidmovement = true;
	
	private boolean overrideSpectator = false;
	private float freezeCamYaw;
	private float freezeCamPitch;
	
	private float gravity = 1F;
	
	private NetHandlerPlayClient handler;
	private StatFileWriter writer;
	
    public EntityPlayerSP(Minecraft mcIn, World worldIn, NetHandlerPlayClient p_i46278_3_, StatFileWriter p_i46278_4_) {
        super(mcIn, worldIn, p_i46278_3_, p_i46278_4_);
        this.handler = p_i46278_3_;
        this.writer = p_i46278_4_;
    }

	public void setFluidMovement(boolean fluidmovement) {
		this.fluidmovement = fluidmovement;
	}
	
	public boolean getFluidMovement() {
		return this.fluidmovement;
	}
    
	@Override
	public boolean isInWater() {
		if (!this.fluidmovement) return false;
		return super.isInWater();
	}
	
	@Override
	public boolean isInLava() {
		if (!this.fluidmovement) return false;
		return super.isInLava();
	}
    
    public StatFileWriter getWriter() {
    	return this.writer;
    }
    
    public NetHandlerPlayClient getHandler() {
    	return this.handler;
    }
    
    @Override
    public boolean isEntityInsideOpaqueBlock() {
        return !this.overrideNoclip && super.isEntityInsideOpaqueBlock();
    }
    
	public void setOverrideOnLadder(boolean flag) {
		this.overrideOnLadder = flag;
	}
	
	public boolean isOnLadderOverridden() {
		return this.overrideOnLadder;
	}
	
	public void setOverrideNoclip(boolean override) {
		this.overrideNoclip = override;
	}
	
	public boolean getOverrideNoclip() {
		return this.overrideNoclip;
	}
	
	@Override
	public boolean isOnLadder() {
		if (this.overrideOnLadder && this.isCollidedHorizontally) return true;
		else return super.isOnLadder();
	}
	
	public void setFreezeCamera(boolean freezeCamera) {
		this.freezeCam = freezeCamera;
	}
	
	public boolean getFreezeCamera() {
		return this.freezeCam;
	}
	
	public void setFreeCam(boolean freeCamera) {
		this.freeCam = freeCamera;
	}
	
	public boolean getFreeCam() {
		return this.freeCam;
	}
	
	public void setFreezeCamYawAndPitch(float yaw, float pitch) {
		this.freezeCamYaw = yaw;
		this.freezeCamPitch = pitch;
	}
	
	public float getGravity() {
		return this.gravity;
	}
	
	public void setGravity(float gravity) {
		this.gravity = gravity;
	}
	
    @Override
    public boolean isUser() {
    	return !(this.freeCam || this.freezeCam);
    }
	
	@Override
	public void moveEntity(double x, double y, double z) {
		if (this.freezeCam && Minecraft.getMinecraft().getRenderViewEntity() instanceof EntityCamera) {
			((EntityCamera) Minecraft.getMinecraft().getRenderViewEntity()).setFreezeCamera(0, 0, 0, this.freezeCamYaw, this.freezeCamPitch);
		}
		else if (this.freeCam && Minecraft.getMinecraft().getRenderViewEntity() instanceof EntityCamera) {
			((EntityCamera) Minecraft.getMinecraft().getRenderViewEntity()).setFreeCamera(x, y, z, this.rotationYaw, this.rotationPitch);
			return;
		}
		
		super.moveEntity(x, y, z);
	}
	
	@Override
	protected float getJumpUpwardsMotion() {
		return super.getJumpUpwardsMotion() * this.gravity;
	}
	
	@Override
	public void onUpdate() {
		if (this.overrideNoclip) {
			this.overrideSpectator = true;
			super.onUpdate();
			this.overrideSpectator = false;
		}
		else super.onUpdate();
	}
	
	@Override
	public boolean isSpectator() {
		return this.overrideSpectator || super.isSpectator();
	}
	
	@Override
	public boolean isCurrentViewEntity() {
		return Minecraft.getMinecraft().getRenderViewEntity() instanceof EntityCamera || super.isCurrentViewEntity();
	}
}
