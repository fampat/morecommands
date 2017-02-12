package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.Coordinate;
import com.mrnobody.morecommands.util.EntityUtils;
import com.mrnobody.morecommands.util.WorldUtils;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntityMobSpawner;

@Command(
		description = "command.spawner.description",
		example = "command.spawner.example",
		name = "spawner",
		syntax = "command.spawner.syntax",
		videoURL = "command.spawner.videoURL"
		)
public class CommandSpawner extends StandardCommand implements ServerCommandProperties {
	@Override
	public String getCommandName() {
		return "spawner";
	}

	@Override
	public String getCommandUsage() {
		return "command.spawner.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params)throws CommandException {
		Coordinate trace;
		
		if (params.length > 3)
			trace = getCoordFromParams(sender.getMinecraftISender(), params, 1);
		else
			trace = EntityUtils.traceBlock(getSenderAsEntity(sender.getMinecraftISender(), EntityLivingBase.class), 128D);

		if (trace != null && params.length > 0) {
			if (WorldUtils.getTileEntity(sender.getWorld(), trace) instanceof TileEntityMobSpawner) {
				TileEntityMobSpawner spawner = (TileEntityMobSpawner) WorldUtils.getTileEntity(sender.getWorld(), trace);
				
				if (EntityUtils.getEntityClass(params[0], true) == null) {
					try {
						params[0] = EntityList.getStringFromID(Integer.parseInt(params[0]));
						if (params[0] == null) throw new CommandException("command.spawner.unknownEntityID", sender);
					} catch (NumberFormatException nfe) {throw new CommandException("command.spawner.unknownEntity", sender);}
				}
				
				spawner.func_145881_a().setEntityName(params[0]);
				spawner.func_145881_a().updateSpawner();
				MinecraftServer.getServer().getConfigurationManager().sendPacketToAllPlayers(spawner.getDescriptionPacket());
				sender.sendLangfileMessage("command.spawner.success");
			}
			else throw new CommandException("command.spawner.notASpawner", sender);
		}
		else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
		
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
	public boolean canSenderUse(String commanName, ICommandSender sender, String[] params) {
		return params.length > 3 || isSenderOfEntityType(sender, net.minecraft.entity.EntityLivingBase.class);
	}
}
