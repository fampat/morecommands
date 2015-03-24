package com.mrnobody.morecommands.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import net.minecraft.command.ICommandSender;

import org.apache.logging.log4j.Logger;

import com.mrnobody.morecommands.command.ClientCommand;
import com.mrnobody.morecommands.command.CommandBase.ServerType;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.util.DynamicClassLoader;
import com.mrnobody.morecommands.util.LanguageManager;
import com.mrnobody.morecommands.util.Reference;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

/**
 * The main mod class loaded by forge
 * 
 * @author MrNobody98
 *
 */

@Mod(modid = Reference.MODID, version = Reference.VERSION, name = Reference.NAME, acceptableRemoteVersions = "*")
public class MoreCommands {
	@SidedProxy(clientSide="com.mrnobody.morecommands.core.ClientProxy", serverSide="com.mrnobody.morecommands.core.ServerProxy", modId=Reference.MODID)
	public static CommonProxy proxy;
	
	@Instance
	private static MoreCommands instance;
	
	public static final DynamicClassLoader CLASSLOADER = new DynamicClassLoader(MoreCommands.class.getClassLoader());
	
	private static final String CHANNEL = "mrnobody_cmd";
	private static SimpleNetworkWrapper network;
	
	private static UUID playerUUID;
	private static Logger logger;
	
	private static boolean handlersLoaded = false;
	private static boolean packetsLoaded = false;
	
	private static final String clientCommandsPackage = "com.mrnobody.morecommands.command.client";
	private static List<Class<? extends ClientCommand>> clientCommandClasses = new ArrayList<Class<? extends ClientCommand>>();
	
	private static final String serverCommandsPackage = "com.mrnobody.morecommands.command.server";
	private static List<Class<? extends ServerCommand>> serverCommandClasses = new ArrayList<Class<? extends ServerCommand>>();
	
	private static final String clientPacketPackage = "com.mrnobody.morecommands.packet.client";
	private static List<Class<?>> clientPacketClasses = new ArrayList<Class<?>>();
	
	private static final String serverPacketPackage = "com.mrnobody.morecommands.packet.server";
	private static List<Class<?>> serverPacketClasses = new ArrayList<Class<?>>();
	
	private static List<String> disabledCommands;
	
	/**
	 * @return The Singleton mod instance
	 */
	public static MoreCommands getModInstance() {
		return MoreCommands.instance;
	}
	
	/**
	 * @return The running proxy
	 */
	public static CommonProxy getProxy() {
		return MoreCommands.proxy;
	}
	
	/**
	 * @return The UUID for the server side player or null if the mod isn't installed server side
	 */
	public static UUID getPlayerUUID() {
		return MoreCommands.playerUUID;
	}
	
	/**
	 * Sets the player UUID
	 */
	public static void setPlayerUUID(UUID uuid) {
		MoreCommands.playerUUID = uuid;
	}
	
	/**
	 * @return A list of commands, which shall be disabled
	 */
	public static List<String> getDisabledCommands() {
		return MoreCommands.disabledCommands;
	}
	
	/**
	 * @return Whether the mod runs on a dedicated server
	 */
	public static boolean isServerSide() {
		return MoreCommands.proxy instanceof ServerProxy;
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
	public static ServerType getRunningServer() {
		return MoreCommands.proxy.getRunningServerType();
	}
	
	/**
	 * @return The Patcher Instance
	 */
	public static Patcher getPatcherInstance() {
		return MoreCommands.proxy.getPatcher();
	}
	
	/**
	 * @return The Mod Logger
	 */
	public static Logger getLogger() {
		return MoreCommands.logger;
	}
	
	/**
	 * @return The Network Wrapper
	 */
	public static SimpleNetworkWrapper getNetwork() {
		return MoreCommands.network;
	}
	
	/**
	 * @return The Client Command Classes
	 */
	public static List<Class<? extends ClientCommand>> getClientCommandClasses() {
		return MoreCommands.clientCommandClasses;
	}
	
	/**
	 * @return The Server Command Classes
	 */
	public static List<Class<? extends ServerCommand>> getServerCommandClasses() {
		return MoreCommands.serverCommandClasses;
	}
	
	/**
	 * @return Whether the mod is enabled
	 */
	public static boolean isModEnabled() {
		return MoreCommands.proxy.commandsLoaded() && MoreCommands.handlersLoaded && MoreCommands.packetsLoaded;
	}
	
	/**
	 * @return Whether the command is enabled
	 */
	public static boolean isCommandEnabled(String command) {
		return !MoreCommands.disabledCommands.contains(command);
	}
	
	/**
	 * @return The current language
	 */
	public static String getCurrentLang(ICommandSender sender) {
		return MoreCommands.proxy.getLang(sender);
	}
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		MoreCommands.logger = event.getModLog();
		Reference.init(event);
		LanguageManager.readTranslations();
		MoreCommands.network = NetworkRegistry.INSTANCE.newSimpleChannel(MoreCommands.CHANNEL);
		
		if (MoreCommands.packetsLoaded = this.registerPackets()) MoreCommands.getLogger().info("Packets successfully registered");
		this.loadCommands();
		MoreCommands.disabledCommands = this.readDisabledCommands();
		
		MoreCommands.proxy.preInit(event);
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
		MoreCommands.handlersLoaded = MoreCommands.proxy.registerHandlers();
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
	public void serverStop(FMLServerStoppedEvent event) {
		MoreCommands.proxy.serverStop(event);
	}
	
	/**
	 * Loads Command Classes
	 * 
	 * @return if the commands were loaded successfully
	 */
	private boolean loadCommands() {
		List<Class<?>> clientCommands = MoreCommands.CLASSLOADER.getCommandClasses(MoreCommands.clientCommandsPackage, ClientCommand.class);
		Iterator<Class<?>> clientCommandIterator = clientCommands.iterator();
		
		while (clientCommandIterator.hasNext()) {
			try {
				Class<? extends ClientCommand> command = clientCommandIterator.next().asSubclass(ClientCommand.class);
				MoreCommands.clientCommandClasses.add(command);
			}
			catch (Exception ex) {ex.printStackTrace(); return false;}
		}
		
		List<Class<?>> serverCommands = MoreCommands.CLASSLOADER.getCommandClasses(MoreCommands.serverCommandsPackage, ServerCommand.class);
		Iterator<Class<?>> serverCommandIterator = serverCommands.iterator();
		
		while (serverCommandIterator.hasNext()) {
			try {
				Class<? extends ServerCommand> handler = serverCommandIterator.next().asSubclass(ServerCommand.class);
				MoreCommands.serverCommandClasses.add(handler);
			}
			catch (Exception ex) {ex.printStackTrace(); return false;}
		}
		
		return true;
	}
	
	/**
	 * Registers all Packets
	 * 
	 * @return if the packets were registered successfully
	 */
	private boolean registerPackets() {
		List<Class<?>> packets = new ArrayList<Class<?>>();
		packets.addAll(MoreCommands.CLASSLOADER.getPacketClasses(MoreCommands.clientPacketPackage, Side.CLIENT));
		packets.addAll(MoreCommands.CLASSLOADER.getPacketClasses(MoreCommands.serverPacketPackage, Side.SERVER));
		
		int discriminator = 0;
		Method register;
		
		for (Class<?> packet : packets) {
			try {
				register = packet.getMethod("register", int.class);
				
				if (register != null) {
					register.invoke(null, discriminator);
					discriminator++;
				}
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
			while ((line = br.readLine()) != null) {disabled.add(line); MoreCommands.getLogger().info("Disabling command '" + line + "'");}
			br.close();
		}
		catch (IOException ex) {ex.printStackTrace(); MoreCommands.getLogger().info("Could not read disable.cfg");}
		
		return disabled;
	}
}
