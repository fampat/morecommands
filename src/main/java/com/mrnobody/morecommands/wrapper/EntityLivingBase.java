package com.mrnobody.morecommands.wrapper;

import java.util.List;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
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
	 * Sets the entity's position
	 */
	public void setPosition(Coordinate c) {
		this.entity.setPositionAndUpdate(c.getX(), c.getY(), c.getZ());
	}
	
	/**
	 * Removes a potion effect
	 */
	public void removePotionEffect(int potion) {
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
	public void addPotionEffect(int id, int duration, int strength, boolean invisible) {
		this.entity.addPotionEffect(new PotionEffect(id, duration, strength, invisible));
	}
	
	/**
	 * removes enchantments from the current item
	 */
	public void removeEnchantments() {
		if (this.entity.getHeldItem() == null) return;
		NBTTagCompound compound = this.entity.getHeldItem().getTagCompound();
		if (compound == null) return;
		
		compound.removeTag("ench");
	}
	
	/**
	 * adds an enchantment from the current item
	 * @return whether this enchantment can be applied to the current item
	 */
	public boolean addEnchantment(Enchantment enchantment, int level) {
		if (this.entity.getHeldItem() == null) return false;
		if (!enchantment.canApply(this.entity.getHeldItem())) return false;
		if (this.entity.getHeldItem() == null) return false; NBTTagList ench;
		
		if ((ench = this.entity.getHeldItem().getEnchantmentTagList()) != null) {
			for (int i = 0; i < ench.tagCount(); i++) {
				Enchantment other = Enchantment.enchantmentsList[ench.getCompoundTagAt(i).getShort("id")];
				if (!other.canApplyTogether(enchantment) || !enchantment.canApplyTogether(other)) return false;
			}
		}
		
		this.entity.getHeldItem().addEnchantment(enchantment, level); return true;
	}
	
	/**
	 * removes an enchantment from the current item
	 */
	public void removeEnchantment(Enchantment enchantment) {
		if (this.entity.getHeldItem() == null) return;
		NBTTagCompound compound = this.entity.getHeldItem().getTagCompound();
		if (compound == null) return;
		
		NBTTagList enchantments = compound.hasKey("ench", NBT.TAG_LIST) ? compound.getTagList("ench", NBT.TAG_COMPOUND) : null;
		if (enchantments == null) return;
		
		NBTTagList newList = new NBTTagList();
		for (int i = 0; i < enchantments.tagCount(); i++) {
			if (enchantments.getCompoundTagAt(i).getShort("id") != enchantment.effectId) {
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
		if (this.entity.getHeldItem() == null) return false;
		NBTTagCompound compound = this.entity.getHeldItem().getTagCompound();
		if (compound == null) return false;
		
		NBTTagList enchantments = compound.hasKey("ench", NBT.TAG_LIST) ? compound.getTagList("ench", NBT.TAG_COMPOUND) : null;
		if (enchantments == null) return false;
		
		for (int i = 0; i < enchantments.tagCount(); i++) {
			if (enchantments.getCompoundTagAt(i).getShort("id") == enchantment.effectId) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * traces an entity in the direction the player is looking
	 * 
	 * @return the traced entity
	 */
	public net.minecraft.entity.Entity traceEntity(double distance) {
		return this.rayTrace(distance, 0.0D, 1.0F).entityHit;
	}
	
	/**
	 * Performs a full raytrace
	 * 
	 * @return a {@link MovingObjectPosition} with the trace results
	 */
	public MovingObjectPosition rayTrace(double distance, double borderSize, float partialTickTime) {
		Vec3 startVec = getPositionVec(partialTickTime);
		Vec3 lookVec = entity.getLook(partialTickTime);
		Vec3 endVec = startVec.addVector(lookVec.xCoord * distance, lookVec.yCoord * distance, lookVec.zCoord * distance);
	  
		double minX = startVec.xCoord < endVec.xCoord ? startVec.xCoord : endVec.xCoord;
		double minY = startVec.yCoord < endVec.yCoord ? startVec.yCoord : endVec.yCoord;
		double minZ = startVec.zCoord < endVec.zCoord ? startVec.zCoord : endVec.zCoord;
		double maxX = startVec.xCoord > endVec.xCoord ? startVec.xCoord : endVec.xCoord;
		double maxY = startVec.yCoord > endVec.yCoord ? startVec.yCoord : endVec.yCoord;
		double maxZ = startVec.zCoord > endVec.zCoord ? startVec.zCoord : endVec.zCoord;
		
		AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ).expand(borderSize, borderSize, borderSize);
		List<net.minecraft.entity.Entity> allEntities = this.entity.worldObj.getEntitiesWithinAABBExcludingEntity(this.entity, bb);  
		MovingObjectPosition blockHit = this.entity.worldObj.rayTraceBlocks(startVec, endVec);
		
		startVec = getPositionVec(partialTickTime);
		lookVec = entity.getLook(partialTickTime);
		endVec = startVec.addVector(lookVec.xCoord * distance, lookVec.yCoord * distance, lookVec.zCoord * distance);
		
		double maxDistance = endVec.distanceTo(startVec);
	  
		if (blockHit != null) {
			maxDistance = blockHit.hitVec.distanceTo(startVec);
		}
		
		net.minecraft.entity.Entity closestHitEntity = null;
		double closestHit = maxDistance;
		double currentHit = 0.D;
		AxisAlignedBB entityBb;
		MovingObjectPosition intercept;
		
		for (net.minecraft.entity.Entity ent : allEntities) {
			if (ent.canBeCollidedWith()) {
				double entBorder = ent.getCollisionBorderSize();
				entityBb = ent.boundingBox;
				
				if (entityBb != null) {
					entityBb = entityBb.expand(entBorder, entBorder, entBorder);
					intercept = entityBb.calculateIntercept(startVec, endVec);
					
					if(intercept != null) {
						currentHit = intercept.hitVec.distanceTo(startVec);
						
						if(currentHit < closestHit || currentHit==0) {            
							closestHit = currentHit;
							closestHitEntity = ent;
						}
					}
				}
			}
		}  
	  
		if(closestHitEntity != null) {
			blockHit = new MovingObjectPosition(closestHitEntity);
		}
		
		return blockHit;
	}
	   
	/**
	 * traces the block the entity is looking at
	 * 
	 * @return the traced coordinate or null
	 */
	public Coordinate traceBlock(double distance) {
		MovingObjectPosition m = rayTraceBlock(distance, 1.0F);
		if (m == null) return null;
		return new Coordinate(m.blockX, m.blockY, m.blockZ);
	}

	/**
	 * raytraces a block
	 * 
	 * @return a {@link MovingObjectPosition} with the coordinates
	 */
	public MovingObjectPosition rayTraceBlock(double distance, float partialTickTime) {
		Vec3 positionVec = getPositionVec(partialTickTime);
		Vec3 lookVec = this.entity.getLook(partialTickTime);
		Vec3 hitVec = positionVec.addVector(lookVec.xCoord * distance, lookVec.yCoord * distance, lookVec.zCoord * distance);
		return this.entity.worldObj.rayTraceBlocks(positionVec, hitVec, false);
	}
   
	/**
	 * Gets the entity's position vector
	 * 
	 * @return the position vector
	 */
	public Vec3 getPositionVec(float partialTickTime) {
		double offsetY = entity.posY + entity.getEyeHeight();
		if (partialTickTime == 1.0F) {
			return Vec3.createVectorHelper(this.entity.posX, offsetY, this.entity.posZ);
		} else {
			double var2 = this.entity.prevPosX + (this.entity.posX - this.entity.prevPosX) * partialTickTime;
			double var4 = this.entity.prevPosY + (offsetY - (entity.prevPosY + entity.getEyeHeight())) * partialTickTime;
			double var6 = this.entity.prevPosZ + (this.entity.posZ - this.entity.prevPosZ) * partialTickTime;
			return Vec3.createVectorHelper(var2, var4, var6);
		}
	}
	
	/**
	 * @return the  {@link net.minecraft.entity.EntityLivingBase} this object wraps
	 */
	public net.minecraft.entity.EntityLivingBase getMinecraftEntityLivingBase() {
		return this.entity;
	}
}
