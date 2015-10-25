package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.command.CommandBase.Requirement;
import com.mrnobody.morecommands.command.CommandBase.ServerType;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Player;

import cpw.mods.fml.relauncher.Side;

@Command(
		name = "repair",
		description = "command.repair.description",
		example = "command.repair.example",
		syntax = "command.repair.syntax",
		videoURL = "command.repair.videoURL"
		)
public class CommandRepair extends ServerCommand {

	@Override
	public String getCommandName() {
		return "repair";
	}

	@Override
	public String getUsage() {
		return "command.repair.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		String repair = "this";
		if (params.length > 0 && params[0].equalsIgnoreCase("all")) repair = "all";
		
		Player player = new Player((EntityPlayerMP) sender.getMinecraftISender());
		
		if (repair.equals("this")) {this.resetDamageOnItem(player, player.getCurrentSlot());}
		else if (repair.equals("all")) {
			for (int i = 0; i < player.getMinecraftPlayer().inventory.getSizeInventory(); i++) {
	        	 this.resetDamageOnItem(player, i);
	         }
		}
	}
	
	public static void resetDamageOnItem(Player player, int slot) {
		if (slot < 0 || slot >= player.getMinecraftPlayer().inventory.mainInventory.length) {
			return;
		}
		ItemStack item = player.getMinecraftPlayer().inventory.mainInventory[slot];
		if (item == null) {
			return;
		}
		if (item.getHasSubtypes() || !item.isItemDamaged()) {
			return;
		}
		item.setItemDamage(0);
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}

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
