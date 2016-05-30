package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Entity;
import com.mrnobody.morecommands.wrapper.World;

import net.minecraft.command.ICommandSender;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

@Command(
		name = "extinguish",
		description = "command.extinguish.description",
		example = "command.extinguish.example",
		syntax = "command.extinguish.syntax",
		videoURL = "command.extinguish.videoURL"
		)
public class CommandExtinguish extends StandardCommand implements ServerCommandProperties {

	@Override
	public String getCommandName() {
		return "extinguish";
	}

	@Override
	public String getCommandUsage() {
		return "command.extinguish.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Entity entity = !isSenderOfEntityType(sender.getMinecraftISender(), net.minecraft.entity.Entity.class) ? null : 
		new Entity(getSenderAsEntity(sender.getMinecraftISender(), net.minecraft.entity.Entity.class));
		
		if (params.length > 0) {
			if (params[0].equalsIgnoreCase("me")) {
				if (entity != null) entity.getMinecraftEntity().extinguish();
			}
			else if (params[0].equalsIgnoreCase("all")) {
				int radius = 16;
				
				if (params.length > 1) {
					try {radius = Integer.parseInt(params[1]);}
					catch (NumberFormatException nfe) {throw new CommandException("command.extinguish.invalidArg", sender);}
				}
				
				this.extinguish(sender.getWorld(), sender.getPosition(), radius);
				if (entity != null) entity.getMinecraftEntity().extinguish();
				sender.sendLangfileMessage("command.extinguish.extinguished");
			}
			else {
				int radius;
				
				try {radius = Integer.parseInt(params[0]);}
				catch (NumberFormatException nfe) {throw new CommandException("command.extinguish.invalidArg", sender);}
				
				this.extinguish(sender.getWorld(), sender.getPosition(), radius);
				sender.sendLangfileMessage("command.extinguish.extinguished");
			}
		}
		else {
			this.extinguish(sender.getWorld(), sender.getPosition(), 16);
			if (entity != null) entity.getMinecraftEntity().extinguish();
			sender.sendLangfileMessage("command.extinguish.extinguished");
		}
	}
	
	private void extinguish(World world, BlockPos coord, int radius) {
		int x = coord.getX();
		int y = coord.getY();
		int z = coord.getZ();
		
		for (int i = 0; i < radius; i++) {
			for (int j = 0; j < radius; j++) {
				if (y - j < 0 || y + j > 256) continue;
				
				for (int k = 0; k < radius; k++) {
					if (world.getBlock(x + i, y + j, z + k) == Blocks.FIRE) world.setBlock(new BlockPos(x + i, y + j, z + k), Blocks.AIR);
					if (world.getBlock(x - i, y + j, z + k) == Blocks.FIRE) world.setBlock(new BlockPos(x - i, y + j, z + k), Blocks.AIR);
					if (world.getBlock(x - i, y + j, z - k) == Blocks.FIRE) world.setBlock(new BlockPos(x - i, y + j, z - k), Blocks.AIR);
					if (world.getBlock(x + i, y + j, z - k) == Blocks.FIRE) world.setBlock(new BlockPos(x + i, y + j, z - k), Blocks.AIR);
					if (world.getBlock(x + i, y - j, z + k) == Blocks.FIRE) world.setBlock(new BlockPos(x + i, y - j, z + k), Blocks.AIR);
					if (world.getBlock(x - i, y - j, z + k) == Blocks.FIRE) world.setBlock(new BlockPos(x - i, y - j, z + k), Blocks.AIR);
					if (world.getBlock(x - i, y - j, z - k) == Blocks.FIRE) world.setBlock(new BlockPos(x - i, y - j, z - k), Blocks.AIR);
					if (world.getBlock(x + i, y - j, z - k) == Blocks.FIRE) world.setBlock(new BlockPos(x + i, y - j, z - k), Blocks.AIR);
				}
			}
		}
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
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return params.length > 0 ? params[0].equalsIgnoreCase("me") ? isSenderOfEntityType(sender, net.minecraft.entity.Entity.class) : true : true;
	}
}
