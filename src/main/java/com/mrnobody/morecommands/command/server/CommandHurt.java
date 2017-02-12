package com.mrnobody.morecommands.command.server;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;

@Command(
		name = "hurt",
		description = "command.hurt.description",
		example = "command.hurt.example",
		syntax = "command.hurt.syntax",
		videoURL = "command.hurt.videoURL"
		)
public class CommandHurt extends StandardCommand implements ServerCommandProperties {
	@Override
	public String getCommandName() {
		return "hurt";
	}

	@Override
	public String getCommandUsage() {
		return "command.hurt.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		if (params.length < 2) throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
		
		EntityLivingBase entity = getSenderAsEntity(sender.getMinecraftISender(), EntityLivingBase.class);
		float strength;
		
		try {strength = Float.parseFloat(params[1]);}
		catch (NumberFormatException nfe) {throw new CommandException("command.generic.NaN", sender);}
		
		DamageSource dmgSrc = parseDamageSource(params[0]);
		if (dmgSrc == null) throw new CommandException("command.hurt.unknownSrc", sender, params[0]);
		
		entity.attackEntityFrom(dmgSrc, strength);
		return null;
	}
	
	private DamageSource parseDamageSource(String src) {
		try {
			for (Field f : DamageSource.class.getFields()) {
				if (Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers())) {
					Object o = f.get(null);
					
					if (o instanceof DamageSource && ((DamageSource) o).getDamageType().equals(src))
						return (DamageSource) o;
				}
			}
		}
		catch (Exception ex) {}
		
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
		return 0;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return isSenderOfEntityType(sender, EntityLivingBase.class);
	}
}
