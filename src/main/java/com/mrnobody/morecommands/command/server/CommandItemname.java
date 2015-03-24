package com.mrnobody.morecommands.command.server;

import net.minecraft.entity.player.EntityPlayer;

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
			EntityPlayer player = sender.toPlayer().getMinecraftPlayer();
			String name = "";
			
			for (String param : params) name += " " + param;
			
			if (player.inventory.mainInventory[player.inventory.currentItem] != null)
				player.inventory.mainInventory[player.inventory.currentItem].setStackDisplayName(name.trim());
			else
				sender.sendLangfileMessageToPlayer("command.itemname.noSelection", new Object[0]);
		}
		else
			sender.sendLangfileMessageToPlayer("command.itemname.invalidUsage", new Object[0]);
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
