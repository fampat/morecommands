package com.mrnobody.morecommands.core;

import java.util.Iterator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;

import com.mrnobody.morecommands.command.ClientCommand;
import com.mrnobody.morecommands.command.CommandBase.ServerType;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.network.PacketHandlerClient;
import com.mrnobody.morecommands.util.GlobalSettings;
import com.mrnobody.morecommands.util.Reference;
import com.mrnobody.morecommands.util.XrayHelper;

/**
 * The proxy used for the client
 * 
 * @author MrNobody98
 *
 */
public class ClientProxy extends CommonProxy {
	private Patcher clientPatcher;
	private MoreCommands mod;
	
	private boolean clientCommandsRegistered = false;
	private boolean serverCommandsRegistered = false;
	
	public ClientProxy() {
		this.clientPatcher = new ClientPatcher();
		this.mod = MoreCommands.getMoreCommands();
	}
	
	@Override
	public void preInit(FMLPreInitializationEvent event) {
		this.clientPatcher.applyModStatePatch(event);
	}

	@Override
	public void init(FMLInitializationEvent event) {
		this.clientPatcher.applyModStatePatch(event);
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {
		this.clientPatcher.applyModStatePatch(event);
		if (this.clientCommandsRegistered = this.registerClientCommands()) 
			this.mod.getLogger().info("Client Commands successfully registered");
		XrayHelper.init();
	}

	@Override
	public void serverStart(FMLServerAboutToStartEvent event) {
		Patcher.setServerModded(true);
		this.clientPatcher.applyModStatePatch(event);
	}

	@Override
	public void serverInit(FMLServerStartingEvent event) {
		this.clientPatcher.applyModStatePatch(event);
		GlobalSettings.readSettings();
		if (this.serverCommandsRegistered = this.registerServerCommands()) 
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
		return this.clientPatcher;
	}

	@Override
	public boolean commandsLoaded() {
		if (this.getRunningServerType() == ServerType.INTEGRATED) return this.clientCommandsRegistered && this.serverCommandsRegistered;
		else if (this.getRunningServerType() == ServerType.DEDICATED) return this.clientCommandsRegistered;
		else return false;
	}
	
	@Override
	public boolean registerHandlers() {
		ModContainer container = Loader.instance().activeModContainer();
		if (container == null || !container.getModId().equals(Reference.MODID)) return false;
		
		for (EventHandler handler : EventHandler.values())
			EventHandler.register(handler.getBus(), handler.getHandler(), container);
		
		this.mod.getLogger().info("Event Handlers registered");
		return true;
	}
	
	/**
	 * Registers all client commands
	 * 
	 * @return Whether the client commands were registered successfully
	 */
	private boolean registerClientCommands() {
		List<Class<? extends ClientCommand>> clientCommands = this.mod.getClientCommandClasses();
		if (clientCommands == null) return false;
		Iterator<Class<? extends ClientCommand>> commandIterator = clientCommands.iterator();
		
		try {
			while (commandIterator.hasNext()) {
				ClientCommand clientCommand = commandIterator.next().newInstance();
				if (this.mod.isCommandEnabled(clientCommand.getName()))
						ClientCommandHandler.instance.registerCommand(clientCommand);
			}
			
			return true;
		}
		catch (Exception ex) {ex.printStackTrace(); return false;}
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
		if (Minecraft.getMinecraft().isSingleplayer()) return ServerType.INTEGRATED;
		else return ServerType.DEDICATED;
	}
	
	@Override
	public String getLang(ICommandSender sender) {
		return Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode();
	}
}
