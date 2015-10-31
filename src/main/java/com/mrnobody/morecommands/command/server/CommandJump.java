package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Coordinate;
import com.mrnobody.morecommands.wrapper.Player;

@Command(
		name = "jump",
		description = "command.jump.description",
		example = "command.jump.example",
		syntax = "command.jump.syntax",
		videoURL = "command.jump.videoURL"
		)
public class CommandJump extends ServerCommand {

	@Override
	public String getCommandName() {
		return "jump";
	}

	@Override
	public String getUsage() {
		return "command.jump.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Player player = new Player((EntityPlayerMP) sender.getMinecraftISender());
		Coordinate hit = player.traceBlock(128);
		
		if (hit == null) throw new CommandException("command.jump.notInSight", sender);
		else {
			int y = hit.getBlockY() + 1;
			while (y < 260) {
				if (player.getWorld().isClear(new Coordinate(hit.getBlockX(), y++, hit.getBlockZ()))) {
					player.setPosition(new Coordinate(hit.getBlockX() + 0.5F, --y, hit.getBlockZ() + 0.5F));
					break;
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
		return 2;
	}
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return sender instanceof EntityPlayerMP;
	}
}
