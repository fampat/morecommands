package com.mrnobody.morecommands.patch;

import java.util.Collection;
import java.util.Iterator;

import com.mojang.authlib.GameProfile;
import com.mrnobody.morecommands.command.AbstractCommand.ResultAcceptingCommandSender;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.potion.Potion;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;

/**
 * The patched class of {@link net.minecraft.entity.player.EntityPlayerMP} <br>
 * This patch is needed to make several commands available and working, <br>
 * e.g. instant kill, always critical hits, keeping inventory on death, etc.
 * 
 * @author MrNobody98
 *
 */
public class EntityPlayerMP extends net.minecraft.entity.player.EntityPlayerMP implements ResultAcceptingCommandSender {
	private boolean instantkill = false;
	private boolean criticalhit = false;
	private boolean instantmine = false;
	private boolean keepinventory = false;
	private boolean infinitesprinting = false;
	private boolean fluidmovement = true;
	private boolean overrideOnLadder = false;
	
	private double gravity = 1.0D;
	
	private String capturedCommandResult = null;
	private StringBuilder capturedCommandMessages = new StringBuilder();
	private boolean captureNextCommandResult = false;
	
	public EntityPlayerMP(MinecraftServer p_i45285_1_, WorldServer p_i45285_2_, GameProfile p_i45285_3_, ItemInWorldManager p_i45285_4_) {
		super(p_i45285_1_, p_i45285_2_, p_i45285_3_, p_i45285_4_);
	}
	
    /**
     * This method should be invoked before this entity is passed to {@link net.minecraft.command.ICommandManager#executeCommand(net.minecraft.command.ICommandSender, String)}. 
     * Invoking this method will make this entity capture the result of the command exection. Result either means the return value
     * of the {@link com.mrnobody.morecommands.command.AbstractCommand#execute(com.mrnobody.morecommands.wrapper.CommandSender, String[])} method
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
    	
    	this.capturedCommandResult = null;
    	this.capturedCommandMessages = new StringBuilder();
    	this.captureNextCommandResult = false;
    	
    	return result;
    }
	
	public boolean getCriticalHit() {
		return this.criticalhit;
	}
	
	public boolean getInstantkill() {
		return this.instantkill;
	}
	
	public boolean getInstantmine() {
		return this.instantmine;
	}
	
	public boolean getKeepInventory() {
		return this.keepinventory;
	}
	
	public void setCriticalhit(boolean criticalhit) {
		this.criticalhit = criticalhit;
	}
	
	public void setInstantkill(boolean instantkill) {
		this.instantkill = instantkill;
	}
	
	public void setInstantmine(boolean instantmine) {
		this.instantmine = instantmine;
	}
	
	public void setKeepInventory(boolean keepinventory) {
		this.keepinventory = keepinventory;
	}
	

	public void setInfiniteSprinting(boolean sprinting) {
		this.infinitesprinting = sprinting;
	}
	
	public boolean getInfiniteSprinting() {
		return this.infinitesprinting;
	}
	
	public void setFluidMovement(boolean fluidmovement) {
		this.fluidmovement = fluidmovement;
	}
	
	public boolean getFluidMovement() {
		return this.fluidmovement;
	}
	
	public double getGravity() {
		return this.gravity;
	}
	
	public void setGravity(double gravity) {
		this.gravity = gravity;
	}
	
	public void setOverrideOnLadder(boolean override) {
		this.overrideOnLadder = override;
	}
	
	public boolean overrideOnLadder() {
		return this.overrideOnLadder;
	}
	
	@Override
	public boolean isOnLadder() {
		if (this.overrideOnLadder && this.isCollidedHorizontally) return true;
		else return super.isOnLadder();
	}
	
	@Override
	public boolean isInWater() {
		if (!this.fluidmovement) return false;
		return super.isInWater();
	}
	
	@Override
	public boolean handleLavaMovement() {
		if (!this.fluidmovement) return false;
		return super.handleLavaMovement();
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
	public void onLivingUpdate() {
		if (this.infinitesprinting) this.setSprinting(true);
		super.onLivingUpdate();
	}
	
	@Override
	public void attackTargetEntityWithCurrentItem(Entity entity) {
	      if (this.instantkill) {
	    	  IAttributeInstance attackDamage = this.getEntityAttribute(SharedMonsterAttributes.attackDamage);
	    	  double oldValue = attackDamage.getBaseValue();
	    	  attackDamage.setBaseValue(Double.MAX_VALUE / 2);
	    	  super.attackTargetEntityWithCurrentItem(entity);
	          attackDamage.setBaseValue(oldValue);
	          return;
	       } else if (this.criticalhit) {
	          double my = this.motionY;
	          boolean og = this.onGround;
	          boolean iw = this.inWater;
	          float fd = this.fallDistance;
	          super.motionY = -0.1D;
	          super.inWater = false;
	          super.onGround = false;
	          super.fallDistance = 0.1F;
	          super.attackTargetEntityWithCurrentItem(entity);
	          this.motionY = my;
	          this.onGround = og;
	          this.inWater = iw;
	          this.fallDistance = fd;
	          return;
	       }
	       super.attackTargetEntityWithCurrentItem(entity);
	}
	
	@Override
	public float getBreakSpeed(Block p_146096_1_, boolean p_146096_2_, int meta, int x, int y, int z) {
		if (this.instantmine) return Float.MAX_VALUE;
		else return super.getBreakSpeed(p_146096_1_, p_146096_2_, meta, x, y, z);
	}
	
	@Override
    public void onDeath(DamageSource p_70645_1_)
    {
        if (ForgeHooks.onLivingDeath(this, p_70645_1_)) return;
        this.mcServer.getConfigurationManager().sendChatMsg(this.func_110142_aN().func_151521_b());

        if (!this.keepinventory && !this.worldObj.getGameRules().getGameRuleBooleanValue("keepInventory"))
        {
            captureDrops = true;
            capturedDrops.clear();

            this.inventory.dropAllItems();

            captureDrops = false;
            PlayerDropsEvent event = new PlayerDropsEvent(this, p_70645_1_, capturedDrops, recentlyHit > 0);
            if (!MinecraftForge.EVENT_BUS.post(event))
            {
                for (EntityItem item : capturedDrops)
                {
                    joinEntityItemWithWorld(item);
                }
            }
        }

        Collection collection = this.worldObj.getScoreboard().func_96520_a(IScoreObjectiveCriteria.deathCount);
        Iterator iterator = collection.iterator();

        while (iterator.hasNext())
        {
            ScoreObjective scoreobjective = (ScoreObjective)iterator.next();
            Score score = this.getWorldScoreboard().func_96529_a(this.getCommandSenderName(), scoreobjective);
            score.func_96648_a();
        }

        EntityLivingBase entitylivingbase = this.func_94060_bK();

        if (entitylivingbase != null)
        {
            int i = EntityList.getEntityID(entitylivingbase);
            EntityList.EntityEggInfo entityegginfo = (EntityList.EntityEggInfo)EntityList.entityEggs.get(Integer.valueOf(i));

            if (entityegginfo != null)
            {
                this.addStat(entityegginfo.field_151513_e, 1);
            }

            entitylivingbase.addToPlayerScore(this, this.scoreValue);
        }

        this.addStat(StatList.deathsStat, 1);
        this.func_110142_aN().func_94549_h();
    }
}
