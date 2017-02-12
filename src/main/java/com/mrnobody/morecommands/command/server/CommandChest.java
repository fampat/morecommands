package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.EntityUtils;
import com.mrnobody.morecommands.util.WorldUtils;

import net.minecraft.block.Block;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockPos;

@Command(
		name = "chest",
		description = "command.chest.description",
		example = "command.chest.example",
		syntax = "command.chest.syntax",
		videoURL = "command.chest.videoURL"
		)
public class CommandChest extends StandardCommand implements ServerCommandProperties {
	@Override
	public String getCommandName() {
		return "chest";
	}

	@Override
	public String getCommandUsage() {
		return "command.chest.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params)throws CommandException {
		if (params.length < 1)
			throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
		
		EntityPlayerMP player = getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class);
		BlockPos coord = EntityUtils.traceBlock(player, 128.0D);
		
		if (coord == null)
			throw new CommandException("command.chest.noBlockInSight", sender);
		
		Block block = WorldUtils.getBlock(player.worldObj, coord);
		
        int x1 = coord.getX();
        int y1 = coord.getY();
        int z1 = coord.getZ();
        int x2 = coord.getX() + 1;
        int y2 = coord.getY();
        int z2 = coord.getZ();
		
		if (params[0].equalsIgnoreCase("drop")) {
            y1 += 1; y2 += 1;
            WorldUtils.setBlock(player.worldObj, new BlockPos(x1, y1, z1), Blocks.chest);
            WorldUtils.setBlock(player.worldObj, new BlockPos(x2, y2, z2), Blocks.chest);
		}
		else if (params[0].equalsIgnoreCase("fill") || params[0].equalsIgnoreCase("get") || params[0].equalsIgnoreCase("swap") || params[0].equalsIgnoreCase("clear")) {
			if (WorldUtils.getBlock(player.worldObj, coord) == Blocks.chest) {
				if (WorldUtils.getBlock(player.worldObj, x2, y2, z2) == Blocks.chest);
				else if (WorldUtils.getBlock(player.worldObj, x1 - 1, y1, z1) == Blocks.chest) x2 = x1 - 1;
				else if (WorldUtils.getBlock(player.worldObj, x1, y1, z1 + 1) == Blocks.chest) {x2 = x1; z2 = z1 + 1;}
				else if (WorldUtils.getBlock(player.worldObj, x1, y1, z1 - 1) == Blocks.chest) {x2 = x1; z2 = z1 - 1;}
				else y2 = -1;
			}
			else throw new CommandException("command.chest.noChest", sender);
		}
		
		IInventory chest = null;
		
		if (y2 > -1) chest = new InventoryLargeChest("Large chest", (TileEntityChest) WorldUtils.getTileEntity(player.worldObj, new BlockPos(x1, y1, z1)), 
							(TileEntityChest) WorldUtils.getTileEntity(player.worldObj, new BlockPos(x2, y2, z2)));
		else chest = (TileEntityChest) WorldUtils.getTileEntity(player.worldObj, new BlockPos(x1, y1, z1));
		
		if (params[0].equalsIgnoreCase("drop") || params[0].equalsIgnoreCase("fill")) {
			this.transferInventory(player.inventory, chest);
		}
		else if (params[0].equalsIgnoreCase("get")) {
			this.transferInventory(chest, player.inventory);
		}
		else if (params[0].equalsIgnoreCase("clear")) {
			this.transferInventory(chest, null);
		}
		else if (params[0].equalsIgnoreCase("swap")) {
            InventoryPlayer p = new InventoryPlayer(player);
            for (int i = 0; i < p.getSizeInventory(); i++) {
               p.setInventorySlotContents(i, player.inventory.getStackInSlot(i));
               player.inventory.setInventorySlotContents(i, null);
            }
            this.transferInventory(chest, player.inventory);
            this.transferInventory(p, chest);
		}
		
		return null;
	}

	private void transferInventory(IInventory from, IInventory to) {
		int count = 0;
		
		if (from == null) return;
		
		try {
			for (int i = 0; i < from.getSizeInventory(); i++) {
				if (to == null) {
					from.setInventorySlotContents(i, null);
					continue;
				}
				
				try {
					while (to.getStackInSlot(count) != null) count++;
					
					if (count > to.getInventoryStackLimit()) break;
					
					to.setInventorySlotContents(count, from.getStackInSlot(i));
					from.setInventorySlotContents(i, null);
				} 
				catch (Exception e) {break;}
			}
		}
		catch (Exception e) {e.printStackTrace();}
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
		return 2;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return isSenderOfEntityType(sender, EntityPlayerMP.class);
	}
}
