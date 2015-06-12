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
import com.mrnobody.morecommands.wrapper.Player;

@Command(
		name = "fly",
		description = "command.fly.description",
		example = "command.fly.example",
		syntax = "command.fly.syntax",
		videoURL = "command.fly.videoURL"
		)
public class CommandFly extends ServerCommand implements Listener<LivingFallEvent> {
	public CommandFly() {EventHandler.FALL.getHandler().register(this);}
	
	@Override
	public void onEvent(LivingFallEvent event) {
		if (event.entity instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP) event.entity;
			
			if (ServerPlayerSettings.playerSettingsMapping.containsKey(event.entity)) {
				ServerPlayerSettings settings = ServerPlayerSettings.playerSettingsMapping.get(event.entity);
				
		        if (settings.noFall) {event.setCanceled(true);}
		        else if (settings.justDisabled) {
		        	event.setCanceled(true);
		        	settings.justDisabled = false;
		        }
			}
		}
	}

	@Override
    public String getName()
    {
        return "fly";
    }

	@Override
    public String getUsage()
    {
        return "command.fly.usage";
    }
    
	@Override
    public void execute(CommandSender sender, String[] params) throws CommandException {
		Player player = new Player((EntityPlayerMP) sender.getMinecraftISender());
		ServerPlayerSettings settings = ServerPlayerSettings.playerSettingsMapping.get(sender.getMinecraftISender());
		boolean fly;
		
        if (params.length > 0) {
        	if (params[0].equalsIgnoreCase("enable") || params[0].equalsIgnoreCase("1")
            	|| params[0].equalsIgnoreCase("on") || params[0].equalsIgnoreCase("true")) {
        		fly = true;
            }
            else if (params[0].equalsIgnoreCase("disable") || params[0].equalsIgnoreCase("0")
            		|| params[0].equalsIgnoreCase("off") || params[0].equalsIgnoreCase("false")) {
            	fly = false;
            }
            else throw new CommandException("command.freeze.failure", sender);
        }
        else fly = !player.getAllowFlying();
        
    	settings.fly = fly;
    	if (fly) {settings.noFall = true;}
    	else {settings.noFall = false; if (!player.getMinecraftPlayer().onGround) settings.justDisabled = true;}
    	player.setAllowFlying(fly);
    	
    	sender.sendLangfileMessage(player.getAllowFlying() ? "command.fly.on" : "command.fly.off");
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
