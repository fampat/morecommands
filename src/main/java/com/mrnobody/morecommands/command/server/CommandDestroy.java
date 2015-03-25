package com.mrnobody.morecommands.command.server;

import net.minecraft.entity.player.EntityPlayer;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "destroy",
		description = "command.destroy.description",
		example = "command.destroy.example",
		syntax = "command.destroy.syntax",
		videoURL = "command.destroy.videoURL"
		)
public class CommandDestroy extends ServerCommand {

	@Override
	public String getName() {
		return "destroy";
	}

	@Override
	public String getUsage() {
		return "command.destroy.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		EntityPlayer player = sender.toPlayer().getMinecraftPlayer();
		
		if (params.length > 0 && params[0].equalsIgnoreCase("all")) {
			for (int i = 0; i < player.inventory.mainInventory.length; i++) {
				player.inventory.mainInventory[i] = null;
			}
		}
		else {
			player.inventory.mainInventory[player.inventory.currentItem] = null;
		}
		
		sender.sendLangfileMessageToPlayer("command.destroy.destroyed", new Object[0]);
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
		return 0;
	}
}
