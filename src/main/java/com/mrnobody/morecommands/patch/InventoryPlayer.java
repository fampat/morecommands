package com.mrnobody.morecommands.patch;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/**
 * The patched class of {@link net.minecraft.entity.player.InventoryPlayer} <br>
 * This patch is needed for modifying the stack size. Yet changing the <br>
 * stack size via the stacksize command does not work properly, because changing <br>
 * the maximum stack size needs more then just patching this class
 * 
 * @author MrNobody98
 *
 */
public class InventoryPlayer extends net.minecraft.entity.player.InventoryPlayer {
	public static final Map<Item, Integer> stackSizes = new HashMap<Item, Integer>();
	
	private int stacksize = 64;
	
	public InventoryPlayer(EntityPlayer p_i1750_1_) {
		super(p_i1750_1_);
		
		Iterator<Item> items = Item.itemRegistry.iterator();
		
		while (items.hasNext()) {
			Item item = items.next();
			this.stackSizes.put(item, item.getItemStackLimit());
		}
	}
	
	public void setStacksize(int stacksize) {
		this.stacksize = stacksize;
	}

	@Override
    public int getInventoryStackLimit()
    {
        return this.stacksize;
    }
	
	@Override
    public NBTTagList writeToNBT(NBTTagList p_70442_1_)
    {
        int i;
        NBTTagCompound nbttagcompound;

        for (i = 0; i < this.mainInventory.length; ++i)
        {
            if (this.mainInventory[i] != null)
            {
                nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte) i);
                this.mainInventory[i].writeToNBT(nbttagcompound);
                
                //Store stack size in an extra tag to support higher stack sizes for the stacksize command (default maxmium is 127 (1 byte))
                nbttagcompound.setInteger("mrnobody_stacksize", this.mainInventory[i].stackSize);
                
                p_70442_1_.appendTag(nbttagcompound);
            }
        }

        for (i = 0; i < this.armorInventory.length; ++i)
        {
            if (this.armorInventory[i] != null)
            {
                nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte)(i + 100));
                this.armorInventory[i].writeToNBT(nbttagcompound);
                p_70442_1_.appendTag(nbttagcompound);
            }
        }

        return p_70442_1_;
    }

    @Override
    public void readFromNBT(NBTTagList p_70443_1_)
    {
        this.mainInventory = new ItemStack[36];
        this.armorInventory = new ItemStack[4];

        for (int i = 0; i < p_70443_1_.tagCount(); ++i)
        {
            NBTTagCompound nbttagcompound = p_70443_1_.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("Slot") & 255;
            ItemStack itemstack = ItemStack.loadItemStackFromNBT(nbttagcompound);
            
            if (itemstack != null && nbttagcompound.hasKey("mrnobody_stacksize")) {
            	int stacksize = nbttagcompound.getInteger("mrnobody_stacksize");
            	
            	if (stacksize > itemstack.getMaxStackSize()) itemstack.getItem().setMaxStackSize(stacksize);
            	if (stacksize > this.stacksize) this.stacksize = stacksize;
            	
            	itemstack.stackSize = stacksize;
            }

            if (itemstack != null)
            {
                if (j >= 0 && j < this.mainInventory.length)
                {
                    this.mainInventory[j] = itemstack;
                }

                if (j >= 100 && j < this.armorInventory.length + 100)
                {
                    this.armorInventory[j - 100] = itemstack;
                }
            }
        }
    }
}
