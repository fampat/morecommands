package com.mrnobody.morecommands.command.server;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Player;

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
		
		Player player = sender.toPlayer();
		ItemStack[] mainInventory = player.getMinecraftPlayer().inventory.mainInventory;
		
		int length = all ? mainInventory.length : 1;
		int start = all ? 0 : player.getCurrentSlot();
		ItemStack result;
		int smelt = 0;
		
		for (int i = start; i < start + length; i++) {
			if (mainInventory[i] == null) continue;
			result = FurnaceRecipes.instance().getSmeltingResult(mainInventory[i]);
			if (result != null) mainInventory[i] = new ItemStack(result.getItem(), mainInventory[i].stackSize, result.getItemDamage());
			if (result != null) smelt++;
		}
		
		sender.sendLangfileMessageToPlayer("command.melt.molten", new Object[] {smelt});
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
