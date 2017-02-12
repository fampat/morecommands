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
import net.minecraft.util.BlockPos;

@Command(
		name = "explode",
		description = "command.explode.description",
		example = "command.explode.example",
		syntax = "command.explode.syntax",
		videoURL = "command.explode.videoURL"
		)
public class CommandExplode extends StandardCommand implements ServerCommandProperties {

	@Override
	public String getCommandName() {
		return "explode";
	}

	@Override
	public String getCommandUsage() {
		return "command.explode.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		Entity entity = getSenderAsEntity(sender.getMinecraftISender(), Entity.class);
		int size = 4;
		BlockPos spawn = EntityUtils.traceBlock(entity, 128.0D);
		boolean success = spawn != null;
		double x = 0.0D, y = 0.0D, z = 0.0D;
		if (spawn != null) {
			x = spawn.getX();
			y = spawn.getY();
			z = spawn.getZ();
		}
		
		if (params.length > 0) {
			if (params.length > 3) {
				try {
					x = Double.parseDouble(params[1]);
					y = Double.parseDouble(params[2]);
					z = Double.parseDouble(params[3]);
					success = true;
				}
				catch (NumberFormatException e) {throw new CommandException("command.explode.NAN", sender);}
			}
			
			try {size = Integer.parseInt(params[0]);}
			catch (NumberFormatException e) {throw new CommandException("command.explode.NAN", sender);}
		}
		
		if (success) {
			WorldUtils.createExplosion(entity.worldObj, entity, new BlockPos(x, y, z), size);
			sender.sendLangfileMessage("command.explode.booooom");
		}
		else throw new CommandException("command.explode.notInSight", sender);
		
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
		return isSenderOfEntityType(sender, Entity.class);
	}
}
