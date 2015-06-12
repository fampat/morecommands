package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.living.LivingFallEvent;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listeners.Listener;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "falldamage",
		description = "command.falldamage.description",
		example = "command.falldamage.example",
		syntax = "command.falldamage.syntax",
		videoURL = "command.falldamage.videoURL"
		)
public class CommandFalldamage extends ServerCommand implements Listener<LivingFallEvent> {
	public CommandFalldamage() {
		EventHandler.FALL.getHandler().register(this);
	}
	
	@Override
	public void onEvent(LivingFallEvent event) {
		if (event.entity instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP) event.entity;
			
			if (ServerPlayerSettings.playerSettingsMapping.containsKey(player)) {
				if (!ServerPlayerSettings.playerSettingsMapping.get(player).falldamage) event.setCanceled(true);
			}
		}
	}
	
	@Override
	public String getName() {
		return "falldamage";
	}

	@Override
	public String getUsage() {
		return "command.falldamage.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		ServerPlayerSettings settings = ServerPlayerSettings.playerSettingsMapping.get(sender.getMinecraftISender());
    	
        if (params.length > 0) {
        	if (params[0].equalsIgnoreCase("enable") || params[0].equalsIgnoreCase("1")
            	|| params[0].equalsIgnoreCase("on") || params[0].equalsIgnoreCase("true")) {
        		settings.falldamage = true;
            	sender.sendLangfileMessage("command.falldamage.on");
            }
            else if (params[0].equalsIgnoreCase("disable") || params[0].equalsIgnoreCase("0")
            		|| params[0].equalsIgnoreCase("off") || params[0].equalsIgnoreCase("false")) {
            	settings.falldamage = false;
            	sender.sendLangfileMessage("command.falldamage.off");
            }
            else throw new CommandException("command.falldamage.failure", sender);
        }
        else {
        	settings.falldamage = !settings.falldamage;
        	sender.sendLangfileMessage(settings.falldamage ? "command.falldamage.on" : "command.falldamage.off");
        }
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}
	
	@Override
	public void unregisterFromHandler() {
		EventHandler.FALL.getHandler().unregister(this);
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
