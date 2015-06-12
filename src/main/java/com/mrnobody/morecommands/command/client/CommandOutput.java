package com.mrnobody.morecommands.command.client;

import net.minecraft.util.ChatComponentText;

import com.mrnobody.morecommands.command.ClientCommand;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.util.LanguageManager;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;


//MAKE COMMAND CLIENT SIDE TOO

@Command(
		name = "output",
		description = "command.output.description",
		example = "command.output.example",
		syntax = "command.output.syntax",
		videoURL = "command.output.videoURL"
		)
public class CommandOutput extends ClientCommand {

	@Override
	public String getName() {
		return "output";
	}

	@Override
	public String getUsage() {
		return "command.output.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		boolean output = false;
		
		if (params.length > 0) {
        	if (params[0].equalsIgnoreCase("enable") || params[0].equalsIgnoreCase("1")
            	|| params[0].equalsIgnoreCase("on") || params[0].equalsIgnoreCase("true")) {
        		output = true;
            }
            else if (params[0].equalsIgnoreCase("disable") || params[0].equalsIgnoreCase("0")
            		|| params[0].equalsIgnoreCase("off") || params[0].equalsIgnoreCase("false")) {
            	output = false;
            }
            else throw new CommandException("command.output.failure", sender);
        }
        else output = !CommandSender.output;
		
		sender.getMinecraftISender().addChatMessage(new ChatComponentText(LanguageManager.getTranslation(
				MoreCommands.getMoreCommands().getCurrentLang(sender.getMinecraftISender()), output ? "command.output.on" : "command.output.off")));
		
		CommandSender.output = output;
		
    	if (MoreCommands.getMoreCommands().getPlayerUUID() != null)
    		MoreCommands.getMoreCommands().getPacketDispatcher().sendC03Output(output);
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
