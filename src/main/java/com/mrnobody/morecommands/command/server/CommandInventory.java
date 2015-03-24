package com.mrnobody.morecommands.command.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.client.ClientCommandHandler;

import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.util.Reference;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.util.Settings;

@Command(
	name = "inventory",
	description = "command.inventory.description",
	syntax = "command.inventory.syntax",
	example = "command.inventory.example",
	videoURL = "command.inventory.videoURL"
		)
public class CommandInventory extends ServerCommand {
	@Override
	public String getCommandName() {
		return "inventory";
	}

	@Override
	public String getUsage() {
		return "command.inventory.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		if (params.length > 1) {
			ServerPlayerSettings settings = ServerPlayerSettings.playerSettingsMapping.get(sender.getMinecraftISender());
			if (settings == null) {sender.sendLangfileMessageToPlayer("command.inventory.notSettingsFound", new Object[0]); return;}
			
			if ((params[0].equalsIgnoreCase("delete") || params[0].equalsIgnoreCase("del") || params[0].equalsIgnoreCase("remove") || params[0].equalsIgnoreCase("rem"))) {
				NBTTagList inventory = settings.inventories.get(params[1]);
				if (inventory == null) {sender.sendLangfileMessageToPlayer("command.inventory.notFound", new Object[] {params[1]}); return;}
				
				settings.inventories.remove(params[1]);
				settings.saveSettings();
				
				sender.sendLangfileMessageToPlayer("command.inventory.removeSuccess", params[1]);
			}
			else if (params[0].equalsIgnoreCase("load")) {
				NBTTagList inventory = settings.inventories.get(params[1]);
				if (inventory == null) {sender.sendLangfileMessageToPlayer("command.inventory.notFound", new Object[] {params[1]}); return;}
				
				((EntityPlayerMP) sender.getMinecraftISender()).inventory.readFromNBT(inventory);
				sender.sendLangfileMessageToPlayer("command.inventory.loadSuccess", new Object[] {params[1]});
			}
			else if (params[0].equalsIgnoreCase("save")) {
				NBTTagList inventory = new NBTTagList();
				((EntityPlayerMP) sender.getMinecraftISender()).inventory.writeToNBT(inventory);
				
				settings.inventories.put(params[1], inventory);
				settings.saveSettings();
				
				sender.sendLangfileMessageToPlayer("command.inventory.saveSuccess", new Object[] {params[1]});
			}
			else sender.sendLangfileMessageToPlayer("command.inventory.invalidUsage", new Object[0]);
		}
		else sender.sendLangfileMessageToPlayer("command.inventory.invalidUsage", new Object[0]);
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
	public void unregisterFromHandler() {}
}
