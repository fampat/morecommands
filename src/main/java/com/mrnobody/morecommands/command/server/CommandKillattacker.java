package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listeners.Listener;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "killattacker",
		description = "command.killattacker.description",
		example = "command.killattacker.example",
		syntax = "command.killattacker.syntax",
		videoURL = "command.killattacker.videoURL"
		)
public class CommandKillattacker extends ServerCommand implements Listener<LivingHurtEvent> {
	public CommandKillattacker() {
		EventHandler.HURT.getHandler().register(this);
	}

	@Override
	public void onEvent(LivingHurtEvent event) {
		if (!(event.entity instanceof EntityPlayerMP) || !ServerPlayerSettings.playerSettingsMapping.containsKey(event.entity)) return;
		
		if (ServerPlayerSettings.playerSettingsMapping.get(event.entity).killattacker) {
			if (event.source.getSourceOfDamage() != null && (event.source.getSourceOfDamage() instanceof EntityCreature || event.source.getSourceOfDamage() instanceof EntityArrow)) {
				if (event.source.getSourceOfDamage() instanceof EntityCreature) 
					event.source.getSourceOfDamage().attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) event.entity), Float.MAX_VALUE);
				if (event.source.getSourceOfDamage() instanceof EntityArrow && ((EntityArrow) event.source.getSourceOfDamage()).shootingEntity instanceof EntityCreature)
					((EntityArrow) event.source.getSourceOfDamage()).shootingEntity.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) event.entity), Float.MAX_VALUE);
			}
		}
	}
	
	@Override
	public String getCommandName() {
		return "killattacker";
	}

	@Override
	public String getUsage() {
		return "command.killattacker.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params)throws CommandException {
		ServerPlayerSettings settings = ServerPlayerSettings.playerSettingsMapping.get(sender.getMinecraftISender());
    	
        if (params.length > 0) {
        	if (params[0].equalsIgnoreCase("enable") || params[0].equalsIgnoreCase("1")
            	|| params[0].equalsIgnoreCase("on") || params[0].equalsIgnoreCase("true")) {
        		settings.killattacker = true;
            	sender.sendLangfileMessage("command.killattacker.on");
            }
            else if (params[0].equalsIgnoreCase("disable") || params[0].equalsIgnoreCase("0")
            		|| params[0].equalsIgnoreCase("off") || params[0].equalsIgnoreCase("false")) {
            	settings.killattacker = false;
            	sender.sendLangfileMessage("command.killattacker.off");
            }
            else throw new CommandException("command.killattacker.failure", sender);
        }
        else {
        	settings.killattacker = !settings.killattacker;
        	sender.sendLangfileMessage(settings.killattacker ? "command.killattacker.on" : "command.killattacker.off");
        }
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}
	
	@Override
	public void unregisterFromHandler() {
		EventHandler.HURT.getHandler().unregister(this);
	}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	@Override
	public int getPermissionLevel() {
		return 2;
	}
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return sender instanceof EntityPlayerMP;
	}
}
