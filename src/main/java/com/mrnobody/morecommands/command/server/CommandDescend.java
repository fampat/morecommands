package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Coordinate;
import com.mrnobody.morecommands.wrapper.Entity;

import net.minecraft.command.ICommandSender;

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
	public String getUsage() {
		return "command.descend.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
    	Entity entity = new Entity(getSenderAsEntity(sender.getMinecraftISender(), net.minecraft.entity.Entity.class));
    	Coordinate coord = entity.getPosition();
    	int y = coord.getBlockY() - 1;
    	
    	while (y > 0) {
    		System.out.println("CLEAR(" + y + "): " + entity.getWorld().isClear(new Coordinate(coord.getX(), y, coord.getX())));
    		if (entity.getWorld().isClear(new Coordinate(coord.getBlockX(), y--, coord.getBlockZ()))) {
    			entity.setPosition(new Coordinate(coord.getBlockX() + 0.5F, ++y, coord.getBlockZ() + 0.5F));
    			System.out.println("POS:" + entity.getPosition());
     			sender.sendLangfileMessage("command.descend.descended", Math.abs(y - coord.getBlockY()));
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
