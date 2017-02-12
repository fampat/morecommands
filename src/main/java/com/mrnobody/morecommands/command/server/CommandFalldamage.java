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
import com.mrnobody.morecommands.settings.ServerPlayerSettings;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.living.LivingFallEvent;

@Command(
		name = "falldamage",
		description = "command.falldamage.description",
		example = "command.falldamage.example",
		syntax = "command.falldamage.syntax",
		videoURL = "command.falldamage.videoURL"
		)
public class CommandFalldamage extends StandardCommand implements ServerCommandProperties, EventListener<LivingFallEvent> {
	public CommandFalldamage() {
		EventHandler.FALL.register(this);
	}
	
	@Override
	public void onEvent(LivingFallEvent event) {
		if (event.getEntity() instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP) event.getEntity();
			if (!getPlayerSettings(player).falldamage) event.setCanceled(true);
		}
	}
	
	@Override
	public String getCommandName() {
		return "falldamage";
	}

	@Override
	public String getCommandUsage() {
		return "command.falldamage.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		ServerPlayerSettings settings = getPlayerSettings(getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class));
    	
		try {settings.falldamage = parseTrueFalse(params, 0, !settings.falldamage);}
		catch (IllegalArgumentException ex) {throw new CommandException("command.falldamage.failure", sender);}
		
		sender.sendLangfileMessage(settings.falldamage  ? "command.falldamage.on" : "command.falldamage.off");
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
		return isSenderOfEntityType(sender, EntityPlayerMP.class);
	}
}
