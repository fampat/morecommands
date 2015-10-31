package com.mrnobody.morecommands.command.client;

import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.command.ClientCommand;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.util.LanguageManager;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.util.ChatComponentText;


@Command(
		name = "output",
		description = "command.output.description",
		example = "command.output.example",
		syntax = "command.output.syntax",
		videoURL = "command.output.videoURL"
		)
public class CommandOutput extends ClientCommand {

	@Override
	public String getCommandName() {
		return "output";
	}

	@Override
	public String getUsage() {
		return "command.output.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		try {CommandSender.output = parseTrueFalse(params, 0, CommandSender.output);}
		catch (IllegalArgumentException ex) {throw new CommandException("command.output.failure", sender);}
		
		sender.getMinecraftISender().addChatMessage(new ChatComponentText(LanguageManager.translate(
				MoreCommands.getMoreCommands().getCurrentLang(sender.getMinecraftISender()), CommandSender.output ? "command.output.on" : "command.output.off")));
		
    	if (MoreCommands.getMoreCommands().getPlayerUUID() != null)
    		MoreCommands.getMoreCommands().getPacketDispatcher().sendC04Output(CommandSender.output);
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
	public boolean registerIfServerModded() {
		return true;
	}
	
	@Override
	public int getPermissionLevel() {
		return 0;
	}
}
