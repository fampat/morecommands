package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Entity;

@Command(
		name = "ascend",
		description = "command.ascend.description",
		example = "command.ascend.example",
		syntax = "command.ascend.syntax",
		videoURL = "command.ascend.videoURL"
		)
public class CommandAscend extends ServerCommand {
    public String getName()
    {
        return "ascend";
    }
    
    public String getUsage()
    {
        return "command.ascend.syntax";
    }
    
    public void execute(CommandSender sender, String[] params) throws CommandException {
    	Entity entity = new Entity((net.minecraft.entity.Entity) sender.getMinecraftISender());
    	BlockPos coord = entity.getPosition();
    	int y = coord.getY() + 1;
    	
    	while (y < 260) {
    		if (entity.getWorld().isClear(new BlockPos(coord.getX(), y++, coord.getZ()))) {
    			entity.setPosition(new BlockPos(coord.getX() + 0.5F, --y, coord.getZ() + 0.5F));
    			sender.sendLangfileMessage("command.ascend.ascended", Math.abs(y - coord.getY()));
    			break;
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
	
	public void unregisterFromHandler() {}
	
	@Override
	public int getPermissionLevel() {
		return 2;
	}
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return sender instanceof net.minecraft.entity.Entity;
	}
}
