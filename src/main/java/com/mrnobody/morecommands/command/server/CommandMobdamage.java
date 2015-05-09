package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listeners.Listener;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "mobdamage",
		description = "command.mobdamage.description",
		example = "command.mobdamage.example",
		syntax = "command.mobdamage.syntax",
		videoURL = "command.mobdamage.videoURL"
		)
public class CommandMobdamage extends ServerCommand implements Listener<LivingHurtEvent> {
	private boolean mobdamage = true;
	
	public CommandMobdamage() {
		EventHandler.HURT.getHandler().register(this);
	}

	@Override
	public void onEvent(LivingHurtEvent event) {
		if (!(event.entity instanceof EntityPlayerMP) || !ServerPlayerSettings.playerSettingsMapping.containsKey(event.entity)) return;
		
		if (!ServerPlayerSettings.playerSettingsMapping.get(event.entity).mobdamage) {
			if (event.source.getSourceOfDamage() != null && (event.source.getSourceOfDamage() instanceof EntityCreature || event.source.getSourceOfDamage() instanceof EntityArrow)) {
				if (event.source.getSourceOfDamage() instanceof EntityCreature) event.setCanceled(true);
				if (event.source.getSourceOfDamage() instanceof EntityArrow && ((EntityArrow) event.source.getSourceOfDamage()).shootingEntity instanceof EntityCreature) event.setCanceled(true);
			}
		}
	}
	
	@Override
	public String getCommandName() {
		return "mobdamage";
	}

	@Override
	public String getUsage() {
		return "command.mobdamage.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params)throws CommandException {
		ServerPlayerSettings ability = ServerPlayerSettings.playerSettingsMapping.get(sender.getMinecraftISender());
    		
        boolean mobdamage = false;
        boolean success = false;
        	
        if (params.length >= 1) {
        	if (params[0].equalsIgnoreCase("true")) {mobdamage = true; success = true;}
        	else if (params[0].equalsIgnoreCase("false")) {mobdamage = false; success = true;}
        	else if (params[0].equalsIgnoreCase("0")) {mobdamage = false; success = true;}
        	else if (params[0].equalsIgnoreCase("1")) {mobdamage = true; success = true;}
        	else if (params[0].equalsIgnoreCase("on")) {mobdamage = true; success = true;}
        	else if (params[0].equalsIgnoreCase("off")) {mobdamage = false; success = true;}
    		else if (params[0].equalsIgnoreCase("enable")) {mobdamage = true; success = true;}
    		else if (params[0].equalsIgnoreCase("disable")) {mobdamage = false; success = true;}
        	else {success = false;}
        }
        else {mobdamage = !ability.mobdamage; success = true;}
        	
        if (success) ability.mobdamage = mobdamage;
        	
        sender.sendLangfileMessage(success ? mobdamage ? "command.mobdamage.on" : "command.mobdamage.off" : "command.mobdamage.failure", new Object[0]);
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
