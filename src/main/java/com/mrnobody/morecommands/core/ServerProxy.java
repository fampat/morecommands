package com.mrnobody.morecommands.core;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import com.mrnobody.morecommands.command.CommandBase.ServerType;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.util.GlobalSettings;
import com.mrnobody.morecommands.util.LanguageManager;
import com.mrnobody.morecommands.util.Reference;
import com.mrnobody.morecommands.util.ReflectionHelper;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.eventhandler.EventBus;

/**
 * The proxy used on a dedicated server
 * 
 * @author MrNobody98
 *
 */
public class ServerProxy extends CommonProxy {
	private Patcher serverPatcher;
	
	private boolean serverCommandsLoaded = false;
	
	public ServerProxy() {
		this.serverPatcher = new ServerPatcher();
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
			MoreCommands.getLogger().info("Server Commands successfully registered");
	}

	@Override
	public void serverStop(FMLServerStoppedEvent event) {
		GlobalSettings.writeSettings();
		for (Object command : MinecraftServer.getServer().getCommandManager().getCommands().values()) {
			if (command instanceof ServerCommand) ((ServerCommand) command).unregisterFromHandler();
		}
		MoreCommands.getPatcherInstance().setServerCommandManagerPatched(false);
		MoreCommands.getPatcherInstance().setServerConfigManagerPatched(false);
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
		Method register = null;
		try {
			register = EventBus.class.getDeclaredMethod("register", Class.class, Object.class, Method.class, ModContainer.class);
			register.setAccessible(true);
		}
		catch (Exception ex) {ex.printStackTrace(); return false;}
		
		if (container == null || register == null || !container.getModId().equals(Reference.MODID)) return false;
		for (EventHandler handler : EventHandler.values()) 
			{if (!EventHandler.isClientOnly(handler)) EventHandler.register(handler.getBus(), handler.getHandler(), register, container);}
		
		MoreCommands.getLogger().info("Event Handlers registered");
		return true;
	}
	
	/**
	 * Registers all server commands
	 * 
	 * @return Whether the server commands were registered successfully
	 */
	private boolean registerServerCommands() {
		List<Class<? extends ServerCommand>> serverCommands = MoreCommands.getServerCommandClasses();
		if (serverCommands == null) return false;
		Iterator<Class<? extends ServerCommand>> commandIterator = serverCommands.iterator();
		
		try {
			if (MinecraftServer.getServer().getCommandManager() instanceof ServerCommandManager) {
				ServerCommandManager commandManager = (ServerCommandManager) MinecraftServer.getServer().getCommandManager(); 
				
				while (commandIterator.hasNext()) {
					ServerCommand serverCommand = commandIterator.next().newInstance();
					if (MoreCommands.isCommandEnabled(serverCommand.getCommandName()))
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
