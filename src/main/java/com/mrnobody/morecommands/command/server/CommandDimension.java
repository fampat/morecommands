package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;

@Command(
		name = "dimension",
		description = "command.dimension.description",
		example = "command.dimension.example",
		syntax = "command.dimension.syntax",
		videoURL = "command.dimension.videoURL"
		)
public class CommandDimension extends StandardCommand implements ServerCommandProperties {
	private static final int DIMENSION_SURFACE = 0;
	private static final int DIMENSION_NETHER = -1;
	private static final int DIMENSION_END = 1;

	@Override
	public String getCommandName() {
		return "dimension";
	}

	@Override
	public String getCommandUsage() {
		return "command.dimension.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params)throws CommandException {
		if (params.length > 0) {
			Entity entity = getSenderAsEntity(sender.getMinecraftISender(), Entity.class);
			if (entity.getLastPortalVec() == null) entity.setPortal(entity.getPosition());
		
			if (params[0].equalsIgnoreCase("normal") || params[0].equalsIgnoreCase("surface") || params[0].equalsIgnoreCase(String.valueOf(this.DIMENSION_SURFACE))) 
				entity.changeDimension(DIMENSION_SURFACE);
			else if (params[0].equalsIgnoreCase("nether") || params[0].equalsIgnoreCase(String.valueOf(this.DIMENSION_NETHER))) 
				entity.changeDimension(DIMENSION_NETHER);
			else if (params[0].equalsIgnoreCase("end") || params[0].equalsIgnoreCase(String.valueOf(this.DIMENSION_END))) 
				entity.changeDimension(DIMENSION_END);
			else throw new CommandException("command.dimension.unknown", sender);
			
			sender.sendLangfileMessage("command.dimension.changed");
		}
		else throw new CommandException("command.dimension.notSpecified", sender);
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
		return isSenderOfEntityType(sender, Entity.class);
	}
}
