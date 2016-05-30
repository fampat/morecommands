package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.event.EventHandler;
import com.mrnobody.morecommands.event.Listeners.EventListener;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;

@Command(
		name = "pickup",
		description = "command.pickup.description",
		example = "command.pickup.example",
		syntax = "command.pickup.syntax",
		videoURL = "command.pickup.videoURL"
		)
public class CommandPickup extends StandardCommand implements ServerCommandProperties, EventListener<EntityItemPickupEvent> {
	public CommandPickup() {
		EventHandler.PICKUP.register(this);
	}

	@Override
	public String getCommandName() {
		return "pickup";
	}

	@Override
	public String getCommandUsage() {
		return "command.pickup.syntax";
	}
	
	@Override
	public void onEvent(EntityItemPickupEvent event) {
		if (event.getEntityPlayer() instanceof EntityPlayerMP) {
			if (getPlayerSettings((EntityPlayerMP) event.getEntityPlayer()).disablePickups.contains(event.getItem().getEntityItem().getItem()))
				event.setCanceled(true);
		}
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		ServerPlayerSettings settings = getPlayerSettings(getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class));
    	
		if (params.length > 0) {
			Item item = getItem(params[0]);
				
			if (item == null) 
				throw new CommandException("command.pickup.notFound", sender);
			
			if (settings.disablePickups.contains(item)) {
				settings.disablePickups.remove(item);
				sender.sendLangfileMessage("command.pickup.removed");
			}
			else {
				settings.disablePickups.add(item);
				sender.sendLangfileMessage("command.pickup.added");
			}
		}
		else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
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
	public int getDefaultPermissionLevel() {
		return 2;
	}

	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return isSenderOfEntityType(sender, EntityPlayerMP.class);
	}
}
