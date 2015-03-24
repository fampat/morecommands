package com.mrnobody.morecommands.wrapper;

import java.util.List;

import com.mrnobody.morecommands.wrapper.Coordinate;
import com.mrnobody.morecommands.wrapper.World;

import net.minecraft.stats.Achievement;
import net.minecraft.world.WorldSettings.GameType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.command.CommandBase;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.Vec3;

/**
 * A wrapper for the {@link EntityPlayer} class
 * 
 * @author MrNobody98
 */
public class Player {
	private final EntityPlayer player;
	
	public Player(CommandSender sender, String name) {
		this(CommandBase.getPlayer(sender.getMinecraftISender(), name));
	}
	
	public Player(CommandSender sender) {
		this(CommandBase.getCommandSenderAsPlayer(sender.getMinecraftISender()));
	}

	public Player(EntityPlayer player) {
		this.player = player;
	}

	/**
	 * Sets the players position
	 */
	public void setPosition(Coordinate c) {
		player.setPositionAndUpdate(c.getX(), c.getY(), c.getZ());
	}

	/**
	 * @return the players position
	 */
	public Coordinate getPosition() {
		return new Coordinate(player.posX, player.posY, player.posZ);
	}
   
	/**
	 * @return the players rotation yaw
	 */
	public float getYaw() {
		return player.rotationYaw;
	}
   
	/**
	 * Sets the players rotation yaw
	 */
	public void setYaw(float yaw) {
		player.rotationYaw = yaw;
	}
   
	/**
	 * @return the players rotation pitch
	 */
	public float getPitch() {
		return player.rotationPitch;
	}
   
	/**
	 * Sets the players rotation pitch
	 */
	public void setPitch(float pitch) {
		player.rotationPitch = pitch;
	}
   
	/**
	 * @return the players world
	 */
	public World getWorld() {
		return new World(player.worldObj);
	}
   
	/**
	 * Sends a chat message from this player
	 */
	public void sendChatMessage(String message) {
		player.addChatMessage(new ChatComponentText(message));
	}
	
	/**
	 * Gives an item to the player
	 */
	public void givePlayerItem(Item item) {
		givePlayerItem(item, new ItemStack(item).stackSize);
	}
   
	/**
	 * Gives an amount of items to the player
	 */
	public void givePlayerItem(Item item, int quantity) {
		givePlayerItem(item, quantity, 0);
	}
   
	/**
	 * Gives an amount of items with metadata to the player
	 */
	public void givePlayerItem(Item item, int quantity, int meta) {
		ItemStack itemStack = new ItemStack(item, quantity, meta);
		if (!player.inventory.addItemStackToInventory(itemStack)) {
			player.dropItem(item, quantity);
		}
	}
   
	/**
	 * @return the players health
	 */
	public float getHealth() {
		return player.getHealth();
	}
   
	/**
	 * Sets the players health
	 */
	public void setHealth(float health) {
		player.setHealth(health);
	}
   
	/**
	 * Heals the player by a certain amount
	 */
	public void heal(float quantity) {
		setHealth(getHealth() + quantity);
	}
   
	/**
	 * @return the players hunger
	 */
	public int getHunger() {
		return player.getFoodStats().getFoodLevel();
	}
   
	/**
	 * Sets the players hunger
	 */
	public void setHunger(int food) {
		player.getFoodStats().setFoodLevel(food);
	}
   
	/**
	 * @return whether player damage is enabled
	 */
	public boolean getDamage() {
		return !player.capabilities.disableDamage;
	}
   
	/**
	 * Sets whether player damage is enabled
	 */
	public void setDamage(boolean damage) {
		player.capabilities.disableDamage = !damage;
	}
   
	/**
	 * @return whether this block exists
	 */
	public boolean isValidBlockType(int id) {
		Item ret = (Item) Item.itemRegistry.getObjectById(id);
		return ret != null;
	}
   
	/**
	 * Sets an inventory slot
	 */
	public boolean setInventorySlot(int slot, int id, int quantity, int damage) {
		if (slot < 0 || slot >= player.inventory.mainInventory.length) {
			return false;
		} else if ((Item) Item.itemRegistry.getObjectById(id) == null) {
			if (id == 0) {
				player.inventory.mainInventory[slot] = null;
				return true;
			}
		return false;
		}
		player.inventory.mainInventory[slot] = new ItemStack(Item.getItemById(id), quantity, damage);
		return true;
	}
	
	/**
	 * traces an entity in the direction the player is looking
	 * 
	 * @return the traced entity
	 */
	public Entity traceEntity(double distance) {
		return this.tracePath(distance, 0.0D, 1.0F).entityHit;
	}
	
	/**
	 * Performs a full raytrace
	 * 
	 * @return a {@link MovingObjectPosition} with the trace results
	 */
	public MovingObjectPosition tracePath(double distance, double borderSize, float partialTickTime) {
		Vec3 startVec = getPositionVec(partialTickTime);
		Vec3 lookVec = player.getLook(partialTickTime);
		Vec3 endVec = startVec.addVector(lookVec.xCoord * distance, lookVec.yCoord * distance, lookVec.zCoord * distance);
	  
		double minX = startVec.xCoord < endVec.xCoord ? startVec.xCoord : endVec.xCoord;
		double minY = startVec.yCoord < endVec.yCoord ? startVec.yCoord : endVec.yCoord;
		double minZ = startVec.zCoord < endVec.zCoord ? startVec.zCoord : endVec.zCoord;
		double maxX = startVec.xCoord > endVec.xCoord ? startVec.xCoord : endVec.xCoord;
		double maxY = startVec.yCoord > endVec.yCoord ? startVec.yCoord : endVec.yCoord;
		double maxZ = startVec.zCoord > endVec.zCoord ? startVec.zCoord : endVec.zCoord;
		
		AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ).expand(borderSize, borderSize, borderSize);
		List<Entity> allEntities = this.player.worldObj.getEntitiesWithinAABBExcludingEntity(this.player, bb);  
		MovingObjectPosition blockHit = this.player.worldObj.rayTraceBlocks(startVec, endVec);
		
		startVec = getPositionVec(partialTickTime);
		lookVec = player.getLook(partialTickTime);
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
	 * traces the block the player is looking at
	 * 
	 * @return the traced coordinate or null
	 */
	public Coordinate trace(double distance) {
		MovingObjectPosition m = rayTrace(distance, 1.0F);
		if (m == null) return null;
		return new Coordinate(m.blockX, m.blockY, m.blockZ);
	}

	/**
	 * raytraces a block
	 * 
	 * @return a {@link MovingObjectPosition} with the coordinates
	 */
	public MovingObjectPosition rayTrace(double distance, float partialTickTime) {
		Vec3 positionVec = getPositionVec(partialTickTime);
		Vec3 lookVec = player.getLook(partialTickTime);
		Vec3 hitVec = positionVec.addVector(lookVec.xCoord * distance, lookVec.yCoord * distance, lookVec.zCoord * distance);
		return player.worldObj.rayTraceBlocks(positionVec, hitVec, false);
	}
   
	/**
	 * Gets the players position vector
	 * 
	 * @return the position vector
	 */
	public Vec3 getPositionVec(float partialTickTime) {
		double offsetY = player.posY + player.getEyeHeight();
		if (partialTickTime == 1.0F) {
			return Vec3.createVectorHelper(player.posX, offsetY, player.posZ);
		} else {
			double var2 = player.prevPosX + (player.posX - player.prevPosX) * partialTickTime;
			double var4 = player.prevPosY + (offsetY - (player.prevPosY + player.getEyeHeight())) * partialTickTime;
			double var6 = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTickTime;
			return Vec3.createVectorHelper(var2, var4, var6);
		}
	}

	/**
	 * @return the current game type
	 */
	public String getGameType() {
		return MinecraftServer.getServer().getGameType().getName();
	}
   
	/**
	 * Sets the current game type
	 */
	public boolean setGameType(String gametype) {
		GameType chosen = null;
		if ((chosen = GameType.getByName(gametype)) == null) {
			return false;
		}
		player.setGameType(chosen);
		return true;
	}
   
	/**
	 * @return the players name
	 */
	public String getPlayerName() {
		return player.getCommandSenderName();
	}
   
	/**
	 * @return the players current item
	 */
	public int getCurrentItem() {
		return Item.itemRegistry.getIDForObject(player.getCurrentEquippedItem());
	}
   
	/**
	 * @return the players current slot
	 */
	public int getCurrentSlot() {
		return player.inventory.currentItem;
	}
	
	/**
	 * Sets the players current slot
	 */
	public void setCurrentSlot(int index, ItemStack item) {
		player.inventory.setInventorySlotContents(index, item);
	}
	
	/**
	 * removes enchantments from the current item
	 */
	public void removeEnchantment() {
		ItemStack stack = player.getCurrentEquippedItem();
		int damage = stack.getItemDamage();
		
		ItemStack newItem = new ItemStack(stack.getItem());
		setCurrentSlot(getCurrentSlot(), newItem);
		newItem.damageItem(stack.getItemDamage(), player);
	}
	
	/**
	 * adds an enchantment from the current item
	 */
	public void addEnchantment(Enchantment enchantment, int level) {
		player.getCurrentEquippedItem().addEnchantment(enchantment, level);
	}
   
	/**
	 * Sets the players motion
	 */
	public void setMotion(Coordinate motion) {
		player.motionX = motion.getX();
		player.motionY = motion.getY();
		player.motionZ = motion.getZ();
	}
   
	/**
	 * @return the players motion
	 */
	public Coordinate getMotion() {
		return new Coordinate(player.motionX, player.motionY, player.motionZ);
	}
   
	/**
	 * @return whether the blocks around are air blocks
	 */
	public boolean isClear(Coordinate location) {
		return getWorld().isAirBlock(location.getBlockX(), location.getBlockY(), location.getBlockZ())
		&& getWorld().isAirBlock(location.getBlockX(), location.getBlockY() + 1, location.getBlockZ())
		&& !(getWorld().isAirBlock(location.getBlockX(), location.getBlockY() - 1, location.getBlockZ()));
	}
   
	/**
	 * @return whether the blocks below are air blocks
	 */
	public boolean isClearBelow(Coordinate location) {
		return getWorld().isAirBlock(location.getBlockX(), location.getBlockY(), location.getBlockZ())
		&& getWorld().isAirBlock(location.getBlockX(), location.getBlockY() + 1, location.getBlockZ())
		&& getWorld().isAirBlock(location.getBlockX(), location.getBlockY() - 1, location.getBlockZ());
	}
   
	/**
	 * @return the players forward movement
	 */
	public float getMovementForward() {
		return player.moveForward;
	}
   
	/**
	 * @return the players strafe movement
	 */
	public float getMovementStrafe() {
		return player.moveStrafing;
	}
   
	/**
	 * Sets the players step height
	 */
	public void setStepHeight(float height) {
		player.stepHeight = height;
	}
   
	/**
	 * @return the players step height
	 */
	public float getStepHeight() {
		return player.stepHeight;
	}
   
	/**
	 * @return the {@link EntityPlayer} object
	 */
	public EntityPlayer getMinecraftPlayer() {
		return player;
	}
   
	/**
	 * Removes a potion effect
	 */
	public void removePotionEffect(int potion) {
		player.removePotionEffect(potion);
	}
   
	/**
	 * Removes all potion effects
	 */
	public void removeAllPotionEffects() {
		player.clearActivePotions();
	}
   
	/**
	 * Adds a potion effect
	 */
	public void addPotionEffect(int id, int duration, int strength) {
		player.addPotionEffect(new PotionEffect(id, duration, strength));
	}
   
	/**
	 * Changes the players dimension
	 */
	public void changeDimension(int dimension) {
		player.travelToDimension(dimension);
	}
   
	/**
	 * Sets the players air
	 */
	public void setAir(int air) {
		player.setAir(air);
	}
   
	/**
	 * triggers an achievement
	 */
	public boolean addAchievement(String name) {
		Achievement ach = Achievements.getAchievementByName(name);
		Achievement requirement = Achievements.getAchievementByName(Achievements.getAchievementRequirement(name));
		
		if (player instanceof EntityPlayerMP) {
			if (requirement != null) {
				if (((EntityPlayerMP) player).func_147099_x().hasAchievementUnlocked(requirement)) {
					player.triggerAchievement(ach); return true;
				}
				else {return false;}
			}
			else {
				player.triggerAchievement(ach); return true;
			}
		}
		else {return false;}
	}
	
	/**
	 * @return the player's spawn point
	 */
	public Coordinate getSpawn() {
		return this.player.getBedLocation() != null ? new Coordinate(this.player.getBedLocation().posX, this.player.getBedLocation().posY, this.player.getBedLocation().posZ) : null;
	}
	
	/**
	 * Sets the players spawn point
	 */
	public void setSpawn(Coordinate coord) {
		this.player.setSpawnChunk(new ChunkCoordinates(coord.getBlockX(), coord.getBlockY(), coord.getBlockZ()), true);
	}
	
	/**
	 * Sets the players spawn point
	 */
	public void setSpawn(int x, int y, int z) {
		this.player.setSpawnChunk(new ChunkCoordinates(x, y, z), true);
	}
   
	/**
	 * @return whether player is allowed to fly
	 */
	public boolean getAllowFlying() {
		return player.capabilities.allowFlying;
	}
   
	/**
	 * Sets whether player is allowed to fly
	 */
	public void setAllowFlying(boolean allow) {
		player.capabilities.allowFlying = allow;
		player.capabilities.isFlying = allow;
		((EntityPlayerMP) player).sendPlayerAbilities();
	}
   
	/**
	 * @return whether player is in creative mode
	 */
	public boolean isCreativeMode() {
		return player.capabilities.isCreativeMode;
	}
   
	/**
	 * @return The display name
	 */
	public String getDisplayName() {
		return player.getDisplayName();
	}
}
