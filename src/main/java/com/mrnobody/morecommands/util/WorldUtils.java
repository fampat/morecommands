package com.mrnobody.morecommands.util;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public final class WorldUtils {
	private WorldUtils() {}
	
	/**
	 * @return The block at the given coordinate
	 */
	public static Block getBlock(World world, BlockPos coord) {
		return world.getBlockState(coord).getBlock();
	}
   
	/**
	 * @return The block at the given coordinate
	 */
	public static Block getBlock(World world, int x, int y, int z) {
		return world.getBlockState(new BlockPos(x, y, z)).getBlock();
	}
	
	/**
	 * @return Whether the block at the given coordinate is an air block
	 */
	public static boolean isAirBlock(World world, BlockPos coord) {
		return world.isAirBlock(coord);
	}
	
	/**
	 * @return Whether the block at the given coordinate is an air block
	 */
	public static boolean isAirBlock(World world, int x, int y, int z) {
		return world.isAirBlock(new BlockPos(x, y, z));
	}
	
	/**
	 * @return The metadata of the block at the given coordinates
	 */
	public static int getBlockMeta(World world, BlockPos coord) {
		IBlockState state = world.getBlockState(coord);
		return state.getBlock().getMetaFromState(state);
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
		world.getWorldInfo().setHardcore(hardcore);
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
		world.getWorldInfo().setAllowCommands(cheats);
	}
	
	/**
	 * Sets the block at the given coordinates
	 * 
	 * @return whether the block was successfully set
	 */
	public static boolean setBlock(World world, int x, int y, int z, Block block) {
		return world.setBlockState(new BlockPos(x, y, z), block.getDefaultState());
	}
	
	/**
	 * Sets the block at the given coordinates
	 * 
	 * @return whether the block was successfully set
	 */
	public static boolean setBlock(World world, BlockPos coord, Block block) {
		return world.setBlockState(coord, block.getDefaultState());
	}
	
	/**
	 * Sets the block's metadata
	 * 
	 * @return whether the block metadata was successfully set
	 */
	public static boolean setBlockMeta(World world, BlockPos coord, int meta) {
		return world.setBlockState(coord, world.getBlockState(coord).getBlock().getStateFromMeta(meta));
	}
	
	/**
	 * Sets a block with metadata
	 * 
	 * @return whether the block was successfully set
	 */
	public static boolean setBlockWithMeta(World world, BlockPos coord, Block block, int meta) {
		return world.setBlockState(coord, block.getStateFromMeta(meta));
	}
	
	/**
	 * Destroys a block
	 * @param drop whether to drop the block
	 * @return whether the block was destroyed
	 */
	public static boolean destroyBlock(World world, BlockPos coord, boolean drop) {
		return world.destroyBlock(coord, drop);
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
	public static void useLightning(World world, BlockPos coordinate) {
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
	public static int getBlockLightLevel(World world, BlockPos coordinate) {
		return world.getBlockLightOpacity(coordinate);
	}
	
	/**
	 * @return the world's spawn point
	 */
	public static BlockPos getSpawn(World world) {
		return world.getSpawnPoint();
	}
	
	/**
	 * Sets the world's spawn point
	 */
	public static void setSpawn(World world, BlockPos coordinate) {
		world.setSpawnPoint(coordinate);
	}
	
	/**
	 * @return whether the player can stand in this position
	 */
	public static boolean canStand(World world, BlockPos location) {
		return isAirBlock(world, location)
			&& isAirBlock(world, location.up())
			&& (!isAirBlock(world, location.down(1)) ||
				!isAirBlock(world, location.down(2)) ||
				!isAirBlock(world, location.down(3)) ||
				!isAirBlock(world, location.down(4)) ||
				!isAirBlock(world, location.down(5)));
	}
	   
	/**
	 * @return whether the blocks around are air blocks
	 */
	public static boolean isClear(World world, BlockPos location) {
		return isAirBlock(world, location)
			&& isAirBlock(world, location.up())
			&& !isAirBlock(world, location.down());
	}
	   
	/**
	 * @return whether the blocks below are air blocks
	 */
	public static boolean isClearBelow(World world, BlockPos location) {
		return isAirBlock(world, location)
			&& isAirBlock(world, location.up())
			&& isAirBlock(world, location.down());
	}
	
	/**
	 * Creates an explosion at the given coordinates
	 */
	public static void createExplosion(World world, Entity cause, BlockPos coordinate, int size) {
		world.createExplosion(cause, coordinate.getX(), coordinate.getY(), coordinate.getZ(), size, true);
	}
	
	/**
	 * Gets a tile entity at the given coordinate if there is one else null
	 */
	public static TileEntity getTileEntity(World world, BlockPos coord) {
		return world.getTileEntity(coord);
	}
	
	/**
	 * Gets a tile entity at the given coordinate if there is one else null
	 */
	public static TileEntity getTileEntity(World world, int blockX, int blockY, int blockZ) {
		return world.getTileEntity(new BlockPos(blockX, blockY, blockZ));
	}
}