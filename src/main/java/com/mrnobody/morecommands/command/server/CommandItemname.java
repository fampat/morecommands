package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.command.CommandBase.Requirement;
import com.mrnobody.morecommands.command.CommandBase.ServerType;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import cpw.mods.fml.relauncher.Side;

@Command(
		name = "itemname",
		description = "command.itemname.description",
		example = "command.itemname.example",
		syntax = "command.itemname.syntax",
		videoURL = "command.itemname.videoURL"
		)
public class CommandItemname extends ServerCommand {

	@Override
	public String getCommandName() {
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
				throw new CommandException("command.itemname.noSelection", sender);
		}
		else
			throw new CommandException("command.itemname.invalidUsage", sender);
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
