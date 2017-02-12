package com.mrnobody.morecommands.patch;

import com.mojang.authlib.GameProfile;
import com.mrnobody.morecommands.command.AbstractCommand.ResultAcceptingCommandSender;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.network.play.server.SPacketCombatEvent;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;

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
	
	private float gravity = 1F;

	private String capturedCommandResult = null;
	private StringBuilder capturedCommandMessages = new StringBuilder();
	private boolean captureNextCommandResult = false;
	
	public EntityPlayerMP(MinecraftServer p_i45285_1_, WorldServer p_i45285_2_, GameProfile p_i45285_3_, PlayerInteractionManager p_i45285_4_) {
		super(p_i45285_1_, p_i45285_2_, p_i45285_3_, p_i45285_4_);
	}

    /**
     * This method should be invoked before this entity is passed to {@link net.minecraft.command.ICommandManager#executeCommand(net.minecraft.command.ICommandSender, String)}. 
     * Invoking this method will make this entity capture the result of the command exection. Result either means the return value
     * of the {@link com.mrnobody.morecommands.command.AbstractCommand#processCommand(com.mrnobody.morecommands.wrapper.CommandSender, String[])} method
     * if the command is a subclass of this class or, if it is not, or if the return value is null, the chat messages sent via the
     * {@link #addChatMessage(IChatComponent)} method. After command execution, the captured results must be reset via the
     * {@link #getCapturedCommandResult()} method. This method also returns the result. 
     */
    public void setCaptureNextCommandResult() {
    	this.captureNextCommandResult = true;
    }
    
    @Override
    public void addChatMessage(ITextComponent message) {
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
	
	public float getGravity() {
		return this.gravity;
	}
	
	public void setGravity(float gravity) {
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
	public boolean isInLava() {
		if (!this.fluidmovement) return false;
		return super.isInLava();
	}
	
	@Override
	protected float getJumpUpwardsMotion() {
		return super.getJumpUpwardsMotion() * this.gravity;
	}
	
	@Override
	public void onLivingUpdate() {
		if (this.infinitesprinting) this.setSprinting(true);
		super.onLivingUpdate();
	}
	
	@Override
	public void attackTargetEntityWithCurrentItem(Entity entity) {
	      if (this.instantkill && this.interactionManager.getGameType() != WorldSettings.GameType.SPECTATOR) {
	    	  IAttributeInstance attackDamage = this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
	    	  double oldValue = attackDamage.getBaseValue();
	    	  attackDamage.setBaseValue(Double.MAX_VALUE / 2);
	    	  super.attackTargetEntityWithCurrentItem(entity);
	          attackDamage.setBaseValue(oldValue);
	          return;
	       } else if (this.criticalhit && this.interactionManager.getGameType() != WorldSettings.GameType.SPECTATOR) {
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
	public float getBreakSpeed(IBlockState state, BlockPos pos) {
		if (this.instantmine) return Float.MAX_VALUE;
		else return super.getBreakSpeed(state, pos);
	}
	
	@Override
    public void onDeath(DamageSource cause)
    {
        if (net.minecraftforge.common.ForgeHooks.onLivingDeath(this, cause)) return;
        boolean flag = this.worldObj.getGameRules().getBoolean("showDeathMessages");
        this.playerNetServerHandler.sendPacket(new SPacketCombatEvent(this.getCombatTracker(), SPacketCombatEvent.Event.ENTITY_DIED, flag));

        if (flag)
        {
            Team team = this.getTeam();

            if (team != null && team.getDeathMessageVisibility() != Team.EnumVisible.ALWAYS)
            {
                if (team.getDeathMessageVisibility() == Team.EnumVisible.HIDE_FOR_OTHER_TEAMS)
                {
                    this.mcServer.getPlayerList().sendMessageToAllTeamMembers(this, this.getCombatTracker().getDeathMessage());
                }
                else if (team.getDeathMessageVisibility() == Team.EnumVisible.HIDE_FOR_OWN_TEAM)
                {
                    this.mcServer.getPlayerList().sendMessageToTeamOrAllPlayers(this, this.getCombatTracker().getDeathMessage());
                }
            }
            else
            {
                this.mcServer.getPlayerList().sendChatMsg(this.getCombatTracker().getDeathMessage());
            }
        }

        if (!this.keepinventory && !this.worldObj.getGameRules().getBoolean("keepInventory") && !this.isSpectator())
        {
            captureDrops = true;
            capturedDrops.clear();

            this.inventory.dropAllItems();

            captureDrops = false;
            net.minecraftforge.event.entity.player.PlayerDropsEvent event = new net.minecraftforge.event.entity.player.PlayerDropsEvent(this, cause, capturedDrops, recentlyHit > 0);
            if (!net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event))
            {
                for (net.minecraft.entity.item.EntityItem item : capturedDrops)
                {
                    this.worldObj.spawnEntityInWorld(item);
                }
            }
        }

        for (ScoreObjective scoreobjective : this.worldObj.getScoreboard().getObjectivesFromCriteria(IScoreCriteria.deathCount))
        {
            Score score = this.getWorldScoreboard().getOrCreateScore(this.getName(), scoreobjective);
            score.func_96648_a();
        }

        EntityLivingBase entitylivingbase = this.getAttackingEntity();

        if (entitylivingbase != null)
        {
            EntityList.EntityEggInfo entitylist$entityegginfo = (EntityList.EntityEggInfo)EntityList.entityEggs.get(EntityList.getEntityString(entitylivingbase));

            if (entitylist$entityegginfo != null)
            {
                this.addStat(entitylist$entityegginfo.field_151513_e);
            }

            entitylivingbase.addToPlayerScore(this, this.scoreValue);
        }

        this.addStat(StatList.deaths);
        this.takeStat(StatList.timeSinceDeath);
        this.getCombatTracker().reset();
    }
}
