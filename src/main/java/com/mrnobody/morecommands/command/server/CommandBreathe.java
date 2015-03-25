package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Player;

@Command(
		name = "breathe",
		description = "command.breathe.description",
		example = "command.breathe.example",
		syntax = "command.breathe.syntax",
		videoURL = "command.breathe.videoURL"
		)
public class CommandBreathe extends ServerCommand {
	private final int AIR_MAX = 300;

	@Override
	public String getName() {
		return "breathe";
	}

	@Override
	public String getUsage() {
		return "command.breathe.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Player player = sender.toPlayer();
		int air = 0;
		
		if (params.length > 0) {
			try {air = Integer.parseInt(params[0]);}
			catch (NumberFormatException e) {sender.sendLangfileMessageToPlayer("command.breathe.noNumber", new Object[0]);}
		}
		else air = this.AIR_MAX;
		
		if (player.getMinecraftPlayer().isInWater()) {player.setAir(player.getMinecraftPlayer().getAir() + air > this.AIR_MAX ? this.AIR_MAX : player.getMinecraftPlayer().getAir() + air);}
		else {sender.sendLangfileMessageToPlayer("command.breathe.notInWater", new Object[0]);}
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
