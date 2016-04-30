package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Entity;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

@Command(
		name = "ascend",
		description = "command.ascend.description",
		example = "command.ascend.example",
		syntax = "command.ascend.syntax",
		videoURL = "command.ascend.videoURL"
		)
public class CommandAscend extends StandardCommand implements ServerCommandProperties {
    public String getName()
    {
        return "ascend";
    }
    
    public String getUsage()
    {
        return "command.ascend.syntax";
    }
    
    public void execute(CommandSender sender, String[] params) throws CommandException {
    	Entity entity = new Entity(getSenderAsEntity(sender.getMinecraftISender(), net.minecraft.entity.Entity.class));
    	BlockPos coord = entity.getPosition();
    	int y = coord.getY() + 1;
    	
    	while (y < 260) {
    		if (entity.getWorld().isClear(new BlockPos(coord.getY(), y++, coord.getZ()))) {
    			entity.setPosition(new BlockPos(coord.getX() + 0.5F, --y, coord.getZ() + 0.5F));
    			sender.sendLangfileMessage("command.ascend.ascended", Math.abs(y - coord.getY()));
    			break;
    		}
    	}
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
	public int getDefaultPermissionLevel() {
		return 2;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return isSenderOfEntityType(sender, net.minecraft.entity.Entity.class);
	}
}
