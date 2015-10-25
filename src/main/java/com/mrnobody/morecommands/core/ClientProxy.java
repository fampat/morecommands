package com.mrnobody.morecommands.core;

import java.util.Iterator;
import java.util.List;

import com.mrnobody.morecommands.command.ClientCommand;
import com.mrnobody.morecommands.command.CommandBase.ServerType;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.util.Reference;
import com.mrnobody.morecommands.util.XrayHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;

/**
 * The proxy used for the client
 * 
 * @author MrNobody98
 *
 */
public class ClientProxy extends CommonProxy {
	private boolean clientCommandsRegistered = false;
	
	@Override
	protected void setPatcher() {
		this.patcher = new ClientPatcher();
	}

	@Override
	protected void postInit(FMLPostInitializationEvent event) {
		super.postInit(event);
		if (this.clientCommandsRegistered = this.registerClientCommands()) 
			this.mod.getLogger().info("Client Commands successfully registered");
		XrayHelper.init();
	}
	
	@Override
	protected void serverStarted(FMLServerStartedEvent event) {} //NOOP

	@Override
	public boolean commandsLoaded() {
		if (this.getRunningServerType() == ServerType.INTEGRATED) return this.clientCommandsRegistered && super.commandsLoaded();
		else if (this.getRunningServerType() == ServerType.DEDICATED) return this.clientCommandsRegistered;
		else return false;
	}
	
	@Override
	public boolean registerHandlers() {
		ModContainer container = Loader.instance().activeModContainer();
		if (container == null || !container.getModId().equals(Reference.MODID)) return false;
		boolean errored = false;
		
		for (EventHandler handler : EventHandler.values())
			if (!EventHandler.register(handler, container)) {errored = true; break;}
		
		if (!errored) {this.mod.getLogger().info("Event Handlers registered"); return true;}
		else return false;
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
