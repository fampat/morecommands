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

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

@Command(
		name = "waterdamage",
		description = "command.waterdamage.description",
		example = "command.waterdamage.example",
		syntax = "command.waterdamage.syntax",
		videoURL = "command.waterdamage.videoURL"
		)
public class CommandWaterdamage extends StandardCommand implements ServerCommandProperties, EventListener<LivingAttackEvent> {
	public CommandWaterdamage() {
		EventHandler.ATTACK.register(this);
	}
	
	@Override
	public void onEvent(LivingAttackEvent event) {
		if (event.entity instanceof EntityPlayerMP && event.source == DamageSource.drown) {
			EntityPlayerMP player = (EntityPlayerMP) event.entity;
			if (!getPlayerSettings((EntityPlayerMP) player).waterdamage) event.setCanceled(true);
		}
	}
	
	@Override
	public String getName() {
		return "waterdamage";
	}

	@Override
	public String getUsage() {
		return "command.waterdamage.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		ServerPlayerSettings settings = getPlayerSettings(getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class));
    	
		try {settings.waterdamage = parseTrueFalse(params, 0, settings.waterdamage);}
		catch (IllegalArgumentException ex) {throw new CommandException("command.waterdamage.failure", sender);}
		
		sender.sendLangfileMessage(settings.waterdamage ? "command.waterdamage.on" : "command.waterdamage.off");
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
