package com.mrnobody.morecommands.command.server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.block.Block;
import net.minecraft.command.ICommandSender;

@Command(
		name = "slippery",
		description = "command.slippery.description",
		example = "command.slippery.example",
		syntax = "command.slippery.syntax",
		videoURL = "command.slippery.videoURL"
		)
public class CommandSlippery extends StandardCommand implements ServerCommandProperties {
	private final Map<Block, Float> slipperies = new HashMap<Block, Float>();
	
	public CommandSlippery() {
		Iterator<Block> blocks = Block.REGISTRY.iterator();
		
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
	public String getCommandUsage() {
		return "command.slippery.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		if (params.length > 1) {
			Block block = getBlock(params[0]);
			
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
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return true;
	}
}
