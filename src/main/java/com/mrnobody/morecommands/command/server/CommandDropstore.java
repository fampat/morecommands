package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.WorldUtils;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;

@Command(
		name = "dropstore",
		description = "command.dropstore.description",
		example = "command.dropstore.example",
		syntax = "command.dropstore.syntax",
		videoURL = "command.dropstore.videoURL"
		)
public class CommandDropstore extends StandardCommand implements ServerCommandProperties {

	@Override
	public String getCommandName() {
		return "dropstore";
	}

	@Override
	public String getCommandUsage() {
		return "command.dropstore.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params)throws CommandException {
		EntityPlayerMP player = getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class);
		BlockPos coord1 = new BlockPos(player.getPosition().getX() + 1, player.getPosition().getY(), player.getPosition().getZ());
		BlockPos coord2 = new BlockPos(player.getPosition().getX() + 1, player.getPosition().getY(), player.getPosition().getZ() + 1);
		
		WorldUtils.setBlock(player.worldObj, coord1, Blocks.CHEST);
		WorldUtils.setBlock(player.worldObj, coord2, Blocks.CHEST);
		
		InventoryLargeChest chestInv = new InventoryLargeChest("Large chest", (TileEntityChest) player.worldObj.getTileEntity(coord1), (TileEntityChest) player.worldObj.getTileEntity(coord2));
		
        int count = 0;
        for (int i = 0; i < player.inventory.getSizeInventory() && count < chestInv.getSizeInventory(); i++) {
        	chestInv.setInventorySlotContents(count++, player.inventory.getStackInSlot(i));
        	player.inventory.setInventorySlotContents(i, null);
        }
        
        sender.sendLangfileMessage("command.dropstore.stored");
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
		return 2;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return isSenderOfEntityType(sender, EntityPlayerMP.class);
	}
}
