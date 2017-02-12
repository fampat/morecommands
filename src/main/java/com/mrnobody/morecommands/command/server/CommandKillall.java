package com.mrnobody.morecommands.command.server;

import java.util.List;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.EntityUtils;
import com.mrnobody.morecommands.util.TargetSelector;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityList;

@Command(
		name = "killall",
		description = "command.killall.description",
		example = "command.killall.example",
		syntax = "command.killall.syntax",
		videoURL = "command.killall.videoURL"
		)
public class CommandKillall extends StandardCommand implements ServerCommandProperties {

	@Override
	public String getCommandName() {
		return "killall";
	}

	@Override
	public String getCommandUsage() {
		return "command.killall.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		params = reparseParamsWithNBTData(params);
		double radius = 128.0D;
		String entityType = "mob";
		
		if (params.length > 0) {
			if (isTargetSelector(params[0])) {
				List<net.minecraft.entity.Entity> removedEntities = EntityUtils.killEntities(TargetSelector.EntitySelector.matchEntites(sender.getMinecraftISender(), params[0], net.minecraft.entity.Entity.class));
				sender.sendLangfileMessage("command.killall.killed", removedEntities.size());
			}
			else {
				entityType = params[0];
				
				if (EntityUtils.getEntityClass(entityType, true) == null) {
					try {entityType = EntityList.getStringFromID(Integer.parseInt(params[0]));}
					catch (NumberFormatException e) {throw new CommandException("command.killall.unknownEntity", sender);}
				}
				
				if (params.length > 1) {
					try {radius = Double.parseDouble(params[1]);}
					catch (NumberFormatException e) {throw new CommandException("command.killall.NAN", sender);}
				}
				
				if (radius <= 0 || radius > 256) throw new CommandException("command.killall.invalidRadius", sender);
				else {
					List<net.minecraft.entity.Entity> removedEntities = EntityUtils.killEntities(entityType, true, sender.getPosition(), sender.getWorld(), radius);
					sender.sendLangfileMessage("command.killall.killed", removedEntities.size());
				}
			}
		}
		else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
		
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
		return true;
	}
}
