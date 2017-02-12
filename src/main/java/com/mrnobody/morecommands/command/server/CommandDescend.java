package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.EntityUtils;
import com.mrnobody.morecommands.util.WorldUtils;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

@Command(
		name = "descend",
		description = "command.descend.description",
		example = "command.descend.example",
		syntax = "command.descend.syntax",
		videoURL = "command.descend.videoURL"
		)
public class CommandDescend extends StandardCommand implements ServerCommandProperties {

	@Override
	public String getCommandName() {
		return "descend";
	}

	@Override
	public String getCommandUsage() {
		return "command.descend.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
    	Entity entity = getSenderAsEntity(sender.getMinecraftISender(), Entity.class);
    	BlockPos coord = entity.getPosition();
    	int y = coord.getY() - 1;
    	
    	while (y > 0) {
    		if (WorldUtils.isClear(entity.worldObj, new BlockPos(coord.getX(), y--, coord.getZ()))) {
    			EntityUtils.setPosition(entity, new BlockPos(coord.getX() + 0.5F, ++y, coord.getZ() + 0.5F));
    			sender.sendLangfileMessage("command.descend.descended", Math.abs(y - coord.getY()));
    			break;
    		}
    	}
    	
    	return null;
    }
	
	@Override
	public CommandRequirement[] getRequirements() {
		return new CommandRequirement[0];
	}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	@Override
	public int getDefaultPermissionLevel(String[] args) {
		return 2;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return isSenderOfEntityType(sender, Entity.class);
	}
}
