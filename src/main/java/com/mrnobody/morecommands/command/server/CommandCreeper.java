package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.world.ExplosionEvent;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listeners.Listener;
import com.mrnobody.morecommands.util.GlobalSettings;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "creeper",
		description = "command.creeper.description",
		example = "command.creeper.example",
		syntax = "command.creeper.syntax",
		videoURL = "command.creeper.videoURL"
		)
public class CommandCreeper extends ServerCommand implements Listener<ExplosionEvent> {
	public CommandCreeper() {
		EventHandler.EXPLOSION.getHandler().register(this);
	}

	@Override
	public void onEvent(ExplosionEvent event) {
		if (event instanceof ExplosionEvent.Start && event.explosion.getExplosivePlacedBy() instanceof EntityCreeper) {
			if (!GlobalSettings.creeperExplosion) {event.setCanceled(true); return;}
			
			EntityCreeper creeper = (EntityCreeper) event.explosion.getExplosivePlacedBy();
			
			if (creeper.getAttackTarget() instanceof EntityPlayerMP) {
				EntityPlayerMP player = (EntityPlayerMP) creeper.getAttackTarget();
				
				if (ServerPlayerSettings.playerSettingsMapping.containsKey(player)) {
					if (!ServerPlayerSettings.playerSettingsMapping.get(player).creeperExplosion) event.setCanceled(true);
				}
			}
		}
	}
	
	@Override
	public String getCommandName() {
		return "creeper";
	}

	@Override
	public String getUsage() {
		return "command.creeper.syntax";
	}
	
	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		ServerPlayerSettings ability = ServerPlayerSettings.playerSettingsMapping.get(sender.getMinecraftISender());
    		
        boolean allowExplosion = false;
        boolean success = false;
    		
        if (params.length >= 1) {
        	if (params[0].equalsIgnoreCase("true")) {allowExplosion = true; success = true;}
        	else if (params[0].equalsIgnoreCase("false")) {allowExplosion = false; success = true;}
        	else if (params[0].equalsIgnoreCase("0")) {allowExplosion = false; success = true;}
        	else if (params[0].equalsIgnoreCase("1")) {allowExplosion = true; success = true;}
        	else if (params[0].equalsIgnoreCase("on")) {allowExplosion = true; success = true;}
        	else if (params[0].equalsIgnoreCase("off")) {allowExplosion = false; success = true;}
    		else if (params[0].equalsIgnoreCase("enable")) {allowExplosion = true; success = true;}
    		else if (params[0].equalsIgnoreCase("disable")) {allowExplosion = false; success = true;}
        	else {success = false;}
        }
        else {allowExplosion = !ability.creeperExplosion; success = true;}
        	
        if (success) ability.creeperExplosion = allowExplosion;
        	
        sender.sendLangfileMessage(success ? allowExplosion ? "command.creeper.on" : "command.creeper.off" : "command.creeper.failure", new Object[0]);
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
		return sender instanceof EntityPlayerMP;
	}
}
