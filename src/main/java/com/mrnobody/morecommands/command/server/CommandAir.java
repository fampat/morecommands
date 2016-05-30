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
		name = "air",
		description = "command.air.description",
		example = "command.air.example",
		syntax = "command.air.syntax",
		videoURL = "command.air.videoURL"
		)
public class CommandAir extends StandardCommand implements ServerCommandProperties {
	private static final int AIR_MIN = 1;
	private static final int AIR_MAX = 300;
	
	@Override
	public String getCommandName() {
		return "air";
	}

	@Override
	public String getCommandUsage() {
		return "command.air.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Entity entity = getSenderAsEntity(sender.getMinecraftISender(), Entity.class);
    	
		if (params.length > 0 && entity.isInWater()) {
			try {entity.setAir(Integer.parseInt(params[0])); sender.sendLangfileMessage("command.air.success");}
			catch (NumberFormatException e) {
				if (params[0].equalsIgnoreCase("min")) {entity.setAir(this.AIR_MIN); sender.sendLangfileMessage("command.air.success");}
				else if (params[0].equalsIgnoreCase("max")) {entity.setAir(this.AIR_MAX); sender.sendLangfileMessage("command.air.success");}
				else if (params[0].equalsIgnoreCase("get")) {sender.sendLangfileMessage("command.air.get", entity.getAir());}
				else throw new CommandException("command.air.invalidParam", sender);
			}
		}
		else if (!entity.isInWater()) throw new CommandException("command.air.notInWater", sender);
		else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
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
