package com.mrnobody.morecommands.command.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.ObfuscatedNames.ObfuscatedField;
import com.mrnobody.morecommands.util.ObfuscatedNames.ObfuscatedMethod;
import com.mrnobody.morecommands.util.ReflectionHelper;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.network.play.server.SPacketHeldItemChange;
import net.minecraft.network.play.server.SPacketPlayerAbilities;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.AnvilSaveConverter;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.common.FMLCommonHandler;

@Command(
		name = "world",
		description = "command.world.description",
		example = "command.world.example",
		syntax = "command.world.syntax",
		videoURL = "command.world.videoURL"
		)
public class CommandWorld extends StandardCommand implements ServerCommandProperties {
	private final Method saveWorlds = ReflectionHelper.getMethod(ObfuscatedMethod.MinecraftServer_saveAllWorlds);
	private final Method loadWorlds = ReflectionHelper.getMethod(ObfuscatedMethod.MinecraftServer_loadAllWorlds);
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
			if (params[0].equalsIgnoreCase("seed")) {
				if (params.length > 2 && params[1].equalsIgnoreCase("set")) {
					try {
						long seed = Long.parseLong(params[2]);
						NBTTagCompound data = sender.getWorld().getWorldInfo().cloneNBTCompound(null);
						data.setLong("RandomSeed", seed);
						ReflectionHelper.set(ObfuscatedField.World_worldInfo, this.worldInfo, sender.getWorld(), new WorldInfo(data));
						sender.sendLangfileMessage("command.world.setseed", String.valueOf(seed));
					}
					catch (Exception ex) {throw new CommandException("command.world.NAN", sender);}
				}
				else {
					long seed = sender.getWorld().getSeed();
					sender.sendLangfileMessage("command.world.currentseed", String.valueOf(seed));
				}
				return null;
			}
			else if (params[0].equalsIgnoreCase("name")) {
				if (params.length > 2 && params[1].equalsIgnoreCase("set")) {
					sender.getWorld().getWorldInfo().setWorldName(params[2]);
					sender.sendLangfileMessage("command.world.setname", String.valueOf(params[2]));
				}
				else {
					String name = sender.getWorld().getWorldInfo().getWorldName();
					sender.sendLangfileMessage("command.world.currentname", name);
				}
				return null;
			}
			
			if (!MoreCommands.isServerSide())
				throw new CommandException("command.generic.notDedicated", sender);
			
			DedicatedServer server = (DedicatedServer) sender.getServer();
			ISaveFormat sf = server.getActiveAnvilConverter();
			if (sf == null || !(sf instanceof AnvilSaveConverter))
				throw new CommandException("command.world.loaderNotFound", sender);
			
			AnvilSaveConverter loader = (AnvilSaveConverter) sf;
			
			if (params[0].equalsIgnoreCase("load") && params.length > 1) {
				String world = "";
				for (int i = 1; i < params.length; i++) world += " " + params[i];
					
				if ((new File(loader.savesDirectory, world.trim()).isDirectory())) {
					loadWorld(server, loader, world.trim(), (new Random()).nextLong(), 
							WorldType.parseWorldType(server.getStringProperty("level-type", "DEFAULT")), 
							server.getStringProperty("generator-settings", ""));
				}
				else throw new CommandException("command.world.notLoadable", sender, world.trim());
			}
			else if (params[0].equalsIgnoreCase("backup") || params[0].equalsIgnoreCase("save")) {
				if (this.saveWorlds != null)
					ReflectionHelper.invoke(ObfuscatedMethod.MinecraftServer_saveAllWorlds, server, Boolean.FALSE);
			
				if (params[0].equalsIgnoreCase("backup")) {
					if (!(new File(loader.savesDirectory, server.getFolderName())).isDirectory()) {
						MoreCommands.INSTANCE.getLogger().info("Couldn't backup world");
						throw new CommandException("command.world.backupfailed", sender);
					}
					
					MoreCommands.INSTANCE.getLogger().info("Backing up world \"" + server.getFolderName() + "\"");
					
					SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");
					String time = format.format(new Date());
					copyDirectory(new File(loader.savesDirectory, server.getFolderName()), new File(loader.savesDirectory, "backup/" + server.getFolderName() + "/" + time));
					
					sender.sendLangfileMessage("command.world.backupsuccess");
					MoreCommands.INSTANCE.getLogger().info("Backup successfully created");
				}
				else sender.sendLangfileMessage("command.world.saved");
			}
			else if (params[0].equalsIgnoreCase("exit")) {
				if (isSenderOfEntityType(sender.getMinecraftISender(), EntityPlayerMP.class))
						getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class).connection.kickPlayerFromServer("World exited");
				else throw new CommandException("command.generic.notServer", sender);
			}
			else if (params[0].equalsIgnoreCase("new") && params.length > 1) {
				long seed = 0;
				int sub = 0;
				
				if (params.length > 2) {
					try {seed = Long.parseLong(params[params.length - 1]); sub = 1;}
					catch (Exception ex) {sender.sendLangfileMessage("command.world.NAN", new Object[0]); seed = (new Random()).nextLong();}
				}
				else seed = (new Random()).nextLong();
				
				String world = "";
				for (int i = 1; i < params.length - sub; i++) world += " " + params[i];
				File fldr = new File(loader.savesDirectory, world);
				
				if (fldr.exists())
					throw new CommandException("command.world.cantcreate", sender);
				
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
				sender.sendLangfileMessage("command.world.saves");
				sender.sendStringMessage(saves);
			}
			else throw new CommandException("command.world.invalidArg", sender);
		}
		
		return null;
	}
	
	private void loadWorld(DedicatedServer server, AnvilSaveConverter saveLoader, String world, long seed, WorldType type, String genSettings) {
		if (this.loadWorlds != null) {
			try {
				server.getPlayerList().saveAllPlayerData();
				this.loadWorlds.invoke(server, world, world, seed, type == null ? WorldType.DEFAULT : type, genSettings);
				server.setFolderName(world);
				
				for (EntityPlayerMP player : server.getPlayerList().getPlayerList()) {
					transferPlayer(server, player);
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

		FileInputStream source = null;
		FileOutputStream destination = null;
		      
		try {
			source = new FileInputStream(sourceFile);
			destination = new FileOutputStream(destFile);
			destination.getChannel().transferFrom(source.getChannel(), 0, source.getChannel().size());
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
		NBTTagCompound nbttagcompound = server.getPlayerList().readPlayerDataFromFile(player);
		player = server.getPlayerList().recreatePlayerEntity(player, player.dimension, player.playerConqueredTheEnd);
        player.connection.playerEntity = player;
		nbttagcompound = server.getPlayerList().readPlayerDataFromFile(player);
		
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
        player.interactionManager.setWorld((WorldServer)player.worldObj);
        
        WorldServer worldserver = server.worldServerForDimension(player.dimension);
        WorldInfo worldinfo = worldserver.getWorldInfo();
        player.connection.sendPacket(new SPacketPlayerAbilities(player.capabilities));
        player.connection.sendPacket(new SPacketHeldItemChange(player.inventory.currentItem));
        
        player.connection.setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
        server.getPlayerList().updateTimeAndWeatherForPlayer(player, server.worldServerForDimension(player.dimension));
        
        Iterator iterator = player.getActivePotionEffects().iterator();

        while (iterator.hasNext())
        {
            PotionEffect potioneffect = (PotionEffect) iterator.next();
            player.connection.sendPacket(new SPacketEntityEffect(player.getEntityId(), potioneffect));
        }
        
        if (nbttagcompound != null)
        {
            if (nbttagcompound.hasKey("RootVehicle", 10))
            {
                NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("RootVehicle");
                Entity entity2 = AnvilChunkLoader.readWorldEntity(nbttagcompound1.getCompoundTag("Entity"), worldserver, true);

                if (entity2 != null)
                {
                    UUID uuid = nbttagcompound1.getUniqueId("Attach");

                    if (entity2.getUniqueID().equals(uuid))
                    {
                        player.startRiding(entity2, true);
                    }
                    else
                    {
                        for (Entity entity : entity2.getRecursivePassengers())
                        {
                            if (entity.getUniqueID().equals(uuid))
                            {
                                player.startRiding(entity, true);
                                break;
                            }
                        }
                    }

                    if (!player.isRiding())
                    {
                        worldserver.removeEntityDangerously(entity2);

                        for (Entity entity3 : entity2.getRecursivePassengers())
                        {
                            worldserver.removeEntityDangerously(entity3);
                        }
                    }
                }
            }
            else if (nbttagcompound.hasKey("Riding", 10))
            {
                Entity entity1 = AnvilChunkLoader.readWorldEntity(nbttagcompound.getCompoundTag("Riding"), worldserver, true);

                if (entity1 != null)
                {
                    player.startRiding(entity1, true);
                }
            }
        }
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
		return 2;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return true;
	}
}
