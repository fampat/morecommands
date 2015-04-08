package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.living.LivingFallEvent;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listener;
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
		ServerPlayerSettings ability = ServerPlayerSettings.playerSettingsMapping.get(sender.getMinecraftISender());
    		
        boolean allowFly = false;
        boolean success = false;
        	
        if (params.length >= 1) {
        	if (params[0].equalsIgnoreCase("true")) {allowFly = true; success = true;}
        	else if (params[0].equalsIgnoreCase("false")) {allowFly = false; success = true;}
        	else if (params[0].equalsIgnoreCase("0")) {allowFly = false; success = true;}
        	else if (params[0].equalsIgnoreCase("1")) {allowFly = true; success = true;}
        	else if (params[0].equalsIgnoreCase("on")) {allowFly = true; success = true;}
        	else if (params[0].equalsIgnoreCase("off")) {allowFly = false; success = true;}
    		else if (params[0].equalsIgnoreCase("enable")) {allowFly = true; success = true;}
    		else if (params[0].equalsIgnoreCase("disable")) {allowFly = false; success = true;}
        	else {success = false;}
        }
        else {allowFly = !player.getAllowFlying(); success = true;}
        	
        if (success) {
        	ability.fly = allowFly;
        	if (allowFly) {ability.noFall = true;}
        	else {ability.noFall = false; if (!player.getMinecraftPlayer().onGround) ability.justDisabled = true;}
        	player.setAllowFlying(allowFly);
        }
        	
        sender.sendLangfileMessage(success ? player.getAllowFlying() ? "command.fly.on" : "command.fly.off" : "command.fly.failure", new Object[0]);
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
