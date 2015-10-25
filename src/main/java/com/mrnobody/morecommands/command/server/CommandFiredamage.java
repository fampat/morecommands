package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listeners.EventListener;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

@Command(
		name = "firedamage",
		description = "command.firedamage.description",
		example = "command.firedamage.example",
		syntax = "command.firedamage.syntax",
		videoURL = "command.firedamage.videoURL"
		)
public class CommandFiredamage extends ServerCommand implements EventListener<LivingAttackEvent> {
	public CommandFiredamage() {
		EventHandler.ATTACK.getHandler().register(this);
	}
	
	@Override
	public void onEvent(LivingAttackEvent event) {
		if (event.entity instanceof EntityPlayerMP && (event.source == DamageSource.inFire || event.source == DamageSource.onFire || event.source == DamageSource.lava)) {
			EntityPlayerMP player = (EntityPlayerMP) event.entity;
			if (!ServerPlayerSettings.getPlayerSettings(player).firedamage) event.setCanceled(true);
		}
	}
	
	@Override
	public String getName() {
		return "firedamage";
	}

	@Override
	public String getUsage() {
		return "command.firedamage.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		ServerPlayerSettings settings = ServerPlayerSettings.getPlayerSettings((EntityPlayerMP) sender.getMinecraftISender());
    	
        if (params.length > 0) {
        	if (params[0].equalsIgnoreCase("enable") || params[0].equalsIgnoreCase("1")
            	|| params[0].equalsIgnoreCase("on") || params[0].equalsIgnoreCase("true")) {
        		settings.firedamage = true;
            	sender.sendLangfileMessage("command.firedamage.on");
            }
            else if (params[0].equalsIgnoreCase("disable") || params[0].equalsIgnoreCase("0")
            		|| params[0].equalsIgnoreCase("off") || params[0].equalsIgnoreCase("false")) {
            	settings.firedamage = false;
            	sender.sendLangfileMessage("command.firedamage.off");
            }
            else throw new CommandException("command.firedamage.failure", sender);
        }
        else {
        	settings.firedamage = !settings.firedamage;
        	sender.sendLangfileMessage(settings.firedamage ? "command.firedamage.on" : "command.firedamage.off");
        }
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
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
