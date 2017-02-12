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
import com.mrnobody.morecommands.event.EventHandler;
import com.mrnobody.morecommands.event.Listeners.EventListener;
import com.mrnobody.morecommands.settings.MoreCommandsConfig;

import net.minecraft.block.Block;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;

@Command(
		name = "enderman",
		description = "command.enderman.description",
		example = "command.enderman.example",
		syntax = "command.enderman.syntax",
		videoURL = "command.enderman.videoURL"
		)
public class CommandEnderman extends StandardCommand implements ServerCommandProperties, EventListener<EnderTeleportEvent> {
	private final Map<Block, Boolean> carriables = new HashMap<Block, Boolean>();
	private boolean endermanteleport = true, endermanpickup = true;
	
	public CommandEnderman() {
		Iterator<Block> blocks = Block.blockRegistry.iterator();
		
		while (blocks.hasNext()) {
			Block block = blocks.next();
			this.carriables.put(block, EntityEnderman.getCarriable(block));
		}
		
		EventHandler.ENDER_TELEPORT.register(this);
	}
	
	@Override
	public void onEvent(EnderTeleportEvent event) {
		if (!this.endermanteleport) event.setCanceled(true);
	}
	
	@Override
	public String getCommandName() {
		return "enderman";
	}

	@Override
	public String getCommandUsage() {
		return "command.enderman.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		if (params.length <= 0)
			throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
		
		if (params[0].equalsIgnoreCase("pickup")) {
			try {this.endermanpickup = parseTrueFalse(params, 1, !this.endermanpickup);}
			catch (IllegalArgumentException ex) {throw new CommandException("command.enderman.failure", sender);}
			
			sender.sendLangfileMessage(this.endermanpickup ? "command.enderman.pickup.on" : "command.enderman.pickup.off");
	        
			Iterator<Block> blocks = Block.blockRegistry.iterator();
			
			while (blocks.hasNext()) {
				Block block = blocks.next();
				boolean allowPickup = this.endermanpickup ? this.carriables.get(block) : false;
				EntityEnderman.setCarriable(block, allowPickup);
			}
		}
		else if (params[0].equalsIgnoreCase("teleport")) {
			try {this.endermanteleport = parseTrueFalse(params, 1, !this.endermanteleport);}
			catch (IllegalArgumentException ex) {throw new CommandException("command.enderman.failure", sender);}
			
			sender.sendLangfileMessage(this.endermanteleport ? "command.enderman.teleport.on" : "command.enderman.teleport.off");
		}
		
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
