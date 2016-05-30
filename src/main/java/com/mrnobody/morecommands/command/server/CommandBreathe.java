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
		name = "breathe",
		description = "command.breathe.description",
		example = "command.breathe.example",
		syntax = "command.breathe.syntax",
		videoURL = "command.breathe.videoURL"
		)
public class CommandBreathe extends StandardCommand implements ServerCommandProperties {
	private static final int AIR_MAX = 300;

	@Override
	public String getCommandName() {
		return "breathe";
	}

	@Override
	public String getCommandUsage() {
		return "command.breathe.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Entity entity = getSenderAsEntity(sender.getMinecraftISender(), Entity.class);
		int air = 0;
		
		if (params.length > 0) {
			try {air = Integer.parseInt(params[0]);}
			catch (NumberFormatException e) {throw new CommandException("command.breathe.noNumber", sender);}
		}
		else air = this.AIR_MAX;
		
		if (entity.isInWater()) {entity.setAir(entity.getAir() + air > this.AIR_MAX ? this.AIR_MAX : entity.getAir() + air);}
		else throw new CommandException("command.breathe.notInWater", sender);
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
