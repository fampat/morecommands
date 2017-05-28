package com.mrnobody.morecommands.patch;

import java.io.IOException;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mrnobody.morecommands.command.AbstractCommand.ResultAcceptingCommandSender;
import com.mrnobody.morecommands.util.EntityCamera;
import com.mrnobody.morecommands.util.ObfuscatedNames.ObfuscatedField;
import com.mrnobody.morecommands.util.ReflectionHelper;

import io.netty.buffer.Unpooled;
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
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.login.server.S02PacketLoginSuccess;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.StatFileWriter;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.client.ExtendedServerListData;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.GuiAccessDenied;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
public class PatchEntityPlayerSP implements PatchManager.StateEventBasedPatch, PatchManager.ForgeEventBasedPatch {
	private static final Field serverNameField = ReflectionHelper.getField(ObfuscatedField.Minecraft_serverName);
	private static final Field serverPortField = ReflectionHelper.getField(ObfuscatedField.Minecraft_serverPort);
	private static final Field networkManagers = ReflectionHelper.getField(ObfuscatedField.NetworkSystem_networkManagers);
	private static final Field startupConnectionDataField = ReflectionHelper.getField(ObfuscatedField.FMLClientHandler_startupConnectionData);
	private static final Field serverDataTagField = ReflectionHelper.getField(ObfuscatedField.FMLClientHandler_serverDataTag);
	private static final Field parentScreenField = ReflectionHelper.getField(ObfuscatedField.GuiMultiplayer_parentScreen);
	private static final Field serverListSelector = ReflectionHelper.getField(ObfuscatedField.GuiMultiplayer_serverListSelector);
	private static final Field directConnectField = ReflectionHelper.getField(ObfuscatedField.GuiMultiplayer_directConnect);
	private static final Field selectedServerField = ReflectionHelper.getField(ObfuscatedField.GuiMultiplayer_selectedServer);
	private static final Field myNetworkManager = ReflectionHelper.getField(ObfuscatedField.Minecraft_myNetworkManager);
	
	private String startupServerName;
	private int startupServerPort;
	private String displayName;
	
	PatchEntityPlayerSP(String displayName) {
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
				networkManagers, event.getServer().getNetworkSystem(), Collections.<NetworkManager>synchronizedList(new ArrayList<NetworkManager>() {
				
				private static final long serialVersionUID = 498247631553542876L;
				
				@Override
				public boolean add(NetworkManager manager) {
					PatchEntityPlayerSP.this.onNetworkManagerAdd(manager);
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
			if (parentScreenField == null) {
				PatchManager.instance().getGlobalAppliedPatches().setPatchSuccessfullyApplied(this.displayName, false);
				return false;
			}
			
			GuiScreen parentScreen = ReflectionHelper.get(ObfuscatedField.GuiMultiplayer_parentScreen, parentScreenField, (net.minecraft.client.gui.GuiMultiplayer) event.gui);
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
		if (startupConnectionDataField == null || serverDataTagField == null)
			return null;
		
		CountDownLatch startupConnectionData = ReflectionHelper.get(ObfuscatedField.FMLClientHandler_startupConnectionData, startupConnectionDataField, null);
		Map<ServerData, ExtendedServerListData> serverDataTag = ReflectionHelper.get(ObfuscatedField.FMLClientHandler_serverDataTag, serverDataTagField, FMLClientHandler.instance());
		
		if (startupConnectionData == null || serverDataTag == null)
			return null;
		
		FMLClientHandler.instance().setupServerList();
		OldServerPinger osp = new OldServerPinger();
		ServerData serverData = new ServerData("Command Line", host+":"+port);
		
		try {
			osp.ping(serverData);
			startupConnectionData.await(30, TimeUnit.SECONDS);
		}
		catch (Exception e) {
			return GuiConnecting.newGuiConnecting(new GuiMainMenu(), Minecraft.getMinecraft(), host, port);
		}
		
		ExtendedServerListData extendedData = serverDataTag.get(serverData);
		
		if (extendedData != null && extendedData.isBlocked)
            return new GuiAccessDenied(new GuiMainMenu(), serverData);
		else
			return GuiConnecting.newGuiConnecting(new GuiMainMenu(), Minecraft.getMinecraft(), serverData);
	}
	
	private static GuiScreen connectToServer(GuiScreen guiMultiplayer, ServerData serverEntry) {
		Map<ServerData, ExtendedServerListData> serverDataTag = ReflectionHelper.get(ObfuscatedField.FMLClientHandler_serverDataTag, serverDataTagField, FMLClientHandler.instance());
		
		if (serverDataTag == null)
			return null;
		
		ExtendedServerListData extendedData = serverDataTag.get(serverEntry);
		
		if (extendedData != null && extendedData.isBlocked)
            return new GuiAccessDenied(guiMultiplayer, serverEntry);
		else
			return GuiConnecting.newGuiConnecting(guiMultiplayer, Minecraft.getMinecraft(), serverEntry);
    }
	
	@SideOnly(Side.CLIENT)
	private static class GuiConnecting extends GuiScreen {
	    private static final AtomicInteger CONNECTION_ID = ReflectionHelper.get(ObfuscatedField.GuiConnecting_CONNECTION_ID, null);
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
	    	LOGGER.info("Connecting to {}, {}", new Object[] {ip, Integer.valueOf(port)});
	    	
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
	    				GuiConnecting.this.networkManager.sendPacket(new C00Handshake(47, ip, port, EnumConnectionState.LOGIN, true));
	    				GuiConnecting.this.networkManager.sendPacket(new C00PacketLoginStart(GuiConnecting.this.mc.getSession().getProfile()));
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
	            else
	            	this.networkManager.checkDisconnected();
	        }
	    }
	    
	    @Override
	    protected void keyTyped(char typedChar, int keyCode) throws IOException {
	    	//NOOP
	    }
	    
	    @Override
	    public void initGui() {
	    	this.buttonList.clear();
	    	this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 120 + 12, I18n.format("gui.cancel")));
	    }
	    
	    @Override
	    protected void actionPerformed(GuiButton button) throws IOException {
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
			Boolean directConnect = ReflectionHelper.get(ObfuscatedField.GuiMultiplayer_directConnect, directConnectField, this);
			
			if (directConnect == null) {
				PatchManager.instance().getGlobalAppliedPatches().setPatchSuccessfullyApplied(PatchList.PATCH_ENTITYPLAYERSP, false);
				super.confirmClicked(result, id);
				
				return;
			}
			
			if (directConnect) {
				ServerData selectedServer = ReflectionHelper.get(ObfuscatedField.GuiMultiplayer_selectedServer, selectedServerField, this);
				
				if (selectedServer == null || !ReflectionHelper.set(ObfuscatedField.GuiMultiplayer_directConnect, directConnectField, this, false)) {
					PatchManager.instance().getGlobalAppliedPatches().setPatchSuccessfullyApplied(PatchList.PATCH_ENTITYPLAYERSP, false);
					super.confirmClicked(result, id);
					
					return;
				}
				
				if (result)
	            	this.connectToServer(selectedServer);
	            else
	            	this.mc.displayGuiScreen(this);
	        }
			else super.confirmClicked(result, id);
		}
		
		@Override
		public void connectToSelected() {
			ServerSelectionList selector = ReflectionHelper.get(ObfuscatedField.GuiMultiplayer_serverListSelector, serverListSelector, this);
			
			if (selector == null) {
				PatchManager.instance().getGlobalAppliedPatches().setPatchSuccessfullyApplied(PatchList.PATCH_ENTITYPLAYERSP, false);
				super.connectToSelected();
				
				return;
			}
			
			GuiListExtended.IGuiListEntry entry = selector.func_148193_k() < 0 ? null : selector.getListEntry(selector.func_148193_k());
			
	        if (entry instanceof ServerListEntryNormal)
	        	this.connectToServer(((ServerListEntryNormal) entry).getServerData());
	        else if (entry instanceof ServerListEntryLanDetected) {
	            LanServerDetector.LanServer lanserver = ((ServerListEntryLanDetected) entry).getLanServer();
	            this.connectToServer(new ServerData(lanserver.getServerMotd(), lanserver.getServerIpPort()));
	        }
	    }
		
		private void connectToServer(ServerData server) {
			GuiScreen screen = PatchEntityPlayerSP.connectToServer(this, server);
			
			if (screen == null) {
				PatchManager.instance().getGlobalAppliedPatches().setPatchSuccessfullyApplied(PatchList.PATCH_ENTITYPLAYERSP, false);
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
		public void processHandshake(C00Handshake packetIn) {
			if (!net.minecraftforge.fml.common.FMLCommonHandler.instance().handleServerHandshake(packetIn, this.networkManager)) return;
			this.networkManager.setConnectionState(packetIn.getRequestedState());
			this.networkManager.setNetHandler(new NetHandlerLoginServer(this.mcServer, this.networkManager));
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
				PatchManager.instance().getGlobalAppliedPatches().setPatchSuccessfullyApplied(PatchList.PATCH_ENTITYPLAYERSP, false);
				return;
			}
			
			NetworkManager manager = ReflectionHelper.get(ObfuscatedField.Minecraft_myNetworkManager, myNetworkManager, Minecraft.getMinecraft());
			
			if (manager == null) {
				PatchManager.instance().getGlobalAppliedPatches().setPatchSuccessfullyApplied(PatchList.PATCH_ENTITYPLAYERSP, false);
				return;
			}
			
			manager.setNetHandler(new NetHandlerLoginClient(manager, Minecraft.getMinecraft(), null));
		}
	}
	
	public static class NetHandlerLoginClient extends net.minecraft.client.network.NetHandlerLoginClient {
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
		public void handleLoginSuccess(S02PacketLoginSuccess packetIn) {
			super.handleLoginSuccess(packetIn);
			
			NetHandlerPlayClient handler = new NetHandlerPlayClient(this.mc, this.previousScreen, this.networkManager, packetIn.getProfile());
	        this.networkManager.setNetHandler(handler);
	        net.minecraftforge.fml.client.FMLClientHandler.instance().setPlayClient(handler);
	    }
	}
	
	private static class NetHandlerPlayClient extends net.minecraft.client.network.NetHandlerPlayClient {
		private static final Field clientWorldController = ReflectionHelper.getField(ObfuscatedField.NetHandlerPlayClient_clientWorldController);
		private Minecraft mc;
		
		public NetHandlerPlayClient(Minecraft mc, GuiScreen screen, NetworkManager manager, GameProfile profile) {
			super(mc, screen, manager, profile);
			this.mc = mc;
		}
		
		@Override
		public void handleJoinGame(S01PacketJoinGame packetIn) {
			PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.mc);
	        if (this.clientWorldController == null) super.handleJoinGame(packetIn);
	        this.mc.playerController = new PlayerControllerMP(this.mc, this); //Replaces the playerController with my own patched PlayerControllerMP
	        ReflectionHelper.set(ObfuscatedField.NetHandlerPlayClient_clientWorldController, this.clientWorldController, this, new WorldClient(this, new WorldSettings(0L, packetIn.getGameType(), false, packetIn.isHardcoreMode(), packetIn.getWorldType()), packetIn.getDimension(), packetIn.getDifficulty(), this.mc.mcProfiler));
	        this.mc.gameSettings.difficulty = packetIn.getDifficulty();
	        this.mc.loadWorld(ReflectionHelper.get(ObfuscatedField.NetHandlerPlayClient_clientWorldController, this.clientWorldController, this));
	        this.mc.thePlayer.dimension = packetIn.getDimension();
	        this.mc.displayGuiScreen(new GuiDownloadTerrain(this));
	        this.mc.thePlayer.setEntityId(packetIn.getEntityId());
	        this.currentServerMaxPlayers = packetIn.getMaxPlayers();
	        this.mc.thePlayer.setReducedDebug(packetIn.isReducedDebugInfo());
	        this.mc.playerController.setGameType(packetIn.getGameType());
	        this.mc.gameSettings.sendSettingsToServer();
	        this.getNetworkManager().sendPacket(new C17PacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString(ClientBrandRetriever.getClientModName())));
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
		public net.minecraft.client.entity.EntityPlayerSP func_178892_a(World worldIn, StatFileWriter writer) {
			return new EntityPlayerSP(this.mc, worldIn, this.netClientHandler, writer);
		}
		
		@Override
		public void attackEntity(EntityPlayer player, Entity target) {
			if (!(Minecraft.getMinecraft().getRenderViewEntity() instanceof EntityCamera))
				super.attackEntity(player, target);
		}
	}
	
	public static class EntityPlayerSP extends net.minecraft.client.entity.EntityPlayerSP implements ResultAcceptingCommandSender {
		private net.minecraft.client.network.NetHandlerPlayClient netClientHandler;
		private StatFileWriter statManager;
		
		private boolean overrideOnLadder = false;
		private boolean freezeCam = false;
		private boolean freeCam = false;
		private boolean overrideNoclip = false;
		private boolean fluidmovement = true;
		
		private boolean overrideSpectator = false;
		private float freezeCamYaw;
		private float freezeCamPitch;
		
		private double gravity = 1F;

		private String capturedCommandResult = null, cmdSentToServer = null;
		private StringBuilder capturedCommandMessages = new StringBuilder();
		private boolean captureNextCommandResult = false;	
		
		protected EntityPlayerSP(Minecraft mcIn, World worldIn, net.minecraft.client.network.NetHandlerPlayClient p_i46278_3_, StatFileWriter p_i46278_4_) {
	        super(mcIn, worldIn, p_i46278_3_, p_i46278_4_);
	        this.netClientHandler = p_i46278_3_;
	        this.statManager = p_i46278_4_;
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
		public boolean isInLava() {
			if (!this.fluidmovement) return false;
			return super.isInLava();
		}
	    
	    @Override
	    public boolean isEntityInsideOpaqueBlock() {
	        return !this.overrideNoclip && super.isEntityInsideOpaqueBlock();
	    }
	    
		public void setOverrideOnLadder(boolean flag) {
			this.overrideOnLadder = flag;
		}
		
		public boolean isOnLadderOverridden() {
			return this.overrideOnLadder;
		}
		
		public void setOverrideNoclip(boolean override) {
			this.overrideNoclip = override;
		}
		
		public boolean getOverrideNoclip() {
			return this.overrideNoclip;
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
	    public boolean isUser() {
	    	return !(this.freeCam || this.freezeCam);
	    }
		
		@Override
		public void moveEntity(double x, double y, double z) {
			if (this.freezeCam && Minecraft.getMinecraft().getRenderViewEntity() instanceof EntityCamera) {
				((EntityCamera) Minecraft.getMinecraft().getRenderViewEntity()).setFreezeCamera(0, 0, 0, this.freezeCamYaw, this.freezeCamPitch);
			}
			else if (this.freeCam && Minecraft.getMinecraft().getRenderViewEntity() instanceof EntityCamera) {
				((EntityCamera) Minecraft.getMinecraft().getRenderViewEntity()).setFreeCamera(x, y, z, this.rotationYaw, this.rotationPitch);
				return;
			}
			
			super.moveEntity(x, y, z);
		}
		
		@Override
		protected float func_175134_bD() {
			return super.func_175134_bD() * (float) this.gravity;
		}
		
		@Override
		public void onUpdate() {
			if (this.overrideNoclip) {
				this.overrideSpectator = true;
				super.onUpdate();
				this.overrideSpectator = false;
			}
			else super.onUpdate();
		}
		
		@Override
		public boolean isSpectator() {
			return this.overrideSpectator || super.isSpectator();
		}
		
		@Override
		public boolean isCurrentViewEntity() {
			return Minecraft.getMinecraft().getRenderViewEntity() instanceof EntityCamera || super.isCurrentViewEntity();
		}
	}
}
