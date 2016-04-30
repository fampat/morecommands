package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.EntityLivingBase;

import net.minecraft.command.ICommandSender;

@Command(
		name = "health",
		description = "command.health.description",
		example = "command.health.example",
		syntax = "command.health.syntax",
		videoURL = "command.health.videoURL"
		)
public class CommandHealth extends StandardCommand implements ServerCommandProperties {
	private static final float MIN_HEALTH = 0.5f;
	private static final float MAX_HEALTH = 20.0f;
	
	@Override
	public String getName() {
		return "health";
	}

	@Override
	public String getUsage() {
		return "command.health.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		EntityLivingBase entity = new EntityLivingBase(getSenderAsEntity(sender.getMinecraftISender(), net.minecraft.entity.EntityLivingBase.class));
		
		if (params.length > 0) {
			try {entity.setHealth(Float.parseFloat(params[0])); sender.sendLangfileMessage("command.health.success");}
			catch (NumberFormatException e) {
				if (params[0].equalsIgnoreCase("min")) {entity.setHealth(MIN_HEALTH); sender.sendLangfileMessage("command.health.success");}
				else if (params[0].equalsIgnoreCase("max")) {entity.setHealth(MAX_HEALTH); sender.sendLangfileMessage("command.health.success");}
				else if (params[0].equalsIgnoreCase("get")) {sender.sendLangfileMessage("command.health.get", entity.getHealth());}
				else throw new CommandException("command.health.invalidParam", sender);
			}
		}
		else throw new CommandException("command.generic.invalidUsage", sender, this.getName());
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
		return isSenderOfEntityType(sender, net.minecraft.entity.EntityLivingBase.class);
	}
}
