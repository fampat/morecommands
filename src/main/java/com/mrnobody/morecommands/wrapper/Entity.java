package com.mrnobody.morecommands.wrapper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

/**
 * A wrapper for minecraft entities
 * 
 * @author MrNobody98
 */
public class Entity {
	private net.minecraft.entity.EntityLivingBase entity;
	private World world;
	
	public Entity(net.minecraft.entity.EntityLivingBase entity) {
		this.entity = entity;
		this.world = new World(entity.worldObj);
	}
	
	/**
	 * Sets the entity's position
	 */
	public void setPosition(Coordinate c) {
		entity.setPositionAndUpdate(c.getX(), c.getY(), c.getZ());
	}
	
	/**
	 * @return the entity's position
	 */
	public Coordinate getPosition() {
		return new Coordinate(entity.posX, entity.posY, entity.posZ);
	}
	   
	/**
	 * @return the entity's world
	 */
	public World getWorld() {
		return this.world;
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
		Vec3 lookVec = entity.getLook(partialTickTime);
		Vec3 hitVec = positionVec.addVector(lookVec.xCoord * distance, lookVec.yCoord * distance, lookVec.zCoord * distance);
		return entity.worldObj.rayTraceBlocks(positionVec, hitVec, false);
	}
   
	/**
	 * Gets the entity's position vector
	 * 
	 * @return the position vector
	 */
	public Vec3 getPositionVec(float partialTickTime) {
		double offsetY = entity.posY + entity.getEyeHeight();
		if (partialTickTime == 1.0F) {
			return Vec3.createVectorHelper(entity.posX, offsetY, entity.posZ);
		} else {
			double var2 = entity.prevPosX + (entity.posX - entity.prevPosX) * partialTickTime;
			double var4 = entity.prevPosY + (offsetY - (entity.prevPosY + entity.getEyeHeight())) * partialTickTime;
			double var6 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTickTime;
			return Vec3.createVectorHelper(var2, var4, var6);
		}
	}
	
	public net.minecraft.entity.EntityLivingBase getMinecraftEntity() {
		return this.entity;
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
	 * Gets a map from the {@link EntityList} class with the given parameter types
	 * 
	 * @return the map or null if it wasn't found
	 */
	public static <K,V> Map<K,V> getEntityMap(Class<K> from, Class<V> to) {
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
						map = (Map<K,V>) temp;
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
		if (NAME_ID_MAPPING == null) NAME_ID_MAPPING = getEntityMap(String.class,Integer.class);
		return new ArrayList<String>(NAME_ID_MAPPING.keySet());
	}

	/**
	 * @return A map of entity names to entity ids
	 */
	public static Map<String,Integer> getNameToIdEntityList() {
		if (NAME_ID_MAPPING == null) NAME_ID_MAPPING = getEntityMap(String.class,Integer.class);
		return NAME_ID_MAPPING;
	}

	/**
	 * @return The entity name from its id
	 */
	public static String getEntityName(int id) {
		if (ID_NAME_MAPPING == null) ID_NAME_MAPPING = getEntityMap(Integer.class,String.class);
		return ID_NAME_MAPPING.get(id);
	}

	/**
	 * @return The entity class from its name
	 */
	public static Class<?> getEntityClass(String name) {
		if (NAME_CLASS_MAPPING == null) NAME_CLASS_MAPPING = getEntityMap(String.class, Class.class);
		
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
	public static boolean spawnEntity(String entity, Coordinate location, World world) {
		Class<?> entityClass = getEntityClass(entity);
		
		try {
			if (entityClass != null) {
				net.minecraft.entity.Entity entityInstance = (net.minecraft.entity.Entity)entityClass.getConstructor(net.minecraft.world.World.class).newInstance(world.getMinecraftWorld());
				entityInstance.setPosition(location.getX(), location.getY() + 1, location.getZ());
				
				if(entityInstance instanceof EntityLiving) 
					((EntityLiving)entityInstance).onSpawnWithEgg((IEntityLivingData)null);
				
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
	public static List<net.minecraft.entity.Entity> killEntities(String entity, Coordinate location, World world, double distance) {
		List<net.minecraft.entity.Entity> removedEntities = new ArrayList<net.minecraft.entity.Entity>();
		Class<?> entityClass = getEntityClass(entity);
		
		try {
			if (entityClass != null) {
				List<net.minecraft.entity.Entity> toremove = new ArrayList<net.minecraft.entity.Entity>();
				List<net.minecraft.entity.Entity> entities = world.getMinecraftWorld().loadedEntityList;
				
				for (net.minecraft.entity.Entity loaded : entities) {
					if(!(entityClass.isInstance(loaded))) continue;
					if (location.getDistanceBetweenCoordinates(new Coordinate(loaded.posX,loaded.posY,loaded.posZ)) < distance)
						toremove.add(loaded);
					
				}
				
				for (net.minecraft.entity.Entity remove : toremove) {
					if(remove instanceof EntityPlayer || remove.isEntityInvulnerable()) continue;
					remove.attackEntityFrom(DamageSource.outOfWorld, 1000);
					removedEntities.add(remove);
				}
			}
		}
		catch (Exception e) {e.printStackTrace();}
		
		return removedEntities;
	}
   
	/**
	 * Finds all entities of the given type in a radius
	 * 
	 * @return a list of found entities
	 */
	public static List<net.minecraft.entity.Entity> findEntities(String entity, Coordinate location, World world, double distance) {
		List<net.minecraft.entity.Entity> foundEntities = new ArrayList<net.minecraft.entity.Entity>();
		Class<?> entityClass = getEntityClass(entity);
		
		try {
			if (entityClass != null) {
				List<net.minecraft.entity.Entity> found = new ArrayList<net.minecraft.entity.Entity>();
				List<net.minecraft.entity.Entity> entities = world.getMinecraftWorld().loadedEntityList;
				
				for (net.minecraft.entity.Entity loaded : entities) {
					if(!(entityClass.isInstance(loaded))) continue;
					if (location.getDistanceBetweenCoordinates(new Coordinate(loaded.posX,loaded.posY,loaded.posZ)) < distance)
						found.add(loaded);
				}
				
				for (net.minecraft.entity.Entity foundEntity : found) foundEntities.add(foundEntity);
			}
		}
		catch (Exception e) {e.printStackTrace();}
		
		return foundEntities;
	}
}