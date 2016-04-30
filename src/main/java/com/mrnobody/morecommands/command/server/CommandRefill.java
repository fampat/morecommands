package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

@Command(
		name = "refill",
		description = "command.refill.description",
		example = "command.refill.example",
		syntax = "command.refill.syntax",
		videoURL = "command.refill.videoURL"
		)
public class CommandRefill extends StandardCommand implements ServerCommandProperties {

	@Override
	public String getCommandName() {
		return "refill";
	}

	@Override
	public String getUsage() {
		return "command.refill.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params)throws CommandException {
		EntityPlayer player = getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class);
		
		if (params.length > 0 && params[0].equalsIgnoreCase("all")) {
			for (int i = 0; i < player.inventory.mainInventory.length; i++) {
				if (player.inventory.mainInventory[i] != null) 
					player.inventory.mainInventory[i].stackSize = player.inventory.mainInventory[i].getMaxStackSize();
			}
		}
		else {
			if (player.inventory.mainInventory[player.inventory.currentItem] != null) 
				player.inventory.mainInventory[player.inventory.currentItem].stackSize = player.inventory.mainInventory[player.inventory.currentItem].getMaxStackSize();
			else
				throw new CommandException("command.refill.noSelection", sender);
		}
		
		sender.sendLangfileMessage("command.refill.refilled");
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
