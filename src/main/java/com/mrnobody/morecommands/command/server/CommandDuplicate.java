package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.patch.EntityPlayerMP;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "duplicate",
		description = "command.duplicate.description",
		example = "command.duplicate.example",
		syntax = "command.duplicate.syntax",
		videoURL = "command.duplicate.videoURL"
		)
public class CommandDuplicate extends ServerCommand {

	@Override
	public String getName() {
		return "duplicate";
	}

	@Override
	public String getUsage() {
		return "command.duplicate.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		EntityPlayer player = (EntityPlayer) sender.getMinecraftISender();
		
		if (params.length > 0 && params[0].equalsIgnoreCase("all")) {
			for (int i = 0; i < player.inventory.mainInventory.length; i++) {
				if (player.inventory.mainInventory[i] == null) continue;
				
				ItemStack item = player.inventory.mainInventory[i];
				ItemStack duplicate = new ItemStack(item.getItem(), item.stackSize, item.getItemDamage());
				duplicate.setTagCompound((NBTTagCompound) item.getTagCompound().copy());
				
				EntityItem itemEntity = new EntityItem(player.worldObj, player.posX, player.posY, player.posZ, duplicate);
				player.worldObj.spawnEntityInWorld(itemEntity);
			}
			
			for (int i = 0; i < player.inventory.armorInventory.length; i++) {
				if (player.inventory.armorInventory[i] == null) continue;
				
				ItemStack item = player.inventory.armorInventory[i];
				ItemStack duplicate = new ItemStack(item.getItem(), item.stackSize, item.getItemDamage());
				duplicate.setTagCompound((NBTTagCompound) item.getTagCompound().copy());
				
				EntityItem itemEntity = new EntityItem(player.worldObj, player.posX, player.posY, player.posZ, duplicate);
				player.worldObj.spawnEntityInWorld(itemEntity);
			}
			
			
		}
		else {
			if (player.inventory.mainInventory[player.inventory.currentItem] == null) {
				sender.sendLangfileMessage("command.duplicate.notSelected", new Object[0]);
				return;
			}
			
			ItemStack item = player.inventory.mainInventory[player.inventory.currentItem];
			ItemStack duplicate = new ItemStack(item.getItem(), item.stackSize, item.getItemDamage());
			duplicate.setTagCompound((NBTTagCompound) item.getTagCompound().copy());
			
			EntityItem itemEntity = new EntityItem(player.worldObj, player.posX, player.posY, player.posZ, duplicate);
			player.worldObj.spawnEntityInWorld(itemEntity);
		}
		
		sender.sendLangfileMessage("command.duplicate.duplicated");
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
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return sender instanceof EntityPlayerMP;
	}
}
