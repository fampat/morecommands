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
		name = "invrotate",
		description = "command.invrotate.description",
		example = "command.invrotate.example",
		syntax = "command.invrotate.syntax",
		videoURL = "command.invrotate.videoURL"
		)
public class CommandInvrotate extends ServerCommand {
    public String getCommandName()
    {
        return "invrotate";
    }
    
    public String getUsage()
    {
        return "command.invrotate.syntax";
    }
    
	public void execute(CommandSender sender, String[] params) throws CommandException {
		int items = 9;
		boolean leftToRight = true;
		
		if (params.length > 0) {
			if (params[0].equalsIgnoreCase("item")) items = 1;
			else if (params[0].equalsIgnoreCase("line")) items  = 9;
			else {
				try {items = Integer.parseInt(params[0]);}
				catch (Exception e) {throw new CommandException("command.invrotate.invalidUsage", sender);}
			}
		}
		
		if (params.length > 1) {
			if (params[1].equalsIgnoreCase("left") || params[1].equalsIgnoreCase("up")) leftToRight = false;
			else if (params[1].equalsIgnoreCase("right") || params[1].equalsIgnoreCase("down")) leftToRight = true;
			else throw new CommandException("command.invrotate.invalidUsage", sender);
		}
		
		ItemStack main[] = ((EntityPlayerMP) sender.getMinecraftISender()).inventory.mainInventory;
		ItemStack sorted[] = new ItemStack[main.length];
		items %= main.length;
        
		for (int i = 0; i < main.length; i++) {
			int pos = 0;
			
			if (leftToRight) pos = i + items;
			else pos = i - items;
			
			pos = pos < 0 ? main.length + pos : pos;
			sorted[pos % main.length] = main[i];
		}

		((EntityPlayerMP) sender.getMinecraftISender()).inventory.mainInventory = sorted;
		main = null;
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
