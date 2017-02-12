package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

@Command(
		name = "melt",
		description = "command.melt.description",
		example = "command.melt.example",
		syntax = "command.melt.syntax",
		videoURL = "command.melt.videoURL"
		)
public class CommandMelt extends StandardCommand implements ServerCommandProperties {

	@Override
	public String getCommandName() {
		return "melt";
	}

	@Override
	public String getCommandUsage() {
		return "command.melt.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params)throws CommandException {
		boolean all = false;
		if (params.length > 0 && params[0].equalsIgnoreCase("all")) all = true;
		
		EntityPlayerMP player = getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class);
		ItemStack result;
		int smelt = 0;
		
		if (!all) {
			if (player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND) == ItemStack.field_190927_a) return null;
			result = FurnaceRecipes.instance().getSmeltingResult(player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND));
			if (result != ItemStack.field_190927_a) player.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(result.getItem(),
													player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).func_190916_E(), result.getItemDamage()));
		}
		else {
			for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
				if (player.inventory.getStackInSlot(i) == ItemStack.field_190927_a) continue;
				result = FurnaceRecipes.instance().getSmeltingResult(player.inventory.getStackInSlot(i));
				if (result != ItemStack.field_190927_a) player.inventory.setInventorySlotContents(i, new ItemStack(result.getItem(),
														player.inventory.getStackInSlot(i).func_190916_E(), result.getItemDamage()));
				if (result != ItemStack.field_190927_a) smelt++;
			}
		}
		
		sender.sendLangfileMessage("command.melt.molten", smelt);
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
