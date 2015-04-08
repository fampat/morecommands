package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.patch.EntityPlayerMP;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Player;

@Command(
		name = "damage",
		description = "command.damage.description",
		example = "command.damage.example",
		syntax = "command.damage.syntax",
		videoURL = "command.damage.videoURL"
		)
public class CommandDamage extends ServerCommand {

	@Override
    public String getName()
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
		Player player = new Player((EntityPlayerMP) sender.getMinecraftISender());
    	boolean enableDamage = false;
    	boolean success = false;
    	
    	if (params.length >= 1) {
    		if (params[0].equalsIgnoreCase("true")) {enableDamage = true; success = true;}
    		else if (params[0].equalsIgnoreCase("false")) {enableDamage = false; success = true;}
    		else if (params[0].equalsIgnoreCase("0")) {enableDamage = false; success = true;}
    		else if (params[0].equalsIgnoreCase("1")) {enableDamage = true; success = true;}
    		else if (params[0].equalsIgnoreCase("on")) {enableDamage = true; success = true;}
    		else if (params[0].equalsIgnoreCase("off")) {enableDamage = false; success = true;}
    		else if (params[0].equalsIgnoreCase("enable")) {enableDamage = true; success = true;}
    		else if (params[0].equalsIgnoreCase("disable")) {enableDamage = false; success = true;}
    		else {success = false;}
    	}
    	else {enableDamage = !player.getDamage(); success = true;}
    	
    	if (success) {player.setDamage(enableDamage);}
    	
    	sender.sendLangfileMessage(success ? player.getDamage() ? "command.damage.on" : "command.damage.off" : "command.damage.failure", new Object[0]);
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
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return sender instanceof EntityPlayerMP;
	}
}
