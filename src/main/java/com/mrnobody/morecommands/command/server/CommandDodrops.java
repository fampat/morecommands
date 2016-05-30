package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.event.EventHandler;
import com.mrnobody.morecommands.event.Listeners.EventListener;
import com.mrnobody.morecommands.util.GlobalSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.item.EntityItem;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

@Command(
		name = "dodrops",
		description = "command.dodrops.description",
		example = "command.dodrops.example",
		syntax = "command.dodrops.syntax",
		videoURL = "command.dodrops.videoURL"
		)
public class CommandDodrops extends StandardCommand implements ServerCommandProperties, EventListener<EntityJoinWorldEvent> {
	public CommandDodrops() {
		EventHandler.ENTITYJOIN.register(this);
	}
	
	@Override
	public void onEvent(EntityJoinWorldEvent event) {
		if (event.getEntity() instanceof EntityItem && !GlobalSettings.dodrops) event.setCanceled(true);
	}

	@Override
	public String getCommandName() {
		return "dodrops";
	}

	@Override
	public String getCommandUsage() {
		return "command.dodrops.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		GlobalSettings.dodrops = !GlobalSettings.dodrops;
		sender.sendLangfileMessage(GlobalSettings.dodrops ? "command.dodrops.enabled" : "command.dodrops.disabled");
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
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return true;
	}
}
