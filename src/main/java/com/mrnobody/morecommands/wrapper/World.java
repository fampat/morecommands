package com.mrnobody.morecommands.wrapper;

import java.lang.reflect.Field;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.gen.feature.WorldGenBigTree;
import net.minecraft.world.gen.feature.WorldGenForest;
import net.minecraft.world.gen.feature.WorldGenTaiga1;
import net.minecraft.world.gen.feature.WorldGenTaiga2;
import net.minecraft.world.gen.feature.WorldGenTrees;
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
      random = new Random();
   }
   
	/**
	 * @return The block at the given coordinate
	 */
	public Block getBlock(Coordinate coord) {
		return this.world.getBlockState(coord.toBlockPos()).getBlock();
	}
   
	/**
	 * @return The block at the given coordinate
	 */
	public Block getBlock(int x, int y, int z) {
		return this.world.getBlockState(new BlockPos(x, y, z)).getBlock();
	}
	
	/**
	 * @return Whether the block at the given coordinate is an air block
	 */
	public boolean isAirBlock(Coordinate coord) {
		return this.world.isAirBlock(coord.toBlockPos());
	}
	
	/**
	 * @return Whether the block at the given coordinate is an air block
	 */
	public boolean isAirBlock(int x, int y, int z) {
		return this.world.isAirBlock(new BlockPos(x, y, z));
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
	public boolean setBlock(BlockPos pos, Block block) {
		return this.world.setBlockState(pos, block.getDefaultState());
	}

	/**
	 * Sets the block's metadata
	 * 
	 * @return whether the block metadata was successfully set
	 */
	public boolean setBlockMeta(BlockPos pos, int meta) {
		return world.setBlockState(pos, world.getBlockState(pos).getBlock().getStateFromMeta(meta));
	}
	
	/**
	 * Sets a block with metadata
	 * 
	 * @return whether the block was successfully set
	 */
	public boolean setBlockWithMeta(BlockPos pos, Block block, int meta) {
		return this.world.setBlockState(pos, block.getStateFromMeta(meta));
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
		return this.world.getBlockLightOpacity(coordinate.toBlockPos());
	}
   
	/**
	 * @return the world's spawn point
	 */
	public Coordinate getSpawn() {
		return new Coordinate(this.world.getSpawnPoint().getX(), this.world.getSpawnPoint().getY() + 5, this.world.getSpawnPoint().getZ());
	}
   
	/**
	 * Sets the world's spawn point
	 */
	public void setSpawn(Coordinate coordinate) {
		this.world.setSpawnPoint(coordinate.toBlockPos());
	}
   
	/**
	 * Generates a big tree at the given coordinates
	 */
	public boolean generateBigTree(Coordinate coordinate) {
		return (new WorldGenBigTree(true)).generate(this.world, this.random, coordinate.toBlockPos());
	}
   
	/**
	 * Generates a normal tree at the given coordinates
	 */
	public boolean generateTree(Coordinate coordinate) {
		return (new WorldGenTrees(true)).generate(this.world, this.random, coordinate.toBlockPos());
	}
	   
	/**
	 * Generates a birch tree at the given coordinates
	 */
	public boolean generateBirchTree(Coordinate coordinate) {
		return (new WorldGenForest(true, true)).generate(this.world, this.random, coordinate.toBlockPos());
	}
   
	/**
	 * Generates a redwood tree at the given coordinates
	 */
	public boolean generateRedwoodTree(Coordinate coordinate) {
		return (new WorldGenTaiga1()).generate(this.world, this.random, coordinate.toBlockPos());
	}
   
	/**
	 * Generates a tall redwood tree at the given coordinates
	 */
	public boolean generateTallRedwoodTree(Coordinate coordinate) {
		return (new WorldGenTaiga2(true)).generate(this.world, this.random, coordinate.toBlockPos());
	}
   
	/**
	 * Creates an explosion at the given coordinates
	 */
	public void createExplosion(Player player, Coordinate coordinate, int size) {
		this.world.createExplosion(player.getMinecraftPlayer(), coordinate.getX(), coordinate.getY(), coordinate.getZ(), size, true);
	}
   
	/**
	 * Gets a tile entity at the given coordinate if there is one else null
	 */
	public TileEntity getTileEntity(BlockPos pos) {
		return this.world.getTileEntity(pos);
	}
   
	/**
	 * Gets a tile entity at the given coordinate if there is one else null
	 */
	public TileEntity getTileEntity(Coordinate coord) {
		return this.world.getTileEntity(coord.toBlockPos());
	}
   
	/**
	 * Gets a tile entity at the given coordinate if there is one else null
	 */
	public TileEntity getTileEntity(int x, int y, int z) {
		return this.world.getTileEntity(new BlockPos(x, y, z));
	}
   
	/**
	 * Gets the minecraft world object
	 */
	public net.minecraft.world.World getMinecraftWorld() {
		return this.world;
	}
}
