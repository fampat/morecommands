package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.event.EventHandler;
import com.mrnobody.morecommands.event.Listeners.EventListener;

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
	private boolean dodrops = true;
	
	public CommandDodrops() {
		EventHandler.ENTITYJOIN.register(this);
	}
	
	@Override
	public void onEvent(EntityJoinWorldEvent event) {
		if (event.getEntity() instanceof EntityItem && !this.dodrops) event.setCanceled(true);
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
	public String execute(CommandSender sender, String[] params) throws CommandException {
		this.dodrops = !this.dodrops;
		sender.sendLangfileMessage(this.dodrops ? "command.dodrops.enabled" : "command.dodrops.disabled");
		
		return null;
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
