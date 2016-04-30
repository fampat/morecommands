package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.event.EventHandler;
import com.mrnobody.morecommands.event.Listeners.EventListener;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

@Command(
		name = "scuba",
		description = "command.scuba.description",
		example = "command.scuba.example",
		syntax = "command.scuba.syntax",
		videoURL = "command.scuba.videoURL"
		)
public class CommandScuba extends StandardCommand implements ServerCommandProperties, EventListener<TickEvent> {
	private static final int AIR_MAX = 300;
	
	public CommandScuba() {EventHandler.TICK.register(this);}
	
	@Override
	public void onEvent(TickEvent e) {
		if (e instanceof TickEvent.PlayerTickEvent) {
			TickEvent.PlayerTickEvent event = (TickEvent.PlayerTickEvent) e;
			if (!(event.player instanceof EntityPlayerMP)) return;
			if (event.player.isInWater() && getPlayerSettings((EntityPlayerMP) event.player).scuba) 
				event.player.setAir(AIR_MAX);
		}
	}

	@Override
	public String getCommandName() {
		return "scuba";
	}

	@Override
	public String getUsage() {
		return "command.scuba.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		ServerPlayerSettings settings = getPlayerSettings(getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class));
    	
		try {settings.scuba = parseTrueFalse(params, 0, settings.scuba);}
		catch (IllegalArgumentException ex) {throw new CommandException("command.scuba.failure", sender);}
		
		sender.sendLangfileMessage(settings.scuba ? "command.scuba.on" : "command.scuba.off");
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
		return isSenderOfEntityType(sender, EntityPlayerMP.class);
	}
}
