package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listeners.Listener;
import com.mrnobody.morecommands.handler.Listeners.TwoEventListener;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;

@Command(
		name = "noattack",
		description = "command.noattack.description",
		example = "command.noattack.example",
		syntax = "command.noattack.syntax",
		videoURL = "command.noattack.videoURL"
		)
public class CommandNoattack extends ServerCommand implements TwoEventListener<LivingSetAttackTargetEvent, LivingAttackEvent> {
	public CommandNoattack() {
		EventHandler.SET_TARGET.getHandler().register(this, true);
		EventHandler.ATTACK.getHandler().register(this, false);
	}

	@Override
	public String getName() {
		return "noattack";
	}

	@Override
	public String getUsage() {
		return "command.noattack.syntax";
	}
	
	@Override
	public void onEvent1(LivingSetAttackTargetEvent event) {
		if (event.target instanceof EntityPlayerMP && event.entityLiving instanceof EntityLiving) {
			if (ServerPlayerSettings.getPlayerSettings((EntityPlayerMP) event.target).disableAttacks.contains(event.entityLiving.getClass())) {
				((EntityLiving) event.entityLiving).setAttackTarget(null);
				event.entityLiving.setRevengeTarget(null);
			}
		}
	}
	
	@Override
	public void onEvent2(LivingAttackEvent event) {
		if (event.entityLiving instanceof EntityPlayerMP && event.source.getEntity() instanceof EntityLiving) {
			if (ServerPlayerSettings.getPlayerSettings((EntityPlayerMP) event.entityLiving).disableAttacks.contains(event.source.getEntity().getClass())) {
				((EntityLiving) event.source.getEntity()).setAttackTarget(null);
				((EntityLiving) event.source.getEntity()).setRevengeTarget(null);
				event.setCanceled(true);
			}
		}
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		ServerPlayerSettings settings = ServerPlayerSettings.getPlayerSettings((EntityPlayerMP) sender.getMinecraftISender());
    	
		if (params.length > 0) {
			Class<? extends Entity> entityClass = (Class<? extends Entity>) com.mrnobody.morecommands.wrapper.Entity.getEntityClass(params[0]);
				
			if (entityClass == null) {
				try {entityClass = (Class<? extends Entity>) EntityList.idToClassMapping.get(Integer.parseInt(params[0]));}
				catch (NumberFormatException nfe) {throw new CommandException("command.noattack.unknownEntity", sender);}
			}
			
			if (!EntityLiving.class.isAssignableFrom(entityClass))
				throw new CommandException("command.noattack.notLiving", sender);
			
			if (settings.disableAttacks.contains(entityClass)) {
				settings.disableAttacks.remove(entityClass);
				sender.sendLangfileMessage("command.noattack.removed");
			}
			else {
				settings.disableAttacks.add((Class<? extends EntityLiving>) entityClass);
				sender.sendLangfileMessage("command.noattack.added");
			}
		}
		else throw new CommandException("command.noattack.invalidUsage", sender);
	}

	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}
	
	@Override
	public void unregisterFromHandler() {
		EventHandler.SET_TARGET.getHandler().unregister(this);
		EventHandler.ATTACK.getHandler().unregister(this);
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
