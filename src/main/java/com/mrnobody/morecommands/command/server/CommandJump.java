package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Entity;

@Command(
		name = "jump",
		description = "command.jump.description",
		example = "command.jump.example",
		syntax = "command.jump.syntax",
		videoURL = "command.jump.videoURL"
		)
public class CommandJump extends ServerCommand {

	@Override
	public String getName() {
		return "jump";
	}

	@Override
	public String getUsage() {
		return "command.jump.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Entity entity = new Entity((net.minecraft.entity.Entity) sender.getMinecraftISender());
		BlockPos hit = entity.traceBlock(128);
		
		if (hit == null) throw new CommandException("command.jump.notInSight", sender);
		else {
			int y = hit.getY() + 1;
			while (y < 260) {
				if (entity.getWorld().isClear(new BlockPos(hit.getX(), y++, hit.getZ()))) {
					entity.setPosition(new BlockPos(hit.getX() + 0.5F, --y, hit.getZ() + 0.5F));
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
		return sender instanceof net.minecraft.entity.Entity;
	}
}
