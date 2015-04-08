package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listener;
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
		ServerPlayerSettings ability = ServerPlayerSettings.playerSettingsMapping.get(sender.getMinecraftISender());
    	
    	boolean dealDamage = true;
    	boolean success = false;
    	
    	if (params.length >= 1) {
    		if (params[0].equalsIgnoreCase("true")) {dealDamage = true; success = true;}
    		else if (params[0].equalsIgnoreCase("false")) {dealDamage = false; success = true;}
    		else if (params[0].equalsIgnoreCase("0")) {dealDamage = false; success = true;}
    		else if (params[0].equalsIgnoreCase("1")) {dealDamage = true; success = true;}
    		else if (params[0].equalsIgnoreCase("on")) {dealDamage = true; success = true;}
    		else if (params[0].equalsIgnoreCase("off")) {dealDamage = false; success = true;}
    		else if (params[0].equalsIgnoreCase("enable")) {dealDamage = true; success = true;}
    		else if (params[0].equalsIgnoreCase("disable")) {dealDamage = false; success = true;}
    		else {success = false;}
    	}
    	else {dealDamage = !ability.waterdamage; success = true;}
    	
    	if (success) ability.waterdamage = dealDamage;
    	
    	sender.sendLangfileMessage(success ? dealDamage ? "command.waterdamage.on" : "command.waterdamage.off" : "command.waterdamage.failure", new Object[0]);
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
