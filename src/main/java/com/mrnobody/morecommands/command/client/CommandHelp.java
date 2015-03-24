package com.mrnobody.morecommands.command.client;

import java.util.Arrays;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;

import com.mrnobody.morecommands.command.ClientCommand;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "help",
		description = "command.helpSideClient.description",
		example = "command.helpSideClient.example",
		syntax = "command.helpSideClient.syntax",
		videoURL = "command.helpSideClient.videoURL"
		)
public class CommandHelp extends ClientCommand {
	@Override
	public String getCommandName() {
		return "help";
	}

	@Override
	public String getUsage() {
		return "command.helpSideClient.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		if (!CommandClientcommands.clientCommandsEnabled()) {
			String args = "";
			for (String param : params) args += " " + param;
			Minecraft.getMinecraft().thePlayer.sendChatMessage("/help" + args);
			return;
		}
		
		if (params.length > 0) {
			String args = "";
			if (params.length > 1) for (String param : Arrays.copyOfRange(params, 1, params.length)) args += " " + param;
			
			if (params[0].equalsIgnoreCase("client")) ClientCommandHandler.instance.executeCommand(sender.getMinecraftISender(), "chelp" + args);
			if (params[0].equalsIgnoreCase("server")) ClientCommandHandler.instance.executeCommand(sender.getMinecraftISender(), "shelp" + args);
			if (params[0].equalsIgnoreCase("help")) sender.sendLangfileMessageToPlayer("command.helpSideClient.info", new Object[0]);
			else sender.sendLangfileMessageToPlayer("command.helpSideClient.info", new Object[0]);
		}
		else sender.sendLangfileMessageToPlayer("command.helpSideClient.info", new Object[0]);
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
