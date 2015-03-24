package com.mrnobody.morecommands.command.server;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.EntityList;
import net.minecraft.tileentity.TileEntityMobSpawner;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Coordinate;
import com.mrnobody.morecommands.wrapper.Entity;
import com.mrnobody.morecommands.wrapper.Player;

@Command(
		description = "command.spawner.description",
		example = "command.spawner.example",
		name = "spawner",
		syntax = "command.spawner.syntax",
		videoURL = "command.spawner.videoURL"
		)
public class CommandSpawner extends ServerCommand {

	@Override
	public void unregisterFromHandler() {}

	@Override
	public String getCommandName() {
		return "spawner";
	}

	@Override
	public String getUsage() {
		return "command.spawner.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params)throws CommandException {
		Player player = sender.toPlayer();
		Coordinate trace = player.trace(128.0D);

		if (trace != null && params.length > 0) {
			if (player.getWorld().getTileEntity(trace) instanceof TileEntityMobSpawner) {
				TileEntityMobSpawner spawner = (TileEntityMobSpawner) player.getWorld().getTileEntity(trace);
				
				if (Entity.getEntityClass(params[0]) == null) {
					try {
						params[0] = EntityList.getStringFromID(Integer.parseInt(params[0]));
						if (params[0] == null) {
							sender.sendLangfileMessageToPlayer("command.spawner.unknownEntityID", new Object[0]);
							return;
						}
					} catch (NumberFormatException nfe) {sender.sendLangfileMessageToPlayer("command.spawner.unknownEntity", new Object[0]); return;}
				}
				
				spawner.func_145881_a().setEntityName(params[0]);
				spawner.func_145881_a().updateSpawner();
				sender.sendLangfileMessageToPlayer("command.spawner.success", new Object[0]);
			}
			else sender.sendLangfileMessageToPlayer("command.spawner.notASpawner", new Object[0]);
		}
		else sender.sendLangfileMessageToPlayer("command.spawner.invalidUsage", new Object[0]);
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

}
