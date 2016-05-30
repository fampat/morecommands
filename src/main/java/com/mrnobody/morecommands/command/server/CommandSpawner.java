package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Entity;
import com.mrnobody.morecommands.wrapper.EntityLivingBase;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityList;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;

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
	public void execute(CommandSender sender, String[] params)throws CommandException {
		BlockPos trace;
		
		if (params.length > 3)
			trace = getCoordFromParams(sender.getMinecraftISender(), params, 1);
		else
			trace = new EntityLivingBase(getSenderAsEntity(sender.getMinecraftISender(), net.minecraft.entity.EntityLivingBase.class)).traceBlock(128D);

		if (trace != null && params.length > 0) {
			if (sender.getWorld().getTileEntity(trace) instanceof TileEntityMobSpawner) {
				TileEntityMobSpawner spawner = (TileEntityMobSpawner) sender.getWorld().getTileEntity(trace);
				
				if (Entity.getEntityClass(params[0]) == null) {
					try {
						params[0] = EntityList.CLASS_TO_NAME.get(EntityList.ID_TO_CLASS.get(Integer.parseInt(params[0])));
						if (params[0] == null) throw new CommandException("command.spawner.unknownEntityID", sender);
					} catch (NumberFormatException nfe) {throw new CommandException("command.spawner.unknownEntity", sender);}
				}
				
				spawner.getSpawnerBaseLogic().setEntityName(params[0]);
				spawner.getSpawnerBaseLogic().updateSpawner();
				sender.getServer().getPlayerList().sendPacketToAllPlayers(spawner.getUpdatePacket());
				sender.sendLangfileMessage("command.spawner.success");
			}
			else throw new CommandException("command.spawner.notASpawner", sender);
		}
		else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
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
	public int getDefaultPermissionLevel() {
		return 2;
	}

	@Override
	public boolean canSenderUse(String commanName, ICommandSender sender, String[] params) {
		return params.length > 3 || isSenderOfEntityType(sender, net.minecraft.entity.EntityLivingBase.class);
	}
}
