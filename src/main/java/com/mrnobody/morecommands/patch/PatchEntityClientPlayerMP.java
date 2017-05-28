package com.mrnobody.morecommands.patch;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import com.mrnobody.morecommands.command.AbstractCommand.ResultAcceptingCommandSender;
import com.mrnobody.morecommands.util.EntityCamera;
import com.mrnobody.morecommands.util.ObfuscatedNames.ObfuscatedField;
import com.mrnobody.morecommands.util.ReflectionHelper;

import cpw.mods.fml.client.ExtendedServerListData;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.GuiAccessDenied;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLStateEvent;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ServerListEntryLanDetected;
import net.minecraft.client.gui.ServerListEntryNormal;
import net.minecraft.client.gui.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerAddress;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.LanServerDetector;
import net.minecraft.client.network.OldServerPinger;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.potion.Potion;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.StatFileWriter;
import net.minecraft.stats.StatList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Session;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.ForgeHooks;

/**
 * This patch uses a hacky series of object substitutions to eventually replace
 * all created {@link net.minecraft.client.entity.EntityPlayerSP} objects with
 * a modified version and a modified version of {@link net.minecraft.client.multiplayer.PlayerControllerMP}.<br>
 * This patch is needed to make several commands available and working, <br>
 * e.g. climbing on any wall, free-/freezecam, modifying reach distance, etc.
 * 
 * @author MrNobody98
 *
 */
public class PatchEntityClientPlayerMP implements PatchManager.StateEventBasedPatch, PatchManager.ForgeEventBasedPatch {
	private static final Field serverNameField = ReflectionHelper.getField(ObfuscatedField.Minecraft_serverName);
	private static final Field serverPortField = ReflectionHelper.getField(ObfuscatedField.Minecraft_serverPort);
	private static final Field networkManagers = ReflectionHelper.getField(ObfuscatedField.NetworkSystem_networkManagers);
	private static final Field startupConnectionDataField = ReflectionHelper.getField(ObfuscatedField.FMLClientHandler_startupConnectionData);
	private static final Field playClientBlockField = ReflectionHelper.getField(ObfuscatedField.FMLClientHandler_playClientBlock);
	private static final Field serverDataTagField = ReflectionHelper.getField(ObfuscatedField.FMLClientHandler_serverDataTag);
	private static final Field field_146798_g = ReflectionHelper.getField(ObfuscatedField.GuiMultiplayer_field_146798_g);
	private static final Field field_146803_h = ReflectionHelper.getField(ObfuscatedField.GuiMultiplayer_field_146803_h);
	private static final Field field_146813_x = ReflectionHelper.getField(ObfuscatedField.GuiMultiplayer_field_146813_x);
	private static final Field field_146811_z = ReflectionHelper.getField(ObfuscatedField.GuiMultiplayer_field_146811_z);
	private static final Field myNetworkManager = ReflectionHelper.getField(ObfuscatedField.Minecraft_myNetworkManager);
	
	private String startupServerName;
	private int startupServerPort;
	private String displayName;
	
	PatchEntityClientPlayerMP(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public <T extends FMLStateEvent> boolean needsToBeApplied(T event) {
		return event instanceof FMLServerAboutToStartEvent ? !((FMLServerAboutToStartEvent) event).getServer().isDedicatedServer() : true;
	}
	
	@Override
	public <T extends Event> boolean needsToBeApplied(T e) {
		GuiOpenEvent event = (GuiOpenEvent) e;
		return event.gui instanceof net.minecraft.client.gui.GuiMultiplayer || (event.gui instanceof GuiMainMenu && this.startupServerName != null);
	}

	@Override
	public String getDisplayName() {
		return this.displayName;
	}

	@Override
	public String getFailureConsequences() {
		return "Disables several commands which rely on modifications on EntityPlayerSP and PlayerControllerMP such as reach, climb, ...";
	}
	
	@Override
	public <T extends Event> boolean printLogFor(T event) {
		return true;
	}
	
	@Override
	public <T extends FMLStateEvent> boolean printLogFor(T event) {
		return !(event instanceof FMLPostInitializationEvent);
	}
	
	@Override
	public Collection<Class<? extends Event>> forgeEventClasses() {
		return Sets.<Class<? extends Event>>newHashSet(GuiOpenEvent.class);
	}
	
	@Override
	public Collection<Class<? extends FMLStateEvent>> stateEventClasses() {
		return Sets.<Class<? extends FMLStateEvent>>newHashSet(FMLPostInitializationEvent.class, FMLServerAboutToStartEvent.class);
	}
	
	@Override
	public boolean applyStateEventPatch(FMLStateEvent event) {
		if (event instanceof FMLPostInitializationEvent)
			return applyPostInitPatches((FMLPostInitializationEvent) event);
		else if (event instanceof FMLServerAboutToStartEvent)
			return applyServerStartPatches((FMLServerAboutToStartEvent) event);
		else
			throw new IllegalArgumentException("Invalid Event");
	}
	
	@Override
	public boolean applyForgeEventPatch(Event event) {
		return applyGuiOpenPatches((GuiOpenEvent) event);
	}
	
	private boolean applyPostInitPatches(FMLPostInitializationEvent event) {
		if (serverNameField != null && serverPortField != null) {
			String serverName = ReflectionHelper.get(ObfuscatedField.Minecraft_serverName, serverNameField, Minecraft.getMinecraft());
			Integer serverPort = ReflectionHelper.get(ObfuscatedField.Minecraft_serverPort, serverPortField, Minecraft.getMinecraft());
			
			if (serverName != null && serverPort != null) {
				this.startupServerName = serverName;
				this.startupServerPort = serverPort;
				ReflectionHelper.set(ObfuscatedField.Minecraft_serverName, serverNameField, Minecraft.getMinecraft(), null);
				
				return true;
			}
			else return false;
		}
		else return false;
	}
	
	private boolean applyServerStartPatches(FMLServerAboutToStartEvent event) {
		boolean success = networkManagers == null ? false : ReflectionHelper.set(ObfuscatedField.NetworkSystem_networkManagers, 
				networkManagers, event.getServer().func_147137_ag(), Collections.<NetworkManager>synchronizedList(new ArrayList<NetworkManager>() {
				
				private static final long serialVersionUID = 498247631553542876L;
				
				@Override
				public boolean add(NetworkManager manager) {
					PatchEntityClientPlayerMP.this.onNetworkManagerAdd(manager);
					return super.add(manager);
				}
		}));
		
		PatchManager.instance().getGlobalAppliedPatches().setPatchSuccessfullyApplied(this.displayName, success);
		return success;
	}
	
	private boolean applyGuiOpenPatches(GuiOpenEvent event) {
		if (event.gui instanceof GuiMainMenu && this.startupServerName != null) {
			GuiScreen screen = connectToServerAtStartup(this.startupServerName, this.startupServerPort);
			this.startupServerName = null;
			
			if (screen == null) {
				FMLClientHandler.instance().connectToServerAtStartup(this.startupServerName, this.startupServerPort);
				PatchManager.instance().getGlobalAppliedPatches().setPatchSuccessfullyApplied(this.displayName, false);
				return false;
			}
			else {
				event.gui = screen;
				
				PatchManager.instance().getGlobalAppliedPatches().setPatchSuccessfullyApplied(this.displayName, true);
				return true;
			}
		}
		else if (event.gui instanceof net.minecraft.client.gui.GuiMultiplayer) {
			if (field_146798_g == null) {
				PatchManager.instance().getGlobalAppliedPatches().setPatchSuccessfullyApplied(this.displayName, false);
				return false;
			}
			
			GuiScreen parentScreen = ReflectionHelper.get(ObfuscatedField.GuiMultiplayer_field_146798_g, field_146798_g, (net.minecraft.client.gui.GuiMultiplayer) event.gui);
			event.gui = new GuiMultiplayer(parentScreen);
			
			PatchManager.instance().getGlobalAppliedPatches().setPatchSuccessfullyApplied(this.displayName, true);
			return true;
		}
		else throw new IllegalArgumentException("Invalid argument: GuiOpenEvent with GuiMainMenu or GuiMultiplayer required");
	}
	
	private void onNetworkManagerAdd(NetworkManager manager) {
		if (manager.getNetHandler() instanceof net.minecraft.client.network.NetHandlerHandshakeMemory)
			manager.setNetHandler(new NetHandlerHandshakeMemory(FMLCommonHandler.instance().getMinecraftServerInstance(), manager));
	}
	
	private static GuiScreen connectToServerAtStartup(String host, int port) {
		if (startupConnectionDataField == null)
			return null;
		
		CountDownLatch startupConnectionData = ReflectionHelper.get(ObfuscatedField.FMLClientHandler_startupConnectionData, startupConnectionDataField, null);
		
		if (startupConnectionData == null)
			return null;
		
		FMLClientHandler.instance().setupServerList();
		OldServerPinger osp = new OldServerPinger();
		ServerData serverData = new ServerData("Command Line", host+":"+port);
		
		try {
			osp.func_147224_a(serverData);
			startupConnectionData.await(30, TimeUnit.SECONDS);
		}
		catch (Exception e) {
			return GuiConnecting.newGuiConnecting(new GuiMainMenu(), Minecraft.getMinecraft(), host, port);
		}
		
		return connectToServer(new GuiMainMenu(), serverData);
	}
	
	private static GuiScreen connectToServer(GuiScreen guiMultiplayer, ServerData serverEntry) {
		if (playClientBlockField == null || serverDataTagField == null)
			return null;
		
		Map<ServerData, ExtendedServerListData> serverDataTag = ReflectionHelper.get(ObfuscatedField.FMLClientHandler_serverDataTag, serverDataTagField, FMLClientHandler.instance());
		
		if (serverDataTag == null || !ReflectionHelper.set(ObfuscatedField.FMLClientHandler_playClientBlock, 
															playClientBlockField, FMLClientHandler.instance(), new CountDownLatch(1)))
			return null;
		
		ExtendedServerListData extendedData = serverDataTag.get(serverEntry);
		
		if (extendedData != null && extendedData.isBlocked)
            return new GuiAccessDenied(guiMultiplayer, serverEntry);
		else
			return GuiConnecting.newGuiConnecting(guiMultiplayer, Minecraft.getMinecraft(), serverEntry);
    }
	
	@SideOnly(Side.CLIENT)
	private static class GuiConnecting extends GuiScreen {
	    private static final AtomicInteger CONNECTION_ID = ReflectionHelper.get(ObfuscatedField.GuiConnecting_field_146372_a, null);
	    private static final Logger LOGGER = LogManager.getLogger(net.minecraft.client.multiplayer.GuiConnecting.class);
	    private NetworkManager networkManager;
	    private boolean cancel;
	    private final GuiScreen previousGuiScreen;
	    
	    public static GuiScreen newGuiConnecting(GuiScreen parent, Minecraft mcIn, ServerData serverDataIn) {
	    	if (CONNECTION_ID == null) return null;
	    	return new GuiConnecting(parent, mcIn, serverDataIn);
	    }
	    
	    public static GuiScreen newGuiConnecting(GuiScreen parent, Minecraft mcIn, String hostName, int port) {
	    	if (CONNECTION_ID == null) return null;
	    	return new GuiConnecting(parent, mcIn, hostName, port);
	    }
	    
	    private GuiConnecting(GuiScreen parent, Minecraft mcIn, ServerData serverDataIn) {
	    	this.mc = mcIn;
	    	this.previousGuiScreen = parent;
	    	
	    	ServerAddress serveraddress = ServerAddress.func_78860_a(serverDataIn.serverIP);
	    	mcIn.loadWorld(null);
	    	mcIn.setServerData(serverDataIn);
	    	
	    	connect(serveraddress.getIP(), serveraddress.getPort());
	    }
	    
	    private GuiConnecting(GuiScreen parent, Minecraft mcIn, String hostName, int port) {
	    	this.mc = mcIn;
	    	this.previousGuiScreen = parent;
	    	
	    	mcIn.loadWorld(null);
	    	connect(hostName, port);
	    }
	    
	    private void connect(final String ip, final int port) {
	    	LOGGER.info("Connecting to " + ip + ", " + port);
	    	
	    	new Thread("Server Connector #" + CONNECTION_ID.incrementAndGet()) {
	    		@Override 
	    		public void run() {
	    			InetAddress inetaddress = null;
	    			
	    			try {
	    				if (GuiConnecting.this.cancel)
	    					return;
	    				
	    				inetaddress = InetAddress.getByName(ip);
	    				GuiConnecting.this.networkManager = NetworkManager.provideLanClient(inetaddress, port);
	    				GuiConnecting.this.networkManager.setNetHandler(new NetHandlerLoginClient(GuiConnecting.this.networkManager, GuiConnecting.this.mc, GuiConnecting.this.previousGuiScreen));
	    				GuiConnecting.this.networkManager.scheduleOutboundPacket(new C00Handshake(5, ip, port, EnumConnectionState.LOGIN));
	    				GuiConnecting.this.networkManager.scheduleOutboundPacket(new C00PacketLoginStart(GuiConnecting.this.mc.getSession().func_148256_e()));
	    			}
	    			catch (UnknownHostException unknownhostexception) {
	                    if (GuiConnecting.this.cancel)
	                    	return;
	                    
	                    GuiConnecting.LOGGER.error("Couldn\'t connect to server", unknownhostexception);
	                    GuiConnecting.this.mc.displayGuiScreen(new GuiDisconnected(GuiConnecting.this.previousGuiScreen, "connect.failed", new ChatComponentTranslation("disconnect.genericReason", new Object[] {"Unknown host"})));
	                }
	    			catch (Exception exception) {
	                    if (GuiConnecting.this.cancel)
	                    	return;
	                    
	                    GuiConnecting.LOGGER.error("Couldn\'t connect to server", exception);
	                    String s = exception.toString();
	                    
	                    if (inetaddress != null) {
	                    	String s1 = inetaddress + ":" + port;
	                    	s = s.replaceAll(s1, "");
	                    }
	                    
	                    GuiConnecting.this.mc.displayGuiScreen(new GuiDisconnected(GuiConnecting.this.previousGuiScreen, "connect.failed", new ChatComponentTranslation("disconnect.genericReason", new Object[] {s})));
	                }
	            }
	        }.start();
	    }
	    
	    @Override
	    public void updateScreen() {
	    	if (this.networkManager != null) {
	    		if (this.networkManager.isChannelOpen())
	    			this.networkManager.processReceivedPackets();
	            else if (this.networkManager.getExitMessage() != null)
	            	this.networkManager.getNetHandler().onDisconnect(this.networkManager.getExitMessage());
	        }
	    }
	    
	    @Override
	    protected void keyTyped(char typedChar, int keyCode) {
	    	//NOOP
	    }
	    
	    @Override
	    public void initGui() {
	    	this.buttonList.clear();
	    	this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 120 + 12, I18n.format("gui.cancel")));
	    }
	    
	    @Override
	    protected void actionPerformed(GuiButton button) {
	    	if (button.id == 0) {
	    		this.cancel = true;
	    		
	            if (this.networkManager != null)
	            	this.networkManager.closeChannel(new ChatComponentText("Aborted"));
	            
	            this.mc.displayGuiScreen(this.previousGuiScreen);
	        }
	    }
	    
	    @Override
	    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
	    	this.drawDefaultBackground();
	    	
	    	if (this.networkManager == null)
	        	this.drawCenteredString(this.fontRendererObj, I18n.format("connect.connecting"), this.width / 2, this.height / 2 - 50, 16777215);
	        else
	        	this.drawCenteredString(this.fontRendererObj, I18n.format("connect.authorizing"), this.width / 2, this.height / 2 - 50, 16777215);
	    	
	    	super.drawScreen(mouseX, mouseY, partialTicks);
	    }
	}
	
	@SideOnly(Side.CLIENT)
	private static class GuiMultiplayer extends net.minecraft.client.gui.GuiMultiplayer {
		public GuiMultiplayer(GuiScreen parentScreen) {
			super(parentScreen);
		}
		
		@Override
		public void confirmClicked(boolean result, int id) {
			Boolean directConnect = ReflectionHelper.get(ObfuscatedField.GuiMultiplayer_field_146813_x, field_146813_x, this);
			
			if (directConnect == null) {
				PatchManager.instance().getGlobalAppliedPatches().setPatchSuccessfullyApplied(PatchList.PATCH_ENTITYCLIENTPLAYERMP, false);
				super.confirmClicked(result, id);
				
				return;
			}
			
			if (directConnect) {
				ServerData selectedServer = ReflectionHelper.get(ObfuscatedField.GuiMultiplayer_field_146811_z, field_146811_z, this);
				
				if (selectedServer == null || !ReflectionHelper.set(ObfuscatedField.GuiMultiplayer_field_146813_x, field_146813_x, this, false)) {
					PatchManager.instance().getGlobalAppliedPatches().setPatchSuccessfullyApplied(PatchList.PATCH_ENTITYCLIENTPLAYERMP, false);
					super.confirmClicked(result, id);
					
					return;
				}
				
				if (result)
	            	this.func_146791_a(selectedServer);
	            else
	            	this.mc.displayGuiScreen(this);
	        }
			else super.confirmClicked(result, id);
		}
		
		@Override
		public void func_146796_h() {
			ServerSelectionList selector = ReflectionHelper.get(ObfuscatedField.GuiMultiplayer_field_146803_h, field_146803_h, this);
			
			if (selector == null) {
				PatchManager.instance().getGlobalAppliedPatches().setPatchSuccessfullyApplied(PatchList.PATCH_ENTITYCLIENTPLAYERMP, false);
				super.func_146796_h();
				
				return;
			}
			
			GuiListExtended.IGuiListEntry entry = selector.func_148193_k() < 0 ? null : selector.getListEntry(selector.func_148193_k());
			
	        if (entry instanceof ServerListEntryNormal)
	        	this.func_146791_a(((ServerListEntryNormal) entry).func_148296_a());
	        else if (entry instanceof ServerListEntryLanDetected) {
	            LanServerDetector.LanServer lanserver = ((ServerListEntryLanDetected) entry).func_148289_a();
	            this.func_146791_a(new ServerData(lanserver.getServerMotd(), lanserver.getServerIpPort(), true));
	        }
	    }
		
		private void func_146791_a(ServerData server) {
			GuiScreen screen = PatchEntityClientPlayerMP.connectToServer(this, server);
			
			if (screen == null) {
				PatchManager.instance().getGlobalAppliedPatches().setPatchSuccessfullyApplied(PatchList.PATCH_ENTITYCLIENTPLAYERMP, false);
				FMLClientHandler.instance().connectToServer(this, server);
			}
			else Minecraft.getMinecraft().displayGuiScreen(screen);
		}
	}
	
	@SideOnly(Side.CLIENT)
	private static class NetHandlerHandshakeMemory extends net.minecraft.client.network.NetHandlerHandshakeMemory {
		private MinecraftServer mcServer;
		private NetworkManager networkManager;
		
		public NetHandlerHandshakeMemory(MinecraftServer mcServerIn, NetworkManager networkManagerIn) {
			super(mcServerIn, networkManagerIn);
			this.mcServer = mcServerIn;
			this.networkManager = networkManagerIn;
		}
		
		@Override
		public void onConnectionStateTransition(EnumConnectionState from, EnumConnectionState to) {
			Validate.validState(to == EnumConnectionState.LOGIN || to == EnumConnectionState.STATUS, "Unexpected protocol " + to);
			
			switch (to) {
	            case LOGIN:
	            	this.networkManager.setNetHandler(new NetHandlerLoginServer(this.mcServer, this.networkManager));
	            	break;
	            case STATUS:
	                throw new UnsupportedOperationException("NYI");
	            default:
	        }
	    }
	}
	
	private static class NetHandlerLoginServer extends net.minecraft.server.network.NetHandlerLoginServer {
		public NetHandlerLoginServer(MinecraftServer serverIn, NetworkManager networkManagerIn) {
			super(serverIn, networkManagerIn);
		}
		
		@Override
		public void processLoginStart(C00PacketLoginStart packetIn) {
			super.processLoginStart(packetIn);
			
			if (myNetworkManager == null) {
				PatchManager.instance().getGlobalAppliedPatches().setPatchSuccessfullyApplied(PatchList.PATCH_ENTITYCLIENTPLAYERMP, false);
				return;
			}
			
			NetworkManager manager = ReflectionHelper.get(ObfuscatedField.Minecraft_myNetworkManager, myNetworkManager, Minecraft.getMinecraft());
			
			if (manager == null) {
				PatchManager.instance().getGlobalAppliedPatches().setPatchSuccessfullyApplied(PatchList.PATCH_ENTITYCLIENTPLAYERMP, false);
				return;
			}
			
			manager.setNetHandler(new NetHandlerLoginClient(manager, Minecraft.getMinecraft(), null));
		}
	}
	
	public static class NetHandlerLoginClient extends net.minecraft.client.network.NetHandlerLoginClient {
		private static final Logger LOGGER = LogManager.getLogger(net.minecraft.client.network.NetHandlerLoginClient.class);
		private GuiScreen previousScreen;
		private NetworkManager networkManager;
		private Minecraft mc;
		
		public NetHandlerLoginClient(NetworkManager networkManagerIn, Minecraft mcIn, GuiScreen previousScreenIn) {
			super(networkManagerIn, mcIn, previousScreenIn);
			this.previousScreen = previousScreenIn;
			this.networkManager = networkManagerIn;
			this.mc = mcIn;
		}
		
		@Override
		public void onConnectionStateTransition(EnumConnectionState from, EnumConnectionState to) {
	        LOGGER.debug("Switching protocol from " + from + " to " + to);
	        
	        if (to == EnumConnectionState.PLAY) {
	        	NetHandlerPlayClient nhpc = new NetHandlerPlayClient(this.mc, this.previousScreen, this.networkManager);
	            this.networkManager.setNetHandler(nhpc);
	            FMLClientHandler.instance().setPlayClient(nhpc);
	        }
	    }
	}
	
	private static class NetHandlerPlayClient extends net.minecraft.client.network.NetHandlerPlayClient {
		private static final Field clientWorldController = ReflectionHelper.getField(ObfuscatedField.NetHandlerPlayClient_clientWorldController);
		private Minecraft mc;
		
		public NetHandlerPlayClient(Minecraft mc, GuiScreen screen, NetworkManager manager) {
			super(mc, screen, manager);
			this.mc = mc;
		}
		
		@Override
		public void handleJoinGame(S01PacketJoinGame packetIn) {
			if (clientWorldController == null) super.handleJoinGame(packetIn);
	        this.mc.playerController = new PlayerControllerMP(this.mc, this); //Replaces the playerController with my own patched PlayerControllerMP
	        ReflectionHelper.set(ObfuscatedField.NetHandlerPlayClient_clientWorldController, this.clientWorldController, this, new WorldClient(this, new WorldSettings(0L, packetIn.func_149198_e(), false, packetIn.func_149195_d(), packetIn.func_149196_i()), packetIn.func_149194_f(), packetIn.func_149192_g(), this.mc.mcProfiler));
	        ReflectionHelper.get(ObfuscatedField.NetHandlerPlayClient_clientWorldController, this.clientWorldController, this).isRemote = true;
	        this.mc.loadWorld(ReflectionHelper.get(ObfuscatedField.NetHandlerPlayClient_clientWorldController, this.clientWorldController, this));
	        this.mc.thePlayer.dimension = packetIn.func_149194_f();
	        this.mc.displayGuiScreen(new GuiDownloadTerrain(this));
	        this.mc.thePlayer.setEntityId(packetIn.func_149197_c());
	        this.currentServerMaxPlayers = packetIn.func_149193_h();
	        this.mc.playerController.setGameType(packetIn.func_149198_e());
	        this.mc.gameSettings.sendSettingsToServer();
	        this.getNetworkManager().scheduleOutboundPacket(new C17PacketCustomPayload("MC|Brand", ClientBrandRetriever.getClientModName().getBytes(Charsets.UTF_8)), new GenericFutureListener[0]);
	    }
	}
	
	public static class PlayerControllerMP extends net.minecraft.client.multiplayer.PlayerControllerMP {
		private NetHandlerPlayClient netClientHandler;
		private Minecraft mc;
		
		private float reachDistance = 5.0F;
		
		PlayerControllerMP(Minecraft mc, NetHandlerPlayClient netClientHandler) {
			super(mc, netClientHandler);
			this.netClientHandler = netClientHandler;
			this.mc = mc;
		}
		
		@Override
	    public float getBlockReachDistance() {
			return this.reachDistance;
		}
		
		public void setBlockReachDistance(float distance) {
			this.reachDistance = distance;
		}
		
		@Override
		public EntityClientPlayerMP func_147493_a(World p_147493_1_, StatFileWriter p_147493_2_){
			return new EntityClientPlayerMP(this.mc, p_147493_1_, this.mc.getSession(), this.netClientHandler, p_147493_2_);
		}
	}
	
	public static class EntityClientPlayerMP extends net.minecraft.client.entity.EntityClientPlayerMP implements ResultAcceptingCommandSender {
		private net.minecraft.client.network.NetHandlerPlayClient netClientHandler;
		private StatFileWriter statManager;
		
		private boolean overrideOnLadder = false;
		private boolean freezeCam = false;
		private boolean freeCam = false;
		private boolean fluidmovement = true;
		
		private boolean overrideSpectator = false;
		private float freezeCamYaw;
		private float freezeCamPitch;
		
		private double gravity = 1F;

		private String capturedCommandResult = null, cmdSentToServer = null;
		private StringBuilder capturedCommandMessages = new StringBuilder();
		private boolean captureNextCommandResult = false;	
		
	    protected EntityClientPlayerMP(Minecraft minecraft, World world, Session session, net.minecraft.client.network.NetHandlerPlayClient netClientHandler, StatFileWriter statFileWriter) {
	        super(minecraft, world, session, netClientHandler, statFileWriter);
	        this.netClientHandler = netClientHandler;
	        this.statManager = statFileWriter;
	    }
	    
	    public net.minecraft.client.network.NetHandlerPlayClient getNetHandler() {
	    	return this.netClientHandler;
	    }
	    
	    public StatFileWriter getStatWriter() {
	    	return this.statManager;
	    }

	    /**
	     * This method should be invoked before this entity is passed to {@link net.minecraft.command.ICommandManager#executeCommand(net.minecraft.command.ICommandSender, String)}. 
	     * Invoking this method will make this entity capture the result of the command execution. Result either means the return value
	     * of the {@link com.mrnobody.morecommands.command.AbstractCommand#execute(com.mrnobody.morecommands.command.CommandSender, String[])} method
	     * if the command is a subclass of this class or, if it is not, or if the return value is null, the chat messages sent via the
	     * {@link #addChatMessage(IChatComponent)} method. After command execution, the captured results must be reset via the
	     * {@link #getCapturedCommandResult()} method. This method also returns the result. 
	     */
	    public void setCaptureNextCommandResult() {
	    	this.captureNextCommandResult = true;
	    }
	    
	    @Override
	    public void addChatMessage(IChatComponent message) {
	    	if (this.captureNextCommandResult) this.capturedCommandMessages.append(" " + message.getUnformattedText());
	    	super.addChatMessage(message);
	    }
	    
	    @Override
	    public void setCommandResult(String commandName, String[] args, String result) {
	    	if (this.captureNextCommandResult && result != null) 
	    		this.capturedCommandResult = result;
	    }
	    
	    /**
	     * Disables capturing of command results and resets and returns them.
	     * 
	     * @return the captured result of the command execution (requires enabling capturing before command execution via
	     * 			{@link #setCaptureNextCommandResult()}. Will never be null
	     * @see #setCaptureNextCommandResult()
	     */
	    public String getCapturedCommandResult() {
	    	String result = null;
	    	
	    	if (this.capturedCommandResult != null) result = this.capturedCommandResult;
	    	else result = this.capturedCommandMessages.toString().trim();
	    	
	    	this.capturedCommandResult = this.cmdSentToServer = null;
	    	this.capturedCommandMessages = new StringBuilder();
	    	this.captureNextCommandResult = false;
	    	
	    	return result;
	    }
	    
	    @Override
	    public void sendChatMessage(String message) {
	    	if (this.captureNextCommandResult && message.startsWith("/")) this.cmdSentToServer = message;
	    	else super.sendChatMessage(message);
	    }
	    
	    /**
	     * @return the last command that should have been sent to the server via {@link #sendChatMessage(String)}
	     * (if command result capturing is enabled via {@link #setCaptureNextCommandResult()}
	     */
	    public String getCmdSentToServer() {
	    	return this.cmdSentToServer;
	    }
	    
		public void setFluidMovement(boolean fluidmovement) {
			this.fluidmovement = fluidmovement;
		}
		
		public boolean getFluidMovement() {
			return this.fluidmovement;
		}
	    
		@Override
		public boolean isInWater() {
			if (!this.fluidmovement) return false;
			return super.isInWater();
		}
		
		@Override
		public boolean handleLavaMovement() {
			if (!this.fluidmovement) return false;
			return super.handleLavaMovement();
		}
	    
	    @Override
	    public boolean isEntityInsideOpaqueBlock() {
	        return !this.noClip && super.isEntityInsideOpaqueBlock();
	    }
	    
		public void setOverrideOnLadder(boolean flag) {
			this.overrideOnLadder = flag;
		}
		
		public boolean isOnLadderOverridden() {
			return this.overrideOnLadder;
		}
		
		@Override
		public boolean isOnLadder() {
			if (this.overrideOnLadder && this.isCollidedHorizontally) return true;
			else return super.isOnLadder();
		}
		
		public void setFreezeCamera(boolean freezeCamera) {
			this.freezeCam = freezeCamera;
		}
		
		public boolean getFreezeCamera() {
			return this.freezeCam;
		}
		
		public void setFreeCam(boolean freeCamera) {
			this.freeCam = freeCamera;
		}
		
		public boolean getFreeCam() {
			return this.freeCam;
		}
		
		public void setFreezeCamYawAndPitch(float yaw, float pitch) {
			this.freezeCamYaw = yaw;
			this.freezeCamPitch = pitch;
		}
		
		public double getGravity() {
			return this.gravity;
		}
		
		public void setGravity(double gravity) {
			this.gravity = gravity;
		}
		
		@Override
		public void moveEntity(double x, double y, double z) {
			if (this.freezeCam && Minecraft.getMinecraft().renderViewEntity instanceof EntityCamera) {
				((EntityCamera) Minecraft.getMinecraft().renderViewEntity).setFreezeCamera(0, 0, 0, this.freezeCamYaw, this.freezeCamPitch);
			}
			else if (this.freeCam && Minecraft.getMinecraft().renderViewEntity instanceof EntityCamera) {
				((EntityCamera) Minecraft.getMinecraft().renderViewEntity).setFreeCamera(x, y, z, this.rotationYaw, this.rotationPitch);
				return;
			}
			
			super.moveEntity(x, y, z);
		}
		
		@Override
		public void jump() {
			this.motionY = 0.42F * this.gravity;
			
	        if (this.isPotionActive(Potion.jump))
	        	this.motionY += (double)((float)(this.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F);
	        
	        if (this.isSprinting()) {
	            float f = this.rotationYaw * 0.017453292F;
	            this.motionX -= (double)(MathHelper.sin(f) * 0.2F);
	            this.motionZ += (double)(MathHelper.cos(f) * 0.2F);
	        }
	        
	        this.isAirBorne = true;
	        ForgeHooks.onLivingJump(this);
	        
	        this.addStat(StatList.jumpStat, 1);
	        
	        if (this.isSprinting())
	        	this.addExhaustion(0.8F);
	        else
	        	this.addExhaustion(0.2F);
		}
	}
}
