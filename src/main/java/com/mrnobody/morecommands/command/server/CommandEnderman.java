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
	public String getCommandName() {
		return "enderman";
	}

	@Override
	public String getUsage() {
		return "command.enderman.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
    	boolean allowPickup = false;
    	boolean success = false;
    	
    	if (params.length >= 1) {
    		if (params[0].equalsIgnoreCase("true")) {allowPickup = true; success = true;}
    		else if (params[0].equalsIgnoreCase("false")) {allowPickup = false; success = true;}
    		else if (params[0].equalsIgnoreCase("0")) {allowPickup = false; success = true;}
    		else if (params[0].equalsIgnoreCase("1")) {allowPickup = true; success = true;}
    		else if (params[0].equalsIgnoreCase("on")) {allowPickup = true; success = true;}
    		else if (params[0].equalsIgnoreCase("off")) {allowPickup = false; success = true;}
    		else if (params[0].equalsIgnoreCase("enable")) {allowPickup = true; success = true;}
    		else if (params[0].equalsIgnoreCase("disable")) {allowPickup = false; success = true;}
    		else {success = false;}
    	}
    	else {allowPickup = !GlobalSettings.endermanpickup; success = true;}
    	
    	if (success) {GlobalSettings.endermanpickup = allowPickup; updatePickup();}
    	
    	sender.sendLangfileMessage(success ? allowPickup ? "command.enderman.on" : "command.enderman.off" : "command.enderman.failure", new Object[0]);
	}
	
	private void updatePickup() {
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
