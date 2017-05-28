package com.mrnobody.morecommands.command.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import com.mrnobody.morecommands.command.ClientCommandProperties;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.patch.PatchList;
import com.mrnobody.morecommands.patch.PatchManager;
import com.mrnobody.morecommands.util.ObfuscatedNames.ObfuscatedField;
import com.mrnobody.morecommands.util.ObfuscatedNames.ObfuscatedMethod;
import com.mrnobody.morecommands.util.ReflectionHelper;

import net.minecraft.client.LoadingScreenRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldSettings.GameType;
import net.minecraft.world.WorldType;

@Command(
		name = "world",
		description = "command.world.description",
		example = "command.world.example",
		syntax = "command.world.syntax",
		videoURL = "command.world.videoURL"
		)
public class CommandWorld extends StandardCommand implements ClientCommandProperties {
	private final Method saveWorlds = ReflectionHelper.getMethod(ObfuscatedMethod.MinecraftServer_saveAllWorlds);
	private final Field worldInfo = ReflectionHelper.getField(ObfuscatedField.World_worldInfo);
	
	@Override
	public String getCommandName() {
		return "world";
	}
	
	@Override
	public String getCommandUsage() {
		return "command.world.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		if (params.length > 0) {
			if (params[0].equalsIgnoreCase("load") && params.length > 1) {
				String world = "";
				
				for (int i = 1; i < params.length; i++) world += " " + params[i];
				
				if (Minecraft.getMinecraft().getSaveLoader().canLoadWorld(world.trim()))
					Minecraft.getMinecraft().launchIntegratedServer(world.trim(), world.trim(), new WorldSettings(new Random().nextLong(), GameType.SURVIVAL, true, false, WorldType.DEFAULT));
				else throw new CommandException("command.world.notLoadable", sender, world.trim());
			}
			else if (params[0].equalsIgnoreCase("backup") || params[0].equalsIgnoreCase("save")) {
				LoadingScreenRenderer l = new LoadingScreenRenderer(Minecraft.getMinecraft());
				l.displaySavingString("Please wait... Saving level");
				ReflectionHelper.invoke(ObfuscatedMethod.MinecraftServer_saveAllWorlds, this.saveWorlds, sender.getServer(), Boolean.FALSE);
				
				if (params[0].equalsIgnoreCase("backup")) {
					l = new LoadingScreenRenderer(Minecraft.getMinecraft());
					l.displaySavingString("Please wait... World is being backed up");
					SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");
					String time = format.format(new Date());
					copyDirectory(new File(Minecraft.getMinecraft().mcDataDir, "saves/" + sender.getServer().getFolderName()), new File(Minecraft.getMinecraft().mcDataDir, "backup/" + sender.getServer().getFolderName() + "/" + time), l);
				}
			}
			else if (params[0].equalsIgnoreCase("exit")) {
				if (sender.getServer() != null) sender.getServer().stopServer();
				Minecraft.getMinecraft().loadWorld(null);
				Minecraft.getMinecraft().displayGuiScreen(new GuiMainMenu());
			}
			else if (params[0].equalsIgnoreCase("new") && params.length > 1) {
				long seed = 0;
				
				if (params.length > 2) {
					try {seed = Long.parseLong(params[2]);}
					catch (Exception ex) {sender.sendLangfileMessage("command.world.NAN", TextFormatting.RED); seed = (new Random()).nextLong();}
				}
				else seed = (new Random()).nextLong();
				
				File parent = new File(Minecraft.getMinecraft().mcDataDir, "saves");
				String name = params[1];
				File child = new File(parent, name);
				
				if (child.exists())
					throw new CommandException("command.world.cantcreate", sender);
				
				Minecraft.getMinecraft().launchIntegratedServer(name, name, new WorldSettings(seed, GameType.SURVIVAL, true, false, WorldType.DEFAULT));
			}
			else if (params[0].equalsIgnoreCase("list")) {
				File parent = new File(Minecraft.getMinecraft().mcDataDir, "saves");
				File list[] = parent.listFiles();
				String saves = null;
				
				for (int i = 0; i < list.length; i++) {
					if (list[i].isDirectory()) {
						if (saves == null) saves = list[i].getName();
						else saves += ", " + list[i].getName();
					}
				}
				sender.sendLangfileMessage("command.world.saves");
				sender.sendStringMessage(saves);
			}
			else if (params[0].equalsIgnoreCase("seed") || params[0].equals("name")) {
				if (!PatchManager.instance().getGlobalAppliedPatches().wasPatchSuccessfullyApplied(PatchList.SERVER_MODDED))
					throw new CommandException("command.world.serverNotModded", sender);
				
				Minecraft.getMinecraft().thePlayer.sendChatMessage("/world " + rejoinParams(params));
			}
			else throw new CommandException("command.world.invalidArg", sender);
		}
		else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
		
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
		return 0;
	}

	@Override
	public boolean registerIfServerModded() {
		return MoreCommands.getServerType() == ServerType.INTEGRATED;
	}
	
	private boolean copyDirectory(File from, File to, LoadingScreenRenderer ipg) {
		if (!from.isDirectory()) return false;
		if (!to.exists()) to.mkdirs();
		if (!to.isDirectory()) return false;
		if (ipg != null) ipg.displaySavingString("Moving chunks");
		      
		try {
			File list[] = from.listFiles();
			
			for (int i = 0; i < list.length; i++) {
				if (list[i].isDirectory()) {
					copyDirectory(list[i], new File(to, list[i].getName()), null);
				} else if (list[i].isFile()) {
					copyFile(list[i], new File(to, list[i].getName()));
				}
				if (ipg != null) {
					ipg.setLoadingProgress((i * 100) / list.length);
				}
			}
		} catch (Exception e) {return false;}
		return true;
	}
	
	@SuppressWarnings("resource")
	private boolean copyFile(File sourceFile, File destFile) {
		if (!destFile.exists()) {
			try {
				destFile.createNewFile();
			} catch (IOException e) {
				return false;
			}
		}

		FileChannel source = null;
		FileChannel destination = null;
		      
		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} catch (Exception e) {
			return false;
		} finally {
			try {
				if (source != null) source.close();
				if (destination != null) destination.close();
			} catch (Exception e) {}
		}
		return true;
	}	  
}
