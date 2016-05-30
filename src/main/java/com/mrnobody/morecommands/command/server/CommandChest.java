package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Player;

import net.minecraft.block.Block;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;

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
	public void execute(CommandSender sender, String[] params)throws CommandException {
		if (params.length < 1)
			throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
		
		Player player = new Player(getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class));
		BlockPos coord = player.traceBlock(128.0D);
		
		if (coord == null)
			throw new CommandException("command.chest.noBlockInSight", sender);
		
		Block block = player.getWorld().getBlock(coord);
		
        int x1 = coord.getX();
        int y1 = coord.getY();
        int z1 = coord.getZ();
        int x2 = coord.getX() + 1;
        int y2 = coord.getY();
        int z2 = coord.getZ();
		
		if (params[0].equalsIgnoreCase("drop")) {
            y1 += 1; y2 += 1;
            player.getWorld().setBlock(new BlockPos(x1, y1, z1), Blocks.CHEST);
            player.getWorld().setBlock(new BlockPos(x2, y2, z2), Blocks.CHEST);
		}
		else if (params[0].equalsIgnoreCase("fill") || params[0].equalsIgnoreCase("get") || params[0].equalsIgnoreCase("swap") || params[0].equalsIgnoreCase("clear")) {
			if (player.getWorld().getBlock(coord) == Blocks.CHEST) {
				if (player.getWorld().getBlock(x2, y2, z2) == Blocks.CHEST);
				else if (player.getWorld().getBlock(x1 - 1, y1, z1) == Blocks.CHEST) x2 = x1 - 1;
				else if (player.getWorld().getBlock(x1, y1, z1 + 1) == Blocks.CHEST) {x2 = x1; z2 = z1 + 1;}
				else if (player.getWorld().getBlock(x1, y1, z1 - 1) == Blocks.CHEST) {x2 = x1; z2 = z1 - 1;}
				else y2 = -1;
			}
			else throw new CommandException("command.chest.noChest", sender);
		}
		
		IInventory chest = null;
		
		if (y2 > -1) chest = new InventoryLargeChest("Large chest", (TileEntityChest) player.getWorld().getMinecraftWorld().getTileEntity(new BlockPos(x1, y1, z1)), 
							(TileEntityChest) player.getWorld().getMinecraftWorld().getTileEntity(new BlockPos(x2, y2, z2)));
		else chest = (TileEntityChest) player.getWorld().getMinecraftWorld().getTileEntity(new BlockPos(x1, y1, z1));
		
		if (params[0].equalsIgnoreCase("drop") || params[0].equalsIgnoreCase("fill")) {
			this.transferInventory(player.getMinecraftPlayer().inventory, chest);
		}
		else if (params[0].equalsIgnoreCase("get")) {
			this.transferInventory(chest, player.getMinecraftPlayer().inventory);
		}
		else if (params[0].equalsIgnoreCase("clear")) {
			this.transferInventory(chest, null);
		}
		else if (params[0].equalsIgnoreCase("swap")) {
            InventoryPlayer p = new InventoryPlayer(player.getMinecraftPlayer());
            for (int i = 0; i < p.getSizeInventory(); i++) {
               p.setInventorySlotContents(i, player.getMinecraftPlayer().inventory.getStackInSlot(i));
               player.getMinecraftPlayer().inventory.setInventorySlotContents(i, null);
            }
            this.transferInventory(chest, player.getMinecraftPlayer().inventory);
            this.transferInventory(p, chest);
		}
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
	public int getDefaultPermissionLevel() {
		return 2;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return isSenderOfEntityType(sender, EntityPlayerMP.class);
	}
}
