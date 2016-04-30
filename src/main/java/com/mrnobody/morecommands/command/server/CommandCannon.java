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
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.util.MathHelper;

@Command(
		name = "cannon",
		description = "command.cannon.description",
		example = "command.cannon.example",
		syntax = "command.cannon.syntax",
		videoURL = "command.cannon.videoURL"
		)
public class CommandCannon extends StandardCommand implements ServerCommandProperties {

	@Override
	public String getCommandName() {
		return "cannon";
	}

	@Override
	public String getUsage() {
		return "command.cannon.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Entity entity = new Entity(getSenderAsEntity(sender.getMinecraftISender(), net.minecraft.entity.Entity.class));
		
		EntityTNTPrimed tnt = new EntityTNTPrimed(entity.getWorld().getMinecraftWorld());
		
		tnt.setLocationAndAngles(entity.getPosition().getX(), entity.getPosition().getY() + 1, entity.getPosition().getZ(), entity.getYaw(), entity.getPitch());
		tnt.fuse = 40;
		tnt.motionX = -MathHelper.sin((tnt.rotationYaw / 180F) * 3.141593F) * MathHelper.cos((tnt.rotationPitch / 180F) * 3.141593F);
		tnt.motionZ = MathHelper.cos((tnt.rotationYaw / 180F) * 3.141593F) * MathHelper.cos((tnt.rotationPitch / 180F) * 3.141593F);
		tnt.motionY = -MathHelper.sin((tnt.rotationPitch / 180F) * 3.141593F);
		
		double multiplier = 1;
		if (params.length > 0) {
			try {multiplier = Double.parseDouble(params[0]);} 
			catch (NumberFormatException e) {throw new CommandException("command.cannon.NAN", sender);}
		}
		
		tnt.motionX *= multiplier;
		tnt.motionY *= multiplier;
		tnt.motionZ *= multiplier;

		entity.getWorld().getMinecraftWorld().spawnEntityInWorld(tnt);
		sender.sendLangfileMessage("command.cannon.success");
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
