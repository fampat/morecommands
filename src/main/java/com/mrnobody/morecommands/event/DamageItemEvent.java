package com.mrnobody.morecommands.event;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;

/**
 * This event is fired on {@link EventHandler#DAMAGE_ITEM} when an item
 * is about to be damaged. Make sure to modify the {@link ItemStack}s
 * properties and the item damage/cancel the event on both the server 
 * and on the client, otherwise they will be desynchronized
 * 
 * @author MrNobody98
 */
@Cancelable
public class DamageItemEvent extends LivingEvent {
	public ItemStack stack;
	public int damage;
	
	public DamageItemEvent(EntityLivingBase entity, int damage, ItemStack stack) {
		super(entity);
		this.stack = stack;
		this.damage = damage;
	}
}
