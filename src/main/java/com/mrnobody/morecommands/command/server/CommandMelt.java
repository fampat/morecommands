package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.patch.EntityPlayerMP;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "melt",
		description = "command.melt.description",
		example = "command.melt.example",
		syntax = "command.melt.syntax",
		videoURL = "command.melt.videoURL"
		)
public class CommandMelt extends ServerCommand {

	@Override
	public String getName() {
		return "melt";
	}

	@Override
	public String getUsage() {
		return "command.melt.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params)throws CommandException {
		boolean all = false;
		if (params.length > 0 && params[0].equalsIgnoreCase("all")) all = true;
		
		ItemStack[] mainInventory = ((EntityPlayerMP) sender.getMinecraftISender()).inventory.mainInventory;
		
		int length = all ? mainInventory.length : 1;
		int start = all ? 0 : ((EntityPlayerMP) sender.getMinecraftISender()).inventory.currentItem;
		ItemStack result;
		int smelt = 0;
		
		for (int i = start; i < start + length; i++) {
			if (mainInventory[i] == null) continue;
			result = FurnaceRecipes.instance().getSmeltingResult(mainInventory[i]);
			if (result != null) mainInventory[i] = new ItemStack(result.getItem(), mainInventory[i].stackSize, result.getItemDamage());
			if (result != null) smelt++;
		}
		
		sender.sendLangfileMessage("command.melt.molten", new Object[] {smelt});
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
