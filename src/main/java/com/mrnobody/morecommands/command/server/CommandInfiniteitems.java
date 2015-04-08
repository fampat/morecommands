package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listener;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "infiniteitems",
		description = "command.infiniteitems.description",
		example = "command.infiniteitems.example",
		syntax = "command.infiniteitems.syntax",
		videoURL = "command.infiniteitems.videoURL"
		)
public class CommandInfiniteitems extends ServerCommand {
	private class PlayerInteractionListener implements Listener<PlayerInteractEvent> {
		@Override
		public void onEvent(PlayerInteractEvent event) {
			CommandInfiniteitems.this.onInteract(event);
		}
	}
	
	private class ItemDestroyListener implements Listener<PlayerDestroyItemEvent> {
		@Override
		public void onEvent(PlayerDestroyItemEvent event) {
			CommandInfiniteitems.this.onDestroy(event);
		}
	}
	
	private PlayerInteractionListener playerInteractionListener;
	private ItemDestroyListener itemDestroyListener;
	
	public CommandInfiniteitems() {
		this.playerInteractionListener = new PlayerInteractionListener();
		this.itemDestroyListener = new ItemDestroyListener();
		EventHandler.INTERACT.getHandler().register(this.playerInteractionListener);
		EventHandler.ITEM_DESTROY.getHandler().register(this.itemDestroyListener);
	}
	
	public void onInteract(PlayerInteractEvent event) {
		if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && event.entity instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP) event.entity;
			ServerPlayerSettings settings = ServerPlayerSettings.playerSettingsMapping.get(player);
			
			if (settings == null || !settings.infiniteitems) return;
			
			if (player.inventory.mainInventory[player.inventory.currentItem] != null) {
				player.inventory.mainInventory[player.inventory.currentItem].stackSize += 1;
			}
		}
	}
	
	public void onDestroy(PlayerDestroyItemEvent event) {
		if (event.entity instanceof EntityPlayerMP && !ServerPlayerSettings.playerSettingsMapping.containsKey(event.entity) && ServerPlayerSettings.playerSettingsMapping.get(event.entity).infiniteitems) {
			EntityPlayer player = ((EntityPlayer) event.entity);
			player.inventory.mainInventory[player.inventory.currentItem] = event.original;
		}
	}
	
	@Override
	public String getCommandName() {
		return "infiniteitems";
	}

	@Override
	public String getUsage() {
		return "command.infiniteitems.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		EntityPlayerMP player = (EntityPlayerMP) sender.getMinecraftISender();
		ServerPlayerSettings ability = ServerPlayerSettings.playerSettingsMapping.get(sender.getMinecraftISender());
    		
        boolean infnite = false;
        boolean success = false;
        	
        if (params.length >= 1) {
        	if (params[0].equalsIgnoreCase("true")) {infnite = true; success = true;}
        	else if (params[0].equalsIgnoreCase("false")) {infnite = false; success = true;}
        	else if (params[0].equalsIgnoreCase("0")) {infnite = false; success = true;}
        	else if (params[0].equalsIgnoreCase("1")) {infnite = true; success = true;}
        	else if (params[0].equalsIgnoreCase("on")) {infnite = true; success = true;}
        	else if (params[0].equalsIgnoreCase("off")) {infnite = false; success = true;}
    		else if (params[0].equalsIgnoreCase("enable")) {infnite = true; success = true;}
    		else if (params[0].equalsIgnoreCase("disable")) {infnite = false; success = true;}
        	else {success = false;}
        }
        else {infnite = !ability.infiniteitems; success = true;}
        	
        if (success) ability.infiniteitems = infnite;
        	
        sender.sendLangfileMessage(success ? infnite ? "command.infiniteitems.on" : "command.infiniteitems.off" : "command.infiniteitems.failure", new Object[0]);
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}
	
	@Override
	public void unregisterFromHandler() {
		EventHandler.INTERACT.getHandler().unregister(this.playerInteractionListener);
		EventHandler.ITEM_DESTROY.getHandler().unregister(this.itemDestroyListener);
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
		return sender instanceof EntityPlayerMP;
	}
}
