package com.mrnobody.morecommands.command.server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.monster.EntityEnderman;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.util.GlobalSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "enderman",
		description = "command.enderman.description",
		example = "command.enderman.example",
		syntax = "command.enderman.syntax",
		videoURL = "command.enderman.videoURL"
		)
public class CommandEnderman extends ServerCommand {
	private final Map<Block, Boolean> carriables = new HashMap<Block, Boolean>();
	
	public CommandEnderman() {
		Iterator<Block> blocks = Block.blockRegistry.iterator();
		
		while (blocks.hasNext()) {
			Block block = blocks.next();
			this.carriables.put(block, EntityEnderman.getCarriable(block));
		}
	}
	
	@Override
	public String getName() {
		return "enderman";
	}

	@Override
	public String getUsage() {
		return "command.enderman.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
        if (params.length > 0) {
        	if (params[0].equalsIgnoreCase("enable") || params[0].equalsIgnoreCase("1")
            	|| params[0].equalsIgnoreCase("on") || params[0].equalsIgnoreCase("true")) {
        		GlobalSettings.endermanpickup = true;
            	sender.sendLangfileMessage("command.enderman.on");
            }
            else if (params[0].equalsIgnoreCase("disable") || params[0].equalsIgnoreCase("0")
            		|| params[0].equalsIgnoreCase("off") || params[0].equalsIgnoreCase("false")) {
            	GlobalSettings.endermanpickup = false;
            	sender.sendLangfileMessage("command.enderman.off");
            }
            else throw new CommandException("command.enderman.failure", sender);
        }
        else {
        	GlobalSettings.endermanpickup = !GlobalSettings.endermanpickup;
        	sender.sendLangfileMessage(GlobalSettings.endermanpickup ? "command.enderman.on" : "command.enderman.off");
        }
        
		Iterator<Block> blocks = Block.blockRegistry.iterator();
		
		while (blocks.hasNext()) {
			Block block = blocks.next();
			boolean allowPickup = GlobalSettings.endermanpickup ? this.carriables.get(block) : false;
			EntityEnderman.setCarriable(block, allowPickup);
		}
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
