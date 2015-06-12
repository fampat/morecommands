package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraftforge.event.world.ExplosionEvent;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listeners.Listener;
import com.mrnobody.morecommands.util.GlobalSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "explosions",
		description = "command.explosions.description",
		example = "command.explosions.example",
		syntax = "command.explosions.syntax",
		videoURL = "command.explosions.videoURL"
		)
public class CommandExplosions extends ServerCommand implements Listener<ExplosionEvent> {
	public CommandExplosions() {
		EventHandler.EXPLOSION.getHandler().register(this);
	}

	@Override
	public void onEvent(ExplosionEvent event) {
		if (!GlobalSettings.explosions) event.setCanceled(true);
	}
	
	@Override
	public String getName() {
		return "explosions";
	}

	@Override
	public String getUsage() {
		return "command.explosions.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params)throws CommandException {
        if (params.length > 0) {
        	if (params[0].equalsIgnoreCase("enable") || params[0].equalsIgnoreCase("1")
            	|| params[0].equalsIgnoreCase("on") || params[0].equalsIgnoreCase("true")) {
        		GlobalSettings.explosions = true;
            	sender.sendLangfileMessage("command.explosions.on");
            }
            else if (params[0].equalsIgnoreCase("disable") || params[0].equalsIgnoreCase("0")
            		|| params[0].equalsIgnoreCase("off") || params[0].equalsIgnoreCase("false")) {
            	GlobalSettings.explosions = false;
            	sender.sendLangfileMessage("command.explosions.off");
            }
            else throw new CommandException("command.explosions.failure", sender);
        }
        else {
        	GlobalSettings.explosions = !GlobalSettings.explosions;
        	sender.sendLangfileMessage(GlobalSettings.explosions ? "command.explosions.on" : "command.explosions.off");
        }
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}
	
	@Override
	public void unregisterFromHandler() {
		EventHandler.EXPLOSION.getHandler().unregister(this);
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
		return true;
	}
}
