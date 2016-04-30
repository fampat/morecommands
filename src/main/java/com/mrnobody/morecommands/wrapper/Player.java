package com.mrnobody.morecommands.wrapper;

import java.util.Iterator;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.stats.Achievement;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.WorldSettings.GameType;

/**
 * A wrapper for the {@link EntityPlayerMP} class
 * 
 * @author MrNobody98
 */
public class Player extends com.mrnobody.morecommands.wrapper.EntityLivingBase {
	private final EntityPlayerMP player;
	
	public Player(EntityPlayerMP player) {
		super(player);
		this.player = player;
	}
   
	/**
	 * Sends a chat message from this player
	 */
	public void sendChatMessage(String message) {
		this.player.addChatMessage(new ChatComponentText(message));
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
		
		if (!this.player.inventory.addItemStackToInventory(itemStack)) {
			this.player.dropItem(item, quantity);
		}
	}
	
	/**
	 * Gives an amount of items with metadata and nbt data to the player
	 */
	public void givePlayerItem(Item item, int quantity, int meta, NBTTagCompound compound) {
		ItemStack itemStack = new ItemStack(item, quantity, meta);
		if (compound != null) itemStack.setTagCompound(compound);
		
		if (!this.player.inventory.addItemStackToInventory(itemStack)) {
			this.player.dropItem(item, quantity);
		}
	}
   
	/**
	 * @return the players hunger
	 */
	public int getHunger() {
		return this.player.getFoodStats().getFoodLevel();
	}
   
	/**
	 * Sets the players hunger
	 */
	public void setHunger(int food) {
		this.player.getFoodStats().setFoodLevel(food);
	}
   
	/**
	 * @return whether player damage is enabled
	 */
	public boolean getDamage() {
		return !this.player.capabilities.disableDamage;
	}
   
	/**
	 * Sets whether player damage is enabled
	 */
	public void setDamage(boolean damage) {
		this.player.capabilities.disableDamage = !damage;
	}
   
	/**
	 * Sets an inventory slot
	 */
	public boolean setInventorySlot(int slot, int id, int quantity, int damage) {
		if (slot < 0 || slot >= this.player.inventory.mainInventory.length) {
			return false;
		} else if ((Item) Item.itemRegistry.getObjectById(id) == null) {
			if (id == 0) {
				this.player.inventory.mainInventory[slot] = null;
				return true;
			}
		return false;
		}
		this.player.inventory.mainInventory[slot] = new ItemStack(Item.getItemById(id), quantity, damage);
		return true;
	}

	/**
	 * @return the current game type
	 */
	public String getGameType() {
		return this.player.theItemInWorldManager.getGameType().getName();
	}
   
	/**
	 * Sets the current game type
	 */
	public boolean setGameType(String gametype) {
		GameType chosen = null;
		if ((chosen = GameType.getByName(gametype)) == null) {
			return false;
		}
		this.player.setGameType(chosen);
		return true;
	}
   
	/**
	 * @return the players name
	 */
	public String getPlayerName() {
		return this.player.getCommandSenderName();
	}
   
	/**
	 * @return the players current item
	 */
	public Item getCurrentItem() {
		return this.player.getCurrentEquippedItem().getItem();
	}
   
	/**
	 * @return the players current slot
	 */
	public int getCurrentSlot() {
		return this.player.inventory.currentItem;
	}
	
	/**
	 * Sets the players current slot
	 */
	public void setCurrentSlot(int index, ItemStack item) {
		this.player.inventory.setInventorySlotContents(index, item);
	}
   
	/**
	 * @return the {@link EntityPlayerMP} this object wraps
	 */
	public EntityPlayerMP getMinecraftPlayer() {
		return this.player;
	}
	
	/**
	 * @return whether the player has this achievement
	 */
	public boolean hasAchievement(Achievement ach) {
		return this.player.func_147099_x().hasAchievementUnlocked(ach);
	}
   
	/**
	 * triggers an achievement
	 */
	public void addAchievement(Achievement ach) {
		this.player.triggerAchievement(ach);
	}
	
	/**
	 * removes an achievement
	 */
	public void removeAchievement(Achievement ach) {
        this.player.func_147099_x().func_150873_a(this.player, ach, 0);
        Iterator iterator = this.player.getWorldScoreboard().func_96520_a(ach.func_150952_k()).iterator();

        while (iterator.hasNext())
        {
            ScoreObjective scoreobjective = (ScoreObjective) iterator.next();
            this.player.getWorldScoreboard().func_96529_a(this.player.getCommandSenderName(), scoreobjective).setScorePoints(0);
        }

        if (this.player.func_147099_x().func_150879_e())
        {
            this.player.func_147099_x().func_150876_a(this.player);
        }
	}
	
	/**
	 * @return the player's spawn point
	 */
	public Coordinate getSpawn() {
		return this.player.getBedLocation(this.player.dimension) != null ? new Coordinate(this.player.getBedLocation(this.player.dimension).posX, 
		this.player.getBedLocation(this.player.dimension).posY, this.player.getBedLocation(this.player.dimension).posZ) : null;
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
		this.player.setSpawnChunk(new ChunkCoordinates(x, y, z), true, this.player.dimension);
	}
   
	/**
	 * @return whether player is allowed to fly
	 */
	public boolean getAllowFlying() {
		return this.player.capabilities.allowFlying;
	}
   
	/**
	 * Sets whether player is allowed to fly
	 */
	public void setAllowFlying(boolean allow) {
		this.player.capabilities.allowFlying = allow;
		this.player.capabilities.isFlying = allow;
		this.player.sendPlayerAbilities();
	}
   
	/**
	 * @return whether player is in creative mode
	 */
	public boolean isCreativeMode() {
		return this.player.capabilities.isCreativeMode;
	}
   
	/**
	 * @return The display name
	 */
	public String getDisplayName() {
		return this.player.getDisplayName();
	}
}
