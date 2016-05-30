package com.mrnobody.morecommands.wrapper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.mrnobody.morecommands.util.TargetSelector;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

/**
 * A wrapper for minecraft entities
 * 
 * @author MrNobody98
 */
public class Entity {
	private net.minecraft.entity.Entity entity;
	private World world;
	
	public Entity(net.minecraft.entity.Entity entity) {
		this.entity = entity;
		this.world = new World(entity.worldObj);
	}
	
	/**
	 * @return the entity's rotation yaw
	 */
	public float getYaw() {
		return this.entity.rotationYaw;
	}
   
	/**
	 * Sets the entity's rotation yaw
	 */
	public void setYaw(float yaw) {
		this.entity.rotationYaw = yaw;
	}
   
	/**
	 * @return the entity's rotation pitch
	 */
	public float getPitch() {
		return this.entity.rotationPitch;
	}
   
	/**
	 * Sets the entity's rotation pitch
	 */
	public void setPitch(float pitch) {
		this.entity.rotationPitch = pitch;
	}
	
	/**
	 * Sets the entity's motion
	 */
	public void setMotion(BlockPos motion) {
		this.entity.motionX = motion.getX();
		this.entity.motionY = motion.getY();
		this.entity.motionZ = motion.getZ();
	}
   
	/**
	 * @return the entity's motion
	 */
	public BlockPos getMotion() {
		return new BlockPos(this.entity.motionX, this.entity.motionY, this.entity.motionZ);
	}
	
	   
	/**
	 * Sets the entity's step height
	 */
	public void setStepHeight(float height) {
		this.entity.stepHeight = height;
	}
	   
	/**
	 * @return the entity's step height
	 */
	public float getStepHeight() {
		return this.entity.stepHeight;
	}
	
	/**
	 * Sets the entity's position
	 */
	public void setPosition(BlockPos c) {
		this.entity.setPositionAndUpdate(c.getX(), c.getY(), c.getZ());
	}
	
	/**
	 * Changes the players dimension
	 */
	public void changeDimension(int dimension) {
		this.entity.changeDimension(dimension);
	}
	
	/**
	 * @return the current dimension
	 */
	public int getDimension() {
		return this.entity.dimension;
	}
	
	/**
	 * @return the entity's position
	 */
	public BlockPos getPosition() {
		return new BlockPos(this.entity.posX, this.entity.posY, this.entity.posZ);
	}
	   
	/**
	 * @return the entity's world
	 */
	public World getWorld() {
		return this.world;
	}
	
	/**
	 * @return the {@link net.minecraft.entity.Entity} this object wraps
	 */
	public net.minecraft.entity.Entity getMinecraftEntity() {
		return this.entity;
	}
	
	/**
	 * traces an entity in the direction this entity is looking
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
	public RayTraceResult rayTrace(double distance, double borderSize, float partialTickTime) {
		Vec3d startVec = getPositionVec(partialTickTime);
		Vec3d lookVec = entity.getLook(partialTickTime);
		Vec3d endVec = startVec.addVector(lookVec.xCoord * distance, lookVec.yCoord * distance, lookVec.zCoord * distance);
	  
		double minX = startVec.xCoord < endVec.xCoord ? startVec.xCoord : endVec.xCoord;
		double minY = startVec.yCoord < endVec.yCoord ? startVec.yCoord : endVec.yCoord;
		double minZ = startVec.zCoord < endVec.zCoord ? startVec.zCoord : endVec.zCoord;
		double maxX = startVec.xCoord > endVec.xCoord ? startVec.xCoord : endVec.xCoord;
		double maxY = startVec.yCoord > endVec.yCoord ? startVec.yCoord : endVec.yCoord;
		double maxZ = startVec.zCoord > endVec.zCoord ? startVec.zCoord : endVec.zCoord;
		
		AxisAlignedBB bb = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ).expand(borderSize, borderSize, borderSize);
		List<net.minecraft.entity.Entity> allEntities = this.entity.worldObj.getEntitiesWithinAABBExcludingEntity(this.entity, bb);  
		RayTraceResult blockHit = this.entity.worldObj.rayTraceBlocks(startVec, endVec);
		
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
		RayTraceResult intercept;
		
		for (net.minecraft.entity.Entity ent : allEntities) {
			if (ent.canBeCollidedWith()) {
				double entBorder = ent.getCollisionBorderSize();
				entityBb = ent.getEntityBoundingBox();
				
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
			blockHit = new RayTraceResult(closestHitEntity);
		}
		
		return blockHit;
	}
	
	/**
	 * traces the block the entity is looking at
	 * 
	 * @return the traced coordinate or null
	 */
	public BlockPos traceBlock(double distance) {
		RayTraceResult m = rayTraceBlock(distance, 1.0F);
		if (m == null) {
			return null;
		}
		return m.getBlockPos();
	}

	/**
	 * raytraces a block
	 * 
	 * @return a {@link MovingObjectPosition} with the coordinates
	 */
	public RayTraceResult rayTraceBlock(double distance, float partialTickTime) {
		Vec3d positionVec = getPositionVec(partialTickTime);
		Vec3d lookVec = entity.getLook(partialTickTime);
		Vec3d hitVec = positionVec.addVector(lookVec.xCoord * distance, lookVec.yCoord * distance, lookVec.zCoord * distance);
		return entity.worldObj.rayTraceBlocks(positionVec, hitVec, false);
	}
   
	/**
	 * Gets the entity's position vector
	 * 
	 * @return the position vector
	 */
	public Vec3d getPositionVec(float partialTickTime) {
		double offsetY = entity.posY + entity.getEyeHeight();
		if (partialTickTime == 1.0F) {
			return new Vec3d(entity.posX, offsetY, entity.posZ);
		} else {
			double var2 = entity.prevPosX + (entity.posX - entity.prevPosX) * partialTickTime;
			double var4 = entity.prevPosY + (offsetY - (entity.prevPosY + entity.getEyeHeight())) * partialTickTime;
			double var6 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTickTime;
			return new Vec3d(var2, var4, var6);
		}
	}
	
	/**
	 * The map from entity names to entity ids
	 */
	private static Map<String, Integer> NAME_ID_MAPPING;
	/**
	 * The map from entity ids to entity names
	 */
	private static Map<Integer, String> ID_NAME_MAPPING;
	/**
	 * The map from entity names to entity classes
	 */
	private static Map<String, Class> NAME_CLASS_MAPPING;
	/**
	 * The map from entity names to non-abstract entity classes
	 */
	private static Map<String, Class> NAME_CLASS_MAPPING_NON_ABSTRACT;
	
	/**
	 * Gets a map from the {@link EntityList} class with the given parameter types
	 * 
	 * @return the map or null if it wasn't found
	 */
	private static <K, V> Map<K, V> getEntityMap(Class<K> from, Class<V> to, boolean allowAbstract) {
		Map<K,V> map = null;
		
		try {
			Field fields[] = EntityList.class.getDeclaredFields();
			
			for (Field field : fields) {
				field.setAccessible(true);
				Object obj = field.get(null);
				
				if (obj instanceof Map) {
					Map<?,?> temp = (Map<?,?>) obj;
					Object value = temp.keySet().iterator().next();
					
					if (value.getClass().isAssignableFrom(from) && temp.get(value).getClass().isAssignableFrom(to)) {
						if (temp.get(value) instanceof Class<?> && !allowAbstract) {
							map = Maps.filterValues(new HashMap<K, V>((Map<K, V>) temp), new Predicate<V>() {
								@Override
								public boolean apply(V input) {
									return !Modifier.isAbstract(((Class<?>) input).getModifiers());
								}
							});
						}
						else map = new HashMap<K, V>((Map<K, V>) temp);
						break;
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return map;
	}

	/**
	 * @return A list of entities
	 */
	public static List<String> getEntityList() {
		if (NAME_ID_MAPPING == null) NAME_ID_MAPPING = getEntityMap(String.class, Integer.class, true);
		return new ArrayList<String>(NAME_ID_MAPPING.keySet());
	}
	
	/**
	 * @return A list of entities whose classes are not abstract
	 */
	public static List<String> getNonAbstractEntityList() {
		if (NAME_CLASS_MAPPING_NON_ABSTRACT == null) NAME_CLASS_MAPPING_NON_ABSTRACT = getEntityMap(String.class, Class.class, false);
		return new ArrayList<String>(NAME_CLASS_MAPPING_NON_ABSTRACT.keySet());
	}

	/**
	 * @return A map of entity names to entity ids
	 */
	public static Map<String,Integer> getNameToIdEntityList() {
		if (NAME_ID_MAPPING == null) NAME_ID_MAPPING = getEntityMap(String.class, Integer.class, true);
		return NAME_ID_MAPPING;
	}

	/**
	 * @return The entity name from its id
	 */
	public static String getEntityName(int id) {
		if (ID_NAME_MAPPING == null) ID_NAME_MAPPING = getEntityMap(Integer.class, String.class, true);
		return ID_NAME_MAPPING.get(id);
	}

	/**
	 * @return The entity class from its name
	 */
	public static Class<?> getEntityClass(String name) {
		if (NAME_CLASS_MAPPING == null) NAME_CLASS_MAPPING = getEntityMap(String.class, Class.class, true);
		
		for (String key : NAME_CLASS_MAPPING.keySet()) {
			if (key.equalsIgnoreCase(name)) return NAME_CLASS_MAPPING.get(key);
		}
		
		return NAME_CLASS_MAPPING.get(name);
	}

	/**
	 * Spawns an Entity in a world
	 * 
	 * @return whether spawning the entity was successful
	 */
	public static boolean spawnEntity(String entity, BlockPos location, World world, NBTTagCompound compound, boolean mergeLists) {
		Class<?> entityClass = getEntityClass(entity);
		
		try {
			if (entityClass != null) {
				net.minecraft.entity.Entity entityInstance = (net.minecraft.entity.Entity)entityClass.getConstructor(net.minecraft.world.World.class).newInstance(world.getMinecraftWorld());
				entityInstance.setPosition(location.getX(), location.getY() + 1, location.getZ());
				
				if (compound != null) {
					NBTTagCompound original = new NBTTagCompound(); entityInstance.writeToNBT(original);
					TargetSelector.nbtMerge(original, compound, mergeLists);
					entityInstance.readFromNBT(original);
				}
				
				if(entityInstance instanceof EntityLiving) 
					((EntityLiving)entityInstance).onInitialSpawn(entityInstance.worldObj.getDifficultyForLocation(location), null);
				
				world.getMinecraftWorld().spawnEntityInWorld(entityInstance);
				
				if(entityInstance instanceof EntityLiving)
					((EntityLiving)entityInstance).playLivingSound();
				
				return true;
			}
		}
		catch (Exception e) {e.printStackTrace();}
		
		return false;
	}
   
	/**
	 * Kills all entities of the given type in a radius
	 * 
	 * @return a list of killed entities
	 */
	public static List<net.minecraft.entity.Entity> killEntities(String entity, BlockPos location, World world, double distance) {
		List<net.minecraft.entity.Entity> removedEntities = new ArrayList<net.minecraft.entity.Entity>();
		Class<?> entityClass = getEntityClass(entity);
		
		try {
			if (entityClass != null) {
				List<net.minecraft.entity.Entity> toremove = new ArrayList<net.minecraft.entity.Entity>();
				List<net.minecraft.entity.Entity> entities = world.getMinecraftWorld().loadedEntityList;
				
				for (net.minecraft.entity.Entity loaded : entities) {
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
	public static List<net.minecraft.entity.Entity> killEntities(List<? extends net.minecraft.entity.Entity> toRemove) {
		List<net.minecraft.entity.Entity> removedEntities = new ArrayList<net.minecraft.entity.Entity>();
		
		for (net.minecraft.entity.Entity remove : toRemove) {
			if (remove instanceof EntityPlayer || remove.isEntityInvulnerable(DamageSource.outOfWorld)) continue;
			remove.attackEntityFrom(DamageSource.outOfWorld, 1000);
			removedEntities.add(remove);
		}
		
		return removedEntities;
	}
   
	/**
	 * Finds all entities of the given type in a radius
	 * 
	 * @return a list of found entities
	 */
	public static List<net.minecraft.entity.Entity> findEntities(String entity, BlockPos location, World world, double distance) {
		List<net.minecraft.entity.Entity> foundEntities = new ArrayList<net.minecraft.entity.Entity>();
		Class<?> entityClass = getEntityClass(entity);
		
		try {
			if (entityClass != null) {
				List<net.minecraft.entity.Entity> found = new ArrayList<net.minecraft.entity.Entity>();
				List<net.minecraft.entity.Entity> entities = world.getMinecraftWorld().loadedEntityList;
				
				for (net.minecraft.entity.Entity loaded : entities) {
					if(!(entityClass.isInstance(loaded))) continue;
					if (getDistanceBetweenCoordinates(location, new BlockPos(loaded)) < distance)
						found.add(loaded);
				}
				
				for (net.minecraft.entity.Entity foundEntity : found) foundEntities.add(foundEntity);
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
}