package com.mrnobody.morecommands.patch;

import com.mrnobody.morecommands.command.AbstractCommand.ResultAcceptingCommandSender;
import com.mrnobody.morecommands.util.EntityCamera;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.potion.Potion;
//import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.stats.StatFileWriter;
import net.minecraft.stats.StatList;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

/**
 * The patched class of {@link net.minecraft.client.entity.EntityPlayerSP} <br>
 * This patch is needed to make several commands available and working, <br>
 * e.g. climbing on any wall, free-/freezecam, etc.
 * 
 * @author MrNobody98
 *
 */
public class EntityPlayerSP extends net.minecraft.client.entity.EntityPlayerSP implements ResultAcceptingCommandSender {
	private boolean overrideOnLadder = false;
	private boolean freezeCam = false;
	private boolean freeCam = false;
	private boolean overrideNoclip = false;
	private boolean fluidmovement = true;
	
	private boolean overrideSpectator = false;
	private float freezeCamYaw;
	private float freezeCamPitch;
	
	private double gravity = 1.0D;
	
	private NetHandlerPlayClient handler;
	private StatFileWriter writer;

	private String capturedCommandResult = null, cmdSentToServer = null;
	private StringBuilder capturedCommandMessages = new StringBuilder();
	private boolean captureNextCommandResult = false;	
	
    public EntityPlayerSP(Minecraft mcIn, World worldIn, NetHandlerPlayClient p_i46278_3_, StatFileWriter p_i46278_4_) {
        super(mcIn, worldIn, p_i46278_3_, p_i46278_4_);
        this.handler = p_i46278_3_;
        this.writer = p_i46278_4_;
    }

    /**
     * This method should be invoked before this entity is passed to {@link net.minecraft.command.ICommandManager#executeCommand(net.minecraft.command.ICommandSender, String)}. 
     * Invoking this method will make this entity capture the result of the command execution. Result either means the return value
     * of the {@link com.mrnobody.morecommands.command.AbstractCommand#execute(com.mrnobody.morecommands.command.CommandSender, String[])} method
     * if the command is a subclass of this class or, if it is not, or if the return value is null, the chat messages sent via the
     * {@link #addChatMessage(IChatComponent)} method. After command execution, the captured results must be reset via the
     * {@link #getCapturedCommandResult()} method. This method also returns the result. 
     */
    public void setCaptureNextCommandResult() {
    	this.captureNextCommandResult = true;
    }
    
    @Override
    public void addChatMessage(IChatComponent message) {
    	if (this.captureNextCommandResult) this.capturedCommandMessages.append(" " + message.getUnformattedText());
    	super.addChatMessage(message);
    }
    
    @Override
    public void setCommandResult(String commandName, String[] args, String result) {
    	if (this.captureNextCommandResult && result != null) 
    		this.capturedCommandResult = result;
    }
    
    /**
     * Disables capturing of command results and resets and returns them.
     * 
     * @return the captured result of the command execution (requires enabling capturing before command execution via
     * 			{@link #setCaptureNextCommandResult()}. Will never be null
     * @see #setCaptureNextCommandResult()
     */
    public String getCapturedCommandResult() {
    	String result = null;
    	
    	if (this.capturedCommandResult != null) result = this.capturedCommandResult;
    	else result = this.capturedCommandMessages.toString().trim();
    	
    	this.capturedCommandResult = this.cmdSentToServer = null;
    	this.capturedCommandMessages = new StringBuilder();
    	this.captureNextCommandResult = false;
    	
    	return result;
    }
    
    @Override
    public void sendChatMessage(String message) {
    	if (this.captureNextCommandResult && message.startsWith("/")) this.cmdSentToServer = message;
    	else super.sendChatMessage(message);
    }
    
    /**
     * @return the last command that should have been sent to the server via {@link #sendChatMessage(String)}
     * (if command result capturing is enabled via {@link #setCaptureNextCommandResult()}
     */
    public String getCmdSentToServer() {
    	return this.cmdSentToServer;
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
	
	public double getGravity() {
		return this.gravity;
	}
	
	public void setGravity(double gravity) {
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
