package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Coordinate;
import com.mrnobody.morecommands.wrapper.EntityLivingBase;
import com.mrnobody.morecommands.wrapper.World;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;

@Command(
		name = "clone",
		description = "command.clone.description",
		example = "command.clone.example",
		syntax = "command.clone.syntax",
		videoURL = "command.clone.videoURL"
		)
public class CommandClone extends StandardCommand implements ServerCommandProperties {

	@Override
	public String getCommandName() {
		return "clone";
	}

	@Override
	public String getUsage() {
		return "command.clone.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		int quantity = 1;
		
		if (params.length > 0) {
			try {quantity = Integer.parseInt(params[0]);}
			catch (NumberFormatException nfe) {throw new CommandException("command.clone.invalidArg", sender);}
		}
		
		EntityLivingBase living = new EntityLivingBase(getSenderAsEntity(sender.getMinecraftISender(), net.minecraft.entity.EntityLivingBase.class));
		Entity entity = living.traceEntity(128.0D);
		
		if (entity == null)
			throw new CommandException("command.clone.noNPCFound", sender);
		
		NBTTagCompound compound = new NBTTagCompound(); entity.writeToNBT(compound);
		String name = EntityList.getEntityString(entity);
		World world = living.getWorld();
		Coordinate coord = new Coordinate(entity.posX, entity.posY, entity.posZ);
		
		for (int i = 0; i < quantity; i++) {
			if (!com.mrnobody.morecommands.wrapper.Entity.spawnEntity(name, coord, world, compound, false))
				throw new CommandException("command.clone.errored", sender, name);
		}
		
		sender.sendLangfileMessage("command.clone.success");
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
