package com.mrnobody.morecommands.core;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.eventhandler.EventBus;

import com.mrnobody.morecommands.command.CommandBase.ServerType;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.util.GlobalSettings;
import com.mrnobody.morecommands.util.LanguageManager;
import com.mrnobody.morecommands.util.Reference;
import com.mrnobody.morecommands.util.ReflectionHelper;

/**
 * The proxy used on a dedicated server
 * 
 * @author MrNobody98
 *
 */
public class ServerProxy extends CommonProxy {
	private Patcher serverPatcher;
	private MoreCommands mod;
	
	private boolean serverCommandsLoaded = false;
	
	public ServerProxy() {
		this.serverPatcher = new ServerPatcher();
		this.mod = MoreCommands.getMoreCommands();
		
	}
	
	@Override
	public void preInit(FMLPreInitializationEvent event) {
		this.serverPatcher.applyModStatePatch(event);
	}

	@Override
	public void init(FMLInitializationEvent event) {
		this.serverPatcher.applyModStatePatch(event);
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {
		this.serverPatcher.applyModStatePatch(event);
	}

	@Override
	public void serverStart(FMLServerAboutToStartEvent event) {
		Patcher.setServerModded(true);
		this.serverPatcher.applyModStatePatch(event);
	}

	@Override
	public void serverInit(FMLServerStartingEvent event) {
		this.serverPatcher.applyModStatePatch(event);
		GlobalSettings.readSettings();
		if (this.serverCommandsLoaded = this.registerServerCommands()) 
			this.mod.getLogger().info("Server Commands successfully registered");
	}

	@Override
	public void serverStop(FMLServerStoppedEvent event) {
		GlobalSettings.writeSettings();
		for (Object command : MinecraftServer.getServer().getCommandManager().getCommands().values()) {
			if (command instanceof ServerCommand) ((ServerCommand) command).unregisterFromHandler();
		}
		Patcher.setServerCommandManagerPatched(false);
		Patcher.setServerConfigManagerPatched(false);
	}

	@Override
	public Patcher getPatcher() {
		return this.serverPatcher;
	}

	@Override
	public boolean commandsLoaded() {
		return this.serverCommandsLoaded ;
	}
	
	@Override
	public boolean registerHandlers() {
		ModContainer container = Loader.instance().activeModContainer();
		if (container == null || !container.getModId().equals(Reference.MODID)) return false;
		
		for (EventHandler handler : EventHandler.values()) {
			if (!handler.getHandler().isClientOnly()) 
				EventHandler.register(handler.getBus(), handler.getHandler(), container);
		}
		
		this.mod.getLogger().info("Event Handlers registered");
		return true;
	}
	
	/**
	 * Registers all server commands
	 * 
	 * @return Whether the server commands were registered successfully
	 */
	private boolean registerServerCommands() {
		List<Class<? extends ServerCommand>> serverCommands = this.mod.getServerCommandClasses();
		if (serverCommands == null) return false;
		Iterator<Class<? extends ServerCommand>> commandIterator = serverCommands.iterator();
		
		try {
			if (MinecraftServer.getServer().getCommandManager() instanceof ServerCommandManager) {
				ServerCommandManager commandManager = (ServerCommandManager) MinecraftServer.getServer().getCommandManager(); 
				
				while (commandIterator.hasNext()) {
					ServerCommand serverCommand = commandIterator.next().newInstance();
					if (this.mod.isCommandEnabled(serverCommand.getName()))
						commandManager.registerCommand(serverCommand);
				}
				
				return true;
			}
			else return false;
		}
		catch (Exception ex) {ex.printStackTrace(); return false;}
	}
	
	@Override
	public ServerType getRunningServerType() {
		return ServerType.DEDICATED;
	}
	
	@Override
	public String getLang(ICommandSender sender) {
		if (sender instanceof EntityPlayerMP) {
			Field lang = ReflectionHelper.getField(EntityPlayerMP.class, "translator");
			try {return lang != null ? (String) lang.get((EntityPlayerMP) sender) : LanguageManager.defaultLanguage;}
			catch (Exception ex) {return LanguageManager.defaultLanguage;}
		}
		else return LanguageManager.defaultLanguage;
	}
}
