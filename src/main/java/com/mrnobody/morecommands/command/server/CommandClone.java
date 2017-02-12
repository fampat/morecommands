package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.EntityUtils;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
	public String getCommandUsage() {
		return "command.clone.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		int quantity = 1;
		
		if (params.length > 0) {
			try {quantity = Integer.parseInt(params[0]);}
			catch (NumberFormatException nfe) {throw new CommandException("command.clone.invalidArg", sender);}
		}
		
		EntityLivingBase living = getSenderAsEntity(sender.getMinecraftISender(), EntityLivingBase.class);
		Entity entity = EntityUtils.traceEntity(living, 128.0D);
		
		if (entity == null)
			throw new CommandException("command.clone.noNPCFound", sender);
		
		NBTTagCompound compound = new NBTTagCompound(); entity.writeToNBT(compound);
		ResourceLocation name = EntityList.func_191301_a(entity);
		World world = living.worldObj;
		BlockPos coord = new BlockPos(entity.posX, entity.posY, entity.posZ);
		
		for (int i = 0; i < quantity; i++) {
			if (!EntityUtils.spawnEntity(name, true, coord, world, compound, false))
				throw new CommandException("command.clone.errored", sender, name);
		}
		
		sender.sendLangfileMessage("command.clone.success");
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
		return isSenderOfEntityType(sender, EntityLivingBase.class);
	}
}
