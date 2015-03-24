package com.mrnobody.morecommands.command.server;

import java.text.DecimalFormat;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "return",
		description = "command.return.description",
		example = "command.return.example",
		syntax = "command.return.syntax",
		videoURL = "command.return.videoURL"
		)
public class CommandReturn extends ServerCommand {
	@Override
	public String getCommandName() {
		return "return";
	}

	@Override
	public String getUsage() {
		return "command.return.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		ServerPlayerSettings settings = ServerPlayerSettings.playerSettingsMapping.get(sender.getMinecraftISender());
		
		if (settings.lastPos == null) {
			sender.sendLangfileMessageToPlayer("command.return.noLastPos", new Object[0]);
			return;
		}
		
		sender.toPlayer().setPosition(settings.lastPos);
		settings.lastPos = sender.toPlayer().getPosition();
		
		DecimalFormat f = new DecimalFormat("#.##");
				
		sender.sendStringMessageToPlayer("Successfully returned to:"
				+ " X = " + f.format(settings.lastPos.getX())
				+ "; Y = " + f.format(settings.lastPos.getY())
				+ "; Z = " + f.format(settings.lastPos.getZ()));
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
