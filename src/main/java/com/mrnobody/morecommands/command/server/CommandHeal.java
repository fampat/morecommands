package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLivingBase;

@Command(
		name = "heal",
		description = "command.heal.description",
		example = "command.heal.example",
		syntax = "command.heal.syntax",
		videoURL = "command.heal.videoURL"
		)
public class CommandHeal extends StandardCommand implements ServerCommandProperties {
	private static final float MAX_HEALTH = 20F;

	@Override
	public String getCommandName() {
		return "heal";
	}

	@Override
	public String getCommandUsage() {
		return "command.heal.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params)throws CommandException {
		EntityLivingBase entity = getSenderAsEntity(sender.getMinecraftISender(), EntityLivingBase.class);
		
		if (params.length > 0) {
			try {entity.heal(Float.parseFloat(params[0])); sender.sendLangfileMessage("command.heal.success");}
			catch (NumberFormatException e) {throw new CommandException("command.heal.NAN", sender);}
		}
		else {
			entity.heal(MAX_HEALTH - entity.getHealth());
			sender.sendLangfileMessage("command.heal.success");
		}
		
		return null;
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
	public int getDefaultPermissionLevel(String[] args) {
		return 2;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return isSenderOfEntityType(sender, net.minecraft.entity.EntityLivingBase.class);
	}
}
