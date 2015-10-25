package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagList;

@Command(
	name = "inventory",
	description = "command.inventory.description",
	syntax = "command.inventory.syntax",
	example = "command.inventory.example",
	videoURL = "command.inventory.videoURL"
		)
public class CommandInventory extends ServerCommand {
	@Override
	public String getName() {
		return "inventory";
	}

	@Override
	public String getUsage() {
		return "command.inventory.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		if (params.length > 1) {
			ServerPlayerSettings settings = ServerPlayerSettings.getPlayerSettings((EntityPlayerMP) sender.getMinecraftISender());
			if (settings == null) {throw new CommandException("command.inventory.noSettingsFound", sender);}
			
			if ((params[0].equalsIgnoreCase("delete") || params[0].equalsIgnoreCase("del") || params[0].equalsIgnoreCase("remove") || params[0].equalsIgnoreCase("rem"))) {
				NBTTagList inventory = settings.inventories.get(params[1]);
				if (inventory == null) throw new CommandException("command.inventory.notFound", sender, params[1]);
				
				settings.inventories.remove(params[1]);
				settings.saveSettings();
				
				sender.sendLangfileMessage("command.inventory.removeSuccess", params[1]);
			}
			else if (params[0].equalsIgnoreCase("load")) {
				NBTTagList inventory = settings.inventories.get(params[1]);
				if (inventory == null) throw new CommandException("command.inventory.notFound", sender, params[1]);
				
				((EntityPlayerMP) sender.getMinecraftISender()).inventory.readFromNBT(inventory);
				sender.sendLangfileMessage("command.inventory.loadSuccess", params[1]);
			}
			else if (params[0].equalsIgnoreCase("save")) {
				NBTTagList inventory = new NBTTagList();
				((EntityPlayerMP) sender.getMinecraftISender()).inventory.writeToNBT(inventory);
				
				settings.inventories.put(params[1], inventory);
				settings.saveSettings();
				
				sender.sendLangfileMessage("command.inventory.saveSuccess", params[1]);
			}
			else throw new CommandException("command.inventory.invalidUsage", sender);
		}
		else throw new CommandException("command.inventory.invalidUsage", sender);
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
		return 0;
	}
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return sender instanceof EntityPlayerMP;
	}
}
