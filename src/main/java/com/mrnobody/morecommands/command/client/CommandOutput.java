package com.mrnobody.morecommands.command.client;

import com.mrnobody.morecommands.command.ClientCommandProperties;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.LanguageManager;

import net.minecraft.util.text.TextComponentString;


@Command(
		name = "output",
		description = "command.output.description",
		example = "command.output.example",
		syntax = "command.output.syntax",
		videoURL = "command.output.videoURL"
		)
public class CommandOutput extends StandardCommand implements ClientCommandProperties {

	@Override
	public String getCommandName() {
		return "output";
	}

	@Override
	public String getCommandUsage() {
		return "command.output.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		try {CommandSender.output = parseTrueFalse(params, 0, !CommandSender.output);}
		catch (IllegalArgumentException ex) {throw new CommandException("command.output.failure", sender);}
		
		sender.getMinecraftISender().sendMessage(new TextComponentString(LanguageManager.translate(
				MoreCommands.INSTANCE.getCurrentLang(sender.getMinecraftISender()), CommandSender.output ? "command.output.on" : "command.output.off")));
		
    	MoreCommands.INSTANCE.getPacketDispatcher().sendC01Output(CommandSender.output);
    	return null;
	}
	
	@Override
	public CommandRequirement[] getRequirements() {
		return new CommandRequirement[0];
	}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	@Override
	public boolean registerIfServerModded() {
		return true;
	}
	
	@Override
	public int getDefaultPermissionLevel(String[] args) {
		return 0;
	}
}
