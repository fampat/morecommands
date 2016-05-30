package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Entity;

import net.minecraft.command.ICommandSender;
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
	public String getUsage() {
		return "command.explode.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Entity entity = new Entity(getSenderAsEntity(sender.getMinecraftISender(), net.minecraft.entity.Entity.class));
		int size = 4;
		BlockPos spawn = entity.traceBlock(128.0D);
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
			entity.getWorld().createExplosion(entity.getMinecraftEntity(), new BlockPos(x, y, z), size);
			sender.sendLangfileMessage("command.explode.booooom");
		}
		else throw new CommandException("command.explode.notInSight", sender);
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
