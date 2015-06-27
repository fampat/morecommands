package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
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
		Player player = new Player((EntityPlayerMP) sender.getMinecraftISender());
    	
        if (params.length > 0) {
        	if (params[0].equalsIgnoreCase("enable") || params[0].equalsIgnoreCase("1")
            	|| params[0].equalsIgnoreCase("on") || params[0].equalsIgnoreCase("true")) {
        		player.setDamage(true);
            	sender.sendLangfileMessage("command.damage.on");
            }
            else if (params[0].equalsIgnoreCase("disable") || params[0].equalsIgnoreCase("0")
            		|| params[0].equalsIgnoreCase("off") || params[0].equalsIgnoreCase("false")) {
            	player.setDamage(false);
            	sender.sendLangfileMessage("command.damage.off");
            }
            else throw new CommandException("command.damage.failure", sender);
        }
        else {
        	player.setDamage(!player.getDamage());
        	sender.sendLangfileMessage(player.getDamage() ? "command.damage.on" : "command.damage.off");
        }
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
