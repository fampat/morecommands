package com.mrnobody.morecommands.core;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.List;

import com.mrnobody.morecommands.command.ClientCommand;
import com.mrnobody.morecommands.command.MultipleCommands;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.event.EventHandler;
import com.mrnobody.morecommands.settings.JsonSettingsManager;
import com.mrnobody.morecommands.settings.SettingsManager;
import com.mrnobody.morecommands.util.Reference;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;

/**
 * The proxy used for the client
 * 
 * @author MrNobody98
 *
 */
public class ClientProxy extends CommonProxy {
	private SettingsManager settingsManager;
	private String remoteWorldName;
	
	public ClientProxy() {
		super();
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				ClientProxy.this.settingsManager.saveSettings();
			}
		}));
	}
	
	@Override
	protected void setPatcher() {
		this.patcher = new ClientPatcher();
	}

	@Override
	protected void postInit(FMLPostInitializationEvent event) {
		super.postInit(event);
		this.settingsManager = new JsonSettingsManager(new File(Reference.getModDir(), "settings_client.json"), true);
		
		try {
			this.registerClientCommands();
			this.mod.getLogger().info("Client Commands successfully registered");
		}
		catch (Exception ex) {this.mod.getLogger().warn("Failed to register Client Command", ex);}
	}
	
	@Override
	protected void serverStopping(FMLServerStoppingEvent event) {
		//A PlayerLoggedOutEvent is not posted for the local player on an integrated server
		//But in CommonPatcher.playerLogout(), the ServerPlayerSettings are saved, so we have to "post" that event manually
		
		if (MinecraftServer.getServer() != null && !MinecraftServer.getServer().isDedicatedServer()) {
			for (Object o : MinecraftServer.getServer().getConfigurationManager().playerEntityList)
				if (MinecraftServer.getServer().getServerOwner().equals(((EntityPlayer) o).getName()))
						this.patcher.playerLogout(new PlayerLoggedOutEvent((EntityPlayer) o));
		}
	}
	
	@Override
	public void registerHandlers() {
		ModContainer container = Loader.instance().activeModContainer();
		
		if (container == null || !container.getModId().equals(Reference.MODID)){
			this.mod.getLogger().warn("Error registering Event Handlers");
			return;
		}
		
		EventHandler.registerDefaultForgeHandlers(container, true);
		this.mod.getLogger().info("Event Handlers registered");
	}
	
	/**
	 * Registers all client commands
	 * 
	 * @return Whether the client commands were registered successfully
	 */
	private void registerClientCommands() throws Exception {
		List<Class<? extends StandardCommand>> clientCommands = this.mod.getClientCommandClasses();
		if (clientCommands == null) throw new RuntimeException("Client Command Classes not loaded");
		
		for (Class<? extends StandardCommand> cmdClass : clientCommands) {
			try {
				StandardCommand cmd = cmdClass.newInstance();
				
				if (cmd instanceof MultipleCommands) {
					Constructor<? extends StandardCommand> ctr = cmdClass.getConstructor(int.class);
					
					for (int i = 0; i < ((MultipleCommands) cmd).getCommandNames().length; i++)
						if (this.mod.isCommandEnabled(((MultipleCommands) cmd).getCommandNames()[i]))
							ClientCommandHandler.instance.registerCommand(new ClientCommand(ClientCommand.upcast(ctr.newInstance(i))));
				}
				else if (this.mod.isCommandEnabled(cmd.getCommandName()))
					ClientCommandHandler.instance.registerCommand(new ClientCommand(ClientCommand.upcast(cmd)));
			}
			catch (Exception ex) {
				this.mod.getLogger().warn("Skipping Client Command " + cmdClass.getName() + " due to the following exception during loading", ex);
			}
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
	public void setRemoteWorldName(String name) {
		this.remoteWorldName = name;
	}
	
	/**
	 * Gets the name of the world running on the server. Since Minecraft does not
	 * send this information to the client the server is required to have MoreCommands
	 * installed in order to receive this information. The default name if the world
	 * name ist not received from the server is "MpServer"
	 * 
	 * @return the name of the world running on the server
	 */
	public String getRemoteWorldName() {
		return this.remoteWorldName == null ? "MpServer" : this.remoteWorldName;
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
	

	@Override
	public String getCurrentServerNetAddress() {
		return Minecraft.getMinecraft().getCurrentServerData() != null ? Minecraft.getMinecraft().getCurrentServerData().serverIP : "singleplayer";
	}
	
	@Override
	public SettingsManager createSettingsManagerForPlayer(EntityPlayer player) {
		if (player instanceof EntityPlayerSP) return this.settingsManager;
		else if (player instanceof EntityPlayerMP) {
			if (((EntityPlayerMP) player).getName().equals(MinecraftServer.getServer().getServerOwner())) return this.settingsManager;
			else return super.createSettingsManagerForPlayer(player);
		}
		else return super.createSettingsManagerForPlayer(player);
	}
}
