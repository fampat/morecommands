package com.mrnobody.morecommands.network;

import com.google.common.base.Charsets;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.settings.MoreCommandsConfig;
import com.mrnobody.morecommands.util.Reference;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

/**
 * A class to communicate MoreCommands data
 * between server and client
 * 
 * @author MrNobody98
 */
public final class PacketDispatcher {
	private static final byte C00HANDSHAKE            = 0x00;
	private static final byte C01OUTPUT               = 0x01;
	private static final byte C02EXECREMOTECOMMAND    = 0x02;
	private static final byte S00HANDSHAKE            = 0x03;
	private static final byte S01HANDSHAKEFINISHED    = 0x04;
	private static final byte S02CLIMB                = 0x05;
	private static final byte S03FREECAM              = 0x06;
	private static final byte S04FREEZECAM            = 0x07;
	private static final byte S05XRAY                 = 0x08;
	private static final byte S06NOCLIP               = 0x09;
	private static final byte S07LIGHT                = 0x0A;
	private static final byte S08REACH                = 0x0B;
	private static final byte S09GRAVITY              = 0x0C;
	private static final byte S10STEPHEIGHT           = 0x0D;
	private static final byte S11FLUIDMOVEMENT        = 0x0E;
	private static final byte S12INFINITEITEMS        = 0x0F;
	private static final byte S13COMPASSTARGET        = 0x10;
	private static final byte S14REMOTEWORLD          = 0x11;
	private static final byte S15UPDATEBLOCK          = 0x12;
	private static final byte S16ITEMDAMAGE           = 0x13;
	private static final byte S17REMOTECOMMANDRESULT  = 0x14;
	
	private static final byte XRAY_SHOWCONFIG       = 0;
	private static final byte XRAY_CHANGESETTINGS   = 1;
	private static final byte XRAY_LOADSAVESETTINGS = 2;
	
	/**
	 * an enum representing a {@link Block} property to change
	 * 
	 * @author MrNobody98
	 */
	public static enum BlockUpdateType {
		/**
		 * Represents {@link Block#setLightLevel(float)}.
		 * IMPORTANT: Requires a float as int bits as argument. Use {@link Float#floatToIntBits(float)}
		 */
		LIGHT_LEVEL {@Override public void update(Block block, int value) {block.setLightLevel(Float.intBitsToFloat(value));}},
		/**
		 * Represents {@link Block#setLightOpacity(int)}.
		 * IMPORTANT: Requires a float as int bits as argument. Use {@link Float#floatToIntBits(float)}
		 */
		LIGHT_OPACITY {@Override public void update(Block block, int value) {block.setLightOpacity(value);}};
		
		/**
		 * Updates the value of the block this enum constant represents
		 * 
		 * @param block the block of which the value should be changed
		 * @param value the new value
		 */
		public abstract void update(Block block, int value);
	}
	
	private FMLEventChannel channel;
	private PacketHandlerClient packetHandlerClient;
	private PacketHandlerServer packetHandlerServer;
	
	public PacketDispatcher() {
		this.channel = NetworkRegistry.INSTANCE.newEventDrivenChannel(Reference.CHANNEL);
		this.channel.register(this);
		if (MoreCommands.isClientSide()) this.packetHandlerClient = new PacketHandlerClient();
		this.packetHandlerServer = new PacketHandlerServer();
	}
	
	/**
	 * Invoked when the client receives a packet from the server
	 */
	@SubscribeEvent
	public void onServerPacket(final ClientCustomPacketEvent event) {
		if (!event.getPacket().channel().equals(Reference.CHANNEL)) return;
		
		Minecraft.getMinecraft().addScheduledTask(new Runnable() {
			@Override
			public void run() {
				try {handleServerPacket(event.getPacket());}
				catch (Exception ex) {
					ex.printStackTrace(); 
					MoreCommands.INSTANCE.getLogger().warn("Error handling Packet");
				}
			}
		});
	}
	
	/**
	 * Invoked when the server receives a packet from the client
	 */
	@SubscribeEvent
	public void onClientPacket(final ServerCustomPacketEvent event) {
		if (!event.getPacket().channel().equals(Reference.CHANNEL)) return;
		
		((NetHandlerPlayServer) event.getHandler()).playerEntity.getServerForPlayer().addScheduledTask(new Runnable() {
			@Override
			public void run() {
				try {handleClientPacket(event.getPacket(), ((NetHandlerPlayServer) event.getHandler()).playerEntity);}
				catch (Exception ex) {
					ex.printStackTrace(); 
					MoreCommands.INSTANCE.getLogger().warn("Error handling Packet");
				}
			}
		});
	}
	
	/**
	 * Handles a packet from the client on the server
	 * 
	 * @param packet the packet
	 * @param player the player who sent this packet
	 * @throws Exception a possible exception that might occur
	 */
	private void handleClientPacket(FMLProxyPacket packet, EntityPlayerMP player) throws Exception {
		byte id = readID(packet.payload());
		ByteBuf payload = packet.payload();
		
		switch (id) {
			case C00HANDSHAKE:            processC00Handshake(payload, player); break;
			case C01OUTPUT:               processC01Output(payload, player); break;
			case C02EXECREMOTECOMMAND:    processC02ExecuteRemoteCommand(payload, player); break;
			default:                      break;
		}
	}
	
	/**
	 * Handles a packet from the server on the client
	 * 
	 * @param packet the packet
	 * @throws Exception a possible exception that might occur
	 */
	private void handleServerPacket(FMLProxyPacket packet) throws Exception {
		byte id = readID(packet.payload());
		ByteBuf payload = packet.payload();
		
		switch (id) {
			case S00HANDSHAKE:            processS00Handshake(payload); break;
			case S01HANDSHAKEFINISHED:    processS01HandshakeFinished(payload); break;
			case S02CLIMB:                processS02Climb(payload); break;
			case S03FREECAM:              processS03Freecam(payload); break;
			case S04FREEZECAM:            processS04Freezecam(payload); break;
			case S05XRAY:                 processS05Xray(payload); break;
			case S06NOCLIP:               processS06Noclip(payload); break;
			case S07LIGHT:                processS07Light(payload); break;
			case S08REACH:                processS08Reach(payload); break;
			case S09GRAVITY:              processS09Gravity(payload); break;
			case S10STEPHEIGHT:           processS10Stepheight(payload); break;
			case S11FLUIDMOVEMENT:        processS11FluidMovement(payload); break;
			case S12INFINITEITEMS:        processS12Infiniteitems(payload); break;
			case S13COMPASSTARGET:        processS13CompassTarget(payload); break;
			case S14REMOTEWORLD:          processS14RemoteWorld(payload); break;
			case S15UPDATEBLOCK:          processS15UpdateBlock(payload); break;
			case S16ITEMDAMAGE:           processS16ItemDamage(payload); break;
			case S17REMOTECOMMANDRESULT:  processS17RemoteCommandResult(payload); break;
			default:                      break;
		}
	}
	
	/**
	 * Processes a client handshake
	 */
	private void processC00Handshake(ByteBuf payload, EntityPlayerMP player) {
		boolean patched = payload.readBoolean();
		boolean renderGlobalPatched = payload.readBoolean();
		String version = readString(payload);
	    
		this.packetHandlerServer.handshake(player, patched, renderGlobalPatched, version);
	}
	
	/**
	 * Processes a packet setting whether to enable/disable chat output
	 */
	private void processC01Output(ByteBuf payload, EntityPlayerMP player) {
		boolean output = payload.readBoolean();
		this.packetHandlerServer.output(player, output);
	}
	
	/**
	 * Processes a C03ExecuteRemoteCommand message.
	 * @see PacketHandlerServer#handleExecuteRemoteCommand(EntityPlayerMP, int, String)
	 */
	private void processC02ExecuteRemoteCommand(ByteBuf payload, EntityPlayerMP player) {
		int executionID = payload.readInt();
		String command = readString(payload);
		
		this.packetHandlerServer.handleExecuteRemoteCommand(player, executionID, command);
	}
	
	/**
	 * Processes a server handshake
	 */
	private void processS00Handshake(ByteBuf payload) {
		String version = readString(payload);
		MoreCommandsConfig.enablePlayerAliases = payload.readBoolean();
		MoreCommandsConfig.enablePlayerVars = payload.readBoolean();
		
		this.packetHandlerClient.handshake(version);
	}
	
	/**
	 * Processes a packet indicating that the server is done processing the client handshake
	 */
	private void processS01HandshakeFinished(ByteBuf payload) {
		this.packetHandlerClient.handshakeFinished();
	}
	
	/**
	 * Processes a S02Climb packet.
	 * @see PacketHandlerClient#handleClimb(boolean)
	 */
	private void processS02Climb(ByteBuf payload) {
		boolean allowClimb = payload.readBoolean();
		this.packetHandlerClient.handleClimb(allowClimb);
	}
	
	/**
	 * Processes a S03Freecam packet.
	 * @see PacketHandlerClient#handleFreecam()
	 */
	private void processS03Freecam(ByteBuf payload) {
		this.packetHandlerClient.handleFreecam();
	}
	
	/**
	 * Processes a S04Frezeecam packet.
	 * @see PacketHandlerClient#handleFreezeCam()
	 */
	private void processS04Freezecam(ByteBuf payload) {
		this.packetHandlerClient.handleFreezeCam();
	}
	
	/**
	 * Processes a S05Xray packet.
	 * @see PacketHandlerClient#handleXray()
	 * @see PacketHandlerClient#handleXray(boolean, int)
	 * @see PacketHandlerClient#handleXray(boolean, String)
	 */
	private void processS05Xray(ByteBuf payload) {
		byte id = readID(payload);
		
		if (id == XRAY_SHOWCONFIG) this.packetHandlerClient.handleXray();
		else if (id == XRAY_CHANGESETTINGS) {
			boolean enableXray = payload.readBoolean();
			int radius = payload.readInt();
			
			this.packetHandlerClient.handleXray(enableXray, radius);
		}
		else if (id == XRAY_LOADSAVESETTINGS) {
			boolean load = payload.readBoolean();
			String setting = readString(payload);
			
			this.packetHandlerClient.handleXray(load, setting);
		}
	}
	
	/**
	 * Processes a S06Noclip packet.
	 * @see PacketHandlerClient#handleNoclip(boolean)
	 */
	private void processS06Noclip(ByteBuf payload) {
		boolean allowNoclip = payload.readBoolean();
		
		this.packetHandlerClient.handleNoclip(allowNoclip);;
	}
	
	/**
	 * Processes a S07Light packet.
	 * @see PacketHandlerClient#handleLight()
	 */
	private void processS07Light(ByteBuf payload) {
		this.packetHandlerClient.handleLight();
	}
	
	/**
	 * Processes a S08Reach packet.
	 * @see PacketHandlerClient#handleReach(float)
	 */
	private void processS08Reach(ByteBuf payload) {
		float reachDistance = payload.readFloat();
		this.packetHandlerClient.handleReach(reachDistance);
	}
	
	/**
	 * Processes a S09Gravity packet.
	 * @see PacketHandlerClient#setGravity(double)
	 */
	private void processS09Gravity(ByteBuf payload) {
		float gravity = payload.readFloat();
		this.packetHandlerClient.setGravity(gravity);
	}
	
	/**
	 * Processes a S10Stepheight packet.
	 * @see PacketHandlerClient#setStepheight(float)
	 */
	private void processS10Stepheight(ByteBuf payload) {
		float stepheight = payload.readFloat();
		this.packetHandlerClient.setStepheight(stepheight);
	}
	
	/**
	 * Processes a S11FluidMovement packet.
	 * @see PacketHandlerClient#setFluidMovement(boolean)
	 */
	private void processS11FluidMovement(ByteBuf payload) {
		boolean fluidmovement = payload.readBoolean();
		this.packetHandlerClient.setFluidMovement(fluidmovement);
	}
	
	/**
	 * Processes a S12Infiniteitems packet.
	 * @see PacketHandlerClient#setInfiniteitems(boolean)
	 */
	private void processS12Infiniteitems(ByteBuf payload) {
		boolean infiniteitems = payload.readBoolean();
		this.packetHandlerClient.setInfiniteitems(infiniteitems);
	}
	
	/**
	 * Processes a S13CompassTarget packet.
	 * @see PacketHandlerClient#resetCompassTarget()
	 * @see PacketHandlerClient#setCompassTarget(int, int)
	 */
	private void processS13CompassTarget(ByteBuf payload) {
		boolean reset = payload.readBoolean();
		
		if (reset) this.packetHandlerClient.resetCompassTarget();
		else this.packetHandlerClient.setCompassTarget(payload.readInt(), payload.readInt());
	}
	
	/**
	 * Processes a S14RemoteWorld packet.
	 * @see PacketHandlerClient#handleRemoteWorldName(String)
	 */
	private void processS14RemoteWorld(ByteBuf payload) {
		String worldName = readString(payload);
		this.packetHandlerClient.handleRemoteWorldName(worldName);
	}
	
	/**
	 * Processes a S15UpdateBlock packet.
	 * @see PacketHandlerClient#updateBlock(Block, BlockUpdateType, int)
	 */
	private void processS15UpdateBlock(ByteBuf payload) {
		int block = payload.readInt();
		int type = payload.readInt();
		int value = payload.readInt();
		
		this.packetHandlerClient.updateBlock(Block.getBlockById(block), BlockUpdateType.values()[type], value);
	}
	
	/**
	 * Processes a S16ItemDamage packet.
	 * @see PacketHandlerClient#setItemDamage(Item, boolean)
	 */
	private void processS16ItemDamage(ByteBuf payload) {
		int item = payload.readInt();
		boolean itemdamage = payload.readBoolean();
		
		this.packetHandlerClient.setItemDamage(Item.getItemById(item), itemdamage);
	}
	
	/**
	 * Processes a S17RemoteCommandResult packet.
	 * @see PacketHandlerClient#handleRemoteCommandResult(int, String)
	 */
	private void processS17RemoteCommandResult(ByteBuf payload) {
		int executionID = payload.readInt();
		String result = readString(payload);
		
		this.packetHandlerClient.handleRemoteCommandResult(executionID, result);
	}
	
	/**
	 * Sends a handshake packet from the client to the server
	 * @param clientPlayerPatched whether the client player was patched
	 */
	public void sendC00Handshake(boolean clientPlayerPatched, boolean renderGlobalPatched) {
		PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
	    writeID(payload, C00HANDSHAKE);
	    
	    payload.writeBoolean(clientPlayerPatched);
	    payload.writeBoolean(renderGlobalPatched);
	    writeString(Reference.VERSION, payload);
	    
		this.channel.sendToServer(new FMLProxyPacket(payload, Reference.CHANNEL));
	}
	
	/**
	 * Sends a packet setting whether to enable/disable chat ouput
	 * @param output whether to enable/disable chat output
	 */
	public void sendC01Output(boolean output) {
	    PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
	    writeID(payload, C01OUTPUT);
	    
	    payload.writeBoolean(output);
		this.channel.sendToServer(new FMLProxyPacket(payload, Reference.CHANNEL));
	}
	
	/**
	 * Sends a packet to the server to execute a command and capture its output to send it back
	 * @param executionID an id to identify to which command the output belongs to
	 * @param command the command
	 */
	public void sendC02ExecuteRemoteCommand(int executionID, String command) {
		PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
	    writeID(payload, C02EXECREMOTECOMMAND);
	    
	    payload.writeInt(executionID);
	    writeString(command, payload);
	    
		this.channel.sendToServer(new FMLProxyPacket(payload, Reference.CHANNEL));
	}
	
	/**
	 * Sends a handshake from the server to the client
	 * @param player the player who receives the packet
	 */
	public void sendS00Handshake(EntityPlayerMP player) {
	    PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
	    writeID(payload, S00HANDSHAKE);
	    
	    writeString(Reference.VERSION, payload);
	    payload.writeBoolean(MoreCommandsConfig.enablePlayerAliases);
	    payload.writeBoolean(MoreCommandsConfig.enablePlayerVars);
	    
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	/**
	 * Sends a packet from the server to the client indicating that the client handshake was processed
	 * @param player the player who receives the packet
	 */
	public void sendS01HandshakeFinished(EntityPlayerMP player) {
		PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
		writeID(payload, S01HANDSHAKEFINISHED);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	/**
	 * Sends a packet enabling/disabling the player's climb mode
	 * @param player the player who receives the packet
	 * @param climb
	 */
	public void sendS02Climb(EntityPlayerMP player, boolean climb) {
	    PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
	    writeID(payload, S02CLIMB);
	    
	    payload.writeBoolean(climb);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	/**
	 * Sends a packet which toggles the player's freecam mode
	 * @param player the player who receives the packet
	 */
	public void sendS03Freecam(EntityPlayerMP player) {
	    PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
	    writeID(payload, S03FREECAM);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	/**
	 * Sends a packet which toggles the player's freezecam mode
	 * @param player the player who receives the packet
	 */
	public void sendS04Freezecam(EntityPlayerMP player) {
	    PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
	    writeID(payload, S04FREEZECAM);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	/**
	 * Sends a packet toggling xray
	 * @param player the player who receives the packet
	 */
	public void sendS05Xray(EntityPlayerMP player) {
	    PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
	    writeID(payload, S05XRAY);
	    writeID(payload, XRAY_SHOWCONFIG);
	    
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	/**
	 * Sends a packet enabling/disabling xray and setting the xray radius
	 * @param player the player who receives the packet
	 * @param xrayEnabled whether to enable or disable xray
	 * @param blockRadius the xray radius
	 */
	public void sendS05Xray(EntityPlayerMP player, boolean xrayEnabled, int blockRadius) {
	    PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
	    writeID(payload, S05XRAY);
	    writeID(payload, XRAY_CHANGESETTINGS);
	    
	    payload.writeBoolean(xrayEnabled);
	    payload.writeInt(blockRadius);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	/**
	 * Sends a packet which loads/saves an xray setting
	 * @param player the player who receives the packet
	 * @param load true to load a setting, false to save a setting
	 * @param setting the setting name
	 */
	public void sendS05Xray(EntityPlayerMP player, boolean load, String setting) {
	    PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
	    writeID(payload, S05XRAY);
	    writeID(payload, XRAY_LOADSAVESETTINGS);
	    
	    payload.writeBoolean(load);
	    writeString(setting, payload);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	/**
	 * Sends a packet enabling/disabling noclip
	 * @param player the player who receives the packet
	 * @param noclip whether to enable or disable noclip
	 */
	public void sendS06Noclip(EntityPlayerMP player, boolean noclip) {
	    PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
	    writeID(payload, S06NOCLIP);
	    
	    payload.writeBoolean(noclip);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	/**
	 * Sends a packet toggling world lighting
	 * @param player the player who receives the packet
	 */
	public void sendS07Light(EntityPlayerMP player) {
	    PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
	    writeID(payload, S07LIGHT);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	/**
	 * Sends a packet setting the players reach distance
	 * @param player the player who receives the packet
	 * @param reachDistance the reach distance
	 */
	public void sendS08Reach(EntityPlayerMP player, float reachDistance) {
	    PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
	    writeID(payload, S08REACH);
	    
	    payload.writeFloat(reachDistance);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	/**
	 * Sends a packet setting the players gravity/jump height
	 * @param player the player who receives the packet
	 * @param gravity the gravity (higher is less gravity, actually it's a factor for the jump height)
	 */
	public void sendS09Gravity(EntityPlayerMP player, float gravity) {
	    PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
	    writeID(payload, S09GRAVITY);
	    
	    payload.writeFloat(gravity);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	/**
	 * Sends a packet setting the players step height
	 * @param player the player who receives the packet
	 * @param stepheight the step height
	 */
	public void sendS10Stepheight(EntityPlayerMP player, float stepheight) {
	    PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
	    writeID(payload, S10STEPHEIGHT);
	    
	    payload.writeFloat(stepheight);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	/**
	 * Sends a packet enabling/disabling the special movement handling in fluids
	 * @param player the player who receives the packet
	 * @param fluidmovement whether to enable or disable fluid movement handling
	 */
	public void sendS11FluidMovement(EntityPlayerMP player, boolean fluidmovement) {
	    PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
	    writeID(payload, S11FLUIDMOVEMENT);
	    
	    payload.writeBoolean(fluidmovement);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	/**
	 * Sends a packet enabling/disabling infinite items
	 * @param player the player who receives the packet
	 * @param infiniteitems whether to enable or disable infinite items
	 */
	public void sendS12Infiniteitems(EntityPlayerMP player, boolean infiniteitems) {
	    PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
	    writeID(payload, S12INFINITEITEMS);
	    
	    payload.writeBoolean(infiniteitems);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	/**
	 * Sends a packet resetting the compass target to the spawn point
	 * @param player the player who receives the packet
	 */
	public void sendS13ResetCompassTarget(EntityPlayerMP player) {
	    PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
	    writeID(payload, S13COMPASSTARGET);
	    
	    payload.writeBoolean(true);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	/**
	 * Sends a packet setting the compass target
	 * @param player the player who receives the packet
	 * @param x the x coordinate of the target
	 * @param z the z coordinate of the target
	 */
	public void sendS13SetCompassTarget(EntityPlayerMP player, int x, int z) {
	    PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
	    writeID(payload, S13COMPASSTARGET);
	    
	    payload.writeBoolean(false);
	    payload.writeInt(x);
	    payload.writeInt(z);
	    
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	/**
	 * Sends a packet informing the client about the name of a player's world
	 * @param player the player who receives the packet
	 * @param world the player's world name
	 */
	public void sendS14RemoteWorld(EntityPlayerMP player, String world) {
		PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
	    writeID(payload, S14REMOTEWORLD);
	    
	    writeString(world, payload);
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	/**
	 * Sends a packet updating a block property for all clients
	 * @param block the block for which a property should be updated
	 * @param type the property type that should be updated
	 * @param value the updated value
	 */
	public void sendS15UpdateBlock(Block block, BlockUpdateType type, int value) {
	    PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
	    writeID(payload, S15UPDATEBLOCK);
	    
	    payload.writeInt(Block.getIdFromBlock(block));
	    payload.writeInt(type.ordinal());
	    payload.writeInt(value);
	    
		this.channel.sendToAll(new FMLProxyPacket(payload, Reference.CHANNEL));
	}
	
	/**
	 * Sends a packet enabling/disabling item damage for an item
	 * @param player the player who receives the packet
	 * @param item the item for which damage should be enabled/disabled
	 * @param itemdamage whether to enable/disable damage for this item
	 */
	public void sendS16ItemDamage(EntityPlayerMP player, Item item, boolean itemdamage) {
	    PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
	    writeID(payload, S16ITEMDAMAGE);
	    
	    payload.writeInt(Item.getIdFromItem(item));
	    payload.writeBoolean(itemdamage);
	    
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	/**
	 * Sends a packet with the captured content as response to a C03ExecuteRemoteCommand packet
	 * @param player the player who receives the packet
	 * @param executionID the id to identify the command
	 * @param result the captured result
	 */
	public void sendS17RemoteCommandResult(EntityPlayerMP player, int executionID, String result) {
		PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
	    writeID(payload, S17REMOTECOMMANDRESULT);
	    
	    payload.writeInt(executionID);
	    writeString(result, payload);
	    
		this.channel.sendTo(new FMLProxyPacket(payload, Reference.CHANNEL), player);
	}
	
	/**
	 * writes a byte to the buffer which represents the packet id
	 * 
	 * @param buffer the buffer
	 * @param id the id
	 */
	private static void writeID(ByteBuf buffer, byte id) {
		buffer.writeByte(id);
	}
	
	/**
	 * reads a byte from the buffer which represents the packet id
	 * 
	 * @param buffer the buffer
	 * @return the id
	 */
	private static byte readID(ByteBuf buffer) {
		return buffer.readByte();
	}
	
	/**
	 * Reads a string from the buffer
	 * 
	 * @param buffer the buffer
	 * @return the string
	 */
	private static String readString(ByteBuf buffer) {
		int length = buffer.readInt();
		byte[] string = buffer.readBytes(length).array();
		return new String(string, Charsets.UTF_8);
	}
	
	/**
	 * Writes a string to the buffer
	 * 
	 * @param string the string
	 * @param buffer the buffer
	 */
	private static void writeString(String string, ByteBuf buffer) {
		byte[] bytes = string.getBytes(Charsets.UTF_8);
		buffer.writeInt(bytes.length);
		buffer.writeBytes(bytes);
	}
}
