package com.mrnobody.morecommands.command.server;

import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.Vec3;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Entity;

@Command(
		name = "bring",
		description = "command.bring.description",
		example = "command.bring.example",
		syntax = "command.bring.syntax",
		videoURL = "command.bring.videoURL"
		)
public class CommandBring extends ServerCommand {

	@Override
	public String getCommandName() {
		return "bring";
	}

	@Override
	public String getUsage() {
		return "command.bring.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params)throws CommandException {
		Entity entity = new Entity((net.minecraft.entity.EntityLivingBase) sender.getMinecraftISender());
		double radius = 128.0D;
		String entityType = "item";
		
		if (params.length > 1) {
			try {radius = Double.parseDouble(params[1]);}
			catch (NumberFormatException e) {throw new CommandException("command.bring.NAN", sender);}
		}
		
		if (params.length > 0) {
			if (Entity.getEntityClass(params[0]) == null) {
				try {radius = Double.parseDouble(params[0]);}
				catch (NumberFormatException e) {throw new CommandException("command.bring.unknownEntity", sender);}
			}
			else entityType = params[0];
		}
				
		if (radius > 0 && radius < 256) {
			List<net.minecraft.entity.Entity> foundEntities = Entity.findEntities(entityType, entity.getPosition(), entity.getWorld(), radius);
			Vec3 vec3D = entity.getMinecraftEntity().getLook(1.0F);
					
			double d = 5.0D;
			double offsetY = entity.getMinecraftEntity().posY + entity.getMinecraftEntity().getEyeHeight();
			double d1 = entity.getMinecraftEntity().posX + vec3D.xCoord * d;
			double d2 = offsetY  + vec3D.yCoord * d;
			double d3 = entity.getMinecraftEntity().posZ + vec3D.zCoord * d;
					
			for (net.minecraft.entity.Entity foundEntity : foundEntities) {
				if (foundEntity == entity.getMinecraftEntity()) continue;
				foundEntity.setPosition(d1, d2 + 0.5D, d3);
			}
		}
		else throw new CommandException("command.bring.invalidRadius", sender);
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	@Override
	public int getPermissionLevel() {
		return 2;
	}
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return sender instanceof EntityLivingBase;
	}
}
