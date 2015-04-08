package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.patch.EntityPlayerMP;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "itemname",
		description = "command.itemname.description",
		example = "command.itemname.example",
		syntax = "command.itemname.syntax",
		videoURL = "command.itemname.videoURL"
		)
public class CommandItemname extends ServerCommand {

	@Override
	public String getName() {
		return "itemname";
	}

	@Override
	public String getUsage() {
		return "command.itemname.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params)throws CommandException {
		if (params.length > 0) {
			EntityPlayer player = (EntityPlayer) sender.getMinecraftISender();
			String name = "";
			
			for (String param : params) name += " " + param;
			
			if (player.inventory.mainInventory[player.inventory.currentItem] != null)
				player.inventory.mainInventory[player.inventory.currentItem].setStackDisplayName(name.trim());
			else
				sender.sendLangfileMessage("command.itemname.noSelection", new Object[0]);
		}
		else
			sender.sendLangfileMessage("command.itemname.invalidUsage", new Object[0]);
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
