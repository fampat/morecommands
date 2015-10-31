package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listeners.EventListener;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import cpw.mods.fml.common.registry.GameRegistry;
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
public class CommandPickup extends ServerCommand implements EventListener<EntityItemPickupEvent> {
	public CommandPickup() {
		EventHandler.PICKUP.getHandler().register(this);
	}

	@Override
	public String getCommandName() {
		return "pickup";
	}

	@Override
	public String getUsage() {
		return "command.pickup.syntax";
	}
	
	@Override
	public void onEvent(EntityItemPickupEvent event) {
		if (event.entityPlayer instanceof EntityPlayerMP) {
			if (ServerPlayerSettings.getPlayerSettings((EntityPlayerMP) event.entityPlayer).disablePickups.contains(event.item.getEntityItem().getItem()))
				event.setCanceled(true);
		}
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		ServerPlayerSettings settings = ServerPlayerSettings.getPlayerSettings((EntityPlayerMP) sender.getMinecraftISender());
    	
		if (params.length > 0) {
			String modid = params[0].split(":").length > 1 ? params[0].split(":")[0] : "minecraft";
			String name = params[0].split(":").length > 1 ? params[0].split(":")[1] : params[0];
			Item item = GameRegistry.findItem(modid, name);
			
			if (item == null) {
				try {item = Item.getItemById(Integer.parseInt(params[0]));}
				catch (NumberFormatException e) {}
			}
				
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
		else throw new CommandException("command.pickup.invalidUsage", sender);
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
		return sender instanceof EntityPlayerMP;
	}
}
