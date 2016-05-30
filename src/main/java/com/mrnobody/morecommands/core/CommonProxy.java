package com.mrnobody.morecommands.core;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mrnobody.morecommands.command.MultipleCommands;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.event.EventHandler;
import com.mrnobody.morecommands.event.Listeners.EventListener;
import com.mrnobody.morecommands.event.Listeners.TwoEventListener;
import com.mrnobody.morecommands.network.PacketHandlerServer;
import com.mrnobody.morecommands.util.DummyCommand;
import com.mrnobody.morecommands.util.GlobalSettings;
import com.mrnobody.morecommands.util.LanguageManager;
import com.mrnobody.morecommands.util.MoreCommandsUpdater;
import com.mrnobody.morecommands.util.NBTSettingsManager;
import com.mrnobody.morecommands.util.ObfuscatedNames.ObfuscatedField;
import com.mrnobody.morecommands.util.PlayerSettings;
import com.mrnobody.morecommands.util.Reference;
import com.mrnobody.morecommands.util.ReflectionHelper;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.util.SettingsManager;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

/**
 * The common proxy class
 * 
 * @author MrNobody98
 *
 */
public class CommonProxy {
	protected CommonPatcher patcher;
	
	protected MoreCommands mod = MoreCommands.INSTANCE;
	private Field langCode = ReflectionHelper.getField(ObfuscatedField.EntityPlayerMP_language);
	
	private ITextComponent updateText = null;
	protected boolean playerNotified = false;
	private String currentWorld = null;
	
	public CommonProxy() {
		this.setPatcher();
	}
	
	/**
	 * @return the update chat text component
	 */
	public ITextComponent getUpdateText() {
		return this.updateText;
	}
	
	/**
	 * @return whether the player was already notified about an update
	 */
	public boolean wasPlayerNotified() {
		return this.playerNotified;
	}
	
	/**
	 * Fetches the current world's folder name and sends it to the player
	 * 
	 * @param player the player to which the world's folder name is to be sent
	 * @see CommonProxy#setCurrentWorld(String)
	 */
	public void updateWorld(EntityPlayerMP player) {
		if (!player.getServer().getFolderName().equals(this.currentWorld)) {
			this.currentWorld = player.getServer().getFolderName();
			
			if (MoreCommands.isServerSide())
				this.mod.getPacketDispatcher().sendS14ChangeWorld(player, currentWorld);
		}
	}
	
	/**
	 * Sets the world name of the world that is currently running on the server.
	 * <b>IMPORTANT:</b> "World name" means the name of the folder from which the world was loaded.
	 * This is necessary to be able to distinguish between worlds which have the same name
	 * but not the same folder so they are NOT the same worlds
	 * 
	 * @param world the world's folder name
	 */
	public void setCurrentWorld(String world) {
		this.currentWorld = world;
	}
	
	/**
	 * @return The folder name of the current world
	 * @see CommonProxy#setCurrentWorld(String)
	 */
	public String getCurrentWorld() {
		return this.currentWorld;
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
		if (GlobalSettings.searchUpdates) findMoreCommandsUpdates();
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
		
		try {
			this.registerServerCommands(event.getServer());
			this.mod.getLogger().info("Server Commands successfully registered");
		}
		catch (Exception ex) {this.mod.getLogger().warn("Failed to register Server Command", ex);}
		
		if (GlobalSettings.retryHandshake)
			PacketHandlerServer.startHandshakeRetryThread();
	}
	
	/**
	 * Called from the main mod class to do stuff after the server was started.
	 * On dedicated environments, this is executing server startup commands
	 */
	protected void serverStarted(FMLServerStartedEvent event) {
		if (MoreCommands.isServerSide()) PacketHandlerServer.executeStartupCommands();
	}
	
	/**
	 * Called from the main mod class to do stuff before the server is stopping
	 */
	protected void serverStopping(FMLServerStoppingEvent event) {} //NOOP on dedicated environment

	/**
	 * Called from the main mod class to handle the server stop
	 */
	protected void serverStop(FMLServerStoppedEvent event) {
		if (GlobalSettings.retryHandshake)
			PacketHandlerServer.stopHandshakeRetryThread();
		
		GlobalSettings.writeSettings();
		
		for (EventHandler handler : EventHandler.getAllCreatedEventHandlers()) {
			for (TwoEventListener listener : (Set<TwoEventListener>) handler.getDoubleListeners()) {
				if (listener instanceof ServerCommandProperties) handler.unregister(listener);
			}
			
			for (EventListener listener : (Set<EventListener>) handler.getListeners()) {
				if (listener instanceof ServerCommandProperties) handler.unregister(listener);
			}
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
	 * Starts a thread looking for MoreCommands updates
	 */
	private void findMoreCommandsUpdates() {
		this.mod.getLogger().info("Searching for MoreCommands updates");
		
		new Thread(new MoreCommandsUpdater(Loader.MC_VERSION, new MoreCommandsUpdater.UpdateCallback() {
			@Override
			public void udpate(String version, String website, String download) {
				TextComponentString text = new TextComponentString(Reference.VERSION.equals(version) ? 
						"MoreCommands update for this version found " : "new MoreCommands version found: "); 
				text.getStyle().setColor(TextFormatting.BLUE);
				TextComponentString downloadVersion = new TextComponentString(version); downloadVersion.getStyle().setColor(TextFormatting.YELLOW);
				TextComponentString homepage = new TextComponentString("Minecraft Forum"); homepage.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, website)).setColor(TextFormatting.GREEN).setItalic(true).setUnderlined(true);
				TextComponentString downloadPage = new TextComponentString("Download"); downloadPage.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, download)).setColor(TextFormatting.GREEN).setItalic(true).setUnderlined(true);
				TextComponentString comma = new TextComponentString(", "); comma.getStyle().setColor(TextFormatting.DARK_GRAY);
				TextComponentString sep = new TextComponentString(" - "); sep.getStyle().setColor(TextFormatting.DARK_GRAY);
				
				String rawText = text.getUnformattedText() + (Reference.VERSION.equals(version) ? "" : downloadVersion.getUnformattedText()) + " - " + website + ", " + download;
				if (!Reference.VERSION.equals(version)) text.appendSibling(downloadVersion);
				text.appendSibling(sep).appendSibling(homepage).appendSibling(comma).appendSibling(downloadPage);
				
				CommonProxy.this.mod.getLogger().info(rawText);
				CommonProxy.this.updateText = text;
				
				if (MoreCommands.isClientSide() && net.minecraft.client.Minecraft.getMinecraft().thePlayer != null) {
					CommonProxy.this.playerNotified = true;
					net.minecraft.client.Minecraft.getMinecraft().thePlayer.addChatMessage(text);
				}
			}
		}), "MoreCommands Update Thread").start();
	}
	
	/**
	 * Registers Event Handlers
	 */
	public void registerHandlers() {
		ModContainer container = Loader.instance().activeModContainer();
		
		if (container == null || !container.getModId().equals(Reference.MODID)){
			this.mod.getLogger().warn("Error registering Event Handlers");
			return;
		}
		
		EventHandler.registerDefaultForgeHandlers(container, false);
		this.mod.getLogger().info("Event Handlers registered");
	}
	
	/**
	 * Registers all server commands
	 * 
	 * @return Whether the server commands were registered successfully
	 */
	private void registerServerCommands(MinecraftServer server) throws Exception {
		List<Class<? extends StandardCommand>> serverCommands = this.mod.getServerCommandClasses();
		if (serverCommands == null) throw new RuntimeException("Server Command Classes not loaded");
		ServerCommandManager commandManager = (ServerCommandManager) server.getCommandManager(); 
		
		for (Class<? extends StandardCommand> cmdClass : serverCommands) {
			try {
				StandardCommand cmd = cmdClass.newInstance();
				
				if (cmd instanceof MultipleCommands) {
					Constructor<? extends StandardCommand> ctr = cmdClass.getConstructor(int.class);
					
					for (int i = 0; i < ((MultipleCommands) cmd).getNames().length; i++)
						if (this.mod.isCommandEnabled(((MultipleCommands) cmd).getNames()[i]))
							commandManager.registerCommand(new ServerCommand(ServerCommand.upcast(ctr.newInstance(i))));
				}
				else if (this.mod.isCommandEnabled(cmd.getCommandName()))
					commandManager.registerCommand(new ServerCommand(ServerCommand.upcast(cmd)));
			}
			catch (Exception ex) {
				this.mod.getLogger().warn("Skipping Server Command " + cmdClass.getName() + " due to the following exception during loading", ex);
			}
		}
	}
	
	/**
	 * @return The Server Type the player plays on
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
	
	/**
	 * @return the server address of the current server
	 */
	public String getCurrentServerNetAddress() {
		return (FMLCommonHandler.instance().getMinecraftServerInstance().getServerHostname().length() > 0 ? 
				FMLCommonHandler.instance().getMinecraftServerInstance().getServerHostname() : "*") + 
				":" + FMLCommonHandler.instance().getMinecraftServerInstance().getServerPort();
	}
	
	/**
	 * Registers aliases that the player created to the command manager
	 * 
	 * @param player the player whose aliases shall be registered
	 */
	public void registerAliases(EntityPlayer player) {
		if (player instanceof EntityPlayerMP) {
			ServerPlayerSettings settings = player.getCapability(PlayerSettings.SETTINGS_CAP_SERVER, null);
			if (settings != null) this.registerServerAliases(player.getServer(), settings);
		}
	}
	
	/**
	 * Reads aliases for the given server player and registers them
	 */
	private void registerServerAliases(MinecraftServer server, ServerPlayerSettings settings) {
		Map<String, String> aliases = settings.aliases;
		net.minecraft.command.CommandHandler commandHandler = (net.minecraft.command.CommandHandler) server.getCommandManager();
		String command;
		
		for (String alias : aliases.keySet()) {
			command = aliases.get(alias).split(" ")[0];
			
			if (!command.equalsIgnoreCase(alias) && !commandHandler.getCommands().containsKey(alias)) {
				DummyCommand cmd = new DummyCommand(alias, false);
				commandHandler.getCommands().put(alias, cmd);
			}
		}
	}
	
	/**
	 * Creates a {@link SettingsManager} for a player
	 * 
	 * @param player the player to whom the settings manager belongs to
	 * @return the settings manager
	 */
	public SettingsManager createSettingsManagerForPlayer(EntityPlayer player) {
		return new NBTSettingsManager(new File(Reference.getSettingsDirServer(), player.getUniqueID().toString() + ".dat"), true, false);
	}
}
