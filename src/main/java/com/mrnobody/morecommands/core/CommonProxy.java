package com.mrnobody.morecommands.core;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

import com.mrnobody.morecommands.command.CommandBase.ServerType;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.network.PacketHandlerServer;
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
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

/**
 * The common proxy class
 * 
 * @author MrNobody98
 *
 */
public class CommonProxy {
	protected CommonPatcher patcher;
	
	protected MoreCommands mod = MoreCommands.getMoreCommands();
	private boolean serverCommandsRegistered = false;
	private Field langCode = ReflectionHelper.getField(EntityPlayerMP.class, "translator");
	
	public CommonProxy() {
		this.setPatcher();
	}
	
	/**
	 * sets the patcher corresponding to the proxy
	 */
	protected void setPatcher() {
		this.patcher = new CommonPatcher();
	}
	
	/**
	 * Called from the main mod class to do pre initialization
	 */
	protected void preInit(FMLPreInitializationEvent event) {
		this.patcher.applyModStatePatch(event);
	}

	/**
	 * Called from the main mod class to do initialization
	 */
	protected void init(FMLInitializationEvent event) {
		this.patcher.applyModStatePatch(event);
	}

	/**
	 * Called from the main mod class to do post initialization
	 */
	protected void postInit(FMLPostInitializationEvent event) {
		this.patcher.applyModStatePatch(event);
	}
	
	/**
	 * Called from the main mod class to do stuff before the server starts
	 */
	protected void serverStart(FMLServerAboutToStartEvent event) {
		this.patcher.applyModStatePatch(event);
	}
	
	/**
	 * Called from the main mod class to handle the server start
	 */
	protected void serverInit(FMLServerStartingEvent event) {
		this.patcher.applyModStatePatch(event);
		GlobalSettings.readSettings();
		
		if (this.serverCommandsRegistered = this.registerServerCommands()) 
			this.mod.getLogger().info("Server Commands successfully registered");
		
		if (GlobalSettings.retryHandshake)
			PacketHandlerServer.startHandshakeRetryThread();
	}

	/**
	 * Called from the main mod class to handle the server stop
	 */
	protected void serverStop(FMLServerStoppedEvent event) {
		if (GlobalSettings.retryHandshake)
			PacketHandlerServer.stopHandshakeRetryThread();
		
		GlobalSettings.writeSettings();
		
		for (Object command : MinecraftServer.getServer().getCommandManager().getCommands().values()) {
			if (command instanceof ServerCommand) ((ServerCommand) command).unregisterFromHandler();
		}
		
		AppliedPatches.setServerCommandManagerPatched(false);
		AppliedPatches.setServerConfigManagerPatched(false);
	}
	
	/**
	 * Called from the main mod class to get the patcher instance
	 * @return the patcher
	 */
	public CommonPatcher getPatcher() {
		return this.patcher;
	}

	/**
	 * @return Whether this proxy loaded the commands successfully
	 */
	public boolean commandsLoaded() {
		return this.serverCommandsRegistered;
	}
	
	/**
	 * Registers Handlers
	 */
	public boolean registerHandlers() {
		ModContainer container = Loader.instance().activeModContainer();
		if (container == null || !container.getModId().equals(Reference.MODID)) return false;
		boolean errored = false;
		
		for (EventHandler handler : EventHandler.values()) {
			if (!handler.getHandler().isClientOnly()) 
				if (!EventHandler.register(handler, container)) {errored = true; break;}
		}
		
		if (!errored) {this.mod.getLogger().info("Event Handlers registered"); return true;}
		else return false;
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
					if (this.mod.isCommandEnabled(serverCommand.getCommandName()))
						commandManager.registerCommand(serverCommand);
				}
				
				return true;
			}
			else return false;
		}
		catch (Exception ex) {ex.printStackTrace(); return false;}
	}
	
	/**
	 * @return The running Server Type
	 */
	public ServerType getRunningServerType() {
		return ServerType.DEDICATED;
	}
	
	/**
	 * Called from the main mod class to get the language
	 */
	public String getLang(ICommandSender sender) {
		if (sender instanceof EntityPlayerMP) {
			try {return this.langCode != null ? (String) this.langCode.get((EntityPlayerMP) sender) : LanguageManager.defaultLanguage;}
			catch (Exception ex) {return LanguageManager.defaultLanguage;}
		}
		else return LanguageManager.defaultLanguage;
	}
}
