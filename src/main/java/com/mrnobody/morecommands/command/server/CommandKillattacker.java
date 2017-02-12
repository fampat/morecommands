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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

@Command(
		name = "killattacker",
		description = "command.killattacker.description",
		example = "command.killattacker.example",
		syntax = "command.killattacker.syntax",
		videoURL = "command.killattacker.videoURL"
		)
public class CommandKillattacker extends StandardCommand implements ServerCommandProperties, EventListener<LivingAttackEvent> {
	public CommandKillattacker() {
		EventHandler.ATTACK.register(this);
	}

	@Override
	public void onEvent(LivingAttackEvent event) {
		if (!(event.getEntity() instanceof EntityPlayerMP)) return;
		
		if (getPlayerSettings((EntityPlayerMP) event.getEntity()).killattacker) {
			if (event.getSource().getSourceOfDamage() != null && (event.getSource().getSourceOfDamage() instanceof EntityCreature || event.getSource().getSourceOfDamage() instanceof EntityArrow)) {
				if (event.getSource().getSourceOfDamage() instanceof EntityCreature) 
					event.getSource().getSourceOfDamage().attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) event.getEntity()), Float.MAX_VALUE);
				if (event.getSource().getSourceOfDamage() instanceof EntityArrow && ((EntityArrow) event.getSource().getSourceOfDamage()).shootingEntity instanceof EntityCreature)
					((EntityArrow) event.getSource().getSourceOfDamage()).shootingEntity.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) event.getEntity()), Float.MAX_VALUE);
			}
		}
	}
	
	@Override
	public String getCommandName() {
		return "killattacker";
	}

	@Override
	public String getCommandUsage() {
		return "command.killattacker.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params)throws CommandException {
		ServerPlayerSettings settings = getPlayerSettings(getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class));
    	
		try {settings.killattacker = parseTrueFalse(params, 0, !settings.killattacker);}
		catch (IllegalArgumentException ex) {throw new CommandException("command.killattacker.failure", sender);}
		
		sender.sendLangfileMessage(settings.killattacker ? "command.killattacker.on" : "command.killattacker.off");
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
