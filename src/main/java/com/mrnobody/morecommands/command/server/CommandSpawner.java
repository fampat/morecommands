package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntityMobSpawner;

import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Coordinate;
import com.mrnobody.morecommands.wrapper.Entity;

@Command(
		description = "command.spawner.description",
		example = "command.spawner.example",
		name = "spawner",
		syntax = "command.spawner.syntax",
		videoURL = "command.spawner.videoURL"
		)
public class CommandSpawner extends ServerCommand {
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
		Entity entity = new Entity((EntityLivingBase) sender.getMinecraftISender());
		Coordinate trace = entity.traceBlock(128.0D);

		if (trace != null && params.length > 0) {
			if (entity.getWorld().getTileEntity(trace) instanceof TileEntityMobSpawner) {
				TileEntityMobSpawner spawner = (TileEntityMobSpawner) entity.getWorld().getTileEntity(trace);
				
				if (Entity.getEntityClass(params[0]) == null) {
					try {
						params[0] = EntityList.getStringFromID(Integer.parseInt(params[0]));
						if (params[0] == null) 
							throw new CommandException("command.spawner.unknownEntityID", sender);
					} catch (NumberFormatException nfe) {throw new CommandException("command.spawner.unknownEntity", sender);}
				}
				
				spawner.func_145881_a().setEntityName(params[0]);
				spawner.func_145881_a().updateSpawner();
				sender.sendLangfileMessage("command.spawner.success");
			}
			else throw new CommandException("command.spawner.notASpawner", sender);
		}
		else throw new CommandException("command.spawner.invalidUsage", sender);
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
		return sender instanceof EntityLivingBase;
	}
}
