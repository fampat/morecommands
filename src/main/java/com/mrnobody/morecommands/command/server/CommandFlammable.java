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
import com.mrnobody.morecommands.core.MoreCommands.ServerType;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.command.ICommandSender;
import net.minecraft.init.Blocks;

@Command(
		name = "flammable",
		description = "command.flammable.description",
		example = "command.flammable.example",
		syntax = "command.flammable.syntax",
		videoURL = "command.flammable.videoURL"
		)
public class CommandFlammable extends StandardCommand implements ServerCommandProperties {
	private final Map<Block, FireInfo> flammables = new HashMap<Block, FireInfo>();
	
	private class FireInfo {
		private int encouragement;
		private int flammibility;
		
		public FireInfo(int encouragement, int flammibility) {
			this.encouragement = encouragement;
			this.flammibility = flammibility;
		}
	}
	
	public CommandFlammable() {
		Iterator<Block> blocks = Block.blockRegistry.iterator();
		BlockFire fire = Blocks.fire;
		
		while (blocks.hasNext()) {
			Block block = blocks.next();
			this.flammables.put(block, new FireInfo(fire.getEncouragement(block), fire.getFlammability(block)));
		}
	}
	
	@Override
	public String getCommandName() {
		return "flammable";
	}

	@Override
	public String getCommandUsage() {
		return "command.flammable.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		if (params.length > 0) {
			Block block = getBlock(params[0]);
			
			if (block != null) {
				int encouragement = 0, flammibility = 0;
				boolean reset = false;
				
				if (params.length > 1) {
					if (params[1].equalsIgnoreCase("reset")) reset = true;
					else {
						try {encouragement = Integer.parseInt(params[1]);}
						catch (NumberFormatException nfe) {throw new CommandException("command.flammable.invalidArg", sender);}
					}
				}
				
				if (!reset && params.length > 2) {
					try {flammibility = Integer.parseInt(params[2]);}
					catch (NumberFormatException nfe) {throw new CommandException("command.flammable.invalidArg", sender);}
				}
				
				if (!reset) {
					Blocks.fire.setFireInfo(block, encouragement, flammibility);
					sender.sendLangfileMessage("command.flammable.success");
				}
				else {
					Blocks.fire.setFireInfo(block, this.flammables.get(block).encouragement, this.flammables.get(block).flammibility);
					sender.sendLangfileMessage("command.flammable.reset");
				}
			}
			else throw new CommandException("command.flammable.notFound", sender);
		}
		else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
		
		return null;
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
	public int getDefaultPermissionLevel(String[] args) {
		return 2;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return true;
	}
}
