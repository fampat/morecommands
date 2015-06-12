package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Coordinate;
import com.mrnobody.morecommands.wrapper.Player;
import com.mrnobody.morecommands.wrapper.World;

@Command(
		name = "extinguish",
		description = "command.extinguish.description",
		example = "command.extinguish.example",
		syntax = "command.extinguish.syntax",
		videoURL = "command.extinguish.videoURL"
		)
public class CommandExtinguish extends ServerCommand {

	@Override
	public String getCommandName() {
		return "extinguish";
	}

	@Override
	public String getUsage() {
		return "command.extinguish.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Player player = new Player((EntityPlayerMP) sender.getMinecraftISender());
		
		if (params.length > 0) {
			if (params[0].equalsIgnoreCase("me")) {
				player.getMinecraftPlayer().extinguish();
			}
			else if (params[0].equalsIgnoreCase("all")) {
				int radius = 16;
				
				if (params.length > 1) {
					try {radius = Integer.parseInt(params[1]);}
					catch (NumberFormatException nfe) {throw new CommandException("command.extinguish.invalidArg", sender);}
				}
				
				this.extinguish(player.getWorld(), player.getPosition(), radius);
				player.getMinecraftPlayer().extinguish();
				sender.sendLangfileMessage("command.extinguish.extinguished");
			}
			else {
				int radius;
				
				try {radius = Integer.parseInt(params[0]);}
				catch (NumberFormatException nfe) {throw new CommandException("command.extinguish.invalidArg", sender);}
				
				this.extinguish(player.getWorld(), player.getPosition(), radius);
				sender.sendLangfileMessage("command.extinguish.extinguished");
			}
		}
		else {
			this.extinguish(player.getWorld(), player.getPosition(), 16);
			player.getMinecraftPlayer().extinguish();
			sender.sendLangfileMessage("command.extinguish.extinguished");
		}
	}
	
	private void extinguish(World world, Coordinate coord, int radius) {
		int x = coord.getBlockX();
		int y = coord.getBlockY();
		int z = coord.getBlockZ();
		
		for (int i = 0; i < radius; i++) {
			for (int j = 0; j < radius; j++) {
				if (y - j < 0 || y + j > 256) continue;
				
				for (int k = 0; k < radius; k++) {
					if (world.getBlock(x + i, y + j, z + k) == Blocks.fire) world.setBlock(x + i, y + j, z + k, Blocks.air);
					if (world.getBlock(x - i, y + j, z + k) == Blocks.fire) world.setBlock(x - i, y + j, z + k, Blocks.air);
					if (world.getBlock(x - i, y + j, z - k) == Blocks.fire) world.setBlock(x - i, y + j, z - k, Blocks.air);
					if (world.getBlock(x + i, y + j, z - k) == Blocks.fire) world.setBlock(x + i, y + j, z - k, Blocks.air);
					if (world.getBlock(x + i, y - j, z + k) == Blocks.fire) world.setBlock(x + i, y - j, z + k, Blocks.air);
					if (world.getBlock(x - i, y - j, z + k) == Blocks.fire) world.setBlock(x - i, y - j, z + k, Blocks.air);
					if (world.getBlock(x - i, y - j, z - k) == Blocks.fire) world.setBlock(x - i, y - j, z - k, Blocks.air);
					if (world.getBlock(x + i, y - j, z - k) == Blocks.fire) world.setBlock(x + i, y - j, z - k, Blocks.air);
				}
			}
		}
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
