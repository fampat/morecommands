package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.patch.EntityPlayerMP;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
	description = "command.sprint.description",
	example = "command.sprint.example",
	name = "sprint",
	syntax = "command.sprint.syntax",
	videoURL = "command.sprint.videoURL"
		)
public class CommandSprint extends ServerCommand {
	@Override
	public String getCommandName() {
		return "sprint";
	}

	@Override
	public String getUsage() {
		return "command.sprint.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		EntityPlayerMP player = (EntityPlayerMP) sender.getMinecraftISender();
		
        if (params.length > 0) {
        	if (params[0].equalsIgnoreCase("enable") || params[0].equalsIgnoreCase("1")
            	|| params[0].equalsIgnoreCase("on") || params[0].equalsIgnoreCase("true")) {
        		player.setInfniteSprinting(true); player.setSprinting(true);
            	sender.sendLangfileMessage("command.sprint.on");
            }
            else if (params[0].equalsIgnoreCase("disable") || params[0].equalsIgnoreCase("0")
            		|| params[0].equalsIgnoreCase("off") || params[0].equalsIgnoreCase("false")) {
            	player.setInfniteSprinting(false); player.setSprinting(false);
            	sender.sendLangfileMessage("command.sprint.off");
            }
            else throw new CommandException("command.sprint.failure", sender);
        }
        else {
        	player.setInfniteSprinting(!player.getInfniteSprinting()); player.setSprinting(player.getInfniteSprinting());
        	sender.sendLangfileMessage(player.getInfniteSprinting() ? "command.sprint.on" : "command.sprint.off");
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
