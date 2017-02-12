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
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

@Command(
		name = "firedamage",
		description = "command.firedamage.description",
		example = "command.firedamage.example",
		syntax = "command.firedamage.syntax",
		videoURL = "command.firedamage.videoURL"
		)
public class CommandFiredamage extends StandardCommand implements ServerCommandProperties, EventListener<LivingAttackEvent> {
	public CommandFiredamage() {
		EventHandler.ATTACK.register(this);
	}
	
	@Override
	public void onEvent(LivingAttackEvent event) {
		if (event.entity instanceof EntityPlayerMP && (event.source == DamageSource.inFire || event.source == DamageSource.onFire || event.source == DamageSource.lava)) {
			EntityPlayerMP player = (EntityPlayerMP) event.entity;
			if (!getPlayerSettings(player).firedamage) event.setCanceled(true);
		}
	}
	
	@Override
	public String getCommandName() {
		return "firedamage";
	}

	@Override
	public String getCommandUsage() {
		return "command.firedamage.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		ServerPlayerSettings settings = getPlayerSettings(getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class));
    	
		try {settings.firedamage = parseTrueFalse(params, 0, !settings.firedamage);}
		catch (IllegalArgumentException ex) {throw new CommandException("command.firedamage.failure", sender);}
		
		sender.sendLangfileMessage(settings.firedamage  ? "command.firedamage.on" : "command.firedamage.off");
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
