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
		name = "descend",
		description = "command.descend.description",
		example = "command.descend.example",
		syntax = "command.descend.syntax",
		videoURL = "command.descend.videoURL"
		)
public class CommandDescend extends ServerCommand {

	@Override
	public String getCommandName() {
		return "descend";
	}

	@Override
	public String getUsage() {
		return "command.descend.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
    	Entity entity = new Entity((EntityLivingBase) sender.getMinecraftISender());
    	Coordinate coord = entity.getPosition();
    	int y = coord.getBlockY() + 1;
    	
    	while (y > 0) {
    		if (entity.getWorld().isClear(new Coordinate(coord.getBlockX(), y--, coord.getBlockZ()))) {
    			entity.setPosition(new Coordinate(coord.getBlockX() + 0.5F, ++y, coord.getBlockZ() + 0.5F));
    			sender.sendLangfileMessage("command.descend.descended", Math.abs(y - coord.getBlockY()));
    			break;
    		}
    	}
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
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return sender instanceof EntityLivingBase;
	}
}
