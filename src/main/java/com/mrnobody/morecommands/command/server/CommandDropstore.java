package com.mrnobody.morecommands.command.server;

import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.tileentity.TileEntityChest;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Coordinate;
import com.mrnobody.morecommands.wrapper.Player;

@Command(
		name = "dropstore",
		description = "command.dropstore.description",
		example = "command.dropstore.example",
		syntax = "command.dropstore.syntax",
		videoURL = "command.dropstore.videoURL"
		)
public class CommandDropstore extends ServerCommand {

	@Override
	public String getName() {
		return "dropstore";
	}

	@Override
	public String getUsage() {
		return "command.dropstore.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params)throws CommandException {
		Player player = sender.toPlayer();
		Coordinate coord1 = new Coordinate(player.getPosition().getX() + 1, player.getPosition().getY(), player.getPosition().getZ());
		Coordinate coord2 = new Coordinate(player.getPosition().getX() + 1, player.getPosition().getY(), player.getPosition().getZ() + 1);
		
		player.getWorld().setBlock(coord1.toBlockPos(), Blocks.chest);
		player.getWorld().setBlock(coord2.toBlockPos(), Blocks.chest);
		
		InventoryLargeChest chestInv = new InventoryLargeChest("Large chest", (TileEntityChest) player.getWorld().getTileEntity(coord1), (TileEntityChest) player.getWorld().getTileEntity(coord2));
	
        int count = 0;
        for (int i = 0; i < player.getMinecraftPlayer().inventory.mainInventory.length; i++) {
        	chestInv.setInventorySlotContents(count++, player.getMinecraftPlayer().inventory.mainInventory[i]);
           player.getMinecraftPlayer().inventory.mainInventory[i] = null;
        }
        
        for (int i = 0; i < player.getMinecraftPlayer().inventory.armorInventory.length; i++) {
        	chestInv.setInventorySlotContents(count++, player.getMinecraftPlayer().inventory.armorInventory[i]);
           player.getMinecraftPlayer().inventory.armorInventory[i] = null;
        }
        
        sender.sendLangfileMessageToPlayer("command.dropstore.stored", new Object[0]);
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}
	
	@Override
	public void unregisterFromHandler() {}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	@Override
	public int getPermissionLevel() {
		return 2;
	}
}
