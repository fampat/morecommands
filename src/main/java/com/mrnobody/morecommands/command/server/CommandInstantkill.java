package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.patch.EntityPlayerMP;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "instantkill",
		description = "command.instantkill.description",
		example = "command.instantkill.example",
		syntax = "command.instantkill.syntax",
		videoURL = "command.instantkill.videoURL"
		)
public class CommandInstantkill extends ServerCommand {
	@Override
	public String getName() {
		return "instantkill";
	}

	@Override
	public String getUsage() {
		return "command.instantkill.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params)throws CommandException {
		EntityPlayerMP player = (EntityPlayerMP) sender.getMinecraftISender();
		
        if (params.length > 0) {
        	if (params[0].equalsIgnoreCase("enable") || params[0].equalsIgnoreCase("1")
            	|| params[0].equalsIgnoreCase("on") || params[0].equalsIgnoreCase("true")) {
        		player.setInstantkill(true);
            	sender.sendLangfileMessage("command.instantkill.on");
            }
            else if (params[0].equalsIgnoreCase("disable") || params[0].equalsIgnoreCase("0")
            		|| params[0].equalsIgnoreCase("off") || params[0].equalsIgnoreCase("false")) {
            	player.setInstantkill(false);
            	sender.sendLangfileMessage("command.instantkill.off");
            }
            else throw new CommandException("command.instantkill.failure", sender);
        }
        else {
        	player.setInstantkill(!player.getInstantkill());
        	sender.sendLangfileMessage(player.getInstantkill() ? "command.instantkill.on" : "command.instantkill.off");
        }
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[] {Requirement.PATCH_ENTITYPLAYERMP};
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
