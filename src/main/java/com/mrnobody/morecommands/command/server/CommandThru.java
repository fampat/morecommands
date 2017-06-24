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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

@Command(
		name = "thru",
		description = "command.thru.description",
		example = "command.thru.example",
		syntax = "command.thru.syntax",
		videoURL = "command.thru.videoURL"
		)
public class CommandThru extends StandardCommand implements ServerCommandProperties {

	@Override
	public String getCommandName() {
		return "thru";
	}

	@Override
	public String getCommandUsage() {
		return "command.thru.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		EntityLivingBase entity = getSenderAsEntity(sender.getMinecraftISender(), EntityLivingBase.class);
		final int distance;
		
		if (params.length > 0) {
			try {distance = Integer.parseInt(params[0]);}
			catch (NumberFormatException nfe) {throw new CommandException("command.thru.NAN", sender);}
		}
		else distance = 128;
		
		Vec3d posVec = new Vec3d(entity.posX, entity.posY, entity.posZ);
		Vec3d lookVec = entity.getLook(1F);
		Vec3d traceVec = posVec.addVector(lookVec.x * distance, 0, lookVec.z * distance);
		
		RayTraceResult trace = entity.world.rayTraceBlocks(posVec, traceVec, false);
		if (trace == null) throw new CommandException("command.thru.noWall", sender);
		
		final double stepZ = Math.cos(Math.toRadians(entity.rotationYaw)), stepX = -Math.sin(Math.toRadians(entity.rotationYaw));
		
    	for (int step = 1; step < distance; step++) {
    		if (WorldUtils.canStand(entity.world, new BlockPos(trace.getBlockPos().getX() + stepX * step, trace.getBlockPos().getY(), trace.getBlockPos().getZ() + stepZ * step))) {
    			EntityUtils.setPosition(entity, new BlockPos(trace.getBlockPos().getX() + stepX * step, trace.getBlockPos().getY(), trace.getBlockPos().getZ() + stepZ * step));
     			sender.sendLangfileMessage("command.thru.wentThru");
    			break;
    		}
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
		return isSenderOfEntityType(sender, EntityLivingBase.class);
	}
}
