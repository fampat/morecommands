package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listeners.Listener;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.world.ExplosionEvent;

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
			EntityCreeper creeper = (EntityCreeper) event.explosion.getExplosivePlacedBy();
			
			if (creeper.getAttackTarget() instanceof EntityPlayerMP) {
				EntityPlayerMP player = (EntityPlayerMP) creeper.getAttackTarget();
				if (!ServerPlayerSettings.getPlayerSettings(player).creeperExplosion) event.setCanceled(true);
			}
		}
	}
	
	@Override
	public String getName() {
		return "creeper";
	}

	@Override
	public String getUsage() {
		return "command.creeper.syntax";
	}
	
	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		ServerPlayerSettings settings = ServerPlayerSettings.getPlayerSettings((EntityPlayerMP) sender.getMinecraftISender());
    	
        if (params.length > 0) {
        	if (params[0].equalsIgnoreCase("enable") || params[0].equalsIgnoreCase("1")
            	|| params[0].equalsIgnoreCase("on") || params[0].equalsIgnoreCase("true")) {
        		settings.creeperExplosion = true;
            	sender.sendLangfileMessage("command.creeper.on");
            }
            else if (params[0].equalsIgnoreCase("disable") || params[0].equalsIgnoreCase("0")
            		|| params[0].equalsIgnoreCase("off") || params[0].equalsIgnoreCase("false")) {
            	settings.creeperExplosion = false;
            	sender.sendLangfileMessage("command.creeper.off");
            }
            else throw new CommandException("command.creeper.failure", sender);
        }
        else {
        	settings.creeperExplosion = !settings.creeperExplosion;
        	sender.sendLangfileMessage(settings.creeperExplosion ? "command.creeper.on" : "command.creeper.off");
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
		return sender instanceof EntityPlayerMP;
	}
}
