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
		name = "air",
		description = "command.air.description",
		example = "command.air.example",
		syntax = "command.air.syntax",
		videoURL = "command.air.videoURL"
		)
public class CommandAir extends ServerCommand {

	private final int AIR_MIN = 1;
	private final int AIR_MAX = 300;
	
	@Override
	public String getCommandName() {
		return "air";
	}

	@Override
	public String getUsage() {
		return "command.air.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Player player = sender.toPlayer();
		
		if (params.length > 0 && player.getMinecraftPlayer().isInWater()) {
			try {player.setAir(Integer.parseInt(params[0])); sender.sendLangfileMessageToPlayer("command.air.success", new Object[0]);}
			catch (NumberFormatException e) {
				if (params[0].toLowerCase().equals("min")) {player.setAir(this.AIR_MIN); sender.sendLangfileMessageToPlayer("command.air.success", new Object[0]);}
				else if (params[0].toLowerCase().equals("max")) {player.setAir(this.AIR_MAX); sender.sendLangfileMessageToPlayer("command.air.success", new Object[0]);}
				else if (params[0].toLowerCase().equals("get")) {sender.sendLangfileMessageToPlayer("command.air.get", new Object[] {player.getMinecraftPlayer().getAir()});}
				else {sender.sendLangfileMessageToPlayer("command.air.invalidParam", new Object[0]);}
			}
		}
		else if (!player.getMinecraftPlayer().isInWater()) {sender.sendLangfileMessageToPlayer("command.air.notInWater", new Object[0]);}
		else {sender.sendLangfileMessageToPlayer("command.air.invalidUsage", new Object[0]);}
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}
	
	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	public void unregisterFromHandler() {}
	
	@Override
	public int getPermissionLevel() {
		return 2;
	}
}
