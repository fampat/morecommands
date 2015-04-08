package com.mrnobody.morecommands.command.server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.command.ICommandSender;
import net.minecraft.init.Blocks;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "flammable",
		description = "command.flammable.description",
		example = "command.flammable.example",
		syntax = "command.flammable.syntax",
		videoURL = "command.flammable.videoURL"
		)
public class CommandFlammable extends ServerCommand {
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
	public String getUsage() {
		return "command.flammable.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		if (params.length > 0) {
			Block block = (Block) Block.blockRegistry.getObject(params[0].toLowerCase().startsWith("minecraft:") ? params[0].toLowerCase() : "minecraft:" + params[0].toLowerCase());
			
			if (block == null) {
				try {block = Block.getBlockById(Integer.parseInt(params[0]));}
				catch (NumberFormatException nfe) {}
			}
			
			if (block != null) {
				int encouragement = 0, flammibility = 0;
				boolean reset = false;
				
				if (params.length > 1) {
					if (params[1].equalsIgnoreCase("reset")) reset = true;
					else {
						try {encouragement = Integer.parseInt(params[1]);}
						catch (NumberFormatException nfe) {sender.sendLangfileMessage("command.flammable.invalidArg", new Object[0]); return;}
					}
				}
				
				if (!reset && params.length > 2) {
					try {flammibility = Integer.parseInt(params[2]);}
					catch (NumberFormatException nfe) {sender.sendLangfileMessage("command.flammable.invalidArg", new Object[0]); return;}
				}
				
				if (!reset) {
					Blocks.fire.setFireInfo(block, encouragement, flammibility);
					sender.sendLangfileMessage("command.flammable.success", new Object[0]);
				}
				else {
					Blocks.fire.setFireInfo(block, this.flammables.get(block).encouragement, this.flammables.get(block).flammibility);
					sender.sendLangfileMessage("command.flammable.reset", new Object[0]);
				}
			}
			else sender.sendLangfileMessage("command.flammable.notFound", new Object[0]);
		}
		else sender.sendLangfileMessage("command.flammable.invalidUsage", new Object[0]);
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
		return true;
	}
}
