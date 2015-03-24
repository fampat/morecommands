package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.command.CommandBase.Requirement;
import com.mrnobody.morecommands.command.CommandBase.ServerType;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Player;

import cpw.mods.fml.relauncher.Side;

@Command(
		name = "damage",
		description = "command.damage.description",
		example = "command.damage.example",
		syntax = "command.damage.syntax",
		videoURL = "command.damage.videoURL"
		)
public class CommandDamage extends ServerCommand {

	@Override
    public String getCommandName()
    {
        return "damage";
    }

	@Override
    public String getUsage()
    {
        return "command.damage.syntax";
    }
    
	@Override
    public void execute(CommandSender sender, String[] params) throws CommandException {
    	Player player = sender.toPlayer();
    	
    	boolean enableDamage = false;
    	boolean success = false;
    	
    	if (params.length >= 1) {
    		if (params[0].toLowerCase().equals("true")) {enableDamage = true; success = true;}
    		else if (params[0].toLowerCase().equals("false")) {enableDamage = false; success = true;}
    		else if (params[0].toLowerCase().equals("0")) {enableDamage = false; success = true;}
    		else if (params[0].toLowerCase().equals("1")) {enableDamage = true; success = true;}
    		else if (params[0].toLowerCase().equals("on")) {enableDamage = true; success = true;}
    		else if (params[0].toLowerCase().equals("off")) {enableDamage = false; success = true;}
    		else {success = false;}
    	}
    	else {enableDamage = !player.getDamage(); success = true;}
    	
    	if (success) {player.setDamage(enableDamage);}
    	
    	sender.sendLangfileMessageToPlayer(success ? player.getDamage() ? "command.damage.on" : "command.damage.off" : "command.damage.failure", new Object[0]);
    }
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}
	
	@Override
	public void unregisterFromHandler() {}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	@Override
	public int getPermissionLevel() {
		return 2;
	}
}
