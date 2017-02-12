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

@Command(
		name = "position",
		description = "command.position.description",
		example = "command.position.example",
		syntax = "command.position.syntax",
		videoURL = "command.position.videoURL"
		)
public class CommandPosition extends StandardCommand implements ClientCommandProperties {

	@Override
	public String getCommandName() {
		return "position";
	}

	@Override
	public String getCommandUsage() {
		return "command.position.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		DecimalFormat f = new DecimalFormat("#.##");
		
		sender.sendStringMessage("Your current position is:"
				+ " X = " + f.format(player.posX)
				+ "; Y = " + f.format(player.posY)
				+ "; Z = " + f.format(player.posZ));
		
		return "x=" + player.posX + "y,=" + player.posY + ",z=" + player.posZ;
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
