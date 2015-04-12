package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.core.MoreCommands;
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
	public String getName() {
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
			try {height = Float.parseFloat(params[0]); sender.sendLangfileMessage("command.stepheight.setto", new Object[] {height});}
			catch (NumberFormatException e) {
				if (params[0].equalsIgnoreCase("reset")) {height = 0.5F; sender.sendLangfileMessage("command.stepheight.reset", new Object[0]);}
				else {sender.sendLangfileMessage("command.stepheight.invalidArg", new Object[0]); return;}
			}
			
			MoreCommands.getMoreCommands().getPacketDispatcher().sendS11Stepheight((EntityPlayerMP) sender.getMinecraftISender(), height);
		}
		else sender.sendLangfileMessage("command.stepheight.invalidUsage", new Object[0]);
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
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return sender instanceof EntityPlayerMP;
	}
}
