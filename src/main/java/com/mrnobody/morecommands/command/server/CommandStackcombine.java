package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

@Command(
		name = "stackcombine",
		description = "command.stackcombine.description",
		example = "command.stackcombine.example",
		syntax = "command.stackcombine.syntax",
		videoURL = "command.stackcombine.videoURL"
		)
public class CommandStackcombine extends ServerCommand {
    public String getName()
    {
        return "stackcombine";
    }
    
    public String getUsage()
    {
        return "command.stackcombine.syntax";
    }
    
	public void execute(CommandSender sender, String[] params) throws CommandException {
		EntityPlayerMP player = (EntityPlayerMP) sender.getMinecraftISender();
		
		for (int i = 0; i < player.inventory.mainInventory.length; i++) {
			ItemStack sloti = player.inventory.mainInventory[i];
			if (sloti == null) continue;
			
            for (int j = i + 1; j < player.inventory.mainInventory.length; j++) {
            	ItemStack slotj = player.inventory.mainInventory[j];
            	if (slotj == null) continue;
            	
            	if (sloti.isItemEqual(slotj)) {
            		if (sloti.stackSize + slotj.stackSize > sloti.getMaxStackSize()) {
            			int noItems = sloti.stackSize + slotj.stackSize;
            			sloti.stackSize = sloti.getMaxStackSize(); noItems -= sloti.getMaxStackSize();
            			slotj.stackSize = noItems > sloti.getMaxStackSize() ? sloti.getMaxStackSize() : noItems;
            			noItems -= sloti.getMaxStackSize();
            		}
            		else {
            			sloti.stackSize += slotj.stackSize;
            			player.inventory.mainInventory[j] = null;
            		}
            	}
            }
		}
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
		return 0;
	}
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return sender instanceof EntityPlayerMP;
	}
}
