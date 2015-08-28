package com.mrnobody.morecommands.command.server;

import java.text.DecimalFormat;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Player;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;

@Command(
		name = "teleport",
		description = "command.teleport.description",
		example = "command.teleport.example",
		syntax = "command.teleport.syntax",
		videoURL = "command.teleport.videoURL"
		)
public class CommandTeleport extends ServerCommand {
	@Override
	public String getName() {
		return "teleport";
	}

	@Override
	public String getUsage() {
		return "command.teleport.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		ServerPlayerSettings settings = ServerPlayerSettings.getPlayerSettings((EntityPlayerMP) sender.getMinecraftISender());
		Player player = new Player((EntityPlayerMP) sender.getMinecraftISender());
		
		if (params.length > 2) {
			try {
				BlockPos coord = new BlockPos(Double.parseDouble(params[0]), Double.parseDouble(params[1]), Double.parseDouble(params[2]));
				if (settings != null) settings.lastPos = player.getPosition();
				player.setPosition(coord);
				DecimalFormat f = new DecimalFormat("#.##");
				
				sender.sendStringMessage("Successfully teleported to:"
						+ " X = " + f.format(coord.getX())
						+ "; Y = " + f.format(coord.getY())
						+ "; Z = " + f.format(coord.getZ()));
			}
			catch (NumberFormatException nfe) {throw new CommandException("command.teleport.NAN", sender);}
		}
		else if (params.length > 0) {
			EntityPlayerMP teleportTo = MinecraftServer.getServer().getConfigurationManager().getPlayerByUsername(params[0]);
			if (teleportTo == null) throw new CommandException("command.teleport.playerNotFound", sender);
			player.setPosition(teleportTo.getPosition());
			
			sender.sendStringMessage("Successfully teleported to Player '" + params[0] + "'");
		}
		else throw new CommandException("command.teleport.invalidParams", sender);
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
