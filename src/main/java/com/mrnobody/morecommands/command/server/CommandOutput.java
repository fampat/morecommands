package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.settings.ServerPlayerSettings;
import com.mrnobody.morecommands.util.LanguageManager;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;


@Command(
		name = "output",
		description = "command.output.description",
		example = "command.output.example",
		syntax = "command.output.syntax",
		videoURL = "command.output.videoURL"
		)
public class CommandOutput extends StandardCommand implements ServerCommandProperties {

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
		ServerPlayerSettings settings = getPlayerSettings(getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class));
    	boolean output;
    	
		try {output = parseTrueFalse(params, 0, !settings.output);}
		catch (IllegalArgumentException ex) {throw new CommandException("command.output.failure", sender);}
        
        settings.output = output;
        
		sender.getMinecraftISender().addChatMessage(new TextComponentString(LanguageManager.translate(
				MoreCommands.INSTANCE.getCurrentLang(sender.getMinecraftISender()), output ? "command.output.on" : "command.output.off")));
	
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
	public int getDefaultPermissionLevel(String[] args) {
		return 0;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return isSenderOfEntityType(sender, EntityPlayerMP.class);
	}
}
