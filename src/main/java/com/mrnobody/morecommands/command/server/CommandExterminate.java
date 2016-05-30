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
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.BlockPos;

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
	public String getUsage() {
		return "command.exterminate.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		int strength = 4;
		com.mrnobody.morecommands.wrapper.Entity entity = new com.mrnobody.morecommands.wrapper.Entity(
		getSenderAsEntity(sender.getMinecraftISender(), net.minecraft.entity.Entity.class));
		Entity hit = entity.traceEntity(128.0D);
		
		if (hit != null) {
			if (hit instanceof EntityLiving)  {
				if (params.length > 0) {
					try {strength = Integer.parseInt(params[0]);}
					catch (NumberFormatException nfe) {throw new CommandException("command.exterminate.invalidArg", sender);}
				}
				
				entity.getWorld().createExplosion(entity.getMinecraftEntity(), new BlockPos(hit.posX, hit.posY, hit.posZ), strength);
				
				sender.sendLangfileMessage("command.exterminate.boooom");
			}
			else throw new CommandException("command.exterminate.notLiving", sender);
		}
		else throw new CommandException("command.exterminate.notFound", sender);
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
