package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.EntityUtils;
import com.mrnobody.morecommands.util.WorldUtils;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.BlockPos;

@Command(
		name = "exterminate",
		description = "command.exterminate.description",
		example = "command.exterminate.example",
		syntax = "command.exterminate.syntax",
		videoURL = "command.exterminate.videoURL"
		)
public class CommandExterminate extends StandardCommand implements ServerCommandProperties {

	@Override
	public String getCommandName() {
		return "exterminate";
	}

	@Override
	public String getCommandUsage() {
		return "command.exterminate.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		int strength = 4;
		Entity entity = getSenderAsEntity(sender.getMinecraftISender(), Entity.class);
		Entity hit = EntityUtils.traceEntity(entity, 128.0D);
		
		if (hit != null) {
			if (hit instanceof EntityLiving)  {
				if (params.length > 0) {
					try {strength = Integer.parseInt(params[0]);}
					catch (NumberFormatException nfe) {throw new CommandException("command.exterminate.invalidArg", sender);}
				}
				
				WorldUtils.createExplosion(entity.world, entity, new BlockPos(hit.posX, hit.posY, hit.posZ), strength);
				
				sender.sendLangfileMessage("command.exterminate.boooom");
			}
			else throw new CommandException("command.exterminate.notLiving", sender);
		}
		else throw new CommandException("command.exterminate.notFound", sender);
		
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
		return isSenderOfEntityType(sender, net.minecraft.entity.Entity.class);
	}
}
