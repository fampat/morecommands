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
	public void setMotion(Coordinate motion) {
		this.entity.motionX = motion.getX();
		this.entity.motionY = motion.getY();
		this.entity.motionZ = motion.getZ();
	}
   
	/**
	 * @return the entity's motion
	 */
	public Coordinate getMotion() {
		return new Coordinate(this.entity.motionX, this.entity.motionY, this.entity.motionZ);
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
	public void setPosition(Coordinate c) {
		this.entity.setPosition(c.getX(), c.getY(), c.getZ());
	}
	
	/**
	 * Changes the players dimension
	 */
	public void changeDimension(int dimension) {
		this.entity.travelToDimension(dimension);
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
	public Coordinate getPosition() {
		return new Coordinate(this.entity.posX, this.entity.posY, this.entity.posZ);
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
	 * @param allowAbstract if <i><b>V</b></i> is of type <i><b>Class</b></i>, tells if abstract classes are allowed
	 * @return the map or null if it wasn't found
	 */
	public static <K,V> Map<K,V> getEntityMap(Class<K> from, Class<V> to, boolean allowAbstract) {
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
	public static boolean spawnEntity(String entity, Coordinate location, World world, NBTTagCompound compound, boolean mergeLists) {
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
					((EntityLiving)entityInstance).onSpawnWithEgg(null);
				
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
					if (!(entityClass.isInstance(loaded))) continue;
					if (location.getDistanceBetweenCoordinates(new Coordinate(loaded.posX,loaded.posY,loaded.posZ)) < distance)
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
			if (remove instanceof EntityPlayer || remove.isEntityInvulnerable()) continue;
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