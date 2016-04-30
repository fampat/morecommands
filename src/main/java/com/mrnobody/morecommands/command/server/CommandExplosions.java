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
import net.minecraftforge.event.world.ExplosionEvent;

@Command(
		name = "explosions",
		description = "command.explosions.description",
		example = "command.explosions.example",
		syntax = "command.explosions.syntax",
		videoURL = "command.explosions.videoURL"
		)
public class CommandExplosions extends StandardCommand implements ServerCommandProperties, EventListener<ExplosionEvent> {
	public CommandExplosions() {
		EventHandler.EXPLOSION.register(this);
	}

	@Override
	public void onEvent(ExplosionEvent event) {
		if (!GlobalSettings.explosions) event.setCanceled(true);
	}
	
	@Override
	public String getName() {
		return "explosions";
	}

	@Override
	public String getUsage() {
		return "command.explosions.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params)throws CommandException {
		try {GlobalSettings.explosions = parseTrueFalse(params, 0, GlobalSettings.explosions);}
		catch (IllegalArgumentException ex) {throw new CommandException("command.explosions.failure", sender);}
		
		sender.sendLangfileMessage(GlobalSettings.explosions ? "command.explosions.on" : "command.explosions.off");
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
