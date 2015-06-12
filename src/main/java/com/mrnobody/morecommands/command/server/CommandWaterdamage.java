package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
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
		name = "waterdamage",
		description = "command.waterdamage.description",
		example = "command.waterdamage.example",
		syntax = "command.waterdamage.syntax",
		videoURL = "command.waterdamage.videoURL"
		)
public class CommandWaterdamage extends ServerCommand implements Listener<LivingHurtEvent> {
	public CommandWaterdamage() {
		EventHandler.HURT.getHandler().register(this);
	}
	
	@Override
	public void onEvent(LivingHurtEvent event) {
		if (event.entity instanceof EntityPlayerMP && event.source == DamageSource.drown) {
			EntityPlayerMP player = (EntityPlayerMP) event.entity;
			
			if (ServerPlayerSettings.playerSettingsMapping.containsKey(player)) {
				if (!ServerPlayerSettings.playerSettingsMapping.get(player).waterdamage) event.setCanceled(true);
			}
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
		ServerPlayerSettings settings = ServerPlayerSettings.playerSettingsMapping.get(sender.getMinecraftISender());
    	
        if (params.length > 0) {
        	if (params[0].equalsIgnoreCase("enable") || params[0].equalsIgnoreCase("1")
            	|| params[0].equalsIgnoreCase("on") || params[0].equalsIgnoreCase("true")) {
        		settings.waterdamage = true;
            	sender.sendLangfileMessage("command.waterdamage.on");
            }
            else if (params[0].equalsIgnoreCase("disable") || params[0].equalsIgnoreCase("0")
            		|| params[0].equalsIgnoreCase("off") || params[0].equalsIgnoreCase("false")) {
            	settings.waterdamage = false;
            	sender.sendLangfileMessage("command.waterdamage.off");
            }
            else throw new CommandException("command.waterdamage.failure", sender);
        }
        else {
        	settings.waterdamage = !settings.waterdamage;
        	sender.sendLangfileMessage(settings.waterdamage ? "command.waterdamage.on" : "command.waterdamage.off");
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
