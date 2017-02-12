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
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

@Command(
		name = "mobdamage",
		description = "command.mobdamage.description",
		example = "command.mobdamage.example",
		syntax = "command.mobdamage.syntax",
		videoURL = "command.mobdamage.videoURL"
		)
public class CommandMobdamage extends StandardCommand implements ServerCommandProperties, EventListener<LivingAttackEvent> {
	public CommandMobdamage() {
		EventHandler.ATTACK.register(this);
	}

	@Override
	public void onEvent(LivingAttackEvent event) {
		if (!(event.getEntity() instanceof EntityPlayerMP)) return;
		
		if (!getPlayerSettings((EntityPlayerMP) event.getEntity()).mobdamage) {
			if (event.getSource().getSourceOfDamage() != null && (event.getSource().getSourceOfDamage() instanceof EntityCreature || event.getSource().getSourceOfDamage() instanceof EntityArrow)) {
				if (event.getSource().getSourceOfDamage() instanceof EntityCreature) event.setCanceled(true);
				if (event.getSource().getSourceOfDamage() instanceof EntityArrow && ((EntityArrow) event.getSource().getSourceOfDamage()).shootingEntity instanceof EntityCreature) event.setCanceled(true);
			}
		}
	}
	
	@Override
	public String getCommandName() {
		return "mobdamage";
	}

	@Override
	public String getCommandUsage() {
		return "command.mobdamage.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params)throws CommandException {
		ServerPlayerSettings settings = getPlayerSettings(getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class));
    	
		try {settings.mobdamage = parseTrueFalse(params, 0, !settings.mobdamage);}
		catch (IllegalArgumentException ex) {throw new CommandException("command.mobdamage.failure", sender);}
		
		sender.sendLangfileMessage(settings.mobdamage ? "command.mobdamage.on" : "command.mobdamage.off");
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
