package com.mrnobody.morecommands.patch;

import java.util.Collection;
import java.util.Iterator;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;

import com.mojang.authlib.GameProfile;

/**
 * The patched class of {@link net.minecraft.entity.player.EntityPlayerMP} <br>
 * This patch is needed to make several commands available and working, <br>
 * e.g. instant kill, always critical hits, keeping inventory on death, etc.
 * 
 * @author MrNobody98
 *
 */
public class EntityPlayerMP extends net.minecraft.entity.player.EntityPlayerMP {
	private boolean instantkill = false;
	private boolean criticalhit = false;
	private boolean instantmine = false;
	private boolean keepinventory = false;
	private boolean infnitesprinting = false;
	
	public EntityPlayerMP(MinecraftServer p_i45285_1_, WorldServer p_i45285_2_, GameProfile p_i45285_3_, ItemInWorldManager p_i45285_4_) {
		super(p_i45285_1_, p_i45285_2_, p_i45285_3_, p_i45285_4_);
        this.inventory = new InventoryPlayer(this);
        this.inventoryContainer = new ContainerPlayer(this.inventory, !p_i45285_2_.isRemote, this);
        this.openContainer = this.inventoryContainer;
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
	

	public void setInfniteSprinting(boolean sprinting) {
		this.infnitesprinting = sprinting;
	}
	
	public boolean getInfniteSprinting() {
		return this.infnitesprinting;
	}
	
	@Override
	public void onLivingUpdate() {
		if (this.infnitesprinting) this.setSprinting(true);
		super.onLivingUpdate();
	}
	
	@Override
	public void attackTargetEntityWithCurrentItem(Entity entity) {
	      if (this.instantkill) {
	          entity.attackEntityFrom(DamageSource.causePlayerDamage(this), Integer.MAX_VALUE);
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
