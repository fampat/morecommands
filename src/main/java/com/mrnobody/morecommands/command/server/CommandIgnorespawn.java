package com.mrnobody.morecommands.command.server;

import java.util.ArrayList;
import java.util.List;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.event.EventHandler;
import com.mrnobody.morecommands.event.Listeners.EventListener;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityList;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

@Command(
		name="ignorespawn",
		description="command.ignorespawn.description",
		example="command.ignorespawn.example",
		syntax="command.ignorespawn.syntax",
		videoURL="command.ignorespawn.videoURL"
		)
public class CommandIgnorespawn extends StandardCommand implements ServerCommandProperties, EventListener<EntityJoinWorldEvent> {
	private List<Class<? extends net.minecraft.entity.Entity>> ignoreSpawn = new ArrayList<Class<? extends net.minecraft.entity.Entity>>();
  
	public CommandIgnorespawn() {
		EventHandler.ENTITYJOIN.register(this);
	}
  
	@Override
	public void onEvent(EntityJoinWorldEvent event) {
		if (this.ignoreSpawn.contains(event.entity.getClass()))
			event.setCanceled(true);
	}
  
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return true;
	}
  
	@Override
	public String getCommandName() {
		return "ignorespawn";
	}
  
	@Override
	public String getUsage() {
		return "command.ignorespawn.syntax";
	}
  
	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		if (params.length > 0) {
			Class<? extends net.minecraft.entity.Entity> entityClass = (Class<? extends net.minecraft.entity.Entity>) com.mrnobody.morecommands.wrapper.Entity.getEntityClass(params[0]);
      
			if (entityClass == null) {
				try {entityClass = (Class<? extends net.minecraft.entity.Entity>) EntityList.IDtoClassMapping.get(Integer.valueOf(Integer.parseInt(params[0])));}
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
}
