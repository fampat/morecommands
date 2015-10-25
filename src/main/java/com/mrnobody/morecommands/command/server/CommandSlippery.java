package com.mrnobody.morecommands.command.server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.block.Block;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.fml.common.registry.GameRegistry;

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
	public String getName() {
		return "slippery";
	}

	@Override
	public String getUsage() {
		return "command.slippery.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		if (params.length > 1) {
			String modid = params[0].split(":").length > 1 ? params[0].split(":")[0] : "minecraft";
			String name = params[0].split(":").length > 1 ? params[0].split(":")[1] : params[0];
			Block block = GameRegistry.findBlock(modid, name);
			
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
					catch (NumberFormatException nfe) {throw new CommandException("command.slippery.invalidArg", sender);}
				}
				
				if (!reset) {
					block.slipperiness = slipperiness;
					sender.sendLangfileMessage("command.slippery.success");
				}
				else {
					block.slipperiness = this.slipperies.get(block);
					sender.sendLangfileMessage("command.slippery.reset");
				}
			}
			else throw new CommandException("command.slippery.notFound", sender);
		}
		else throw new CommandException("command.slippery.invalidUsage", sender);
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
		return true;
	}
}
