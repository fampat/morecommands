package com.mrnobody.morecommands.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.mrnobody.morecommands.util.ObfuscatedNames.ObfuscatedField;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.stats.Achievement;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings.GameType;
import net.minecraftforge.common.util.Constants.NBT;

public final class EntityUtils {
	private EntityUtils() {}
	
	public static final Map<String, Class<? extends Entity>> NAME_TO_CLASS_MAP;
	public static final Map<Class<? extends Entity>, String> CLASS_TO_NAME_MAP;
	public static final Map<Integer, Class<? extends Entity>> ID_TO_CLASS_MAP;
	public static final Map<Class<? extends Entity>, Integer> CLASS_TO_ID_MAP;
	public static final Map<String, Integer> NAME_TO_ID_MAP;
	
	static {
		NAME_TO_CLASS_MAP = Collections.unmodifiableMap(ReflectionHelper.get(ObfuscatedField.EntityList_stringToClassMapping, null));
		CLASS_TO_NAME_MAP = Collections.unmodifiableMap(ReflectionHelper.get(ObfuscatedField.EntityList_classToStringMapping, null));
		ID_TO_CLASS_MAP = Collections.unmodifiableMap(ReflectionHelper.get(ObfuscatedField.EntityList_idToClassMapping, null));
		CLASS_TO_ID_MAP = Collections.unmodifiableMap(ReflectionHelper.get(ObfuscatedField.EntityList_classToIDMapping, null));
		NAME_TO_ID_MAP = Collections.unmodifiableMap(ReflectionHelper.get(ObfuscatedField.EntityList_stringToIDMapping, null));
	}
	
	/**
	 * @return The entity name from its id
	 */
	public static String getEntityName(int id) {
		return CLASS_TO_NAME_MAP.get(ID_TO_CLASS_MAP.get(id));
	}
	
	/**
	 * @return The entity class from its name
	 */
	public static Class<? extends Entity> getEntityClass(String name, boolean ignoreCase) {
		if (!ignoreCase) return NAME_TO_CLASS_MAP.get(name);
		else {
			for (Map.Entry<String, Class<? extends Entity>> entry : NAME_TO_CLASS_MAP.entrySet())
				if (name.equalsIgnoreCase(entry.getKey())) return entry.getValue();
			
			return null;
		}
	}
	
	/**
	 * Spawns an Entity in a world
	 * 
	 * @return whether spawning the entity was successful
	 */
	public static boolean spawnEntity(String entity, boolean ignoreCase, BlockPos location, World world, NBTTagCompound compound, boolean mergeLists) {
		return spawnEntity(getEntityClass(entity, ignoreCase), location, world, compound, mergeLists);
	}
	
	/**
	 * Spawns an Entity in a world
	 * 
	 * @return whether spawning the entity was successful
	 */
	public static boolean spawnEntity(Class<? extends Entity> entityClass, BlockPos location, World world, NBTTagCompound compound, boolean mergeLists) {
		try {
			if (entityClass != null) {
				Entity entityInstance = (Entity)entityClass.getConstructor(World.class).newInstance(world);
				entityInstance.setPosition(location.getX(), location.getY() + 1, location.getZ());
				
				if (compound != null) {
					NBTTagCompound original = new NBTTagCompound(); entityInstance.writeToNBT(original);
					TargetSelector.nbtMerge(original, compound, mergeLists);
					entityInstance.readFromNBT(original);
				}
				
				//Only for natural spawns
				//if (entityInstance instanceof EntityLiving) 
				//	((EntityLiving)entityInstance).onInitialSpawn(entityInstance.worldObj.getDifficultyForLocation(location), null);
				
				world.spawnEntityInWorld(entityInstance);
				
				if(entityInstance instanceof EntityLiving)
					((EntityLiving)entityInstance).playLivingSound();
				
				return true;
			}
		}
		catch (Exception e) {e.printStackTrace();}
		
		return false;
	}
	
	public static List<Entity> killEntities(String entity, boolean ignoreCase, BlockPos location, World world, double distance) {
		return killEntities(getEntityClass(entity, ignoreCase), location, world, distance);
	}
	
	/**
	 * Kills all entities of the given type in a radius
	 * 
	 * @return a list of killed entities
	 */
	public static List<Entity> killEntities(Class<? extends Entity> entityClass, BlockPos location, World world, double distance) {
		List<Entity> removedEntities = new ArrayList<Entity>();
		
		try {
			if (entityClass != null) {
				List<Entity> toremove = new ArrayList<Entity>();
				List<Entity> entities = world.loadedEntityList;
				
				for (Entity loaded : entities) {
					if (!(entityClass.isInstance(loaded))) continue;
					if (getDistanceBetweenCoordinates(location, new BlockPos(loaded)) < distance)
						toremove.add(loaded);
				}
				
				removedEntities.addAll(killEntities(toremove));
			}
		}
		catch (Exception e) {e.printStackTrace();}
		
		return removedEntities;
	}
	
	/**
	 * Kills all entities of the given list which can be killed (no players, no invulnerable entities)
	 * 
	 * @return a list of killed entities
	 */
	public static List<Entity> killEntities(List<? extends Entity> toRemove) {
		List<Entity> removedEntities = new ArrayList<Entity>();
		
		for (Entity remove : toRemove) {
			if (remove instanceof EntityPlayer || remove.isEntityInvulnerable(DamageSource.outOfWorld)) continue;
			remove.attackEntityFrom(DamageSource.outOfWorld, 1000);
			removedEntities.add(remove);
		}
		
		return removedEntities;
	}
	
	public static List<Entity> findEntities(String entity, boolean ignoreCase, BlockPos location, World world, double distance) {
		return findEntities(getEntityClass(entity, ignoreCase), location, world, distance);
	}
   
	/**
	 * Finds all entities of the given type in a radius
	 * 
	 * @return a list of found entities
	 */
	public static List<Entity> findEntities(Class<? extends Entity> entityClass, BlockPos location, World world, double distance) {
		List<Entity> foundEntities = new ArrayList<Entity>();
		
		try {
			if (entityClass != null) {
				List<Entity> found = new ArrayList<Entity>();
				List<Entity> entities = world.loadedEntityList;
				
				for (Entity loaded : entities) {
					if(!(entityClass.isInstance(loaded))) continue;
					if (getDistanceBetweenCoordinates(location, new BlockPos(loaded)) < distance)
						found.add(loaded);
				}
				
				for (Entity foundEntity : found) foundEntities.add(foundEntity);
			}
		}
		catch (Exception e) {e.printStackTrace();}
		
		return foundEntities;
	}
	
	/**
	 * @param pos1 Coordinate 1
	 * @param pos2 Coordinate 2
	 * @return the distance between these coordinates
	 */
	private static double getDistanceBetweenCoordinates(BlockPos pos1, BlockPos pos2) {
		int diffX = pos1.getX() - pos2.getX();
		int diffY = pos1.getY() - pos2.getY();
		int diffZ = pos1.getZ() - pos2.getZ();
		return Math.sqrt((diffX * diffX) + (diffY * diffY) + (diffZ * diffZ));
	}
	
	/**
	 * Sets the entity's position
	 */
	public static void setPosition(Entity entity, BlockPos c) {
		entity.setPositionAndUpdate(c.getX(), c.getY(), c.getZ());
	}
	
	/**
	 * @return the entity's position
	 */
	public static BlockPos getPosition(Entity entity) {
		return new BlockPos(entity.posX, entity.posY, entity.posZ);
	}
	
	/**
	 * @return the entity's health
	 */
	public static float getHealth(EntityLivingBase entity) {
		return entity.getHealth();
	}
   
	/**
	 * Sets the entity's health
	 */
	public static void setHealth(EntityLivingBase entity, float health) {
		entity.setHealth(health);
	}
	
	/**
	 * Heals the entity by a certain amount
	 */
	public static void heal(EntityLivingBase entity, float quantity) {
		setHealth(entity, getHealth(entity) + quantity);
	}
	
	/**
	 * Removes a potion effect
	 */
	public static void removePotionEffect(EntityLivingBase entity, int potion) {
		entity.removePotionEffect(potion);
	}
   
	/**
	 * Removes all potion effects
	 */
	public static void removeAllPotionEffects(EntityLivingBase entity) {
		entity.clearActivePotions();
	}
   
	/**
	 * Adds a potion effect
	 */
	public static void addPotionEffect(EntityLivingBase entity, int id, int duration, int strength, boolean invisible) {
		entity.addPotionEffect(new PotionEffect(id, duration, strength, false, invisible));
	}
	
	/**
	 * removes enchantments from the current item
	 */
	public static void removeEnchantments(EntityLivingBase entity) {
		if (entity.getHeldItem() == null) return;
		NBTTagCompound compound = entity.getHeldItem().getTagCompound();
		if (compound == null) return;
		
		compound.removeTag("ench");
	}
	
	/**
	 * adds an enchantment from the current item
	 * 
	 * @param enchantment the enchantment
	 * @param level the enchantment level
	 * @return whether this enchantment can be applied to the current item
	 */
	public static boolean addEnchantment(EntityLivingBase entity, Enchantment enchantment, int level) {
		return addEnchantment(entity, enchantment, level, true);
	}
	
	/**
	 * adds an enchantment from the current item
	 * 
	 * @param enchantment the enchantment
	 * @param level the enchantment level
	 * @param strict whether level and compatibility checks for the enchantment should be made
	 * @return whether this enchantment can be applied to the current item
	 */
	public static boolean addEnchantment(EntityLivingBase entity, Enchantment enchantment, int level, boolean strict) {
		if (entity.getHeldItem() == null) return false;
		if (strict && !enchantment.canApply(entity.getHeldItem())) return false;
		
		NBTTagList ench = entity.getHeldItem().getEnchantmentTagList();
		level = Math.min(Math.max(strict ? enchantment.getMinLevel() : 0, level), strict ? enchantment.getMaxLevel() : Byte.MAX_VALUE);
		
		if (strict && ench != null) {
			for (int i = 0; i < ench.tagCount(); i++) {
				Enchantment other = Enchantment.getEnchantmentById(ench.getCompoundTagAt(i).getShort("id"));
				if (!other.canApplyTogether(enchantment) || !enchantment.canApplyTogether(other)) return false;
			}
		}
		
		entity.getHeldItem().addEnchantment(enchantment, level); return true;
	}
	
	/**
	 * removes an enchantment from the current item
	 */
	public static void removeEnchantment(EntityLivingBase entity, Enchantment enchantment) {
		if (entity.getHeldItem() == null) return;
		NBTTagCompound compound = entity.getHeldItem().getTagCompound();
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
	public static boolean hasEnchantment(EntityLivingBase entity, Enchantment enchantment) {
		if (entity.getHeldItem() == null) return false;
		NBTTagCompound compound = entity.getHeldItem().getTagCompound();
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
	public static Entity traceEntity(Entity entity, double distance) {
		return rayTrace(entity, distance, 0.0D, 1.0F).entityHit;
	}
	
	/**
	 * Performs a full raytrace
	 * 
	 * @return a {@link MovingObjectPosition} with the trace results
	 */
	public static MovingObjectPosition rayTrace(Entity entity, double distance, double borderSize, float partialTickTime) {
		Vec3 startVec = getPositionVec(entity, partialTickTime);
		Vec3 lookVec = entity.getLook(partialTickTime);
		Vec3 endVec = startVec.addVector(lookVec.xCoord * distance, lookVec.yCoord * distance, lookVec.zCoord * distance);
	  
		double minX = startVec.xCoord < endVec.xCoord ? startVec.xCoord : endVec.xCoord;
		double minY = startVec.yCoord < endVec.yCoord ? startVec.yCoord : endVec.yCoord;
		double minZ = startVec.zCoord < endVec.zCoord ? startVec.zCoord : endVec.zCoord;
		double maxX = startVec.xCoord > endVec.xCoord ? startVec.xCoord : endVec.xCoord;
		double maxY = startVec.yCoord > endVec.yCoord ? startVec.yCoord : endVec.yCoord;
		double maxZ = startVec.zCoord > endVec.zCoord ? startVec.zCoord : endVec.zCoord;
		
		AxisAlignedBB bb = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ).expand(borderSize, borderSize, borderSize);
		List<Entity> allEntities = entity.worldObj.getEntitiesWithinAABBExcludingEntity(entity, bb);  
		MovingObjectPosition blockHit = entity.worldObj.rayTraceBlocks(startVec, endVec);
		
		startVec = getPositionVec(entity, partialTickTime);
		lookVec = entity.getLook(partialTickTime);
		endVec = startVec.addVector(lookVec.xCoord * distance, lookVec.yCoord * distance, lookVec.zCoord * distance);
		
		double maxDistance = endVec.distanceTo(startVec);
	  
		if (blockHit != null) {
			maxDistance = blockHit.hitVec.distanceTo(startVec);
		}
		
		Entity closestHitEntity = null;
		double closestHit = maxDistance;
		double currentHit = 0.D;
		AxisAlignedBB entityBb;
		MovingObjectPosition intercept;
		
		for (Entity ent : allEntities) {
			if (ent.canBeCollidedWith()) {
				double entBorder = ent.getCollisionBorderSize();
				entityBb = ent.getEntityBoundingBox();
				
				if (entityBb != null) {
					entityBb = entityBb.expand(entBorder, entBorder, entBorder);
					intercept = entityBb.calculateIntercept(startVec, endVec);
					
					if(intercept != null) {
						currentHit = intercept.hitVec.distanceTo(startVec);
						
						if(currentHit < closestHit || currentHit == 0) {            
							closestHit = currentHit;
							closestHitEntity = ent;
						}
					}
				}
			}
		}  
	  
		if (closestHitEntity != null) {
			blockHit = new MovingObjectPosition(closestHitEntity);
		}
		
		return blockHit;
	}
	   
	/**
	 * traces the block the entity is looking at
	 * 
	 * @return the traced coordinate or null
	 */
	public static BlockPos traceBlock(Entity entity, double distance) {
		MovingObjectPosition m = rayTraceBlock(entity, distance, 1.0F);
		if (m == null) return null;
		return m.getBlockPos();
	}

	/**
	 * raytraces a block
	 * 
	 * @return a {@link MovingObjectPosition} with the coordinates
	 */
	public static MovingObjectPosition rayTraceBlock(Entity entity, double distance, float partialTickTime) {
		Vec3 positionVec = getPositionVec(entity, partialTickTime);
		Vec3 lookVec = entity.getLook(partialTickTime);
		Vec3 hitVec = positionVec.addVector(lookVec.xCoord * distance, lookVec.yCoord * distance, lookVec.zCoord * distance);
		return entity.worldObj.rayTraceBlocks(positionVec, hitVec, false);
	}
   
	/**
	 * Gets the entity's position vector
	 * 
	 * @return the position vector
	 */
	public static Vec3 getPositionVec(Entity entity, float partialTickTime) {
		double offsetY = entity.posY + entity.getEyeHeight();
		if (partialTickTime == 1.0F) {
			return new Vec3(entity.posX, offsetY, entity.posZ);
		} else {
			double var2 = entity.prevPosX + (entity.posX - entity.prevPosX) * partialTickTime;
			double var4 = entity.prevPosY + (offsetY - (entity.prevPosY + entity.getEyeHeight())) * partialTickTime;
			double var6 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTickTime;
			return new Vec3(var2, var4, var6);
		}
	}
	
	/**
	 * Gives an item to the player
	 */
	public static void givePlayerItem(EntityPlayerMP player, Item item) {
		givePlayerItem(player, item, new ItemStack(item).stackSize);
	}
   
	/**
	 * Gives an amount of items to the player
	 */
	public static void givePlayerItem(EntityPlayerMP player, Item item, int quantity) {
		givePlayerItem(player, item, quantity, 0);
	}
   
	/**
	 * Gives an amount of items with metadata to the player
	 */
	public static void givePlayerItem(EntityPlayerMP player, Item item, int quantity, int meta) {
		ItemStack itemStack = new ItemStack(item, quantity, meta);
		
		if (!player.inventory.addItemStackToInventory(itemStack)) {
			player.dropItem(item, quantity);
		}
	}
	
	/**
	 * Gives an amount of items with metadata and nbt data to the player
	 */
	public static void givePlayerItem(EntityPlayerMP player, Item item, int quantity, int meta, NBTTagCompound compound) {
		ItemStack itemStack = new ItemStack(item, quantity, meta);
		if (compound != null) itemStack.setTagCompound(compound);
		
		if (!player.inventory.addItemStackToInventory(itemStack)) {
			player.dropItem(item, quantity);
		}
	}
	
	/**
	 * @return the players hunger
	 */
	public static int getHunger(EntityPlayerMP player) {
		return player.getFoodStats().getFoodLevel();
	}
	
	/**
	 * Sets the players hunger
	 */
	public static void setHunger(EntityPlayerMP player, int food) {
		player.getFoodStats().setFoodLevel(food);
	}
	
	/**
	 * @return whether player damage is enabled
	 */
	public static boolean getEnableDamage(EntityPlayerMP player) {
		return !player.capabilities.disableDamage;
	}
	
	/**
	 * Sets whether player damage is enabled
	 */
	public static void setEnableDamage(EntityPlayerMP player, boolean damage) {
		player.capabilities.disableDamage = !damage;
	}
	
	/**
	 * Sets an inventory slot
	 */
	public static boolean setInventorySlot(EntityPlayerMP player, int slot, Item item, int quantity, int damage) {
		if (slot < 0 || slot >= player.inventory.getSizeInventory()) return false;
		player.inventory.setInventorySlotContents(slot, new ItemStack(item, quantity, damage));
		return true;
	}
	
	/**
	 * @return the players current item
	 */
	public static ItemStack getCurrentItem(EntityPlayerMP player) {
		return player.getCurrentEquippedItem();
	}
	
	/**
	 * Sets the players current slot
	 */
	public static void setCurrentSlot(EntityPlayerMP player, ItemStack item) {
		player.inventory.setInventorySlotContents(player.inventory.currentItem, item);
	}
	
	/**
	 * @return whether the player has this achievement
	 */
	public static boolean hasAchievement(EntityPlayerMP player, Achievement ach) {
		return player.getStatFile().hasAchievementUnlocked(ach);
	}
   
	/**
	 * triggers an achievement
	 */
	public static void addAchievement(EntityPlayerMP player, Achievement ach) {
		player.triggerAchievement(ach);
	}
	
	/**
	 * removes an achievement
	 */
	public static void removeAchievement(EntityPlayerMP player, Achievement ach) {
        player.getStatFile().unlockAchievement(player, ach, 0);
        Iterator iterator = player.getWorldScoreboard().getObjectivesFromCriteria(ach.func_150952_k()).iterator();

        while (iterator.hasNext()) {
            ScoreObjective scoreobjective = (ScoreObjective) iterator.next();
            player.getWorldScoreboard().getValueFromObjective(player.getName(), scoreobjective).setScorePoints(0);
        }

        if (player.getStatFile().func_150879_e())
            player.getStatFile().func_150876_a(player);
	}
	
	/**
	 * @return the player's spawn point
	 */
	public static BlockPos getSpawn(EntityPlayerMP player) {
		return player.getBedLocation(player.dimension);
	}
	
	/**
	 * Sets the players spawn point
	 */
	public static void setSpawn(EntityPlayerMP player, BlockPos coord) {
		player.setSpawnChunk(coord, true, player.dimension);
	}
	
	/**
	 * Sets the players spawn point
	 */
	public static void setSpawn(EntityPlayerMP player, int x, int y, int z) {
		player.setSpawnChunk(new BlockPos(x, y, z), true, player.dimension);
	}
   
	/**
	 * @return whether player is allowed to fly
	 */
	public static boolean getAllowFlying(EntityPlayerMP player) {
		return player.capabilities.allowFlying;
	}
   
	/**
	 * Sets whether player is allowed to fly
	 */
	public static void setAllowFlying(EntityPlayerMP player, boolean allow) {
		player.capabilities.allowFlying = allow;
		player.capabilities.isFlying = allow;
		player.sendPlayerAbilities();
	}
   
	/**
	 * @return whether player is in creative mode
	 */
	public static boolean isCreativeMode(EntityPlayerMP player) {
		return player.capabilities.isCreativeMode;
	}
   
	/**
	 * @return The display name
	 */
	public static String getDisplayName(EntityPlayerMP player) {
		return player.getDisplayNameString();
	}
}
