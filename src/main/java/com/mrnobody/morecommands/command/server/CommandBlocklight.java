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
		name="blocklight",
		description="command.blocklight.description",
		example="command.blocklight.example",
		syntax="command.blocklight.syntax",
		videoURL="command.blocklight.videoURL"
		)
public class CommandBlocklight extends ServerCommand {
	private final Map<Block, Integer> lightLevels = new HashMap();
  
	public CommandBlocklight() {
		Iterator<Block> blocks = Block.blockRegistry.iterator();
		
		while (blocks.hasNext()) {
			Block block = (Block)blocks.next();
			this.lightLevels.put(block, Integer.valueOf(block.getLightValue()));
		}
	}
	
	public boolean canSenderUse(ICommandSender sender) {
		return true;
	}
  
	@Override
	public String getName() {
		return "blocklight";
	}
  
	@Override
	public String getUsage() {
		return "command.blocklight.syntax";
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
			
			if (block != null) {
				float level = 0.0F;
				boolean reset = false;
				
				if (params[1].equalsIgnoreCase("reset")) reset = true;
				else {
					try {level = Float.parseFloat(params[1]);}
					catch (NumberFormatException nfe) {throw new CommandException("command.blocklight.invalidArg", sender);}
				}
	        
				if (!reset) {
					block.setLightLevel(level / 15.0F);
					sender.sendLangfileMessage("command.blocklight.success");
				}
				else {
					block.setLightLevel(((float) this.lightLevels.get(block)) / 15.0F);
					sender.sendLangfileMessage("command.blocklight.reset");
				}
			}
			else throw new CommandException("command.blocklight.notFound", sender);
		}
		else throw new CommandException("command.blocklight.invalidUsage", sender);
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
