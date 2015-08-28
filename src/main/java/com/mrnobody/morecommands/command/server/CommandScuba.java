package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listeners.Listener;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Command(
		name = "scuba",
		description = "command.scuba.description",
		example = "command.scuba.example",
		syntax = "command.scuba.syntax",
		videoURL = "command.scuba.videoURL"
		)
public class CommandScuba extends ServerCommand implements Listener<TickEvent> {
	private final int AIR_MAX = 300;
	
	public CommandScuba() {EventHandler.TICK.getHandler().register(this);}
	
	@Override
	public void onEvent(TickEvent e) {
		if (e instanceof TickEvent.PlayerTickEvent) {
			TickEvent.PlayerTickEvent event = (TickEvent.PlayerTickEvent) e;
			if (!(event.player instanceof EntityPlayerMP)) return;
			if (event.player.isInWater() && ServerPlayerSettings.getPlayerSettings((EntityPlayerMP) event.player).scuba) 
				event.player.setAir(this.AIR_MAX);
		}
	}

	@Override
	public String getName() {
		return "scuba";
	}

	@Override
	public String getUsage() {
		return "command.scuba.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		ServerPlayerSettings settings = ServerPlayerSettings.getPlayerSettings((EntityPlayerMP) sender.getMinecraftISender());
    	
        if (params.length > 0) {
        	if (params[0].equalsIgnoreCase("enable") || params[0].equalsIgnoreCase("1")
            	|| params[0].equalsIgnoreCase("on") || params[0].equalsIgnoreCase("true")) {
        		settings.scuba = true;
            	sender.sendLangfileMessage("command.scuba.on");
            }
            else if (params[0].equalsIgnoreCase("disable") || params[0].equalsIgnoreCase("0")
            		|| params[0].equalsIgnoreCase("off") || params[0].equalsIgnoreCase("false")) {
            	settings.scuba = false;
            	sender.sendLangfileMessage("command.scuba.off");
            }
            else throw new CommandException("command.scuba.failure", sender);
        }
        else {
        	settings.scuba = !settings.scuba;
        	sender.sendLangfileMessage(settings.scuba ? "command.scuba.on" : "command.scuba.off");
        }
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}
	
	@Override
	public void unregisterFromHandler() {
		EventHandler.TICK.getHandler().unregister(this);
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
