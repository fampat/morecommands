package com.mrnobody.morecommands.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import net.minecraft.command.ICommandSender;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.relauncher.Side;

import org.apache.logging.log4j.Logger;

import com.mrnobody.morecommands.command.ClientCommand;
import com.mrnobody.morecommands.command.CommandBase.ServerType;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.network.PacketDispatcher;
import com.mrnobody.morecommands.util.DynamicClassLoader;
import com.mrnobody.morecommands.util.LanguageManager;
import com.mrnobody.morecommands.util.Reference;

@Mod(modid = Reference.MODID, version = Reference.VERSION, name = Reference.NAME, acceptableRemoteVersions = "*")
public class MoreCommands {
	@Instance
	private static MoreCommands instance;
	
	@SidedProxy(clientSide="com.mrnobody.morecommands.core.ClientProxy", serverSide="com.mrnobody.morecommands.core.ServerProxy", modId=Reference.MODID)
	private static CommonProxy proxy;
	
	public static final DynamicClassLoader CLASSLOADER = new DynamicClassLoader(MoreCommands.class.getClassLoader());
	
	private PacketDispatcher dispatcher;
	private UUID playerUUID;
	private Logger logger;
	private boolean handlersLoaded = false;
	
	private final String clientCommandsPackage = "com.mrnobody.morecommands.command.client";
	private List<Class<? extends ClientCommand>> clientCommandClasses = new ArrayList<Class<? extends ClientCommand>>();
	
	private final String serverCommandsPackage = "com.mrnobody.morecommands.command.server";
	private List<Class<? extends ServerCommand>> serverCommandClasses = new ArrayList<Class<? extends ServerCommand>>();
	
	private final String clientPacketPackage = "com.mrnobody.morecommands.packet.client";
	private List<Class<?>> clientPacketClasses = new ArrayList<Class<?>>();
	
	private final String serverPacketPackage = "com.mrnobody.morecommands.packet.server";
	private List<Class<?>> serverPacketClasses = new ArrayList<Class<?>>();
	
	private List<String> disabledCommands;
	
	//Need this because forge injects the instance too late -> Causes a NullpointerException using getMoreCommands
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
		this.dispatcher = new PacketDispatcher();
		this.loadCommands();
		this.disabledCommands = this.readDisabledCommands();
		
		MoreCommands.proxy.preInit(event);
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
		Reference.setSavesDir(FMLCommonHandler.instance().getSavesDirectory());
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
}
