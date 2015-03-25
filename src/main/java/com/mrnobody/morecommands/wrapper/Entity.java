package com.mrnobody.morecommands.wrapper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.util.DamageSource;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;

public class Entity {
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
	private static <K,V> Map<K,V> getEntityMap(Class<K> from, Class<V> to) {
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
				net.minecraft.entity.Entity entityInstance = (net.minecraft.entity.Entity) entityClass.getConstructor(net.minecraft.world.World.class).newInstance(world.getMinecraftWorld());
				entityInstance.setPosition(location.getX(), location.getY() + 1, location.getZ());
				world.getMinecraftWorld().spawnEntityInWorld(entityInstance);
				
				if(entityInstance instanceof EntityLiving) {
					((EntityLiving)entityInstance).playLivingSound();
				}
				
				return true;
			}
		}
		catch (Exception e) { 
			e.printStackTrace();
		}
		
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
					if (location.getDistanceBetweenCoordinates(new Coordinate(loaded.posX,loaded.posY,loaded.posZ)) < distance) {
						toremove.add(loaded);
					}
				}
				
				for (net.minecraft.entity.Entity remove : toremove) {
					if(remove instanceof EntityPlayer) continue;
					if(remove.isEntityInvulnerable(DamageSource.outOfWorld)) continue;
					remove.attackEntityFrom(DamageSource.outOfWorld, 1000);
					removedEntities.add(remove);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
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
					if (location.getDistanceBetweenCoordinates(new Coordinate(loaded.posX,loaded.posY,loaded.posZ)) < distance) {
						found.add(loaded);
					}
				}
				
				for (net.minecraft.entity.Entity foundEntity : found) {
					foundEntities.add(foundEntity);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return foundEntities;
	}
}