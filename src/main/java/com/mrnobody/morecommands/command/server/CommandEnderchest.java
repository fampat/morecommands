package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;

import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.patch.EntityPlayerMP;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Player;

@Command(
		name = "enderchest",
		description = "command.enderchest.description",
		example = "command.enderchest.example",
		syntax = "command.enderchest.syntax",
		videoURL = "command.enderchest.videoURL"
		)
public class CommandEnderchest extends ServerCommand {

	@Override
	public String getName() {
		return "enderchest";
	}

	@Override
	public String getUsage() {
		return "command.enderchest.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		((EntityPlayerMP) sender.getMinecraftISender()).displayGUIChest(((EntityPlayerMP) sender.getMinecraftISender()).getInventoryEnderChest());
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
