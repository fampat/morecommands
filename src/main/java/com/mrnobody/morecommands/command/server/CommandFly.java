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
import com.mrnobody.morecommands.wrapper.Player;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.living.LivingFallEvent;

@Command(
		name = "fly",
		description = "command.fly.description",
		example = "command.fly.example",
		syntax = "command.fly.syntax",
		videoURL = "command.fly.videoURL"
		)
public class CommandFly extends StandardCommand implements ServerCommandProperties, EventListener<LivingFallEvent>
{
	public CommandFly() {EventHandler.FALL.register(this);}
	
	@Override
	public void onEvent(LivingFallEvent event) {
		if (event.entity instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP) event.entity;
			ServerPlayerSettings settings = getPlayerSettings(player);
				
		    if (settings.noFall) {event.setCanceled(true);}
		    else if (settings.justDisabled) {
		    	event.setCanceled(true);
		        settings.justDisabled = false;
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
		Player player = new Player(getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class));
		ServerPlayerSettings settings = getPlayerSettings(player.getMinecraftPlayer());
		boolean fly;
		
		try {fly = parseTrueFalse(params, 0, player.getAllowFlying());}
		catch (IllegalArgumentException ex) {throw new CommandException("command.fly.failure", sender);}
        
    	settings.fly = fly;
    	if (fly) settings.noFall = true;
    	else {settings.noFall = false; if (!player.getMinecraftPlayer().onGround) settings.justDisabled = true;}
    	player.setAllowFlying(fly);
    	
    	sender.sendLangfileMessage(player.getAllowFlying() ? "command.fly.on" : "command.fly.off");
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
