package com.mrnobody.morecommands.command.server;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listeners.Listener;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "ignorespawn",
		description = "command.ignorespawn.description",
		example = "command.ignorespawn.example",
		syntax = "command.ignorespawn.syntax",
		videoURL = "command.ignorespawn.videoURL"
		)
public class CommandIgnorespawn extends ServerCommand implements Listener<EntityJoinWorldEvent> {
	private List<Class<? extends Entity>> ignoreSpawn = new ArrayList<Class<? extends Entity>>();
	
	public CommandIgnorespawn() {
		EventHandler.ENTITYJOIN.getHandler().register(this);
	}

	@Override
	public void onEvent(EntityJoinWorldEvent event) {
		if (this.ignoreSpawn.contains(event.entity.getClass()))
			event.setCanceled(true);
	}

	@Override
	public void unregisterFromHandler() {
		EventHandler.ENTITYJOIN.getHandler().unregister(this);
	}

	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return true;
	}

	@Override
	public String getName() {
		return "ignorespawn";
	}

	@Override
	public String getUsage() {
		return "command.ignorespawn.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		if (params.length > 0) {
			Class<? extends Entity> entityClass = (Class<? extends Entity>) com.mrnobody.morecommands.wrapper.Entity.getEntityClass(params[0]);
			
			if (entityClass == null) {
				try {entityClass = (Class<? extends Entity>) EntityList.idToClassMapping.get(Integer.parseInt(params[0]));}
				catch (NumberFormatException nfe) {throw new CommandException("command.ignorespawn.unknownEntity", sender);}
			}
			
			if (this.ignoreSpawn.contains(entityClass)) {
				this.ignoreSpawn.remove(entityClass);
				sender.sendLangfileMessage("command.ignorespawn.removed");
			}
			else {
				this.ignoreSpawn.add(entityClass);
				sender.sendLangfileMessage("command.ignorespawn.added");
			}
		}
		else throw new CommandException("command.ignorespawn.invalidUsage", sender);
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
}
