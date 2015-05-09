package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandBase.Requirement;
import com.mrnobody.morecommands.command.CommandBase.ServerType;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Handler;
import com.mrnobody.morecommands.handler.Listeners.Listener;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
public class CommandIgnorespawn extends ServerCommand implements Listener<EntityJoinWorldEvent> {
	private List<Class<? extends net.minecraft.entity.Entity>> ignoreSpawn = new ArrayList<Class<? extends net.minecraft.entity.Entity>>();
  
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
				catch (NumberFormatException nfe) {sender.sendLangfileMessage("command.ignorespawn.unknownEntity", new Object[0]); return;}
			}
			
			if (this.ignoreSpawn.contains(entityClass)) {
				this.ignoreSpawn.remove(entityClass);
				sender.sendLangfileMessage("command.ignorespawn.removed", new Object[0]);
			}
			else {
				this.ignoreSpawn.add(entityClass);
				sender.sendLangfileMessage("command.ignorespawn.added", new Object[0]);
			}
		}
		else sender.sendLangfileMessage("command.ignorespawn.invalidUsage", new Object[0]);
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
