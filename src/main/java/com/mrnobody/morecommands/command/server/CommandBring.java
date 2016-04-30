package com.mrnobody.morecommands.command.server;

import java.util.List;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.TargetSelector;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Entity;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@Command(
		name = "bring",
		description = "command.bring.description",
		example = "command.bring.example",
		syntax = "command.bring.syntax",
		videoURL = "command.bring.videoURL"
		)
public class CommandBring extends StandardCommand implements ServerCommandProperties {

	@Override
	public String getCommandName() {
		return "bring";
	}

	@Override
	public String getCommandUsage() {
		return "command.bring.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params)throws CommandException {
		params = reparseParamsWithNBTData(params);
		Entity entity = !isSenderOfEntityType(sender.getMinecraftISender(), net.minecraft.entity.Entity.class) ? null :
				new Entity(getSenderAsEntity(sender.getMinecraftISender(), net.minecraft.entity.Entity.class));
		double radius = 128.0D;
		String entityType = null;
		boolean isTarget = false;
		BlockPos pos = entity == null ? sender.getPosition() : null;
		
		if (params.length > 0 && isTargetSelector(params[0])) {
			isTarget = true;
			if (params.length > 3) pos = getCoordFromParams(sender.getMinecraftISender(), params, 1);
		}
		else if (params.length > 0) {
			if (Entity.getEntityClass(params[0]) == null) {
				try {entityType = EntityList.classToStringMapping.get(EntityList.idToClassMapping.get(Integer.parseInt(params[0])));}
				catch (NumberFormatException e) {throw new CommandException("command.bring.unknownEntity", sender);}
			}
			else entityType = params[0];
			
			if (entityType == null) throw new CommandException("command.bring.unknownEntity", sender);
			int rIndex = -1, pIndex = -1;
			
			if (params.length > 4) {rIndex = 1; pIndex = 2;}
			else if (params.length > 3) {rIndex = -1; pIndex = 1;}
			else if (params.length > 1) {rIndex = 1; pIndex = -1;}
			
			if (rIndex != -1) {
				try {radius = Double.parseDouble(params[rIndex]);}
				catch (NumberFormatException e) {throw new CommandException("command.bring.NAN", sender);}
			}
			
			if (pIndex != -1)
				pos = getCoordFromParams(sender.getMinecraftISender(), params, pIndex);
		}
		else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
		
		if (pos == null && entity != null) {
			Vec3d vec3D = entity.getMinecraftEntity().getLook(1.0F);
			double d = 5.0D;
			double offsetY = entity.getMinecraftEntity().posY + entity.getMinecraftEntity().getEyeHeight();
			double d1 = entity.getMinecraftEntity().posX + vec3D.xCoord * d;
			double d2 = offsetY  + vec3D.yCoord * d;
			double d3 = entity.getMinecraftEntity().posZ + vec3D.zCoord * d;
			pos = new BlockPos(d1, d2 + 0.5D, d3);
		}
		
		if (isTarget) {
			List<? extends net.minecraft.entity.Entity> foundEntities = TargetSelector.EntitySelector.matchEntites(sender.getMinecraftISender(), params[0], net.minecraft.entity.Entity.class);
			
			for (net.minecraft.entity.Entity foundEntity : foundEntities) {
				if (entity != null && foundEntity == entity.getMinecraftEntity()) continue;
				foundEntity.setPosition(pos.getX(), pos.getY(), pos.getZ());
			}
		}
		else if (radius > 0 && radius < 256) {
			List<net.minecraft.entity.Entity> foundEntities = Entity.findEntities(entityType, sender.getPosition(), sender.getWorld(), radius);
			
			for (net.minecraft.entity.Entity foundEntity : foundEntities) {
				if (entity != null && foundEntity == entity.getMinecraftEntity()) continue;
				foundEntity.setPosition(pos.getX(), pos.getY(), pos.getZ());
			}
		}
		else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
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
		return true;
	}
}
