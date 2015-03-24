package com.mrnobody.morecommands.command.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.client.Minecraft;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.command.CommandBase.Requirement;
import com.mrnobody.morecommands.command.CommandBase.ServerType;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import cpw.mods.fml.relauncher.Side;

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
	public String getCommandName() {
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
		
		List<Entity> entities = new ArrayList<Entity>();
		EntityCreature creature;
		
		entities = this.getEntitiesInRadius(sender.toPlayer().getMinecraftPlayer(), sender.toPlayer().getWorld().getMinecraftWorld(), EntityCreature.class, radius * radius);
		
		Iterator<Entity> entityIterator = entities.iterator();
		
		while (entityIterator.hasNext()) {
			creature = (EntityCreature) entityIterator.next();
			creature.setTarget(creature);
		}
        
		sender.sendLangfileMessageToPlayer("command.confusesuicide.confused", new Object[] {entities.size(), radius});
	}
	
	private List<Entity> getEntitiesInRadius(final EntityPlayer player, World world, Class<?> class1, double d) {
		ArrayList<Entity> entities = new ArrayList<Entity>();
		
		for (int i = 0; i < world.loadedEntityList.size(); i++) {
			Entity entity = (Entity) world.loadedEntityList.get(i);
			if (entity != player && !entity.isDead && class1.isInstance(entity) && (d <= 0.0D || player.getDistanceSqToEntity(entity) <= d)) {
				entities.add(entity);
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
