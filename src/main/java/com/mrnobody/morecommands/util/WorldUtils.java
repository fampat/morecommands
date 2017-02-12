package com.mrnobody.morecommands.util;

import java.lang.reflect.Field;
import java.util.Random;

import com.mrnobody.morecommands.util.ObfuscatedNames.ObfuscatedField;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;

public final class WorldUtils {
	private WorldUtils() {}
	private static final Field worldInfo = ReflectionHelper.getField(ObfuscatedField.World_worldInfo);
	
	/**
	 * @return The block at the given coordinate
	 */
	public static Block getBlock(World world, Coordinate coord) {
		return world.getBlock(coord.getBlockX(), coord.getBlockY(), coord.getBlockZ());
	}
   
	/**
	 * @return The block at the given coordinate
	 */
	public static Block getBlock(World world, int x, int y, int z) {
		return world.getBlock(x, y, z);
	}
	
	/**
	 * @return Whether the block at the given coordinate is an air block
	 */
	public static boolean isAirBlock(World world, Coordinate coord) {
		return world.isAirBlock(coord.getBlockX(), coord.getBlockY(), coord.getBlockZ());
	}
	
	/**
	 * @return Whether the block at the given coordinate is an air block
	 */
	public static boolean isAirBlock(World world, int x, int y, int z) {
		return world.isAirBlock(x, y, z);
	}
	
	/**
	 * @return The metadata of the block at the given coordinates
	 */
	public static int getBlockMeta(World world, Coordinate coord) {
		return world.getBlockMetadata(coord.getBlockX(), coord.getBlockY(), coord.getBlockZ());
	}
	
	/**
	 * @return Whether the world is in hardore mode
	 */
	public static boolean isHardcore(World world) {
		return world.getWorldInfo().isHardcoreModeEnabled();
	}
	
	/**
	 * Sets the hardcore mode
	 */
	public static void setHardcore(World world, boolean hardcore) {
		changeWorldInfo(world, "hardcore", hardcore);
	}
	
	/**
	 * @return Whether cheats are enabled
	 */
	public static boolean isCheats(World world) {
		return world.getWorldInfo().areCommandsAllowed();
	}
	
	/**
	 * Sets whether you're allowed to use cheats
	 */
	public static void setCheats(World world, boolean cheats) {
		changeWorldInfo(world, "allowCommands", cheats);
	}
	
	/**
	 * Changes NBT stored world info
	 */
	private static void changeWorldInfo(World world, String key, Object value) {
		NBTTagCompound nbt = world.getWorldInfo().getNBTTagCompound();
		
		if (value instanceof String) nbt.setString(key, (String) value);
		else if (value instanceof Boolean) nbt.setBoolean(key, (Boolean) value);
		else if (value instanceof Integer) nbt.setInteger(key, (Integer) value);
		else if (value instanceof Long) nbt.setLong(key, (Long) value);
		
		ReflectionHelper.set(ObfuscatedField.World_worldInfo, world, new WorldInfo(nbt));
	}
	
	/**
	 * Sets the block at the given coordinates
	 * 
	 * @return whether the block was successfully set
	 */
	public static boolean setBlock(World world, int x, int y, int z, Block block) {
		return world.setBlock(x, y, z, block);
	}
	
	/**
	 * Sets the block at the given coordinates
	 * 
	 * @return whether the block was successfully set
	 */
	public static boolean setBlock(World world, Coordinate coord, Block block) {
		return world.setBlock(coord.getBlockX(), coord.getBlockY(), coord.getBlockZ(), block);
	}
	
	/**
	 * Sets the block's metadata
	 * 
	 * @return whether the block metadata was successfully set
	 */
	public static boolean setBlockMeta(World world, Coordinate coord, int meta) {
		return world.setBlockMetadataWithNotify(coord.getBlockX(), coord.getBlockY(), coord.getBlockZ(), meta, 2);
	}
	
	/**
	 * Sets a block with metadata
	 * 
	 * @return whether the block was successfully set
	 */
	public static boolean setBlockWithMeta(World world, Coordinate coord, Block block, int meta) {
		return world.setBlock(coord.getBlockX(), coord.getBlockY(), coord.getBlockZ(), block, meta, 2);
	}
	
	/**
	 * Destroys a block
	 * @param drop whether to drop the block
	 * @return whether the block was destroyed
	 */
	public static boolean destroyBlock(World world, Coordinate coord, boolean drop) {
		return world.func_147480_a(coord.getBlockX(), coord.getBlockY(), coord.getBlockZ(), drop);
	}
	
	/**
	 * @return the world's time
	 */
	public static long getTime(World world) {
		return world.getWorldTime();
	}
	
	/**
	 * Sets the world's time
	 */
	public static void setTime(World world, long time) {
		world.setWorldTime(time);
	}
	
	/**
	 * @return whether it's raining
	 */
	public static boolean isRaining(World world) {
		return world.isRaining();
	}
	
	/**
	 * Sets the rain
	 */
	public static void setRaining(World world, boolean raining) {
		world.getWorldInfo().setRaining(raining);
	}
	
	/**
	 * @return whether it'S thundering
	 */
	public static boolean isThunder(World world) {
		return world.isThundering();
	}
	
	/**
	 * Sets the world's thunder
	 */
	public static void setThunder(World world, boolean thunder) {
		world.getWorldInfo().setThundering(thunder);
	}
	
	/**
	 * Spawns a lightning at the given coordinate
	 */
	public static void useLightning(World world, Coordinate coordinate) {
		EntityLightningBolt lightning = new EntityLightningBolt(world, coordinate.getX(),coordinate.getY(),coordinate.getZ());
		world.addWeatherEffect(lightning);
	}
	
	/**
	 * @return the world's name
	 */
	public static String getName(World world) {
		return world.getWorldInfo().getWorldName();
	}
	
	/**
	 * @return the light level of the block at the given coordinate
	 */
	public static int getBlockLightLevel(World world, Coordinate coordinate) {
		return world.getBlockLightValue(coordinate.getBlockX(), coordinate.getBlockY(), coordinate.getBlockZ());
	}
	
	/**
	 * @return the world's spawn point
	 */
	public static Coordinate getSpawn(World world) {
		return new Coordinate(world.getSpawnPoint().posX, world.getSpawnPoint().posY, world.getSpawnPoint().posZ);
	}
	
	/**
	 * Sets the world's spawn point
	 */
	public static void setSpawn(World world, Coordinate coordinate) {
		world.setSpawnLocation(coordinate.getBlockX(), coordinate.getBlockY(), coordinate.getBlockZ());
	}
	
	/**
	 * @return whether the player can stand in this position
	 */
	public static boolean canStand(World world, Coordinate location) {
		return isAirBlock(world, location.getBlockX(), location.getBlockY(), location.getBlockZ())
			&& isAirBlock(world, location.getBlockX(), location.getBlockY() + 1, location.getBlockZ())
			&& (!isAirBlock(world, location.getBlockX(), location.getBlockY() - 1 , location.getBlockZ()) ||
				!isAirBlock(world, location.getBlockX(), location.getBlockY() - 2 , location.getBlockZ()) ||
				!isAirBlock(world, location.getBlockX(), location.getBlockY() - 3 , location.getBlockZ()) ||
				!isAirBlock(world, location.getBlockX(), location.getBlockY() - 4 , location.getBlockZ()) ||
				!isAirBlock(world, location.getBlockX(), location.getBlockY() - 5 , location.getBlockZ()));
	}
	   
	/**
	 * @return whether the blocks around are air blocks
	 */
	public static boolean isClear(World world, Coordinate location) {
		return isAirBlock(world, location.getBlockX(), location.getBlockY(), location.getBlockZ())
			&& isAirBlock(world, location.getBlockX(), location.getBlockY() + 1, location.getBlockZ())
			&& !isAirBlock(world, location.getBlockX(), location.getBlockY() - 1, location.getBlockZ());
	}
	   
	/**
	 * @return whether the blocks below are air blocks
	 */
	public static boolean isClearBelow(World world, Coordinate location) {
		return isAirBlock(world, location.getBlockX(), location.getBlockY(), location.getBlockZ())
			&& isAirBlock(world, location.getBlockX(), location.getBlockY() + 1, location.getBlockZ())
			&& isAirBlock(world, location.getBlockX(), location.getBlockY() - 1, location.getBlockZ());
	}
	
	/**
	 * Creates an explosion at the given coordinates
	 */
	public static void createExplosion(World world, Entity cause, Coordinate coordinate, int size) {
		world.createExplosion(cause, coordinate.getX(), coordinate.getY(), coordinate.getZ(), size, true);
	}
	
	/**
	 * Gets a tile entity at the given coordinate if there is one else null
	 */
	public static TileEntity getTileEntity(World world, Coordinate coord) {
		return world.getTileEntity(coord.getBlockX(), coord.getBlockY(), coord.getBlockZ());
	}
	
	/**
	 * Gets a tile entity at the given coordinate if there is one else null
	 */
	public static TileEntity getTileEntity(World world, int blockX, int blockY, int blockZ) {
		return world.getTileEntity(blockX, blockY, blockZ);
	}
}
