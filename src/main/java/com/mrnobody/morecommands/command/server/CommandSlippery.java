package com.mrnobody.morecommands.command.server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.command.CommandBase.Requirement;
import com.mrnobody.morecommands.command.CommandBase.ServerType;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Coordinate;
import com.mrnobody.morecommands.wrapper.Player;
import com.mrnobody.morecommands.wrapper.World;

import cpw.mods.fml.relauncher.Side;

@Command(
		name = "slippery",
		description = "command.slippery.description",
		example = "command.slippery.example",
		syntax = "command.slippery.syntax",
		videoURL = "command.slippery.videoURL"
		)
public class CommandSlippery extends ServerCommand {
	private final Map<Block, Float> slipperies = new HashMap<Block, Float>();
	
	public CommandSlippery() {
		Iterator<Block> blocks = Block.blockRegistry.iterator();
		
		while (blocks.hasNext()) {
			Block block = blocks.next();
			this.slipperies.put(block, block.slipperiness);
		}
	}
	
	@Override
	public String getCommandName() {
		return "slippery";
	}

	@Override
	public String getUsage() {
		return "command.slippery.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Player player = sender.toPlayer();
		
		if (params.length > 1) {
			Block block = (Block) Block.blockRegistry.getObject(params[0].toLowerCase().startsWith("minecraft:") ? params[0].toLowerCase() : "minecraft:" + params[0].toLowerCase());
			
			if (block == null) {
				try {block = Block.getBlockById(Integer.parseInt(params[0]));}
				catch (NumberFormatException nfe) {}
			}
			
			if (block != null && this.slipperies.containsKey(block)) {
				float slipperiness = 0.0F;
				boolean reset = false;
				
				if (params[1].equalsIgnoreCase("reset")) reset = true;
				else {
					try {slipperiness = Float.parseFloat(params[1]);}
					catch (NumberFormatException nfe) {sender.sendLangfileMessageToPlayer("command.slippery.invalidArg", new Object[0]); return;}
				}
				
				if (!reset) {
					block.slipperiness = slipperiness;
					sender.sendLangfileMessageToPlayer("command.slippery.success", new Object[0]);
				}
				else {
					block.slipperiness = this.slipperies.get(block);
					sender.sendLangfileMessageToPlayer("command.slippery.reset", new Object[0]);
				}
			}
			else sender.sendLangfileMessageToPlayer("command.slippery.notFound", new Object[0]);
		}
		else sender.sendLangfileMessageToPlayer("command.slippery.invalidUsage", new Object[0]);
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
}
