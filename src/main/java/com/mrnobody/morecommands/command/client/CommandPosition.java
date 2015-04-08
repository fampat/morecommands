package com.mrnobody.morecommands.command.client;

import java.text.DecimalFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

import com.mrnobody.morecommands.command.ClientCommand;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "position",
		description = "command.position.description",
		example = "command.position.example",
		syntax = "command.position.syntax",
		videoURL = "command.position.videoURL"
		)
public class CommandPosition extends ClientCommand {

	@Override
	public String getName() {
		return "position";
	}

	@Override
	public String getUsage() {
		return "command.position.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
		DecimalFormat f = new DecimalFormat("#.##");
		
		sender.sendStringMessage("Your current position is:"
				+ " X = " + f.format(player.posX)
				+ "; Y = " + f.format(player.posY)
				+ "; Z = " + f.format(player.posZ));
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
		return false;
	}
	
	@Override
	public int getPermissionLevel() {
		return 0;
	}
}
