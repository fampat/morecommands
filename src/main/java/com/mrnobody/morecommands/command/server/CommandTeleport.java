package com.mrnobody.morecommands.command.server;

import java.text.DecimalFormat;
import java.util.Iterator;

import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Coordinate;
import com.mrnobody.morecommands.wrapper.Player;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

@Command(
		name = "teleport",
		description = "command.teleport.description",
		example = "command.teleport.example",
		syntax = "command.teleport.syntax",
		videoURL = "command.teleport.videoURL"
		)
public class CommandTeleport extends ServerCommand {
	@Override
	public String getCommandName() {
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
				Coordinate coord = new Coordinate(Double.parseDouble(params[0]), Double.parseDouble(params[1]), Double.parseDouble(params[2]));
				settings.lastPos = player.getPosition();
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
			EntityPlayerMP teleportTo = getPlayerByUsername(params[0]);
			if (teleportTo == null) throw new CommandException("command.teleport.playerNotFound", sender);
			player.setPosition(new Coordinate(teleportTo.posX, teleportTo.posY + 0.5D, teleportTo.posZ));
			
			sender.sendStringMessage("Successfully teleported to Player '" + params[0] + "'");
		}
		else throw new CommandException("command.teleport.invalidParams", sender);
	}
	
	private EntityPlayerMP getPlayerByUsername(String username) {
		Object playerEntity;
		Iterator players = MinecraftServer.getServer().getConfigurationManager().playerEntityList.iterator();
		
		while (players.hasNext()) {
			playerEntity = players.next();
			
			if (playerEntity instanceof EntityPlayerMP) {
				if (((EntityPlayerMP) playerEntity).getCommandSenderName().equalsIgnoreCase(username))
					return (EntityPlayerMP) playerEntity;
			}
		}
		
		return null;
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
	public int getPermissionLevel() {
		return 2;
	}
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return sender instanceof EntityPlayerMP;
	}
}
