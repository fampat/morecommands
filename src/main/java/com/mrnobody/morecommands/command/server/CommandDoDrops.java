package com.mrnobody.morecommands.command.server;

import net.minecraft.entity.item.EntityItem;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listener;
import com.mrnobody.morecommands.util.GlobalSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "dodrops",
		description = "command.dodrops.description",
		example = "command.dodrops.example",
		syntax = "command.dodrops.syntax",
		videoURL = "command.dodrops.videoURL"
		)
public class CommandDoDrops extends ServerCommand implements Listener<EntityJoinWorldEvent> {
	public CommandDoDrops() {
		EventHandler.ENTITYJOIN.getHandler().register(this);
	}
	
	@Override
	public void onEvent(EntityJoinWorldEvent event) {
		if (event.entity instanceof EntityItem && !GlobalSettings.dodrops) event.setCanceled(true);
	}

	@Override
	public String getCommandName() {
		return "dodrops";
	}

	@Override
	public String getUsage() {
		return "command.dodrops.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		GlobalSettings.dodrops = !GlobalSettings.dodrops;
		sender.sendLangfileMessageToPlayer(GlobalSettings.dodrops ? "command.dodrops.enabled" : "command.dodrops.disabled", new Object[0]);
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}
	
	@Override
	public void unregisterFromHandler() {
		EventHandler.ENTITYJOIN.getHandler().unregister(this);
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
