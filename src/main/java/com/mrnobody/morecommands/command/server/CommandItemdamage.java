package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.event.DamageItemEvent;
import com.mrnobody.morecommands.event.EventHandler;
import com.mrnobody.morecommands.event.Listeners.EventListener;
import com.mrnobody.morecommands.settings.ServerPlayerSettings;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;

@Command(
		name = "itemdamage",
		description = "command.itemdamage.description",
		example = "command.itemdamage.example",
		syntax = "command.itemdamage.syntax",
		videoURL = "command.itemdamage.videoURL"
		)
public class CommandItemdamage extends StandardCommand implements ServerCommandProperties, EventListener<DamageItemEvent> {
	public CommandItemdamage() {
		EventHandler.DAMAGE_ITEM.register(this);
	}
	
	@Override
	public String getCommandName() {
		return "itemdamage";
	}

	@Override
	public String getCommandUsage() {
		return "command.itemdamage.syntax";
	}
	
	@Override
	public void onEvent(DamageItemEvent event) {
		if (event.entity instanceof EntityPlayerMP && getPlayerSettings((EntityPlayerMP) event.entity).disableDamage.contains(event.stack.getItem()))
			event.setCanceled(true);
	}
	
	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		ServerPlayerSettings settings = getPlayerSettings(getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class));
		
		if (params.length <= 0) throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
		Item item = getItem(params[0]); boolean disable;
		
		if (item == null)
			throw new CommandException("command.itemdamage.notFound", sender);
		
		if (settings.disableDamage.contains(item)) {
			settings.disableDamage.remove(item); disable = false;
			sender.sendLangfileMessage("command.itemdamage.enabled");
		}
		else {
			settings.disableDamage.add(item); disable = true;
			sender.sendLangfileMessage("command.itemdamage.disabled");
		}
		
		MoreCommands.INSTANCE.getPacketDispatcher().sendS16ItemDamage(getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class), item, disable);
		return null;
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
	public int getDefaultPermissionLevel(String[] args) {
		return 2;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return isSenderOfEntityType(sender, EntityPlayerMP.class);
	}
}
