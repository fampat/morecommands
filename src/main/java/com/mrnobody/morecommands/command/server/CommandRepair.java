package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

@Command(
		name = "repair",
		description = "command.repair.description",
		example = "command.repair.example",
		syntax = "command.repair.syntax",
		videoURL = "command.repair.videoURL"
		)
public class CommandRepair extends StandardCommand implements ServerCommandProperties {

	@Override
	public String getCommandName() {
		return "repair";
	}

	@Override
	public String getCommandUsage() {
		return "command.repair.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		String repair = "this";
		if (params.length > 0 && params[0].equalsIgnoreCase("all")) repair = "all";
		
		EntityPlayerMP player = getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class);
		
		if (repair.equals("this")) {this.resetDamageOnItem(player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND));}
		else if (repair.equals("all")) {
			for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
	        	 this.resetDamageOnItem(player.inventory.getStackInSlot(i));
	         }
		}
		
		return null;
	}
	
	private void resetDamageOnItem(ItemStack stack) {
		if (stack == null || stack.getItem() == null) return;
		if (stack.getHasSubtypes() || !stack.isItemDamaged()) return;
		stack.setItemDamage(0);
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
