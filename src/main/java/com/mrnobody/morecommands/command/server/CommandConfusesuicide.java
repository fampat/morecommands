package com.mrnobody.morecommands.command.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "confusesuicide",
		description = "command.confusesuicide.description",
		example = "command.confusesuicide.example",
		syntax = "command.confusesuicide.syntax",
		videoURL = "command.confusesuicide.videoURL"
		)
public class CommandConfusesuicide extends ServerCommand {
	private final double RADIUS_MAX = 50;
	
	@Override
	public String getName() {
		return "confusesuicide";
	}

	@Override
	public String getUsage() {
		return "command.confusesuicide.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		double radius = 10D;
		
		if (params.length > 0) {
			try {radius = Double.parseDouble(params[0]);}
			catch (NumberFormatException nfe) {sender.sendLangfileMessageToPlayer("command.confusesuicide.invalidArg", new Object[0]); return;}
			if (radius > this.RADIUS_MAX) {sender.sendLangfileMessageToPlayer("command.confusesuicide.invalidRadius", new Object[0]); return;}
		}
		
		List<EntityCreature> entities = new ArrayList<EntityCreature>();
		EntityCreature creature;
		
		entities = this.getEntitiesInRadius(sender.toPlayer().getMinecraftPlayer(), sender.toPlayer().getWorld().getMinecraftWorld(), EntityCreature.class, radius * radius);
		
		Iterator<EntityCreature> entityIterator = entities.iterator();
		
		while (entityIterator.hasNext()) {
			creature = entityIterator.next();
			creature.setAttackTarget(creature);
			creature.setRevengeTarget(creature);
		}
        
		sender.sendLangfileMessageToPlayer("command.confusesuicide.confused", new Object[] {entities.size(), radius});
	}
	
	private <T extends EntityLivingBase> List<T> getEntitiesInRadius(final EntityPlayer player, World world, Class<T> class1, double d) {
		ArrayList<T> entities = new ArrayList<T>();
		
		for (int i = 0; i < world.loadedEntityList.size(); i++) {
			Entity entity = (Entity) world.loadedEntityList.get(i);
			if (entity != player && !entity.isDead && class1.isInstance(entity) && (d <= 0.0D || player.getDistanceSqToEntity(entity) <= d)) {
				entities.add((T) entity);
			}
		}

		Collections.sort(entities, new Comparator<Entity>() {

			public int compare(Entity entity1, Entity entity2) {
				double d1 = player.getDistanceSqToEntity(entity1) - player.getDistanceSqToEntity(entity2);
				return d1 >= 0.0D ? (d1 <= 0.0D ? 0 : 1) : -1;
			}
		});

		return entities;
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}
	
	@Override
	public void unregisterFromHandler() {}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	@Override
	public int getPermissionLevel() {
		return 2;
	}
}
