package com.mrnobody.morecommands.command.server;

import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "cleardrops",
		description = "command.cleardrops.description",
		example = "command.cleardrops.example",
		syntax = "command.cleardrops.syntax",
		videoURL = "command.cleardrops.videoURL"
		)
public class CommandClearDrops extends ServerCommand {

	@Override
	public String getName() {
		return "cleardrops";
	}

	@Override
	public String getUsage() {
		return "command.cleardrops.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		BlockPos pos = sender.getPosition();
		World world = sender.getWorld().getMinecraftWorld();
		int radius = 128;
		int removedDrops = 0;
		
		AxisAlignedBB boundingBox = new AxisAlignedBB(
				pos.getX() - radius, pos.getY() - radius, pos.getZ() - radius,
				pos.getX() + radius, pos.getY() + radius, pos.getZ() + radius);
		
		List<?> nearbyEntities = world.getEntitiesWithinAABB(EntityItem.class, boundingBox);
		
		for (int entityIndex = 0; entityIndex < nearbyEntities.size(); entityIndex++) {
			Entity entity = (Entity) nearbyEntities.get(entityIndex);
			
			if (entity instanceof EntityItem) {
				EntityItem entityItem = (EntityItem)entity;
				
				world.removeEntity(entityItem);
				removedDrops++;
			}
		}
		
		sender.sendLangfileMessage("command.cleardrops.removed", removedDrops);
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
		return true;
	}
}
