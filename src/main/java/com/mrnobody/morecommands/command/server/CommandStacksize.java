package com.mrnobody.morecommands.command.server;

import java.util.Iterator;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.patch.InventoryPlayer;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "stacksize",
		description = "command.stacksize.description",
		example = "command.stacksize.example",
		syntax = "command.stacksize.syntax",
		videoURL = "command.stacksize.videoURL"
		)
public class CommandStacksize extends ServerCommand/* implements Listener<GuiOpenEvent>*/ {
	/*public CommandStacksize() {
		EventHandler.GUIOPEN.getHandler().register(this);
	}*/
	
	@Override
	public String getName() {
		return "stacksize";
	}

	@Override
	public String getUsage() {
		return "command.stacksize.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		sender.sendStringMessage("This command is currently not working properly, please wait for a fix");
		return;
		
		/*if (!(((EntityPlayerMP) sender.getMinecraftISender()).inventory instanceof InventoryPlayer)) {
			sender.sendLangfileMessage("command.stacksize.invPlayerNotPatched", new Object[0]);
			return;
		}
		
		if (params.length > 0) {
			Item item;
			
			if (((params[0].equalsIgnoreCase("get") || params[0].equalsIgnoreCase("reset")) && params.length > 1) || (params[0].equalsIgnoreCase("set") && params.length > 2)) {
				item = (Item)Item.itemRegistry.getObject(params[1].toLowerCase().startsWith("minecraft:") ? params[1].toLowerCase() : "minecraft:" + params[1].toLowerCase());
				
				if (item == null) {
					try {item = Item.getItemById(Integer.parseInt(params[1]));}
					catch (NumberFormatException e) {sender.sendLangfileMessage("command.stacksize.unknownItem", new Object[0]); return;}
				}
			}
			else {
				EntityPlayerMP player = (EntityPlayerMP) sender.getMinecraftISender();
				
				if (player.inventory.mainInventory[player.inventory.currentItem] != null)
					item = player.inventory.mainInventory[player.inventory.currentItem].getItem();
				else item = null;
			}
			
			if (item == null) {sender.sendLangfileMessage("command.stacksize.noSelection", new Object[0]); return;}
			
			if (params[0].equalsIgnoreCase("get")) {
				sender.sendStringMessage("The current stack size for the item '" + (new ItemStack(item)).getDisplayName() + "' is " + item.getItemStackLimit());
			}
			else if (params[0].equalsIgnoreCase("reset")) {
				if (InventoryPlayer.stackSizes.containsKey(item)) {
					int stacksize = InventoryPlayer.stackSizes.get(item);
					
					item.setMaxStackSize(stacksize);
					this.updateMaxInvStacksize((InventoryPlayer) ((EntityPlayerMP) sender.getMinecraftISender()).inventory);
					
					sender.sendLangfileMessage("command.stacksize.resetted", new Object[0]);
				}
				else sender.sendLangfileMessage("command.stacksize.resetError", new Object[0]);
			}
			else if (params[0].equalsIgnoreCase("set") && params.length > 1) {
				int index = params.length > 2 ? 2 : 1;
				int stacksize;
				
				try {stacksize = Integer.parseInt(params[index]);}
				catch (NumberFormatException e) {sender.sendLangfileMessage("command.stacksize.invalidArg", new Object[0]); return;}
				
				item.setMaxStackSize(stacksize);
				this.updateMaxInvStacksize((InventoryPlayer) ((EntityPlayerMP) sender.getMinecraftISender()).inventory);
				
				sender.sendLangfileMessage("command.stacksize.success", new Object[0]);
			}
			else sender.sendLangfileMessage("command.stacksize.invalidUsage", new Object[0]);
		}
		else sender.sendLangfileMessage("command.stacksize.invalidUsage", new Object[0]);*/
	}
	
	private void updateMaxInvStacksize(InventoryPlayer inventory) {
		int stacksize = 0;
		Iterator<Item> items = Item.itemRegistry.iterator();
		
		while (items.hasNext()) {
			Item item = items.next();
			if (item.getItemStackLimit() > stacksize) stacksize = item.getItemStackLimit();
		}
		
		inventory.setStacksize(stacksize);
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[] {Requirement.PATCH_ENTITYPLAYERSP, Requirement.PATCH_ENTITYPLAYERMP};
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

	/*@Override
	public void onEvent(GuiOpenEvent event) {
		if (event.gui instanceof GuiInventory && !(event.gui instanceof com.mrnobody.morecommands.patch.GuiInventory)) {
			event.setCanceled(true);
			Minecraft.getMinecraft().displayGuiScreen(new com.mrnobody.morecommands.patch.GuiInventory(Minecraft.getMinecraft().thePlayer));
		}
	}*/
}
