package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.event.EventHandler;
import com.mrnobody.morecommands.event.ItemStackChangeSizeEvent;
import com.mrnobody.morecommands.event.Listeners.TwoEventListener;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;

@Command(
		name = "infiniteitems",
		description = "command.infiniteitems.description",
		example = "command.infiniteitems.example",
		syntax = "command.infiniteitems.syntax",
		videoURL = "command.infiniteitems.videoURL"
		)
public class CommandInfiniteitems extends StandardCommand implements ServerCommandProperties, TwoEventListener<ItemStackChangeSizeEvent, PlayerDestroyItemEvent> {
	public void onEvent1(ItemStackChangeSizeEvent event) {
		this.onChange(event);
	}
	  
	public void onEvent2(PlayerDestroyItemEvent event) {
		this.onDestroy(event);
	}
	
	public CommandInfiniteitems() {
		EventHandler.ITEMSTACK_CHANGE_SIZE.registerFirst(this);
		EventHandler.ITEM_DESTROY.registerSecond(this);
	}
	
	public void onChange(ItemStackChangeSizeEvent event) {
		if (event.entityPlayer instanceof EntityPlayerMP && getPlayerSettings((EntityPlayerMP) event.entityPlayer).infiniteitems) {
			event.newSize = event.oldSize; event.setCanceled(true);
		}
	}
	  
	public void onDestroy(PlayerDestroyItemEvent event) {
		if (event.entityPlayer instanceof EntityPlayerMP
			&& getPlayerSettings((EntityPlayerMP) event.entityPlayer).infiniteitems
			&& event.original != null && event.original.stackSize < 1) event.original.stackSize += 1;
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
		EntityPlayerMP player = getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class);
		ServerPlayerSettings settings = getPlayerSettings(player);
    	
		try {settings.infiniteitems = parseTrueFalse(params, 0, settings.infiniteitems);}
		catch (IllegalArgumentException ex) {throw new CommandException("command.infiniteitems.failure", sender);}
		
		sender.sendLangfileMessage(settings.infiniteitems ? "command.infiniteitems.on" : "command.infiniteitems.off");
		MoreCommands.INSTANCE.getPacketDispatcher().sendS12Infiniteitems(player, settings.infiniteitems);
	}
	
	@Override
	public CommandRequirement[] getRequirements() {
		return new CommandRequirement[] {CommandRequirement.MODDED_CLIENT};
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
		return isSenderOfEntityType(sender, EntityPlayerMP.class);
	}
}
