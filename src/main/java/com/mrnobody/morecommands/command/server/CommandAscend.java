package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLivingBase;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Coordinate;
import com.mrnobody.morecommands.wrapper.Entity;

@Command(
		name = "ascend",
		description = "command.ascend.description",
		example = "command.ascend.example",
		syntax = "command.ascend.syntax",
		videoURL = "command.ascend.videoURL"
		)
public class CommandAscend extends ServerCommand {
    public String getCommandName()
    {
        return "ascend";
    }
    
    public String getUsage()
    {
        return "command.ascend.syntax";
    }
    
    public void execute(CommandSender sender, String[] params) throws CommandException {
    	Entity entity = new Entity((net.minecraft.entity.EntityLivingBase) sender.getMinecraftISender());
    	Coordinate coord = entity.getPosition();
    	int y = coord.getBlockY() + 1;
    	
    	while (y < 260) {
    		if (entity.getWorld().isClear(new Coordinate(coord.getBlockX(), y++, coord.getBlockZ()))) {
    			entity.setPosition(new Coordinate(coord.getBlockX() + 0.5F, --y, coord.getBlockZ() + 0.5F));
    			sender.sendLangfileMessage("command.ascend.ascended", Math.abs(y - coord.getBlockY()));
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
		return sender instanceof EntityLivingBase;
	}
}
