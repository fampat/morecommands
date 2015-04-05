package com.mrnobody.morecommands.command.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S09PacketHeldItemChange;
import net.minecraft.network.play.server.S1DPacketEntityEffect;
import net.minecraft.network.play.server.S39PacketPlayerAbilities;
import net.minecraft.network.play.server.S41PacketServerDifficulty;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.storage.AnvilSaveConverter;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.WorldInfo;

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
	public String getName() {
		return "world";
	}

	@Override
	public String getUsage() {
		return "command.world.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		if (params.length > 0) {
			if (params[0].equalsIgnoreCase("seed")) {
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
				return;
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
				return;
			}
			
			if (MoreCommands.getMoreCommands().getRunningServer() != ServerType.DEDICATED) {
				sender.sendLangfileMessageToPlayer("command.generic.notDedicated", new Object[0]);
				return;
			}
			
			DedicatedServer server = (DedicatedServer) MinecraftServer.getServer();
			ISaveFormat sf = server.getActiveAnvilConverter();
			if (sf == null || !(sf instanceof AnvilSaveConverter)) {
				sender.sendLangfileMessageToPlayer("command.world.loaderNotFound", new Object[0]);
				return;
			}
			AnvilSaveConverter loader = (AnvilSaveConverter) sf;
				
			if (params[0].equalsIgnoreCase("load") && params.length > 1) {
				String world = "";
				for (int i = 1; i < params.length; i++) world += " " + params[i];
					
				if ((new File(loader.savesDirectory, world.trim()).isDirectory())) {
					loadWorld(server, loader, world.trim(), (new Random()).nextLong(), 
							WorldType.parseWorldType(server.getStringProperty("level-type", "DEFAULT")), 
							server.getStringProperty("generator-settings", ""));
				}
				else sender.sendLangfileMessageToPlayer("command.world.notLoadable", new Object[] {world.trim()});
			}
			else if (params[0].equalsIgnoreCase("backup") || params[0].equalsIgnoreCase("save")) {
				Method saveWorld = ReflectionHelper.getMethod(MinecraftServer.class, "saveAllWorlds", boolean.class);
				if (saveWorld != null) {
					try {saveWorld.invoke(server, Boolean.FALSE);}
					catch(Exception ex) {ex.printStackTrace();}
				}
			
				if (params[0].equalsIgnoreCase("backup")) {
					if (!(new File(loader.savesDirectory, server.getFolderName())).isDirectory()) {
						MoreCommands.getMoreCommands().getLogger().info("Couldn't backup world");
						sender.sendLangfileMessageToPlayer("command.world.backupfailed", new Object[0]);
						return;
					}
					
					MoreCommands.getMoreCommands().getLogger().info("Backing up world \"" + server.getFolderName() + "\"");
					
					SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");
					String time = format.format(new Date());
					copyDirectory(new File(loader.savesDirectory, server.getFolderName()), new File(loader.savesDirectory, "backup/" + server.getFolderName() + "/" + time));
					
					sender.sendLangfileMessageToPlayer("command.world.backupsuccess", new Object[0]);
					MoreCommands.getMoreCommands().getLogger().info("Backup successfully created");
				}
				else sender.sendLangfileMessageToPlayer("command.world.saved", new Object[0]);
			}
			else if (params[0].equalsIgnoreCase("exit")) {
				((EntityPlayerMP) sender.getMinecraftISender()).playerNetServerHandler.kickPlayerFromServer("World exited");
			}
			else if (params[0].equalsIgnoreCase("new") && params.length > 1) {
				long seed = 0;
				int sub = 0;
				
				if (params.length > 2) {
					try {seed = Long.parseLong(params[params.length - 1]); sub = 1;}
					catch (Exception ex) {sender.sendLangfileMessageToPlayer("command.world.NAN", new Object[0]); seed = (new Random()).nextLong();}
				}
				else seed = (new Random()).nextLong();
				
				String world = "";
				for (int i = 1; i < params.length - sub; i++) world += " " + params[i];
				File fldr = new File(loader.savesDirectory, world);
				
				if (fldr.exists()) {
					sender.sendLangfileMessageToPlayer("command.world.cantcreate", new Object[0]);
					return;
				}
				
				loadWorld(server, loader, world.trim(), seed,
						WorldType.parseWorldType(server.getStringProperty("level-type", "DEFAULT")),
						server.getStringProperty("generator-settings", ""));
			}
			else if (params[0].equalsIgnoreCase("list")) {
				File[] list;
				
				if (params.length > 1) {
					String subDir = "";
					for (int i = 1; i < params.length; i++) subDir += " " + params[i];
					if ((new File(loader.savesDirectory, subDir.trim())).isDirectory())
						list = (new File(loader.savesDirectory, subDir.trim())).listFiles();
					else list = loader.savesDirectory.listFiles();
				}
				else list = loader.savesDirectory.listFiles();
				
				String saves = null;
				
				for (int i = 0; i < list.length; i++) {
					if (list[i].isDirectory()) {
						if (saves == null) saves = list[i].getName();
						else saves += ", " + list[i].getName();
					}
				}
				sender.sendLangfileMessageToPlayer("command.world.saves", new Object[0]);
				sender.sendStringMessageToPlayer(saves);
			}
			else sender.sendLangfileMessageToPlayer("command.world.invalidArg", new Object[0]);
		}
	}
	
	private void loadWorld(DedicatedServer server, AnvilSaveConverter saveLoader, String world, long seed, WorldType type, String genSettings) {
		Method loadWorld = ReflectionHelper.getMethod(MinecraftServer.class, "loadAllWorlds", String.class, String.class, long.class, WorldType.class, String.class);
		
		if (loadWorld != null) {
			try {
				server.setFolderName(world);
				server.getConfigurationManager().saveAllPlayerData();
				loadWorld.invoke(server, world, world, seed, type == null ? WorldType.DEFAULT : type, genSettings);
				
				for (Object player : server.getConfigurationManager().playerEntityList) {
					if (player instanceof EntityPlayerMP) 
						transferPlayer(server, (EntityPlayerMP) player);
				}
			}
			catch (Exception ex) {ex.printStackTrace(); return;}
		}
	}
	
	private boolean copyDirectory(File from, File to) {
		if (!from.isDirectory()) return false;
		if (!to.exists()) to.mkdirs();
		if (!to.isDirectory()) return false;
		      
		try {
			File list[] = from.listFiles();
			
			for (int i = 0; i < list.length; i++) {
				if (list[i].isDirectory()) {
					copyDirectory(list[i], new File(to, list[i].getName()));
				} else if (list[i].isFile()) {
					copyFile(list[i], new File(to, list[i].getName()));
				}
			}
		} catch (Exception e) {return false;}
		return true;
	}
	
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
	
	private void transferPlayer(DedicatedServer server, EntityPlayerMP player) {
		NBTTagCompound nbttagcompound = server.getConfigurationManager().readPlayerDataFromFile(player);
		player = server.getConfigurationManager().recreatePlayerEntity(player, player.dimension, player.playerConqueredTheEnd);
        player.playerNetServerHandler.playerEntity = player;
		nbttagcompound = server.getConfigurationManager().readPlayerDataFromFile(player);
		
        player.setWorld(server.worldServerForDimension(player.dimension));

        World playerWorld = server.worldServerForDimension(player.dimension);
        if (playerWorld == null)
        {
            player.dimension = 0;
            playerWorld = server.worldServerForDimension(0);
            BlockPos spawnPoint = playerWorld.provider.getRandomizedSpawnPoint();
            player.setPosition(spawnPoint.getX(), spawnPoint.getY(), spawnPoint.getZ());
        }

        player.setWorld(playerWorld);
        player.theItemInWorldManager.setWorld((WorldServer)player.worldObj);
        
        WorldServer worldserver = server.worldServerForDimension(player.dimension);
        WorldInfo worldinfo = worldserver.getWorldInfo();
        player.playerNetServerHandler.sendPacket(new S41PacketServerDifficulty(worldinfo.getDifficulty(), worldinfo.isDifficultyLocked()));
        player.playerNetServerHandler.sendPacket(new S39PacketPlayerAbilities(player.capabilities));
        player.playerNetServerHandler.sendPacket(new S09PacketHeldItemChange(player.inventory.currentItem));
        
        player.playerNetServerHandler.setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
        server.getConfigurationManager().updateTimeAndWeatherForPlayer(player, server.worldServerForDimension(player.dimension));
        
        Iterator iterator = player.getActivePotionEffects().iterator();

        while (iterator.hasNext())
        {
            PotionEffect potioneffect = (PotionEffect) iterator.next();
            player.playerNetServerHandler.sendPacket(new S1DPacketEntityEffect(player.getEntityId(), potioneffect));
        }
        
        if (nbttagcompound != null && nbttagcompound.hasKey("Riding", 10))
        {
            Entity entity = EntityList.createEntityFromNBT(nbttagcompound.getCompoundTag("Riding"), server.worldServerForDimension(player.dimension));

            if (entity != null)
            {
                entity.forceSpawn = true;
                server.worldServerForDimension(player.dimension).spawnEntityInWorld(entity);
                player.mountEntity(entity);
                entity.forceSpawn = false;
            }
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
