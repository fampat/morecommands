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
import com.mrnobody.morecommands.settings.GlobalSettings;

import net.minecraft.command.ICommandSender;
import net.minecraftforge.event.world.ExplosionEvent;

@Command(
		name = "explosions",
		description = "command.explosions.description",
		example = "command.explosions.example",
		syntax = "command.explosions.syntax",
		videoURL = "command.explosions.videoURL"
		)
public class CommandExplosions extends StandardCommand implements ServerCommandProperties, EventListener<ExplosionEvent> {
	private boolean explosions = true;
	
	public CommandExplosions() {
		EventHandler.EXPLOSION.register(this);
	}

	@Override
	public void onEvent(ExplosionEvent event) {
		if (!this.explosions) event.setCanceled(true);
	}
	
	@Override
	public String getCommandName() {
		return "explosions";
	}

	@Override
	public String getCommandUsage() {
		return "command.explosions.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params)throws CommandException {
		try {this.explosions = parseTrueFalse(params, 0, !this.explosions);}
		catch (IllegalArgumentException ex) {throw new CommandException("command.explosions.failure", sender);}
		
		sender.sendLangfileMessage(this.explosions ? "command.explosions.on" : "command.explosions.off");
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
