package com.mrnobody.morecommands.wrapper;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.common.util.Constants.NBT;

/**
 * A wrapper class for minecrafts {@link net.minecraft.entity.EntityLivingBase}es
 * 
 * @author MrNobody98
 */
public class EntityLivingBase extends Entity {
	private net.minecraft.entity.EntityLivingBase entity;
	private World world;
	
	public EntityLivingBase(net.minecraft.entity.EntityLivingBase entity) {
		super(entity);
		this.entity = entity;
	}
	
	/**
	 * @return the entity's health
	 */
	public float getHealth() {
		return this.entity.getHealth();
	}
   
	/**
	 * Sets the entity's health
	 */
	public void setHealth(float health) {
		this.entity.setHealth(health);
	}
	
	/**
	 * Heals the entity by a certain amount
	 */
	public void heal(float quantity) {
		setHealth(getHealth() + quantity);
	}
	
	/**
	 * @return the entity's forward movement
	 */
	public float getMovementForward() {
		return this.entity.moveForward;
	}
   
	/**
	 * @return the entity's strafe movement
	 */
	public float getMovementStrafe() {
		return this.entity.moveStrafing;
	}
	
	/**
	 * Removes a potion effect
	 */
	public void removePotionEffect(Potion potion) {
		this.entity.removePotionEffect(potion);
	}
   
	/**
	 * Removes all potion effects
	 */
	public void removeAllPotionEffects() {
		this.entity.clearActivePotions();
	}
   
	/**
	 * Adds a potion effect
	 */
	public void addPotionEffect(Potion potion, int duration, int strength, boolean invisible) {
		this.entity.addPotionEffect(new PotionEffect(potion, duration, strength, false, invisible));
	}
	
	/**
	 * removes enchantments from the current item
	 */
	public void removeEnchantments() {
		if (this.entity.getHeldItemMainhand() == null) return;
		NBTTagCompound compound = this.entity.getHeldItemMainhand().getTagCompound();
		if (compound == null) return;
		
		compound.removeTag("ench");
	}
	
	/**
	 * adds an enchantment from the current item
	 * @return whether this enchantment can be applied to the current item
	 */
	public boolean addEnchantment(Enchantment enchantment, int level) {
		if (this.entity.getHeldItemMainhand() == null) return false;
		if (!enchantment.canApply(this.entity.getHeldItemMainhand())) return false;
		if (this.entity.getHeldItemMainhand() == null) return false; NBTTagList ench;
		
		if ((ench = this.entity.getHeldItemMainhand().getEnchantmentTagList()) != null) {
			for (int i = 0; i < ench.tagCount(); i++) {
				Enchantment other = Enchantment.getEnchantmentByID(ench.getCompoundTagAt(i).getShort("id"));
				if (!other.canApplyTogether(enchantment) || !enchantment.canApplyTogether(other)) return false;
			}
		}
		
		this.entity.getHeldItemMainhand().addEnchantment(enchantment, level); return true;
	}
	
	/**
	 * removes an enchantment from the current item
	 */
	public void removeEnchantment(Enchantment enchantment) {
		if (this.entity.getHeldItemMainhand() == null) return;
		NBTTagCompound compound = this.entity.getHeldItemMainhand().getTagCompound();
		if (compound == null) return;
		
		NBTTagList enchantments = compound.hasKey("ench", NBT.TAG_LIST) ? compound.getTagList("ench", NBT.TAG_COMPOUND) : null;
		if (enchantments == null) return;
		int id = Enchantment.getEnchantmentID(enchantment);
		
		NBTTagList newList = new NBTTagList();
		for (int i = 0; i < enchantments.tagCount(); i++) {
			if (enchantments.getCompoundTagAt(i).getShort("id") != id) {
				newList.appendTag(enchantments.getCompoundTagAt(i));
			}
		}
		
		if (newList.tagCount() == 0) compound.removeTag("ench");
		else compound.setTag("ench", newList);
	}
	
	/**
	 * @return whether the player has this enchantment
	 */
	public boolean hasEnchantment(Enchantment enchantment) {
		if (this.entity.getHeldItemMainhand() == null) return false;
		NBTTagCompound compound = this.entity.getHeldItemMainhand().getTagCompound();
		if (compound == null) return false;
		
		NBTTagList enchantments = compound.hasKey("ench", NBT.TAG_LIST) ? compound.getTagList("ench", NBT.TAG_COMPOUND) : null;
		if (enchantments == null) return false;
		int id = Enchantment.getEnchantmentID(enchantment);
		
		for (int i = 0; i < enchantments.tagCount(); i++) {
			if (enchantments.getCompoundTagAt(i).getShort("id") == id) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * @return the  {@link net.minecraft.entity.EntityLivingBase} this object wraps
	 */
	public net.minecraft.entity.EntityLivingBase getMinecraftEntityLivingBase() {
		return this.entity;
	}
}