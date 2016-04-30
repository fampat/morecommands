package com.mrnobody.morecommands.wrapper;

import java.lang.reflect.Field;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.storage.WorldInfo;

/**
 * A wrapper for minecraft worlds
 * 
 * @author MrNobody98
 */
public class World {
	private final net.minecraft.world.World world;
	private final Random random;
   
	public World(net.minecraft.world.World world) {
		this.world = world;
		this.random = new Random();
	}
   
	/**
	 * @return The block at the given coordinate
	 */
	public Block getBlock(Coordinate coord) {
		return this.world.getBlock(coord.getBlockX(), coord.getBlockY(), coord.getBlockZ());
	}
   
	/**
	 * @return The block at the given coordinate
	 */
	public Block getBlock(int x, int y, int z) {
		return this.world.getBlock(x, y, z);
	}
	
	/**
	 * @return Whether the block at the given coordinate is an air block
	 */
	public boolean isAirBlock(Coordinate coord) {
		return this.world.isAirBlock(coord.getBlockX(), coord.getBlockY(), coord.getBlockZ());
	}
	
	/**
	 * @return Whether the block at the given coordinate is an air block
	 */
	public boolean isAirBlock(int x, int y, int z) {
		return this.world.isAirBlock(x, y, z);
	}
   
	/**
	 * @return The metadata of the block at the given coordinates
	 */
	public int getBlockMeta(Coordinate coord) {
		return this.world.getBlockMetadata(coord.getBlockX(), coord.getBlockY(), coord.getBlockZ());
	}
   
	/**
	 * @return Whether the world is in hardore mode
	 */
	public boolean isHardcore() {
		return this.world.getWorldInfo().isHardcoreModeEnabled();
	}
   
	/**
	 * Sets the hardcore mode
	 */
	public void setHardcore(boolean hardcore) {
		changeWorldInfo("hardcore", hardcore);
	}
   
	/**
	 * @return Whether cheats are enabled
	 */
	public boolean isCheats() {
		return this.world.getWorldInfo().areCommandsAllowed();
	}
   
	/**
	 * Sets whether you're allowed to use cheats
	 */
	public void setCheats(boolean cheats) {
		changeWorldInfo("allowCommands", cheats);
	}
   
	/**
	 * Changes NBT stored world info
	 */
	private void changeWorldInfo(String key, Object value) {
		NBTTagCompound nbt = this.world.getWorldInfo().getNBTTagCompound();
		if (value instanceof String) nbt.setString(key, (String) value);
		else if (value instanceof Boolean) nbt.setBoolean(key, (Boolean) value);
		else if (value instanceof Integer) nbt.setInteger(key, (Integer) value);
		else if (value instanceof Long) nbt.setLong(key, (Long) value);
		
		WorldInfo info = new WorldInfo(nbt);
      
		try {
			Field fields[] = net.minecraft.world.World.class.getDeclaredFields();
         
			for (Field field : fields) {
				field.setAccessible(true);
            
				if (field.get(this.world) instanceof WorldInfo) {
					field.set(this.world, info);
					break;
				}
			}
		}
		catch (Exception e) {e.printStackTrace();}
	}
   
	/**
	 * Sets the block at the given coordinates
	 * 
	 * @return whether the block was successfully set
	 */
	public boolean setBlock(int x, int y, int z, Block block) {
		return this.world.setBlock(x, y, z, block);
	}
   
	/**
	 * Sets the block at the given coordinates
	 * 
	 * @return whether the block was successfully set
	 */
	public boolean setBlock(Coordinate coord, Block block) {
		return this.world.setBlock(coord.getBlockX(), coord.getBlockY(), coord.getBlockZ(), block);
	}
   
	/**
	 * Sets the block's metadata
	 * 
	 * @return whether the block metadata was successfully set
	 */
	public boolean setBlockMeta(Coordinate coord, int meta) {
		return this.world.setBlockMetadataWithNotify(coord.getBlockX(), coord.getBlockY(), coord.getBlockZ(), meta, 2);
	}
   
	/**
	 * Sets a block with metadata
	 * 
	 * @return whether the block was successfully set
	 */
	public boolean setBlockWithMeta(Coordinate coord, Block block, int meta) {
		return this.world.setBlock(coord.getBlockX(), coord.getBlockY(), coord.getBlockZ(), block, meta, 2);
	}
	
	/**
	 * Destroys a block
	 * @param drop whether to drop the block
	 * @return whether the block was destroyed
	 */
	public boolean destroyBlock(Coordinate coord, boolean drop) {
		return this.world.func_147480_a(coord.getBlockX(), coord.getBlockY(), coord.getBlockZ(), drop);
	}
   
	/**
	 * @return the world's time
	 */
	public long getTime() {
		return this.world.getWorldTime();
	}
   
	/**
	 * Sets the world's time
	 */
	public void setTime(long time) {
		this.world.setWorldTime(time);
	}
   
	/**
	 * @return whether it's raining
	 */
	public boolean isRaining() {
		return this.world.isRaining();
	}
   
	/**
	 * Sets the rain
	 */
	public void setRaining(boolean raining) {
		this.world.getWorldInfo().setRaining(raining);
	}
   
	/**
	 * @return whether it'S thundering
	 */
	public boolean isThunder() {
		return this.world.isThundering();
	}
   
	/**
	 * Sets the world's thunder
	 */
	public void setThunder(boolean thunder) {
		this.world.getWorldInfo().setThundering(thunder);
	}
   
	/**
	 * Spawns a lightning at the given coordinate
	 */
	public void useLightning(Coordinate coordinate) {
		EntityLightningBolt lightning = new EntityLightningBolt(this.world, coordinate.getX(),coordinate.getY(),coordinate.getZ());
		this.world.addWeatherEffect(lightning);
	}
   
	/**
	 * @return the world's name
	 */
	public String getName() {
		return this.world.getWorldInfo().getWorldName();
	}
   
	/**
	 * @return the light level of the block at the given coordinate
	 */
	public int getBlockLightLevel(Coordinate coordinate) {
		return this.world.getBlockLightValue(coordinate.getBlockX(), coordinate.getBlockY(), coordinate.getBlockZ());
	}
   
	/**
	 * @return the world's spawn point
	 */
	public Coordinate getSpawn() {
		return new Coordinate(this.world.getSpawnPoint().posX, this.world.getSpawnPoint().posY + 5, this.world.getSpawnPoint().posZ);
	}
   
	/**
	 * Sets the world's spawn point
	 */
	public void setSpawn(Coordinate coordinate) {
		this.world.setSpawnLocation(coordinate.getBlockX(), coordinate.getBlockY(), coordinate.getBlockZ());
	}
	
	   
	/**
	 * @return whether the blocks around are air blocks
	 */
	public boolean isClear(Coordinate location) {
		return this.isAirBlock(location.getBlockX(), location.getBlockY(), location.getBlockZ())
			&& this.isAirBlock(location.getBlockX(), location.getBlockY() + 1, location.getBlockZ())
			&& !(this.isAirBlock(location.getBlockX(), location.getBlockY() - 1, location.getBlockZ()));
	}
	   
	/**
	 * @return whether the blocks below are air blocks
	 */
	public boolean isClearBelow(Coordinate location) {
		return this.isAirBlock(location.getBlockX(), location.getBlockY(), location.getBlockZ())
			&& this.isAirBlock(location.getBlockX(), location.getBlockY() + 1, location.getBlockZ())
			&& this.isAirBlock(location.getBlockX(), location.getBlockY() - 1, location.getBlockZ());
	}
   
	/**
	 * Creates an explosion at the given coordinates
	 */
	public void createExplosion(net.minecraft.entity.Entity cause, Coordinate coordinate, int size) {
		this.world.createExplosion(cause, coordinate.getX(), coordinate.getY(), coordinate.getZ(), size, true);
	}
   
	/**
	 * Gets a tile entity at the given coordinate if there is one else null
	 */
	public TileEntity getTileEntity(Coordinate coord) {
		return this.world.getTileEntity(coord.getBlockX(), coord.getBlockY(), coord.getBlockZ());
	}

	/**
	 * Gets a tile entity at the given coordinate if there is one else null
	 */
	public TileEntity getTileEntity(int blockX, int blockY, int blockZ) {
		return this.world.getTileEntity(blockX, blockY, blockZ);
	}
   
	/**
	 * Gets the minecraft world object
	 */
	public net.minecraft.world.World getMinecraftWorld() {
		return this.world;
	}
}
