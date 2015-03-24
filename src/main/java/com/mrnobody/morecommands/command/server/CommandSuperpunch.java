package com.mrnobody.morecommands.command.server;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listener;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
	name = "superpunch",
	description = "command.superpunch.description",
	example = "command.superpunch.example",
	syntax = "command.superpunch.syntax",
	videoURL = "command.superpunch.videoURL"
	)
public class CommandSuperpunch extends ServerCommand implements Listener<AttackEntityEvent> {
	public CommandSuperpunch() {
		EventHandler.PLAYER_ATTACK.getHandler().register(this);
	}
	
	@Override
	public void unregisterFromHandler() {
		EventHandler.PLAYER_ATTACK.getHandler().unregister(this);
	}
	
	@Override
	public void onEvent(AttackEntityEvent event) {
		if (ServerPlayerSettings.playerSettingsMapping.containsKey(event.entity) && event.entity instanceof EntityPlayer) {
			ServerPlayerSettings settings = ServerPlayerSettings.playerSettingsMapping.get(event.entity);
			
			if (settings.superpunch > 0) {
				event.setCanceled(true);
				this.attackWithSuperpunch((EntityPlayer) event.entity, event.target, settings.superpunch);
			}
		}
	}

	@Override
	public String getCommandName() {
		return "superpunch";
	}

	@Override
	public String getUsage() {
		return "command.superpunch.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		ServerPlayerSettings settings = ServerPlayerSettings.playerSettingsMapping.get(sender.getMinecraftISender());
        	
        if (params.length > 0) {
        	if (params[0].equalsIgnoreCase("reset")) {
        		settings.superpunch = -1;
        		sender.sendLangfileMessageToPlayer("command.superpunch.reset", new Object[0]);
        	}
        	else {
        		try {
        			settings.superpunch = Integer.parseInt(params[0]);
        			sender.sendLangfileMessageToPlayer("command.superpunch.success", new Object[0]);
        		}
        		catch (NumberFormatException nfe) {
        			sender.sendLangfileMessageToPlayer("command.superpunch.NAN", new Object[0]);
        		}
        	}
        }
        else sender.sendLangfileMessageToPlayer("command.superpunch.invalidUsage", new Object[0]);
	}

	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}

	@Override
	public int getPermissionLevel() {
		return 2;
	}
	
    private void attackWithSuperpunch(EntityPlayer player, Entity target, int factor)
    {
        ItemStack stack = player.getCurrentEquippedItem();
        if (stack != null && stack.getItem().onLeftClickEntity(stack, player, target))
        {
            return;
        }
        if (target.canAttackWithItem())
        {
            if (!target.hitByEntity(player))
            {
                float f = (float) player.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
                int i = 0;
                float f1 = 0.0F;

                if (target instanceof EntityLivingBase)
                {
                    f1 = EnchantmentHelper.getEnchantmentModifierLiving(player, (EntityLivingBase) target);
                    i += EnchantmentHelper.getKnockbackModifier(player, (EntityLivingBase) target);
                }

                if (player.isSprinting())
                {
                    ++i;
                }
                
                //SUPERPUNCH :D
                i = factor > 0 ? factor : i;

                if (f > 0.0F || f1 > 0.0F)
                {
                    boolean flag = player.fallDistance > 0.0F && !player.onGround && !player.isOnLadder() && !player.isInWater() && !player.isPotionActive(Potion.blindness) && player.ridingEntity == null && target instanceof EntityLivingBase;

                    if (flag && f > 0.0F)
                    {
                        f *= 1.5F;
                    }

                    f += f1;
                    boolean flag1 = false;
                    int j = EnchantmentHelper.getFireAspectModifier(player);

                    if (target instanceof EntityLivingBase && j > 0 && !target.isBurning())
                    {
                        flag1 = true;
                        target.setFire(1);
                    }

                    boolean flag2 = target.attackEntityFrom(DamageSource.causePlayerDamage(player), f);

                    if (flag2)
                    {
                        if (i > 0)
                        {
                            target.addVelocity((double)(-MathHelper.sin(player.rotationYaw * (float)Math.PI / 180.0F) * (float)i * 0.5F), 0.1D, (double)(MathHelper.cos(player.rotationYaw * (float)Math.PI / 180.0F) * (float)i * 0.5F));
                            player.motionX *= 0.6D;
                            player.motionZ *= 0.6D;
                            player.setSprinting(false);
                        }

                        if (flag)
                        {
                        	player.onCriticalHit(target);
                        }

                        if (f1 > 0.0F)
                        {
                        	player.onEnchantmentCritical(target);
                        }

                        if (f >= 18.0F)
                        {
                        	player.triggerAchievement(AchievementList.overkill);
                        }

                        player.setLastAttacker(target);

                        if (target instanceof EntityLivingBase)
                        {
                            EnchantmentHelper.func_151384_a((EntityLivingBase)target, player);
                        }

                        EnchantmentHelper.func_151385_b(player, target);
                        ItemStack itemstack = player.getCurrentEquippedItem();
                        Object object = target;

                        if (target instanceof EntityDragonPart)
                        {
                            IEntityMultiPart ientitymultipart = ((EntityDragonPart)target).entityDragonObj;

                            if (ientitymultipart != null && ientitymultipart instanceof EntityLivingBase)
                            {
                                object = (EntityLivingBase)ientitymultipart;
                            }
                        }

                        if (itemstack != null && object instanceof EntityLivingBase)
                        {
                            itemstack.hitEntity((EntityLivingBase)object, player);

                            if (itemstack.stackSize <= 0)
                            {
                            	player.destroyCurrentEquippedItem();
                            }
                        }

                        if (target instanceof EntityLivingBase)
                        {
                        	player.addStat(StatList.damageDealtStat, Math.round(f * 10.0F));

                            if (j > 0)
                            {
                                target.setFire(j * 4);
                            }
                        }

                        player.addExhaustion(0.3F);
                    }
                    else if (flag1)
                    {
                        target.extinguish();
                    }
                }
            }
        }
    }
}
