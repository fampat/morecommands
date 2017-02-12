package com.mrnobody.morecommands.command.client;

import java.text.DecimalFormat;

import com.mrnobody.morecommands.command.ClientCommandProperties;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayerMP;

@Command(
		name = "playeruuid",
		description = "command.playeruuid.description",
		example = "command.playeruuid.example",
		syntax = "command.playeruuid.syntax",
		videoURL = "command.playeruuid.videoURL"
		)
public class CommandPlayeruuid extends StandardCommand implements ClientCommandProperties {

	@Override
	public String getCommandName() {
		return "playeruuid";
	}

	@Override
	public String getCommandUsage() {
		return "command.playeruuid.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		sender.sendLangfileMessage("command.playeruuid.uuid", Minecraft.getMinecraft().getSession().getProfile().getId().toString());
		return Minecraft.getMinecraft().getSession().getProfile().getId().toString();
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
		return false;
	}
	
	@Override
	public int getDefaultPermissionLevel(String[] args) {
		return 0;
	}
}
