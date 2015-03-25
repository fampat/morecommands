package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Player;

@Command(
		name = "cheats",
		description = "command.cheats.description",
		example = "command.cheats.example",
		syntax = "command.cheats.syntax",
		videoURL = "command.cheats.videoURL"
		)
public class CommandCheats extends ServerCommand {

	@Override
	public boolean canCommandSenderUse(ICommandSender sender) {return true;}
	
	@Override
	public String getName() {
		return "cheats";
	}

	@Override
	public String getUsage() {
		return "command.cheats.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
    	Player player = sender.toPlayer();
    	
    	boolean allowCheats = false;
    	boolean success = false;
    	
    	if (params.length >= 1) {
    		if (params[0].toLowerCase().equals("true")) {allowCheats = true; success = true;}
    		else if (params[0].toLowerCase().equals("false")) {allowCheats = false; success = true;}
    		else if (params[0].toLowerCase().equals("0")) {allowCheats = false; success = true;}
    		else if (params[0].toLowerCase().equals("1")) {allowCheats = true; success = true;}
    		else if (params[0].toLowerCase().equals("on")) {allowCheats = true; success = true;}
    		else if (params[0].toLowerCase().equals("off")) {allowCheats = false; success = true;}
    		else {success = false;}
    	}
    	else {allowCheats = !player.getWorld().isCheats(); success = true;}
    	
    	if (success) {player.getWorld().setCheats(allowCheats);}
    	
    	sender.sendLangfileMessageToPlayer(success ? player.getWorld().isCheats() ? "command.cheats.on" : "command.cheats.off" : "command.cheats.failure", new Object[0]);
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.INTEGRATED;
	}

	@Override
	public void unregisterFromHandler() {}
	
	@Override
	public int getPermissionLevel() {
		return 0;
	}
}
