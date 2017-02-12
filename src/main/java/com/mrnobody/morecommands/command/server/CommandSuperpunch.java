package com.mrnobody.morecommands.command.server;

import java.util.Random;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.event.EventHandler;
import com.mrnobody.morecommands.event.Listeners.EventListener;
import com.mrnobody.morecommands.settings.ServerPlayerSettings;

import net.minecraft.command.ICommandSender;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

@Command(
	name = "superpunch",
	description = "command.superpunch.description",
	example = "command.superpunch.example",
	syntax = "command.superpunch.syntax",
	videoURL = "command.superpunch.videoURL"
	)
public class CommandSuperpunch extends StandardCommand implements ServerCommandProperties, EventListener<AttackEntityEvent> {
	private Random rand = new Random();
	
	public CommandSuperpunch() {
		EventHandler.PLAYER_ATTACK.register(this);
	}
	
	@Override
	public void onEvent(AttackEntityEvent event) {
		if (event.getEntity() instanceof EntityPlayerMP) {
			ServerPlayerSettings settings = getPlayerSettings((EntityPlayerMP) event.getEntity());
			
			if (settings.superpunch > 0) {
				event.setCanceled(true);
				this.attackWithSuperpunch((EntityPlayer) event.getEntity(), event.getTarget(), settings.superpunch);
			}
		}
	}

	@Override
	public String getCommandName() {
		return "superpunch";
	}

	@Override
	public String getCommandUsage() {
		return "command.superpunch.syntax";
	}
	
	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		ServerPlayerSettings settings = getPlayerSettings(getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class));
        	
        if (params.length > 0) {
        	if (params[0].equalsIgnoreCase("reset")) {
        		settings.superpunch = -1;
        		sender.sendLangfileMessage("command.superpunch.reset");
        	}
        	else {
        		try {
        			settings.superpunch = Integer.parseInt(params[0]);
        			sender.sendLangfileMessage("command.superpunch.success");
        		}
        		catch (NumberFormatException nfe) {
        			throw new CommandException("command.superpunch.NAN", sender);
        		}
        	}
        }
        else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
        
        return null;
	}

	@Override
	public CommandRequirement[] getRequirements() {
		return new CommandRequirement[0];
	}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}

	@Override
	public int getDefaultPermissionLevel(String[] args) {
		return 2;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return isSenderOfEntityType(sender, EntityPlayerMP.class);
	}
	
	 private void attackWithSuperpunch(EntityPlayer player, Entity target, int factor)
	    {
	        //if (!net.minecraftforge.common.ForgeHooks.onPlayerAttackTarget(player, target)) return;
		 if (target.canBeAttackedWithItem())
	        {
	            if (!target.hitByEntity(player))
	            {
	                float f = (float)player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
	                float f1;

	                if (target instanceof EntityLivingBase)
	                {
	                    f1 = EnchantmentHelper.getModifierForCreature(player.getHeldItemMainhand(), ((EntityLivingBase)target).getCreatureAttribute());
	                }
	                else
	                {
	                    f1 = EnchantmentHelper.getModifierForCreature(player.getHeldItemMainhand(), EnumCreatureAttribute.UNDEFINED);
	                }

	                float f2 = player.getCooledAttackStrength(0.5F);
	                f = f * (0.2F + f2 * f2 * 0.8F);
	                f1 = f1 * f2;
	                player.resetCooldown();

	                if (f > 0.0F || f1 > 0.0F)
	                {
	                    boolean flag = f2 > 0.9F;
	                    boolean flag1 = false;
	                    int i = 0;
	                    i = i + EnchantmentHelper.getKnockbackModifier(player);

	                    if (player.isSprinting() && flag)
	                    {
	                        player.worldObj.playSound((EntityPlayer)null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, player.getSoundCategory(), 1.0F, 1.0F);
	                        ++i;
	                        flag1 = true;
	                    }

	                    //SUPERPUNCH :D
	                    i *= factor;
	                    
	                    boolean flag2 = flag && player.fallDistance > 0.0F && !player.onGround && !player.isOnLadder() && !player.isInWater() && !player.isPotionActive(MobEffects.BLINDNESS) && !player.isRiding() && target instanceof EntityLivingBase;
	                    flag2 = flag2 && !player.isSprinting();

	                    if (flag2)
	                    {
	                        f *= 1.5F;
	                    }

	                    f = f + f1;
	                    boolean flag3 = false;
	                    double d0 = (double)(player.distanceWalkedModified - player.prevDistanceWalkedModified);

	                    if (flag && !flag2 && !flag1 && player.onGround && d0 < (double)player.getAIMoveSpeed())
	                    {
	                        ItemStack itemstack = player.getHeldItem(EnumHand.MAIN_HAND);

	                        if (itemstack.getItem() instanceof ItemSword)
	                        {
	                            flag3 = true;
	                        }
	                    }

	                    float f3 = 0.0F;
	                    boolean flag4 = false;
	                    int j = EnchantmentHelper.getFireAspectModifier(player);

	                    if (target instanceof EntityLivingBase)
	                    {
	                        f3 = ((EntityLivingBase)target).getHealth();

	                        if (j > 0 && !target.isBurning())
	                        {
	                            flag4 = true;
	                            target.setFire(1);
	                        }
	                    }

	                    double d1 = target.motionX;
	                    double d2 = target.motionY;
	                    double d3 = target.motionZ;
	                    boolean flag5 = target.attackEntityFrom(DamageSource.causePlayerDamage(player), f);

	                    if (flag5)
	                    {
	                        if (i > 0)
	                        {
	                            if (target instanceof EntityLivingBase)
	                            {
	                                ((EntityLivingBase)target).knockBack(player, (float)i * 0.5F, (double)MathHelper.sin(player.rotationYaw * 0.017453292F), (double)(-MathHelper.cos(player.rotationYaw * 0.017453292F)));
	                            }
	                            else
	                            {
	                                target.addVelocity((double)(-MathHelper.sin(player.rotationYaw * 0.017453292F) * (float)i * 0.5F), 0.1D, (double)(MathHelper.cos(player.rotationYaw * 0.017453292F) * (float)i * 0.5F));
	                            }

	                            player.motionX *= 0.6D;
	                            player.motionZ *= 0.6D;
	                            player.setSprinting(false);
	                        }

	                        if (flag3)
	                        {
	                            for (EntityLivingBase entitylivingbase : player.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, target.getEntityBoundingBox().expand(1.0D, 0.25D, 1.0D)))
	                            {
	                                if (entitylivingbase != player && entitylivingbase != target && !player.isOnSameTeam(entitylivingbase) && player.getDistanceSqToEntity(entitylivingbase) < 9.0D)
	                                {
	                                    entitylivingbase.knockBack(player, 0.4F, (double)MathHelper.sin(player.rotationYaw * 0.017453292F), (double)(-MathHelper.cos(player.rotationYaw * 0.017453292F)));
	                                    entitylivingbase.attackEntityFrom(DamageSource.causePlayerDamage(player), 1.0F);
	                                }
	                            }

	                            player.worldObj.playSound((EntityPlayer)null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, player.getSoundCategory(), 1.0F, 1.0F);
	                            player.spawnSweepParticles();
	                        }

	                        if (target instanceof EntityPlayerMP && target.velocityChanged)
	                        {
	                            ((EntityPlayerMP)target).connection.sendPacket(new SPacketEntityVelocity(target));
	                            target.velocityChanged = false;
	                            target.motionX = d1;
	                            target.motionY = d2;
	                            target.motionZ = d3;
	                        }

	                        if (flag2)
	                        {
	                            player.worldObj.playSound((EntityPlayer)null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, player.getSoundCategory(), 1.0F, 1.0F);
	                            player.onCriticalHit(target);
	                        }

	                        if (!flag2 && !flag3)
	                        {
	                            if (flag)
	                            {
	                                player.worldObj.playSound((EntityPlayer)null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, player.getSoundCategory(), 1.0F, 1.0F);
	                            }
	                            else
	                            {
	                                player.worldObj.playSound((EntityPlayer)null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, player.getSoundCategory(), 1.0F, 1.0F);
	                            }
	                        }

	                        if (f1 > 0.0F)
	                        {
	                            player.onEnchantmentCritical(target);
	                        }

	                        if (f >= 18.0F)
	                        {
	                            player.addStat(AchievementList.OVERKILL);
	                        }

	                        player.setLastAttacker(target);

	                        if (target instanceof EntityLivingBase)
	                        {
	                            EnchantmentHelper.applyThornEnchantments((EntityLivingBase)target, player);
	                        }

	                        EnchantmentHelper.applyArthropodEnchantments(player, target);
	                        ItemStack itemstack1 = player.getHeldItemMainhand();
	                        Entity entity = target;

	                        if (target instanceof EntityDragonPart)
	                        {
	                            IEntityMultiPart ientitymultipart = ((EntityDragonPart)target).entityDragonObj;

	                            if (ientitymultipart instanceof EntityLivingBase)
	                            {
	                                entity = (EntityLivingBase)ientitymultipart;
	                            }
	                        }

	                        if (!itemstack1.func_190926_b() && entity instanceof EntityLivingBase)
	                        {
	                            ItemStack beforeHitCopy = itemstack1.copy();
	                            itemstack1.hitEntity((EntityLivingBase)entity, player);

	                            if (itemstack1.func_190926_b())
	                            {
	                                player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.field_190927_a);
	                                net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, beforeHitCopy, EnumHand.MAIN_HAND);
	                            }
	                        }

	                        if (target instanceof EntityLivingBase)
	                        {
	                            float f4 = f3 - ((EntityLivingBase)target).getHealth();
	                            player.addStat(StatList.DAMAGE_DEALT, Math.round(f4 * 10.0F));

	                            if (j > 0)
	                            {
	                                target.setFire(j * 4);
	                            }

	                            if (player.worldObj instanceof WorldServer && f4 > 2.0F)
	                            {
	                                int k = (int)((double)f4 * 0.5D);
	                                ((WorldServer)player.worldObj).spawnParticle(EnumParticleTypes.DAMAGE_INDICATOR, target.posX, target.posY + (double)(target.height * 0.5F), target.posZ, k, 0.1D, 0.0D, 0.1D, 0.2D, new int[0]);
	                            }
	                        }

	                        player.addExhaustion(0.1F);
	                    }
	                    else
	                    {
	                        player.worldObj.playSound((EntityPlayer)null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, player.getSoundCategory(), 1.0F, 1.0F);

	                        if (flag4)
	                        {
	                            target.extinguish();
	                        }
	                    }
	                }
	            }
	        }
	    }
}
