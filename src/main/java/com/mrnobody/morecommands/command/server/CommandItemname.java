package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLivingBase;

@Command(
		name = "itemname",
		description = "command.itemname.description",
		example = "command.itemname.example",
		syntax = "command.itemname.syntax",
		videoURL = "command.itemname.videoURL"
		)
public class CommandItemname extends StandardCommand implements ServerCommandProperties {

	@Override
	public String getCommandName() {
		return "itemname";
	}

	@Override
	public String getCommandUsage() {
		return "command.itemname.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params)throws CommandException {
		if (params.length > 0) {
			EntityLivingBase entity = getSenderAsEntity(sender.getMinecraftISender(), EntityLivingBase.class);
			String name = "";
			
			for (String param : params) name += " " + param;
			
			if (entity.getHeldItemMainhand() != null)
				entity.getHeldItemMainhand().setStackDisplayName(name.trim());
			else
				throw new CommandException("command.itemname.noSelection", sender);
		}
		else
			throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
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
		return isSenderOfEntityType(sender, EntityLivingBase.class);
	}
}
