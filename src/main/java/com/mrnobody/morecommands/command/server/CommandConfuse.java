package com.mrnobody.morecommands.command.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "confuse",
		description = "command.confuse.description",
		example = "command.confuse.example",
		syntax = "command.confuse.syntax",
		videoURL = "command.confuse.videoURL"
		)
public class CommandConfuse extends ServerCommand {
	private final double RADIUS_MAX = 50;
	
	@Override
	public String getName() {
		return "confuse";
	}

	@Override
	public String getUsage() {
		return "command.confuse.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		double radius = 10D;
		
		if (params.length > 0) {
			try {radius = Double.parseDouble(params[0]);}
			catch (NumberFormatException nfe) {throw new CommandException("command.confuse.invalidArg", sender);}
			if (radius > this.RADIUS_MAX) throw new CommandException("command.confuse.invalidRadius", sender);
		}
		
		List<EntityCreature> entities = new ArrayList<EntityCreature>();
		EntityCreature creature;
		
		entities = this.getEntitiesInRadius((EntityPlayerMP) sender.getMinecraftISender(), ((EntityPlayerMP) sender.getMinecraftISender()).worldObj, EntityCreature.class, radius * radius);
		
		for (int index = 1; index < entities.size(); index++) {
			((EntityCreature) entities.get(index)).setAttackTarget(entities.get(index - 1));
			((EntityCreature) entities.get(index)).setRevengeTarget(entities.get(index - 1));
		}
        
		sender.sendLangfileMessage("command.confuse.confused", entities.size(), radius);
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
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	@Override
	public int getPermissionLevel() {
		return 2;
	}
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return sender instanceof EntityPlayerMP;
	}
}
