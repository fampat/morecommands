package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Player;

@Command(
		name = "health",
		description = "command.health.description",
		example = "command.health.example",
		syntax = "command.health.syntax",
		videoURL = "command.health.videoURL"
		)
public class CommandHealth extends ServerCommand {

	private final float MIN_HEALTH = 0.5f;
	private final float MAX_HEALTH = 20.0f;
	
	@Override
	public String getCommandName() {
		return "health";
	}

	@Override
	public String getUsage() {
		return "command.health.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Player player = new Player((EntityPlayerMP) sender.getMinecraftISender());
		
		if (params.length > 0) {
			try {player.setHealth(Float.parseFloat(params[0])); sender.sendLangfileMessage("command.health.success");}
			catch (NumberFormatException e) {
				if (params[0].equalsIgnoreCase("min")) {player.setHealth(MIN_HEALTH); sender.sendLangfileMessage("command.health.success");}
				else if (params[0].equalsIgnoreCase("max")) {player.setHealth(MAX_HEALTH); sender.sendLangfileMessage("command.health.success");}
				else if (params[0].equalsIgnoreCase("get")) {sender.sendLangfileMessage("command.health.get", player.getHealth());}
				else throw new CommandException("command.health.invalidParam", sender);
			}
		}
		else throw new CommandException("command.health.invalidUsage", sender);
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
