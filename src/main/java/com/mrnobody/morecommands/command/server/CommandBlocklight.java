package com.mrnobody.morecommands.command.server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.network.PacketDispatcher.BlockUpdateType;

import net.minecraft.block.Block;
import net.minecraft.command.ICommandSender;

@Command(
		name="blocklight",
		description="command.blocklight.description",
		example="command.blocklight.example",
		syntax="command.blocklight.syntax",
		videoURL="command.blocklight.videoURL"
		)
public class CommandBlocklight extends StandardCommand implements ServerCommandProperties {
	private final Map<Block, Integer> lightLevels = new HashMap<Block, Integer>();
	private final Map<Block, Integer> lightOpacities = new HashMap<Block, Integer>();
	
	public CommandBlocklight() {
		Iterator<Block> blocks = Block.REGISTRY.iterator();
		
		while (blocks.hasNext()) {
			Block block = (Block)blocks.next();
			this.lightLevels.put(block, block.getLightValue(block.getDefaultState()));
			this.lightOpacities.put(block, block.getLightOpacity(block.getDefaultState()));
		}
	}
  
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return true;
	}
  
	@Override
	public String getCommandName() {
		return "blocklight";
	}
  
	@Override
	public String getCommandUsage() {
		return "command.blocklight.syntax";
	}
  
	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		if (params.length > 2) {
			Block block = getBlock(params[1]);
			BlockUpdateType update;
			int value;
			boolean reset = false, opacity = false;
			
			if (params[0].equalsIgnoreCase("level")) {
				update = BlockUpdateType.LIGHT_LEVEL;
				
				if (params[2].equalsIgnoreCase("reset")) {
					value = Float.floatToIntBits(this.lightLevels.get(block) / 15F);
					reset = true;
				}
				else {
					try {value = Float.floatToIntBits(Float.parseFloat(params[2]));}
					catch (NumberFormatException nfe) {throw new CommandException("command.blocklight.invalidArg", sender);}
				}
			}
			else if (params[0].equalsIgnoreCase("opacity")) {
				update = BlockUpdateType.LIGHT_OPACITY;
				opacity = true;
				
				if (params[2].equalsIgnoreCase("reset")) {
					value = this.lightOpacities.get(block);
					reset = true;
				}
				else {
					try {value = Integer.parseInt(params[2]);}
					catch (NumberFormatException nfe) {throw new CommandException("command.blocklight.invalidArg", sender);}
				}
			}
			else throw new CommandException("command.blocklight.invalidArg", sender);
			
			if (block != null) {
				update.update(block, value);
				MoreCommands.INSTANCE.getPacketDispatcher().sendS15UpdateBlock(block, update, value);
				
				if (!reset) sender.sendLangfileMessage("command.blocklight." + (opacity ? "opacitySuccess" : "levelSuccess"));
				else sender.sendLangfileMessage("command.blocklight." + (opacity ? "opcacityReset" : "levelReset"));
			}
			else throw new CommandException("command.blocklight.notFound", sender);
		}
		else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
		
		return null;
	}
  
	@Override
	public CommandRequirement[] getRequirements() {
		return new CommandRequirement[] {CommandRequirement.OTHER_SIDE_MUST_BE_MODDED};
	}
  
	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
  
	@Override
	public int getDefaultPermissionLevel(String[] args) {
		return 2;
	}
}
