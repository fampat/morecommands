package com.mrnobody.morecommands.core;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
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
import com.mrnobody.morecommands.settings.MoreCommandsConfig;
import com.mrnobody.morecommands.settings.NBTSettingsManager;
import com.mrnobody.morecommands.settings.SettingsManager;
import com.mrnobody.morecommands.util.ChatChannel;
import com.mrnobody.morecommands.util.LanguageManager;
import com.mrnobody.morecommands.util.MoreCommandsUpdater;
import com.mrnobody.morecommands.util.ObfuscatedNames.ObfuscatedField;
import com.mrnobody.morecommands.util.Reference;
import com.mrnobody.morecommands.util.ReflectionHelper;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.event.ClickEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

/**
 * The common proxy class
 * 
 * @author MrNobody98
 *
 */
public class CommonProxy {
	protected CommonPatcher patcher;
	
	protected MoreCommands mod = MoreCommands.INSTANCE;
	private Field langCode = ReflectionHelper.getField(ObfuscatedField.EntityPlayerMP_translator);
	
	private IChatComponent updateText = null;
	protected boolean playerNotified = false;
	
	public CommonProxy() {
		this.setPatcher();
	}
	
	/**
	 * @return the update chat text component
	 */
	public IChatComponent getUpdateText() {
		return this.updateText;
	}
	
	/**
	 * @return whether the player was already notified about an update
	 */
	public boolean wasPlayerNotified() {
		return this.playerNotified;
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
		if (MoreCommandsConfig.searchUpdates) findMoreCommandsUpdates();
	}

	/**
	 * Starts a thread looking for MoreCommands updates
	 */
	private void findMoreCommandsUpdates() {
		this.mod.getLogger().info("Searching for MoreCommands updates");
		
		new Thread(new MoreCommandsUpdater(Loader.MC_VERSION, new MoreCommandsUpdater.UpdateCallback() {
			@Override
			public void udpate(String version, String website, String download) {
				ChatComponentText text = new ChatComponentText(Reference.VERSION.equals(version) ? 
						"MoreCommands update for this version found " : "new MoreCommands version found: "); 
				text.getChatStyle().setColor(EnumChatFormatting.BLUE);
				ChatComponentText downloadVersion = new ChatComponentText(version); downloadVersion.getChatStyle().setColor(EnumChatFormatting.YELLOW);
				ChatComponentText homepage = new ChatComponentText("Minecraft Forum"); homepage.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, website)).setColor(EnumChatFormatting.GREEN).setItalic(true).setUnderlined(true);
				ChatComponentText downloadPage = new ChatComponentText("Download"); downloadPage.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, download)).setColor(EnumChatFormatting.GREEN).setItalic(true).setUnderlined(true);
				ChatComponentText comma = new ChatComponentText(", "); comma.getChatStyle().setColor(EnumChatFormatting.DARK_GRAY);
				ChatComponentText sep = new ChatComponentText(" - "); sep.getChatStyle().setColor(EnumChatFormatting.DARK_GRAY);
				
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
		ensureChatChannelsLoaded();
	}
	
	/**
	 * Called from the main mod class to handle the server start
	 */
	protected void serverInit(FMLServerStartingEvent event) {
		this.patcher.applyModStatePatch(event);
		
		try {
			this.registerServerCommands();
			this.mod.getLogger().info("Server Commands successfully registered");
		}
		catch (Exception ex) {this.mod.getLogger().warn("Failed to register Server Command", ex);}
		
		if (MoreCommandsConfig.retryHandshake)
			PacketHandlerServer.startHandshakeRetryThread();
	}
	
	/**
	 * Called from the main mod class to do stuff after the server was started.
	 * Currently this only means executing server startup commands
	 */
	protected void serverStarted(FMLServerStartedEvent event) {
		PacketHandlerServer.executeStartupCommands();
	}
	
	/**
	 * Called from the main mod class to do stuff before the server is stopping
	 */
	protected void serverStopping(FMLServerStoppingEvent event) {} //NOOP on dedicated environment
	
	/**
	 * Called from the main mod class to handle the server stop
	 */
	protected void serverStop(FMLServerStoppedEvent event) {
		if (MoreCommandsConfig.retryHandshake)
			PacketHandlerServer.stopHandshakeRetryThread();
		
		MoreCommandsConfig.writeConfig();
		ChatChannel.saveChannelsToSettings(new File(Reference.getModDir(), "chatChannels.json"));
		
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
	 * Ensures that all chat channes have been loaded from disk. This ensures that {@link ChatChannel#getMasterChannel()}
	 * is not null
	 * @see ChatChannel
	 */
	public void ensureChatChannelsLoaded() {
		if (!ChatChannel.channelsLoaded())
			ChatChannel.loadChannelsFromSettings(new File(Reference.getModDir(), "chatChannels.json"));
	}
	
	/**
	 * Called from the main mod class to get the patcher instance
	 * @return the patcher
	 */
	public CommonPatcher getPatcher() {
		return this.patcher;
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
	private void registerServerCommands() throws Exception {
		List<Class<? extends StandardCommand>> serverCommands = this.mod.getServerCommandClasses();
		if (serverCommands == null) throw new RuntimeException("Server Command Classes not loaded");
		ServerCommandManager commandManager = (ServerCommandManager) MinecraftServer.getServer().getCommandManager(); 
		
		for (Class<? extends StandardCommand> cmdClass : serverCommands) {
			try {
				StandardCommand cmd = cmdClass.newInstance();
				
				if (cmd instanceof MultipleCommands) {
					Constructor<? extends StandardCommand> ctr = cmdClass.getConstructor(int.class);
					
					for (int i = 0; i < ((MultipleCommands) cmd).getCommandNames().length; i++)
						if (this.mod.isCommandEnabled(((MultipleCommands) cmd).getCommandNames()[i]))
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
		return (MinecraftServer.getServer().getHostname().length() > 0 ? MinecraftServer.getServer().getHostname() : "*") + ":" + MinecraftServer.getServer().getPort();
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
