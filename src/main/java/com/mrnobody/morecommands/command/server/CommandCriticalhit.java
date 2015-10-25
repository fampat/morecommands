package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.patch.EntityPlayerMP;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "criticalhit",
		description = "command.criticalhit.description",
		example = "command.criticalhit.example",
		syntax = "command.criticalhit.syntax",
		videoURL = "command.criticalhit.videoURL"
		)
public class CommandCriticalhit extends ServerCommand {

	@Override
	public String getName() {
		return "criticalhit";
	}

	@Override
	public String getUsage() {
		return "command.criticalhit.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
	    EntityPlayerMP player = (EntityPlayerMP) sender.getMinecraftISender();
    	
        if (params.length > 0) {
        	if (params[0].equalsIgnoreCase("enable") || params[0].equalsIgnoreCase("1")
            	|| params[0].equalsIgnoreCase("on") || params[0].equalsIgnoreCase("true")) {
        		player.setCriticalhit(true);
            	sender.sendLangfileMessage("command.criticalhit.on");
            }
            else if (params[0].equalsIgnoreCase("disable") || params[0].equalsIgnoreCase("0")
            		|| params[0].equalsIgnoreCase("off") || params[0].equalsIgnoreCase("false")) {
            	player.setCriticalhit(false);
            	sender.sendLangfileMessage("command.criticalhit.off");
            }
            else throw new CommandException("command.criticalhit.failure", sender);
        }
        else {
        	player.setCriticalhit(!player.getCriticalHit());
        	sender.sendLangfileMessage(player.getCriticalHit() ? "command.criticalhit.on" : "command.criticalhit.off");
        }
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[] {Requirement.PATCH_ENTITYPLAYERMP};
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
