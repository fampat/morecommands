package com.mrnobody.morecommands.command.server;

import java.text.DecimalFormat;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Player;

@Command(
		name = "setspawn",
		description = "command.setspawn.description",
		example = "command.setspawn.example",
		syntax = "command.setspawn.syntax",
		videoURL = "command.setspawn.videoURL"
		)
public class CommandSetspawn extends ServerCommand {

	@Override
	public String getName() {
		return "setspawn";
	}

	@Override
	public String getUsage() {
		return "command.setspawn.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Player player = new Player((EntityPlayerMP) sender.getMinecraftISender());
		BlockPos coord = player.getPosition();
		DecimalFormat f = new DecimalFormat("#.##");
		
		if (params.length > 2) {
			try {coord = new BlockPos(Double.parseDouble(params[0]), Double.parseDouble(params[1]), Double.parseDouble(params[2]));}
			catch (NumberFormatException nfe) {sender.sendLangfileMessage("command.setspawn.invalidPos", new Object[0]); return;}
		}
		
		player.setSpawn(coord);
		sender.sendStringMessage("Spawn point set to:"
				+ " X = " + f.format(coord.getX())
				+ "; Y = " + f.format(coord.getY())
				+ "; Z = " + f.format(coord.getZ()));
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
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
