package com.mrnobody.morecommands.command.server;

import java.text.DecimalFormat;
import java.util.Iterator;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.settings.ServerPlayerSettings;
import com.mrnobody.morecommands.util.Coordinate;
import com.mrnobody.morecommands.util.EntityUtils;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.MathHelper;

@Command(
		name = "teleport",
		description = "command.teleport.description",
		example = "command.teleport.example",
		syntax = "command.teleport.syntax",
		videoURL = "command.teleport.videoURL"
		)
public class CommandTeleport extends StandardCommand implements ServerCommandProperties {
	@Override
	public String getCommandName() {
		return "teleport";
	}

	@Override
	public String getCommandUsage() {
		return "command.teleport.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		ServerPlayerSettings settings = isSenderOfEntityType(sender.getMinecraftISender(), EntityPlayerMP.class) ? getPlayerSettings(getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class)) : null;
		Entity entity = getSenderAsEntity(sender.getMinecraftISender(), Entity.class);
		
		if (params.length > 2) {
			try {
				Coordinate coord = getCoordFromParams(sender.getMinecraftISender(), params, 0);
				float pitch = entity.rotationPitch, yaw = entity.rotationYaw;
				
				if (params.length > 3)
					yaw = params[3].equals("~") ? yaw : MathHelper.wrapAngleTo180_float(Float.parseFloat(params[3]));
				
				if (params.length > 4)
					pitch = params[4].equals("~") ? pitch : MathHelper.wrapAngleTo180_float(Float.parseFloat(params[4]));
				
				if (yaw > 90F || yaw < -90F) yaw = MathHelper.wrapAngleTo180_float(yaw + 180F);
				if (pitch > 90F || pitch < -90F) pitch = MathHelper.wrapAngleTo180_float(pitch + 180F);
				
				if (settings != null) 
					settings.lastTeleport = settings.lastPos = EntityUtils.getPosition(entity);
				
				if (entity instanceof EntityPlayerMP)
					((EntityPlayerMP) entity).playerNetServerHandler.setPlayerLocation(coord.getX(), coord.getY(), coord.getZ(), yaw, pitch);
				else
					entity.setLocationAndAngles(coord.getX(), coord.getY(), coord.getZ(), yaw, pitch);
				
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
			EntityUtils.setPosition(entity, new Coordinate(teleportTo.posX, teleportTo.posY + 0.5D, teleportTo.posZ));
			
			sender.sendStringMessage("Successfully teleported to Player '" + params[0] + "'");
		}
		else throw new CommandException("command.teleport.invalidParams", sender);
		
		return null;
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
	public CommandRequirement[] getRequirements() {
		return new CommandRequirement[0];
	}
	
	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	@Override
	public int getDefaultPermissionLevel(String[] args) {
		return 2;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return isSenderOfEntityType(sender, Entity.class);
	}
}
