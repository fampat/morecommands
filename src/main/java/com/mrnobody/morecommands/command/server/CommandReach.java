package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "reach",
		description = "command.reach.description",
		example = "command.reach.example",
		syntax = "command.reach.syntax",
		videoURL = "command.reach.videoURL"
		)
public class CommandReach extends ServerCommand {
	@Override
	public String getName() {
		return "reach";
	}

	@Override
	public String getUsage() {
		return "command.reach.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		EntityPlayerMP playerEntity = (EntityPlayerMP) sender.getMinecraftISender();
			
		if (params.length > 0) {
			try {
				float distance = Float.parseFloat(params[0]);
				MoreCommands.getMoreCommands().getPacketDispatcher().sendS08Reach(playerEntity, distance);
				playerEntity.theItemInWorldManager.setBlockReachDistance(distance);
				sender.sendLangfileMessage("command.reach.set", new Object[] {params[0]});
			}
			catch (NumberFormatException e) {
				if (params[0].equalsIgnoreCase("reset")) {
					MoreCommands.getMoreCommands().getPacketDispatcher().sendS08Reach(playerEntity, 5.0F);
					playerEntity.theItemInWorldManager.setBlockReachDistance(5.0F);
					sender.sendLangfileMessage("command.reach.reset", new Object[0]);
				}
				else {sender.sendLangfileMessage("command.reach.invalidArg", new Object[0]);}
			}
		}
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[] {Requirement.MODDED_CLIENT, Requirement.PATCH_ENTITYPLAYERSP};
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
