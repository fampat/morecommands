package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.packet.server.S11PacketStepheight;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "stepheight",
		description = "command.stepheight.description",
		example = "command.stepheight.example",
		syntax = "command.stepheight.syntax",
		videoURL = "command.stepheight.videoURL"
		)
public class CommandStepheight extends ServerCommand {

	@Override
	public String getCommandName() {
		return "stepheight";
	}

	@Override
	public String getUsage() {
		return "command.stepheight.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		float height;
		
		if (params.length > 0) {
			try {height = Float.parseFloat(params[0]); sender.sendLangfileMessageToPlayer("command.stepheight.setto", new Object[] {height});}
			catch (NumberFormatException e) {
				if (params[0].toLowerCase().equals("reset")) {height = 0.5F; sender.sendLangfileMessageToPlayer("command.stepheight.reset", new Object[0]);}
				else {sender.sendLangfileMessageToPlayer("command.stepheight.invalidArg", new Object[0]); return;}
			}
			
			S11PacketStepheight packet = new S11PacketStepheight();
			packet.stepheight = height;
			MoreCommands.getNetwork().sendTo(packet, this.getCommandSenderAsPlayer(sender.getMinecraftISender()));
		}
		else sender.sendLangfileMessageToPlayer("command.stepheight.invalidUsage", new Object[0]);
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[] {Requirement.MODDED_CLIENT};
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
