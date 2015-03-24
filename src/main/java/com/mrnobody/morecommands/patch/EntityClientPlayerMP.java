package com.mrnobody.morecommands.patch;

import com.mrnobody.morecommands.wrapper.EntityCamera;

import net.minecraft.potion.Potion;
import net.minecraft.stats.StatFileWriter;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Session;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;

/**
 * The patched class of {@link net.minecraft.client.entity.EntityClientPlayerMP} <br>
 * This patch is needed to make several commands available and working, <br>
 * e.g. climbing on any wall, free-/freezecam, etc.
 * 
 * @author MrNobody98
 *
 */
public class EntityClientPlayerMP extends net.minecraft.client.entity.EntityClientPlayerMP {
	private boolean overrideOnLadder = false;
	private boolean freezeCam = false;
	private boolean freeCam = false;
	
	private float freezeCamYaw;
	private float freezeCamPitch;
	
	private double gravity = 1.0D;
	
	private NetHandlerPlayClient handler;
	private StatFileWriter writer;
	
    public EntityClientPlayerMP(Minecraft minecraft, World world, Session session, NetHandlerPlayClient netClientHandler, StatFileWriter statFileWriter) {
        super(minecraft, world, session, netClientHandler, statFileWriter);
        this.inventory = new InventoryPlayer(this);
        this.inventoryContainer = new ContainerPlayer(this.inventory, !world.isRemote, this);
        this.openContainer = this.inventoryContainer;
    }
    
    public StatFileWriter getWriter() {
    	return this.writer;
    }
    
    public NetHandlerPlayClient getHandler() {
    	return this.handler;
    }
    
    @Override
    public boolean isEntityInsideOpaqueBlock() {
        return !this.noClip && super.isEntityInsideOpaqueBlock();
    }
    
	
	public void OverrideOnLadder(boolean flag) {
		this.overrideOnLadder = flag;
	}
	
	public boolean isOnLadderOverridden() {
		return this.overrideOnLadder;
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
	
	public double getGravity() {
		return this.gravity;
	}
	
	public void setGravity(double gravity) {
		this.gravity = gravity;
	}
	
	@Override
	public void moveEntity(double x, double y, double z) {
		if (this.freezeCam && Minecraft.getMinecraft().renderViewEntity instanceof EntityCamera) {
			((EntityCamera) Minecraft.getMinecraft().renderViewEntity).setFreezeCamera(0, 0, 0, this.freezeCamYaw, this.freezeCamPitch);
		}
		else if (this.freeCam && Minecraft.getMinecraft().renderViewEntity instanceof EntityCamera) {
			((EntityCamera) Minecraft.getMinecraft().renderViewEntity).setFreeCamera(x, y, z, this.rotationYaw, this.rotationPitch);
			return;
		}
		
		super.moveEntity(x, y, z);
	}
	
	@Override
	public void jump() {
		if (this.gravity > 1.0D) {
			this.motionY = 0.41999998688697815D * this.gravity;
			
	        if (this.isPotionActive(Potion.jump))
	        {
	            this.motionY += (double)((float)(this.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F);
	        }

	        if (this.isSprinting())
	        {
	            float f = this.rotationYaw * 0.017453292F;
	            this.motionX -= (double)(MathHelper.sin(f) * 0.2F);
	            this.motionZ += (double)(MathHelper.cos(f) * 0.2F);
	        }

	        this.isAirBorne = true;
	        ForgeHooks.onLivingJump(this);
	        
	        this.addStat(StatList.jumpStat, 1);

	        if (this.isSprinting())
	        {
	            this.addExhaustion(0.8F);
	        }
	        else
	        {
	            this.addExhaustion(0.2F);
	        }
			
			return;
		}
		
		super.jump();
	}
}
