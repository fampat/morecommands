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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;

@Command(
		description = "command.kill.description",
		example = "command.kill.example",
		name = "command.kill.name",
		syntax = "command.kill.syntax",
		videoURL = "command.kill.videoURL"
		)
public class CommandKill extends StandardCommand implements ServerCommandProperties {
	@Override
	public String getName() {
		return "kill";
	}

	@Override
	public String getUsage() {
		return "command.kill.syntax";
	}
	
	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Entity entityToKill = getSenderAsEntity(sender.getMinecraftISender(), Entity.class);
		
		if (entityToKill instanceof EntityLivingBase)
			((EntityLivingBase) entityToKill).attackEntityFrom(DamageSource.outOfWorld, Float.MAX_VALUE);
		else
			entityToKill.setDead();
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
