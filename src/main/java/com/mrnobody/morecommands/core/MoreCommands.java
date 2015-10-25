package com.mrnobody.morecommands.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;

import com.mrnobody.morecommands.command.ClientCommand;
import com.mrnobody.morecommands.command.CommandBase.ServerType;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.network.PacketDispatcher;
import com.mrnobody.morecommands.util.DynamicClassLoader;
import com.mrnobody.morecommands.util.GlobalSettings;
import com.mrnobody.morecommands.util.LanguageManager;
import com.mrnobody.morecommands.util.Reference;

import net.minecraft.command.ICommandSender;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * The main mod class loaded by forge
 * 
 * @author MrNobody98
 *
 */
@Mod(modid = Reference.MODID, version = Reference.VERSION, name = Reference.NAME, acceptableRemoteVersions = "*")
public class MoreCommands {
	@Instance
	private static MoreCommands instance;
	public static final String WEBSITE = "http://bit.ly/morecommands";
	
	@SidedProxy(clientSide="com.mrnobody.morecommands.core.ClientProxy", serverSide="com.mrnobody.morecommands.core.CommonProxy", modId=Reference.MODID)
	private static CommonProxy proxy;
	
	public static final DynamicClassLoader CLASSLOADER = new DynamicClassLoader(MoreCommands.class.getClassLoader());
	private static final Pattern IP_PATTERN = Pattern.compile("^[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}:[0-9]{1,5}[ \t]*\\{$");
	
	private PacketDispatcher dispatcher;
	private UUID playerUUID;
	private Logger logger;
	private boolean handlersLoaded = false;
	
	private final String clientCommandsPackage = "com.mrnobody.morecommands.command.client";
	private List<Class<? extends ClientCommand>> clientCommandClasses = new ArrayList<Class<? extends ClientCommand>>();
	
	private final String serverCommandsPackage = "com.mrnobody.morecommands.command.server";
	private List<Class<? extends ServerCommand>> serverCommandClasses = new ArrayList<Class<? extends ServerCommand>>();
	
	private List<String> disabledCommands;
	private List<String> startupCommands;
	private List<String> startupMultiplayerCommands;
	private Map<String, List<String>> startupServerCommands;
	
	//Need this because forge injects the instance after injecting the proxy, but it uses
	//MoreCommands#getMoreCommands in its constructor -> Causes a NullpointerException
	public MoreCommands() {MoreCommands.instance = this;}
	
	/**
	 * @return The Singleton mod instance
	 */
	public static MoreCommands getMoreCommands() {
		return MoreCommands.instance;
	}
	
	/**
	 * @return The running proxy
	 */
	public static CommonProxy getProxy() {
		return MoreCommands.proxy;
	}
	
	/**
	 * @return Whether the mod is enabled
	 */
	public static boolean isModEnabled() {
		return MoreCommands.proxy.commandsLoaded() && MoreCommands.instance.handlersLoaded;
	}
	
	/**
	 * @return The UUID for the server side player or null if the mod isn't installed server side
	 */
	public UUID getPlayerUUID() {
		return this.playerUUID;
	}
	
	/**
	 * Sets the player UUID
	 */
	public void setPlayerUUID(UUID uuid) {
		this.playerUUID = uuid;
	}
	
	/**
	 * @return A list of commands, which shall be disabled
	 */
	public List<String> getDisabledCommands() {
		return this.disabledCommands;
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
	 * @return The running side (client or server)
	 */
	public static Side getRunningSide() {
		if (MoreCommands.isClientSide()) return Side.CLIENT;
		else if (MoreCommands.isServerSide()) return Side.SERVER;
		else return null;
	}
	
	/**
	 * @return The running Server Type (integrated or dedicated)
	 */
	public ServerType getRunningServer() {
		return MoreCommands.proxy.getRunningServerType();
	}
	
	/**
	 * @return The Mod Logger
	 */
	public Logger getLogger() {
		return this.logger;
	}
	
	/**
	 * @return The Network Wrapper
	 */
	public PacketDispatcher getPacketDispatcher() {
		return this.dispatcher;
	}
	
	/**
	 * @return The Client Command Classes
	 */
	public List<Class<? extends ClientCommand>> getClientCommandClasses() {
		return this.clientCommandClasses;
	}
	
	/**
	 * @return The Server Command Classes
	 */
	public List<Class<? extends ServerCommand>> getServerCommandClasses() {
		return this.serverCommandClasses;
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
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		this.logger = event.getModLog();
		Reference.init(event);
		LanguageManager.readTranslations();
		GlobalSettings.readSettings();
		this.dispatcher = new PacketDispatcher();
		this.loadCommands();
		this.disabledCommands = this.readDisabledCommands();
		
		Object[] startupCommands = this.parseStartupFiles();
		this.startupCommands = (List<String>) startupCommands[0];
		this.startupMultiplayerCommands = (List<String>) startupCommands[1];
		this.startupServerCommands = (Map<String, List<String>>) startupCommands[2];
		
		MoreCommands.proxy.preInit(event);
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
		this.handlersLoaded = MoreCommands.proxy.registerHandlers();
		MoreCommands.proxy.init(event);
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		MoreCommands.proxy.postInit(event);
	}
	
	@EventHandler
	public void serverStart(FMLServerAboutToStartEvent event) {
		MoreCommands.proxy.serverStart(event);
	}
	
	@EventHandler
	public void serverInit(FMLServerStartingEvent event) {
		MoreCommands.proxy.serverInit(event);
	}
	
	@EventHandler
	private void serverStarted(FMLServerStartedEvent event) {
		MoreCommands.proxy.serverStarted(event);
	}
	
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
		List<Class<?>> clientCommands = MoreCommands.CLASSLOADER.getCommandClasses(this.clientCommandsPackage, ClientCommand.class);
		Iterator<Class<?>> clientCommandIterator = clientCommands.iterator();
		
		while (clientCommandIterator.hasNext()) {
			try {
				Class<? extends ClientCommand> command = clientCommandIterator.next().asSubclass(ClientCommand.class);
				this.clientCommandClasses.add(command);
			}
			catch (Exception ex) {ex.printStackTrace(); return false;}
		}
		
		List<Class<?>> serverCommands = MoreCommands.CLASSLOADER.getCommandClasses(this.serverCommandsPackage, ServerCommand.class);
		Iterator<Class<?>> serverCommandIterator = serverCommands.iterator();
		
		while (serverCommandIterator.hasNext()) {
			try {
				Class<? extends ServerCommand> handler = serverCommandIterator.next().asSubclass(ServerCommand.class);
				this.serverCommandClasses.add(handler);
			}
			catch (Exception ex) {ex.printStackTrace(); return false;}
		}
		
		return true;
	}
	
	/**
	 * @return A List of disabled commands
	 */
	private List<String> readDisabledCommands() {
		List<String> disabled = new ArrayList<String>();
		File file = new File(Reference.getModDir(), "disable.cfg");
		
		try {
			if (!file.exists() || !file.isFile()) file.createNewFile();
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {disabled.add(line); this.getLogger().info("Disabling command '" + line + "'");}
			br.close();
		}
		catch (IOException ex) {ex.printStackTrace(); this.getLogger().info("Could not read disable.cfg");}
		
		return disabled;
	}
	
	private Object[] parseStartupFiles() {
		final List<String> startupCommands = new ArrayList<String>();
		final List<String> startupCommandsM = new ArrayList<String>();
		final Map<String, List<String>> startupCommandsMap = new HashMap<String, List<String>>();
		
		try {
			File startup = new File(Reference.getModDir(), "startup.cfg");
			File startupMultiplayer = new File(Reference.getModDir(), "startup_multiplayer.cfg");
			if (!startup.exists() || !startup.isFile()) startup.createNewFile();
			if (!startupMultiplayer.exists() || !startupMultiplayer.isFile()) startup.createNewFile();
		
			BufferedReader br = new BufferedReader(new FileReader(startup));
			String line; while ((line = br.readLine()) != null) {if (!line.startsWith("#")) startupCommands.add(line.trim());}
			br.close();
			
			br = new BufferedReader(new FileReader(startupMultiplayer));
			boolean bracketOpen = false;
			String addr = null;
			
			while ((line = br.readLine()) != null) {
				if (!bracketOpen && IP_PATTERN.matcher(line.trim()).matches()) {bracketOpen = true; addr = line.split("\\{")[0].trim();}
				else if (bracketOpen && line.trim().equals("}")) {bracketOpen = false;}
				else if (!bracketOpen && !line.startsWith("#")) startupCommandsM.add(line.trim());
				else if (bracketOpen && !line.startsWith("#")) {
					if (!startupCommandsMap.containsKey(addr)) startupCommandsMap.put(addr, new ArrayList<String>());
					startupCommandsMap.get(addr).add(line.trim());
				}
			}
			
			br.close();
		}
		catch (IOException ex) {ex.printStackTrace(); MoreCommands.getMoreCommands().getLogger().info("Startup commands file could not be read");}
		
		return new Object[] {startupCommands, startupCommandsM, startupCommandsMap};
	}
	
	public List<String> getStartupCommands() {
		return new ArrayList<String>(this.startupCommands);
	}
	
	public List<String> getStartupCommandsMultiplayer(String socketAddress) {
		socketAddress = socketAddress.trim(); if (socketAddress.startsWith("/")) socketAddress = socketAddress.substring(1);
		List<String> commands = new ArrayList<String>(this.startupMultiplayerCommands);
		if (this.startupServerCommands.containsKey(socketAddress)) commands.addAll(this.startupServerCommands.get(socketAddress));
		return commands;
	}
}
