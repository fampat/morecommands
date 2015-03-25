package com.mrnobody.morecommands.command.server;

import java.lang.reflect.Method;
import java.util.Random;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldType;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.util.ReflectionHelper;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "world",
		description = "command.world.description",
		example = "command.world.example",
		syntax = "command.world.syntax",
		videoURL = "command.world.videoURL"
		)
public class CommandWorld extends ServerCommand {

	@Override
	public void unregisterFromHandler() {}

	@Override
	public String getCommandName() {
		return "world";
	}

	@Override
	public String getUsage() {
		return "command.world.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		if (params.length > 0) {
			if (MoreCommands.getMoreCommands().getRunningServer() == ServerType.DEDICATED) {
				DedicatedServer server = (DedicatedServer) MinecraftServer.getServer();
				
				if (params[0].equalsIgnoreCase("load") && params.length > 1) {
					sender.sendStringMessageToPlayer("Currently only working in singleplayer");
					/*String world = "";
					
					for (int i = 1; i < params.length; i++) world += " " + params[i];
					
					if (MinecraftServer.getServer().getActiveAnvilConverter().getSaveLoader(world, true).getWorldDirectory().exists()) {
						Method loadWorld = ReflectionHelper.getMethod(MinecraftServer.class, "loadAllWorlds", String.class, String.class, long.class, WorldType.class, String.class);
						
						if (loadWorld != null) {
							try {
								String genSettings = server.getStringProperty("generator-settings", "");
								loadWorld.invoke(server, world.trim(), world.trim(), new Random().nextLong(), WorldType.DEFAULT, genSettings);
							}
							catch (Exception ex) {ex.printStackTrace(); return;}
						}
					}*/
				}
				else if (params[0].equalsIgnoreCase("backup") || params[0].equalsIgnoreCase("save")) {
					sender.sendStringMessageToPlayer("Currently only working in singleplayer");
				}
				else if (params[0].equalsIgnoreCase("exit")) {
					sender.sendStringMessageToPlayer("Singleplayer only");
				}
				else if (params[0].equalsIgnoreCase("new") && params.length > 1) {
					sender.sendStringMessageToPlayer("Currently only working in singleplayer");
				}
				else if (params[0].equalsIgnoreCase("list")) {
					sender.sendStringMessageToPlayer("Currently only working in singleplayer");
				}
				else sender.sendLangfileMessageToPlayer("command.world.invalidArg", new Object[0]);
			}
			else if (params[0].equalsIgnoreCase("seed")) {
				if (params.length > 2 && params[1].equalsIgnoreCase("set")) {
					try {
						long seed = Long.parseLong(params[2]);
						NBTTagCompound data = sender.toPlayer().getMinecraftPlayer().worldObj.getWorldInfo().getNBTTagCompound();
						data.setLong("RandomSeed", seed);
						sender.sendLangfileMessageToPlayer("command.world.setseed", new Object[] {String.valueOf(seed)});
					}
					catch (Exception ex) {sender.sendLangfileMessageToPlayer("command.world.NAN", new Object[0]);}
				}
				else {
					long seed = sender.toPlayer().getMinecraftPlayer().worldObj.getSeed();
					sender.sendLangfileMessageToPlayer("command.world.currentseed", new Object[] {String.valueOf(seed)});
				}
			}
			else if (params[0].equalsIgnoreCase("name")) {
				if (params.length > 2 && params[1].equalsIgnoreCase("set")) {
					sender.toPlayer().getMinecraftPlayer().worldObj.getWorldInfo().setWorldName(params[2]);
					sender.sendLangfileMessageToPlayer("command.world.setname", new Object[] {String.valueOf(params[2])});
				}
				else {
					String name = sender.toPlayer().getMinecraftPlayer().worldObj.getWorldInfo().getWorldName();
					sender.sendLangfileMessageToPlayer("command.world.currentname", new Object[] {name});
				}
			}
			else sender.sendLangfileMessageToPlayer("command.world.invalidArg", new Object[0]);
		}
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
}
