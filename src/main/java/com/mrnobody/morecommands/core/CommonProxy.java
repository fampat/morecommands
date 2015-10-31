package com.mrnobody.morecommands.core;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Handler;
import com.mrnobody.morecommands.handler.Listeners.EventListener;
import com.mrnobody.morecommands.handler.Listeners.TwoEventListener;
import com.mrnobody.morecommands.handler.PacketHandler;
import com.mrnobody.morecommands.network.PacketHandlerServer;
import com.mrnobody.morecommands.util.GlobalSettings;
import com.mrnobody.morecommands.util.LanguageManager;
import com.mrnobody.morecommands.util.MoreCommandsUpdater;
import com.mrnobody.morecommands.util.Reference;
import com.mrnobody.morecommands.util.ReflectionHelper;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.event.ClickEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;

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
		
		if (this.serverCommandsRegistered = this.registerServerCommands()) 
			this.mod.getLogger().info("Server Commands successfully registered");
		
		if (GlobalSettings.retryHandshake)
			PacketHandlerServer.startHandshakeRetryThread();
	}
	
	protected void serverStarted(FMLServerStartedEvent event) {
		PacketHandlerServer.executeStartupCommands();
	}

	/**
	 * Called from the main mod class to handle the server stop
	 */
	protected void serverStop(FMLServerStoppedEvent event) {
		if (GlobalSettings.retryHandshake)
			PacketHandlerServer.stopHandshakeRetryThread();
		
		GlobalSettings.writeSettings();
		
		List<Handler> handlers = new ArrayList<Handler>(EventHandler.values().length + PacketHandler.values().length);
		for (EventHandler handler : EventHandler.values()) handlers.add(handler.getHandler());
		for (PacketHandler handler : PacketHandler.values()) handlers.add(handler.getHandler());
		
		for (Handler handler : handlers) {
			for (TwoEventListener listener : (Set<TwoEventListener>) handler.getDoubleListeners()) {
				if (listener instanceof ServerCommand) handler.unregister(listener);
			}
			
			for (EventListener listener : (Set<EventListener>) handler.getListeners()) {
				if (listener instanceof ServerCommand) handler.unregister(listener);
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
	 * @return Whether this proxy loaded the commands successfully
	 */
	public boolean commandsLoaded() {
		return this.serverCommandsRegistered;
	}
	
	private void findMoreCommandsUpdates() {
		MoreCommands.getMoreCommands().getLogger().info("Searching for MoreCommands updates");
		
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
				
				MoreCommands.getMoreCommands().getLogger().info(rawText);
				CommonProxy.this.updateText = text;
				
				if (MoreCommands.isClientSide() && net.minecraft.client.Minecraft.getMinecraft().thePlayer != null) {
					CommonProxy.this.playerNotified = true;
					net.minecraft.client.Minecraft.getMinecraft().thePlayer.addChatMessage(text);
				}
			}
		}), "MoreCommands Update Thread").start();
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
					if (this.mod.isCommandEnabled(serverCommand.getName()))
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
