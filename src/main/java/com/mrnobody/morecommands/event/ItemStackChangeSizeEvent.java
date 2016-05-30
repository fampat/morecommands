package com.mrnobody.morecommands.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

/**
 * This event is fired on {@link EventHandler#ITEMSTACK_CHANGE_SIZE} when an 
 * {@link ItemStack}s stack size is about to be changed. Make sure to modify 
 * the {@link ItemStack}s properties and the new stack size/cancel the event
 * on both the server and on the client, otherwise they will be desynchronized
 * 
 * @author MrNobody98
 */
@Cancelable
public class ItemStackChangeSizeEvent extends PlayerEvent {
	public ItemStack stack;
	public final int oldSize;
	public int newSize;
	
	public ItemStackChangeSizeEvent(EntityPlayer player, ItemStack stack, int oldSize, int newSize) {
		super(player);
		this.stack = stack;
		this.oldSize = oldSize;
		this.newSize = newSize;
	}
}
