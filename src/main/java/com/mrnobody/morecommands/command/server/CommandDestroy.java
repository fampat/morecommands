package com.mrnobody.morecommands.command.server;

import java.util.Arrays;
import java.util.List;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.TargetSelector;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Command(
		name = "destroy",
		description = "command.destroy.description",
		example = "command.destroy.example",
		syntax = "command.destroy.syntax",
		videoURL = "command.destroy.videoURL"
		)
public class CommandDestroy extends StandardCommand implements ServerCommandProperties {

	@Override
	public String getCommandName() {
		return "destroy";
	}

	@Override
	public String getCommandUsage() {
		return "command.destroy.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		params = reparseParamsWithNBTData(params);
		boolean isTarget = params.length > 0 && isTargetSelector(params[0]);
		int startIndex = isTarget ? 1 : 0; int clearMode, slot = -1; 
		int meta = -1; Item item = null; NBTTagCompound nbt = null; boolean equalLists = false;
		
		if (params.length > startIndex) {
			if (params[startIndex].startsWith("slot.")) {
				clearMode = 1;
				slot = TargetSelector.getSlotForShortcut(params[startIndex]);
				if (slot == -1) throw new CommandException("command.destroy.invalidSlot", sender, params[startIndex]);
			}
			else {
				clearMode = 2;
				
				if (!params[startIndex].equals("*")) {
					item = getItem(params[startIndex]);
					if (item == null) throw new CommandException("command.destroy.itemNotFound", sender, params[startIndex]);
				}
				
				if (params.length > startIndex + 1 && !params[startIndex + 1].equals("*")) {
					try {meta = Integer.parseInt(params[startIndex + 1]);}
					catch (NumberFormatException nfe) {throw new CommandException("command.destroy.NAN", sender);}
				}
				
				if (params.length > startIndex + 2) {
					NBTBase base = getNBTFromParam(params[startIndex + 2]);
					if (base == null || !(base instanceof NBTTagCompound)) throw new CommandException("command.destroy.invalidNBT", sender);
					nbt = (NBTTagCompound) base;
				}
				
				if (params.length > startIndex + 3)
					equalLists = isEqualLists(params[startIndex + 3]);
			}
		}
		else clearMode = 0;
		
		if (!isTarget || (isTarget && !params[0].startsWith("@b"))) {
			List<? extends Entity> entities; int replaced = 0;
			
			if (!isTarget) entities = Arrays.asList(getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class));
			else entities = TargetSelector.EntitySelector.matchEntities(sender.getMinecraftISender(), params[0], Entity.class);
			
			if (clearMode == 0) {
				for (Entity entity : entities) {
					if (TargetSelector.replaceCurrentItem(entity, ItemStack.EMPTY)) replaced++;
					if (entity instanceof EntityPlayer) ((EntityPlayer) entity).inventoryContainer.detectAndSendChanges();
				}
			}
			else if (clearMode == 1) {
				for (Entity entity : entities) {
					if (TargetSelector.replaceItemInInventory(entity, slot, ItemStack.EMPTY)) replaced++;
					if (entity instanceof EntityPlayer) ((EntityPlayer) entity).inventoryContainer.detectAndSendChanges();
				}
			}
			else if (clearMode == 2) {
				for (Entity entity : entities) {
					if (TargetSelector.replaceMatchingItems(entity, item, meta, nbt, equalLists, ItemStack.EMPTY)) replaced++;
					if (entity instanceof EntityPlayer) ((EntityPlayer) entity).inventoryContainer.detectAndSendChanges();
				}
			}
		}
		else {
			if (clearMode == 0) throw new CommandException("command.destroy.noCurrentItem", sender);
			else if (clearMode == 1) {
				final int slot_f = slot;
				
				TargetSelector.BlockSelector.matchBlocks(sender.getMinecraftISender(), params[0], true, new TargetSelector.BlockSelector.BlockCallback() {
					@Override public void applyToCoordinate(World world, BlockPos pos) {}
					
					@Override public void applyToTileEntity(TileEntity entity) {
						if (entity instanceof IInventory) 
							TargetSelector.replaceItemInInventory((IInventory) entity, slot_f, ItemStack.EMPTY);
					}
				});
			}
			else if (clearMode == 2) {
				final NBTTagCompound nbt_f = nbt;
				final Item item_f = item;
				final int meta_f = meta;
				final boolean equalLists_f = equalLists;
				
				TargetSelector.BlockSelector.matchBlocks(sender.getMinecraftISender(), params[0], true, new TargetSelector.BlockSelector.BlockCallback() {
					@Override public void applyToCoordinate(World world, BlockPos pos) {}
					
					@Override public void applyToTileEntity(TileEntity entity) {
						if (entity instanceof IInventory) 
							TargetSelector.replaceMatchingItems((IInventory) entity, item_f, meta_f, nbt_f, equalLists_f, ItemStack.EMPTY);
					}
				});
			}
		}
		
		return null;
	}
	
	@Override
	public CommandRequirement[] getRequirements() {
		return new CommandRequirement[0];
	}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	@Override
	public int getDefaultPermissionLevel(String[] args) {
		return 0;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		if (params.length > 0 && isTargetSelector(params[0])) return true;
		else return isSenderOfEntityType(sender, EntityPlayerMP.class);
	}
}
