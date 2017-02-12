package com.mrnobody.morecommands.command.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Command(
		name = "confusesuicide",
		description = "command.confusesuicide.description",
		example = "command.confusesuicide.example",
		syntax = "command.confusesuicide.syntax",
		videoURL = "command.confusesuicide.videoURL"
		)
public class CommandConfusesuicide extends StandardCommand implements ServerCommandProperties {
	private static final double RADIUS_MAX = 50;
	
	@Override
	public String getCommandName() {
		return "confusesuicide";
	}

	@Override
	public String getCommandUsage() {
		return "command.confusesuicide.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		params = reparseParamsWithNBTData(params);
		double radius = 10D;
		
		if (params.length > 0) {
			try {radius = Double.parseDouble(params[0]);}
			catch (NumberFormatException e) {throw new CommandException("command.confusesuicide.NAN", sender);}
			if (radius > RADIUS_MAX) throw new CommandException("command.confusesuicide.invalidRadius", sender);
		}
		
		List<? extends EntityCreature> entities = getEntitiesInRadius(sender.getPosition(), sender.getWorld(), EntityCreature.class, radius * radius);
		
		Iterator<? extends EntityCreature> entityIterator = entities.iterator();
		
		while (entityIterator.hasNext()) {
			EntityCreature creature = entityIterator.next();
			creature.setAttackTarget(creature);
			creature.setRevengeTarget(creature);
		}
        
		sender.sendLangfileMessage("command.confusesuicide.confused", entities.size(), radius);
		return null;
	}
	
	private <T extends Entity> List<? extends T> getEntitiesInRadius(final BlockPos coord, World world, Class<T> class1, double radius) {
		List<T> entities = new ArrayList<T>();
		
		for (int i = 0; i < world.loadedEntityList.size(); i++) {
			Entity found = (Entity) world.loadedEntityList.get(i);
			if (!(found instanceof EntityPlayer) && !found.isDead && class1.isInstance(found) && 
				(radius <= 0.0D || getDistanceBetweenCoordinates(coord, new BlockPos(found)) <= radius)) {
				entities.add((T) found);
			}
		}

		Collections.sort(entities, new Comparator<T>() {

			public int compare(T entity1, T entity2) {
				double d1 =  getDistanceBetweenCoordinates(coord, new BlockPos(entity1)) -  getDistanceBetweenCoordinates(coord, new BlockPos(entity2));
				return d1 >= 0.0D ? (d1 <= 0.0D ? 0 : 1) : -1;
			}
		});

		return entities;
	}
	
	private final double getDistanceBetweenCoordinates(BlockPos coord1, BlockPos coord2) {
		double diffX = Math.abs(coord1.getX() - coord2.getX());
		double diffY = Math.abs(coord1.getY() - coord2.getY());
		double diffZ = Math.abs(coord1.getZ() - coord2.getZ());
		return Math.sqrt((diffX * diffX) + (diffY * diffY) + (diffZ * diffZ));
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
		return true;
	}
}
