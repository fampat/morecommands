package com.mrnobody.morecommands.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.mrnobody.morecommands.asm.MoreCommandsLoadingPlugin;
import com.mrnobody.morecommands.command.ClientCommandProperties;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.AppliedPatches.PlayerPatches;
import com.mrnobody.morecommands.network.PacketDispatcher;
import com.mrnobody.morecommands.util.DynamicClassLoader;
import com.mrnobody.morecommands.util.GlobalSettings;
import com.mrnobody.morecommands.util.LanguageManager;
import com.mrnobody.morecommands.util.ObfuscatedNames;
import com.mrnobody.morecommands.util.PlayerSettings;
import com.mrnobody.morecommands.util.Reference;

import net.minecraft.command.ICommandSender;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.InstanceFactory;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;

/**
 * The main mod class loaded by forge
 * 
 * @author MrNobody98
 *
 */
@Mod(modid = Reference.MODID, version = Reference.VERSION, name = Reference.NAME)
public enum MoreCommands {
	INSTANCE;
	
	@SidedProxy(clientSide = "com.mrnobody.morecommands.core.ClientProxy", serverSide = "com.mrnobody.morecommands.core.CommonProxy", modId = Reference.MODID)
	private static CommonProxy proxy;
	
	/**
	 * A factory method for forge to get the mod instance
	 * 
	 * @return the MoreCommands instance
	 */
	@InstanceFactory
	private static MoreCommands getInstance() {
		return INSTANCE;
	}
	
	/**
	 * An enum of allowed server types for a command
	 * 
	 * @author MrNobody98
	 */
	public static enum ServerType {INTEGRATED, DEDICATED, ALL}
	
	private final DynamicClassLoader commandClassLoader = new DynamicClassLoader(MoreCommands.class.getClassLoader());
	private final Pattern ipPattern = Pattern.compile("^(\\d{1,3}\\.){3}\\d{1,3}:\\d{1,5}[ \t]*\\{$");
	
	private PacketDispatcher dispatcher;
	private Logger logger;
	
	private final String clientCommandsPackage = "com.mrnobody.morecommands.command.client";
	private List<Class<? extends StandardCommand>> clientCommandClasses = new ArrayList<Class<? extends StandardCommand>>();
	
	private final String serverCommandsPackage = "com.mrnobody.morecommands.command.server";
	private List<Class<? extends StandardCommand>> serverCommandClasses = new ArrayList<Class<? extends StandardCommand>>();
	
	private List<String> disabledCommands;
	private List<String> startupCommands;
	private List<String> startupMultiplayerCommands;
	private Map<String, List<String>> startupServerCommands;
	
	/**
	 * @return The running proxy
	 */
	public static CommonProxy getProxy() {
		return MoreCommands.proxy;
	}
	
	/**
	 * @return Whether the mod runs on a dedicated server
	 */
	public static boolean isServerSide() {
		return !(MoreCommands.proxy instanceof ClientProxy);
	}
	
	/**
	 * @return Whether the mod runs client side (e.g. integrated server)
	 */
	public static boolean isClientSide() {
		return MoreCommands.proxy instanceof ClientProxy;
	}
	
	/**
	 * @return The running environment (client or server)
	 */
	public static Side getEnvironment() {
		if (MoreCommands.isClientSide()) return Side.CLIENT;
		else if (MoreCommands.isServerSide()) return Side.SERVER;
		else return null;
	}
	
	/**
	 * @return The Server the player plays on (integrated or dedicated)
	 */
	public static ServerType getServerType() {
		return MoreCommands.proxy.getRunningServerType();
	}
	
	/**
	 * @return A list of commands, which shall be disabled
	 */
	public List<String> getDisabledCommands() {
		return new ArrayList<String>(this.disabledCommands);
	}
	
	/**
	 * @return The Mod Logger
	 */
	public Logger getLogger() {
		return this.logger;
	}
	
	/**
	 * @return The {@link PacketDispatcher}
	 */
	public PacketDispatcher getPacketDispatcher() {
		return this.dispatcher;
	}
	
	/**
	 * @return The Client Command Classes
	 */
	public List<Class<? extends StandardCommand>> getClientCommandClasses() {
		return this.clientCommandClasses;
	}
	
	/**
	 * @return The Server Command Classes
	 */
	public List<Class<? extends StandardCommand>> getServerCommandClasses() {
		return this.serverCommandClasses;
	}
	
	/**
	 * @return the dynamic class loader responsible for command class loading
	 */
	public DynamicClassLoader getCommandClassLoader() {
		return this.commandClassLoader;
	}
	
	/**
	 * @return Whether the command is enabled
	 */
	public boolean isCommandEnabled(String command) {
		return !this.disabledCommands.contains(command);
	}
	
	/**
	 * @return The current language
	 */
	public String getCurrentLang(ICommandSender sender) {
		return MoreCommands.proxy.getLang(sender);
	}
	
	/**
	 * Checks whether a connection should be accepted.
	 * 
	 * @param mods the mods the other side has installed
	 * @param side the side that tries to connect
	 * @return If {@link GlobalSettings#clientMustHaveMod} and the connection is a client connection, returns true if
	 * 		   the client has installed MoreCommands, else return always true. If {@link GlobalSettings#serverMustHaveMod} 
	 * 		   and the connection is a server connection, returns true if the server has installed MoreCommands, else return always true.
	 */
	@NetworkCheckHandler
	public boolean acceptConnection(Map<String, String> mods, Side side) {
		if ((side == Side.CLIENT && GlobalSettings.clientMustHaveMod) || (side == Side.SERVER && GlobalSettings.serverMustHaveMod))
			return mods.containsKey(Reference.MODID) ? mods.get(Reference.MODID).equals(Reference.VERSION) : false;
		else
			return true;
	}
	
	/**
	 * @see FMLPreInitializationEvent
	 */
	@EventHandler
	private void preInit(FMLPreInitializationEvent event) {
		this.logger = event.getModLog();
		
		if (MoreCommandsLoadingPlugin.wasLoaded())
			ObfuscatedNames.setEnvNames(MoreCommandsLoadingPlugin.isDeobf());
		else {
			this.logger.warn("MoreCommands ASM Transformers were not loaded. This is probably because"
					+ " the manifest file of the jar archive of this mod was manipulated."
					+ "This can cause some commands to not work properly.");
		}
		
		Reference.init(event);
		LanguageManager.readTranslations();
		
		GlobalSettings.init();
		GlobalSettings.readSettings();
		
		PlayerSettings.registerCapabilities();
		PlayerPatches.registerCapability();
		
		this.dispatcher = new PacketDispatcher();
		if (this.loadCommands()) this.logger.info("Command Classes successfully loaded");
		
		this.disabledCommands = this.readDisabledCommands();
		this.getLogger().info("Following commands were disabled: " + this.disabledCommands);
		
		Triple<List<String>, List<String>, Map<String, List<String>>> startupCommands = this.parseStartupFiles();
		this.startupCommands = startupCommands.getLeft();
		this.startupMultiplayerCommands = startupCommands.getMiddle();
		this.startupServerCommands = startupCommands.getRight();
		
		MoreCommands.proxy.preInit(event);
	}
	
	/**
	 * @see FMLInitializationEvent
	 */
	@EventHandler
	private void init(FMLInitializationEvent event) {
		MoreCommands.proxy.registerHandlers();
		MoreCommands.proxy.init(event);
	}
	
	/**
	 * @see FMLPostInitializationEvent
	 */
	@EventHandler
	private void postInit(FMLPostInitializationEvent event) {
		MoreCommands.proxy.postInit(event);
	}
	
	/**
	 * @see FMLServerAboutToStartEvent
	 */
	@EventHandler
	private void serverStart(FMLServerAboutToStartEvent event) {
		MoreCommands.proxy.serverStart(event);
	}
	
	/**
	 * @see FMLServerStartingEvent
	 */
	@EventHandler
	private void serverInit(FMLServerStartingEvent event) {
		MoreCommands.proxy.serverInit(event);
	}
	
	/**
	 * @see FMLServerStartedEvent
	 */
	@EventHandler
	private void serverStarted(FMLServerStartedEvent event) {
		MoreCommands.proxy.serverStarted(event);
	}
	
	/**
	 * @see FMLServerStoppingEvent
	 */
	@EventHandler
	private void serverStopping(FMLServerStoppingEvent event) {
		MoreCommands.proxy.serverStopping(event);
	}
	
	/**
	 * @see FMLServerStoppedEvent
	 */
	@EventHandler
	public void serverStop(FMLServerStoppedEvent event) {
		MoreCommands.proxy.serverStop(event);
	}
	
	/**
	 * Loads Command Classes
	 * 
	 * @return Whether the commands were loaded successfully
	 */
	private boolean loadCommands() {
		List<Class<?>> commandClasses = this.commandClassLoader.getCommandClasses(this.clientCommandsPackage, true);
		commandClasses.addAll(this.commandClassLoader.getCommandClasses(this.serverCommandsPackage, false));
		
		for (Class<?> commandClass : commandClasses) {
			try {
				if (StandardCommand.class.isAssignableFrom(commandClass) && ClientCommandProperties.class.isAssignableFrom(commandClass))
					this.clientCommandClasses.add(commandClass.asSubclass(StandardCommand.class));
				else if (StandardCommand.class.isAssignableFrom(commandClass) && ServerCommandProperties.class.isAssignableFrom(commandClass))
					this.serverCommandClasses.add(commandClass.asSubclass(StandardCommand.class));
			}
			catch (Exception ex) {ex.printStackTrace(); return false;}
		}
		
		return true;
	}
	
	/**
	 * @return A List of disabled commands
	 */
	private List<String> readDisabledCommands() {
		ImmutableList.Builder<String> builder = ImmutableList.builder();
		File file = new File(Reference.getModDir(), "disable.cfg");

	    try {
			if (!file.exists() || !file.isFile()) file.createNewFile();
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) builder.add(line);
			br.close();
		}
		catch (IOException ex) {ex.printStackTrace(); this.getLogger().info("Could not read disable.cfg");}
		
		return builder.build();
	}
	
	/**
	 * Reads the files containing the command that shall be executed on startup
	 * 
	 * @return A {@link Triple} of which the left value is the commands that should be run after server startup,
	 * 		   the middle value is the command that should be run on every server the player joins and the right
	 * 		   value is the map of commands which should only be run on certain servers
	 */
	private Triple<List<String>, List<String>, Map<String, List<String>>> parseStartupFiles() {
		final List<String> startupCommands = new ArrayList<String>();
		final List<String> startupCommandsM = new ArrayList<String>();
		final Map<String, List<String>> startupCommandsMap = new HashMap<String, List<String>>();
		
		try {
			File startup = new File(Reference.getModDir(), "startup.cfg");
			File startupMultiplayer = new File(Reference.getModDir(), "startup_multiplayer.cfg");
			if (!startup.exists() || !startup.isFile()) startup.createNewFile();
			if (!startupMultiplayer.exists() || !startupMultiplayer.isFile()) startupMultiplayer.createNewFile();
		
			BufferedReader br = new BufferedReader(new FileReader(startup));
			String line; while ((line = br.readLine()) != null) {if (!line.startsWith("#")) startupCommands.add(line.trim());}
			br.close();
			
			br = new BufferedReader(new FileReader(startupMultiplayer));
			boolean bracketOpen = false;
			String addr = null;
			
			while ((line = br.readLine()) != null) {
				if (!bracketOpen && ipPattern.matcher(line.trim()).matches()) {bracketOpen = true; addr = line.split("\\{")[0].trim();}
				else if (bracketOpen && line.trim().equals("}")) {bracketOpen = false;}
				else if (!bracketOpen && !line.startsWith("#")) startupCommandsM.add(line.trim());
				else if (bracketOpen && !line.startsWith("#")) {
					if (!startupCommandsMap.containsKey(addr)) startupCommandsMap.put(addr, new ArrayList<String>());
					startupCommandsMap.get(addr).add(line.trim());
				}
			}
			
			br.close();
		}
		catch (IOException ex) {ex.printStackTrace(); this.logger.info("Startup commands file could not be read");}
		
		return ImmutableTriple.of(startupCommands, startupCommandsM, startupCommandsMap);
	}
	
	/**
	 * @return The commands that should be executed after server startup
	 */
	public List<String> getStartupCommands() {
		return new ArrayList<String>(this.startupCommands);
	}
	
	/**
	 * @param socketAddress the Socket Address of the server
	 * @return The commands that should be executed when the server joins a server with the given address
	 */
	public List<String> getStartupCommandsMultiplayer(String socketAddress) {
		socketAddress = socketAddress.trim(); if (socketAddress.startsWith("/")) socketAddress = socketAddress.substring(1);
		List<String> commands = new ArrayList<String>(this.startupMultiplayerCommands);
		if (this.startupServerCommands.containsKey(socketAddress)) commands.addAll(this.startupServerCommands.get(socketAddress));
		return commands;
	}
}
