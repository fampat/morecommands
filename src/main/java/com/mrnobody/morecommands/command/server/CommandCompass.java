package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;

@Command(
		description = "command.compass.description",
		example = "command.compass.example",
		name = "command.compass.name",
		syntax = "command.compass.syntax",
		videoURL = "command.compass.videoURL"
		)
public class CommandCompass extends StandardCommand implements ServerCommandProperties {
	@Override
	public String getName() {
		return "compass";
	}

	@Override
	public String getUsage() {
		return "command.compass.syntax";
	}
	
	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		if (params.length > 0) {
			EntityPlayerMP player = getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class);
			
			if (params.length > 1 && params[0].equalsIgnoreCase("set")) {
				if (params.length > 3) {
					BlockPos target;
					
					try {target = getCoordFromParams(sender.getMinecraftISender(), params, 1);}
					catch (NumberFormatException nfe) {throw new CommandException("command.compass.NAN", sender);}
					
					MoreCommands.INSTANCE.getPacketDispatcher().sendS13SetCompassTarget(player, target.getX(), target.getZ());
					getPlayerSettings(player).hasModifiedCompassTarget = true;
					getPlayerSettings(player).waypointCompassTarget = null;
				}
				else {
					ServerPlayerSettings settings = getPlayerSettings(player);
					double[] waypoint = settings.waypoints.get(params[1]);
					
					if (waypoint == null) throw new CommandException("command.compass.waypointNotFound", sender, params[1]);
					else {
						MoreCommands.INSTANCE.getPacketDispatcher().sendS13SetCompassTarget(player, MathHelper.floor_double(waypoint[0]), MathHelper.floor_double(waypoint[2]));
						getPlayerSettings(player).hasModifiedCompassTarget = true;
						getPlayerSettings(player).waypointCompassTarget = params[1];
					}
				}
			}
			else if (params[0].equalsIgnoreCase("reset")) {
				getPlayerSettings(player).hasModifiedCompassTarget = false;
				getPlayerSettings(player).waypointCompassTarget = null;
				MoreCommands.INSTANCE.getPacketDispatcher().sendS13ResetCompassTarget(player);
			}
			else throw new CommandException("command.generic.invalidUsage", sender, this.getName());
		}
		else throw new CommandException("command.generic.invalidUsage", sender, this.getName());
	}

	@Override
	public CommandRequirement[] getRequirements() {
		return new CommandRequirement[] {CommandRequirement.MODDED_CLIENT};
	}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}

	@Override
	public int getDefaultPermissionLevel() {
		return 2;
	}

	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return isSenderOfEntityType(sender, EntityPlayerMP.class);
	}
}
