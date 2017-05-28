package com.mrnobody.morecommands.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import com.mrnobody.morecommands.core.MoreCommands;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockStem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ServerSelectionList;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.command.ICommandManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.NetworkSystem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.Style;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.client.ExtendedServerListData;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;

/**
 * Contains wrapper classes for fields and methods that contains
 * information about them to access them via reflection.
 * 
 * @author MrNobody98
 *
 */
public final class ObfuscatedNames {
	private ObfuscatedNames() {}
	
	private static boolean deobfSet = false, isDeobf = false;
	
	/**
	 * Sets the environment names for the default {@link ObfuscatedField}s and {@link ObfuscatedMethod}s.
	 * The environment name is either de deobfuscated name or the obfuscated name depending on whether
	 * minecraft is running in a deobfuscated environment or not
	 * @param isDeobf whether minecraft is running in a deobfuscated environment
	 */
	public static void setEnvNames(boolean isDeobf) {
		if (deobfSet) return;
		
		try {
			for (Field f : ObfuscatedField.class.getFields()) {
				if (Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers()) && 
					Modifier.isFinal(f.getModifiers()) && f.get(null) instanceof ObfuscatedField<?, ?>)
					((ObfuscatedField<?, ?>) f.get(null)).setEnvName(isDeobf);
			}
			
			for (Field f : ObfuscatedMethod.class.getFields()) {
				if (Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers()) && 
					Modifier.isFinal(f.getModifiers()) && f.get(null) instanceof ObfuscatedMethod<?, ?>)
					((ObfuscatedMethod<?, ?>) f.get(null)).setEnvName(isDeobf);
			}
		}
		catch (Exception ex) {MoreCommands.INSTANCE.getLogger().warn("Failed to set environment names", ex);}
		
		deobfSet = true;
	}
	
	/**
	 * A class that contains information about fields.
	 * (Owner, type, deobfuscated and obfuscated name)
	 * 
	 * @author MrNobody98
	 *
	 * @param <O> the owner type
	 * @param <T> the field's type
	 */
	public static final class ObfuscatedField<O, T> {		
		public static final ObfuscatedField<NetHandlerPlayClient, GuiScreen> NetHandlerPlayClient_guiScreenServer;
		public static final ObfuscatedField<ClientCommandHandler, ClientCommandHandler> ClientCommandHandler_instance;
		public static final ObfuscatedField<NetHandlerPlayClient, WorldClient> NetHandlerPlayClient_clientWorldController;
		public static final ObfuscatedField<SimpleReloadableResourceManager, List<?>> SimpleReloadableResourceManager_reloadListeners;
		public static final ObfuscatedField<GuiChat, String> GuiChat_defaultInputFieldText;
		public static final ObfuscatedField<Minecraft, String> Minecraft_serverName;
		public static final ObfuscatedField<Minecraft, Integer> Minecraft_serverPort;
		public static final ObfuscatedField<Minecraft, NetworkManager> Minecraft_myNetworkManager;
		public static final ObfuscatedField<GuiMultiplayer, GuiScreen> GuiMultiplayer_parentScreen;
		public static final ObfuscatedField<GuiMultiplayer, ServerSelectionList> GuiMultiplayer_serverListSelector;
		public static final ObfuscatedField<GuiMultiplayer, Boolean> GuiMultiplayer_directConnect;
		public static final ObfuscatedField<GuiMultiplayer, ServerData> GuiMultiplayer_selectedServer;
		public static final ObfuscatedField<FMLClientHandler, CountDownLatch> FMLClientHandler_startupConnectionData;
		public static final ObfuscatedField<FMLClientHandler, Map<ServerData, ExtendedServerListData>> FMLClientHandler_serverDataTag;
		public static final ObfuscatedField<GuiConnecting, AtomicInteger> GuiConnecting_CONNECTION_ID;
		
		static {if (FMLCommonHandler.instance().getSide().isClient()) {
			NetHandlerPlayClient_guiScreenServer = new ObfuscatedField<NetHandlerPlayClient, GuiScreen>("guiScreenServer", "field_147307_j", NetHandlerPlayClient.class, GuiScreen.class);
			ClientCommandHandler_instance = new ObfuscatedField<ClientCommandHandler, ClientCommandHandler>("instance", "instance", ClientCommandHandler.class, ClientCommandHandler.class);
			NetHandlerPlayClient_clientWorldController = new ObfuscatedField<NetHandlerPlayClient, WorldClient>("clientWorldController", "field_147300_g", NetHandlerPlayClient.class, WorldClient.class);
			SimpleReloadableResourceManager_reloadListeners = new ObfuscatedField<SimpleReloadableResourceManager, List<?>>("reloadListeners", "field_110546_b", SimpleReloadableResourceManager.class, (Class<List<?>>) (Class<?>) List.class);
			GuiChat_defaultInputFieldText = new ObfuscatedField<GuiChat, String>("defaultInputFieldText", "field_146409_v", GuiChat.class, String.class);
			Minecraft_serverName = new ObfuscatedField<Minecraft, String>("serverName", "field_71475_ae", Minecraft.class, String.class);
			Minecraft_serverPort = new ObfuscatedField<Minecraft, Integer>("serverPort", "field_71477_af", Minecraft.class, int.class);
			Minecraft_myNetworkManager = new ObfuscatedField<Minecraft, NetworkManager>("myNetworkManager", "field_71453_ak", Minecraft.class, NetworkManager.class);
			GuiMultiplayer_parentScreen = new ObfuscatedField<GuiMultiplayer, GuiScreen>("parentScreen", "field_146798_g", GuiMultiplayer.class, GuiScreen.class);
			GuiMultiplayer_serverListSelector = new ObfuscatedField<GuiMultiplayer, ServerSelectionList>("serverListSelector", "field_146803_h", GuiMultiplayer.class, ServerSelectionList.class);
			GuiMultiplayer_directConnect = new ObfuscatedField<GuiMultiplayer, Boolean>("directConnect", "field_146813_x", GuiMultiplayer.class, boolean.class);
			GuiMultiplayer_selectedServer = new ObfuscatedField<GuiMultiplayer, ServerData>("selectedServer", "field_146811_z", GuiMultiplayer.class, ServerData.class);
			FMLClientHandler_startupConnectionData = new ObfuscatedField<FMLClientHandler, CountDownLatch>("startupConnectionData", "startupConnectionData", FMLClientHandler.class, CountDownLatch.class);
			FMLClientHandler_serverDataTag = new ObfuscatedField<FMLClientHandler, Map<ServerData, ExtendedServerListData>>("serverDataTag", "serverDataTag", FMLClientHandler.class, (Class<Map<ServerData, ExtendedServerListData>>) (Class<?>) Map.class);
			GuiConnecting_CONNECTION_ID = new ObfuscatedField<GuiConnecting, AtomicInteger>("CONNECTION_ID", "field_146372_a", GuiConnecting.class, AtomicInteger.class);
		} else {
			NetHandlerPlayClient_guiScreenServer = null;
			ClientCommandHandler_instance = null;
			NetHandlerPlayClient_clientWorldController = null;
			SimpleReloadableResourceManager_reloadListeners = null;
			GuiChat_defaultInputFieldText = null;
			Minecraft_serverName = null;
			Minecraft_serverPort = null;
			Minecraft_myNetworkManager = null;
			GuiMultiplayer_parentScreen = null;
			GuiMultiplayer_serverListSelector = null;
			GuiMultiplayer_directConnect = null;
			GuiMultiplayer_selectedServer = null;
			FMLClientHandler_startupConnectionData = null;
			FMLClientHandler_serverDataTag = null;
			GuiConnecting_CONNECTION_ID = null;
		}}
		
		public static final ObfuscatedField<NetHandlerPlayServer, Double> NetHandlerPlayServer_field_184349_l = new ObfuscatedField
				<NetHandlerPlayServer, Double>("field_184349_l", "field_184349_l", NetHandlerPlayServer.class, double.class);
		public static final ObfuscatedField<NetHandlerPlayServer, Double> NetHandlerPlayServer_field_184350_m = new ObfuscatedField
				<NetHandlerPlayServer, Double>("field_184350_m", "field_184350_m", NetHandlerPlayServer.class, double.class);
		public static final ObfuscatedField<NetHandlerPlayServer, Double> NetHandlerPlayServer_field_184351_n = new ObfuscatedField
				<NetHandlerPlayServer, Double>("field_184351_n", "field_184351_n", NetHandlerPlayServer.class, double.class);
		public static final ObfuscatedField<NetHandlerPlayServer, Double> NetHandlerPlayServer_field_184352_o = new ObfuscatedField
				<NetHandlerPlayServer, Double>("field_184352_o", "field_184352_o", NetHandlerPlayServer.class, double.class);
		public static final ObfuscatedField<NetHandlerPlayServer, Double> NetHandlerPlayServer_field_184353_p = new ObfuscatedField
				<NetHandlerPlayServer, Double>("field_184353_p", "field_184353_p", NetHandlerPlayServer.class, double.class);
		public static final ObfuscatedField<NetHandlerPlayServer, Double> NetHandlerPlayServer_field_184354_q = new ObfuscatedField
				<NetHandlerPlayServer, Double>("field_184354_q", "field_184354_q", NetHandlerPlayServer.class, double.class);
		public static final ObfuscatedField<NetHandlerPlayServer, Vec3d> NetHandlerPlayServer_field_184362_y = new ObfuscatedField
				<NetHandlerPlayServer, Vec3d>("field_184362_y", "field_184362_y", NetHandlerPlayServer.class, Vec3d.class);
		public static final ObfuscatedField<NetHandlerPlayServer, Integer> NetHandlerPlayServer_field_184343_A = new ObfuscatedField
				<NetHandlerPlayServer, Integer>("field_184343_A", "field_184343_A", NetHandlerPlayServer.class, int.class);
		public static final ObfuscatedField<NetHandlerPlayServer, Integer> NetHandlerPlayServer_field_184348_G = new ObfuscatedField
				<NetHandlerPlayServer, Integer>("field_184348_G", "field_184348_G", NetHandlerPlayServer.class, int.class);
		public static final ObfuscatedField<NetHandlerPlayServer, Integer> NetHandlerPlayServer_field_184347_F = new ObfuscatedField
				<NetHandlerPlayServer, Integer>("field_184347_F", "field_184347_F", NetHandlerPlayServer.class, int.class);
		public static final ObfuscatedField<NetHandlerPlayServer, Integer> NetHandlerPlayServer_networkTickCount = new ObfuscatedField
				<NetHandlerPlayServer, Integer>("networkTickCount", "field_147368_e", NetHandlerPlayServer.class, int.class);
		public static final ObfuscatedField<NetHandlerPlayServer, Boolean> NetHandlerPlayServer_floating = new ObfuscatedField
				<NetHandlerPlayServer, Boolean>("floating", "field_184344_B", NetHandlerPlayServer.class, boolean.class);
		
		public static final ObfuscatedField<MinecraftServer, ICommandManager> MinecraftServer_commandManager = new ObfuscatedField
				<MinecraftServer, ICommandManager>("commandManager", "field_71321_q", MinecraftServer.class, ICommandManager.class);
		public static final ObfuscatedField<StatList, Map<String, StatBase>> StatList_idToStatMap = new ObfuscatedField
				<StatList, Map<String, StatBase>>("idToStatMap", "field_188093_a", StatList.class, (Class<Map<String, StatBase>>) (Class<?>) Map.class);
		public static final ObfuscatedField<BlockStem, Block> BlockStem_crop = new ObfuscatedField
				<BlockStem, Block>("crop", "field_149877_a", BlockStem.class, Block.class);
		public static final ObfuscatedField<PlayerList, Map<UUID, EntityPlayerMP>> PlayerList_uuidToPlayerMap = new ObfuscatedField
				<PlayerList, Map<UUID, EntityPlayerMP>>("uuidToPlayerMap", "field_177454_f", PlayerList.class, (Class<Map<UUID, EntityPlayerMP>>) (Class<?>) Map.class);
		public static final ObfuscatedField<PlayerCapabilities, Float> PlayerCapabilities_walkSpeed = new ObfuscatedField
				<PlayerCapabilities, Float>("walkSpeed", "field_75097_g", PlayerCapabilities.class, float.class);
		public static final ObfuscatedField<PlayerCapabilities, Float> PlayerCapabilities_flySpeed = new ObfuscatedField
				<PlayerCapabilities, Float>("flySpeed", "field_75096_f", PlayerCapabilities.class, float.class);
		public static final ObfuscatedField<World, WorldInfo> World_worldInfo = new ObfuscatedField
				<World, WorldInfo>("worldInfo", "field_72986_A", World.class, WorldInfo.class);
		public static final ObfuscatedField<EntityMinecart, Float> EntityMinecart_currentSpeedRail = new ObfuscatedField
				<EntityMinecart, Float>("currentSpeedRail", "currentSpeedRail", EntityMinecart.class, float.class);
		public static final ObfuscatedField<EntityPlayerMP, String> EntityPlayerMP_translator = new ObfuscatedField
				<EntityPlayerMP, String>("translator", "field_71148_cg", EntityPlayerMP.class, String.class);
		
		public static final ObfuscatedField<EntityList, Map<String, Class<? extends Entity>>> EntityList_stringToClassMapping = new ObfuscatedField
				<EntityList, Map<String, Class<? extends Entity>>>("stringToClassMapping", "field_75625_b", EntityList.class, (Class<Map<String, Class<? extends Entity>>>) (Class<?>) Map.class);
		public static final ObfuscatedField<EntityList, Map<Class<? extends Entity>, String>> EntityList_classToStringMapping = new ObfuscatedField
				<EntityList, Map<Class<? extends Entity>, String>>("classToStringMapping", "field_75626_c", EntityList.class, (Class<Map<Class<? extends Entity>, String>>) (Class<?>) Map.class);
		public static final ObfuscatedField<EntityList, Map<Integer, Class<? extends Entity>>> EntityList_idToClassMapping = new ObfuscatedField
				<EntityList, Map<Integer, Class<? extends Entity>>>("idToClassMapping", "field_75623_d", EntityList.class, (Class<Map<Integer, Class<? extends Entity>>>) (Class<?>) Map.class);
		public static final ObfuscatedField<EntityList, Map<Class<? extends Entity>, Integer>> EntityList_classToIDMapping = new ObfuscatedField
				<EntityList, Map<Class<? extends Entity>, Integer>>("classToIDMapping", "field_75624_e", EntityList.class, (Class<Map<Class<? extends Entity>, Integer>>) (Class<?>) Map.class);
		public static final ObfuscatedField<EntityList, Map<String, Integer>> EntityList_stringToIDMapping = new ObfuscatedField
				<EntityList, Map<String, Integer>>("stringToIDMapping", "field_180126_g", EntityList.class, (Class<Map<String, Integer>>) (Class<?>) Map.class);
		public static final ObfuscatedField<NetworkSystem, List<NetworkManager>> NetworkSystem_networkManagers = new ObfuscatedField
				<NetworkSystem, List<NetworkManager>>("networkManagers", "field_151272_f", NetworkSystem.class, (Class<List<NetworkManager>>) (Class<?>) List.class);
		
		//This field is generated dynamically via com.mrnbobody.morecommands.asm.transform.TransformStyle
		public static final ObfuscatedField<Style, Style> Style_defaultStyle = new ObfuscatedField
				<Style, Style>("defaultStyle", "defaultStyle", Style.class, Style.class);
		public static final ObfuscatedField<Field, Integer> Field_modifiers = new ObfuscatedField
				<Field, Integer>("modifiers", "modifiers", Field.class, int.class);
		
		private final String obfName, deobfName; private String envName;
		private final Class<T> retClass; private final Class<O> owner;
		
		/**
		 * Constructs a new {@link ObfuscatedField}
		 * 
		 * @param deobfName the deobfuscated name of the field
		 * @param obfName the obfuscated name of the field
		 * @param owner the owner class of the field
		 * @param retClass the type class of the field
		 */
		public ObfuscatedField(String deobfName, String obfName, Class<O> owner, Class<T> retClass) {
			this.obfName = obfName;
			this.deobfName = deobfName;
			this.retClass = retClass;
			this.owner = owner;
			if (ObfuscatedNames.deobfSet) this.setEnvName(ObfuscatedNames.isDeobf);
		}
		
		/**
		 * Sets the environment name of this field depending on whether minecraft is running in a deobfusctated environment or not
		 * @param isDeobf whether minecraft is running in a deobfuscated environment
		 */
		public void setEnvName(boolean isDeobf) {
			this.envName = isDeobf ? this.deobfName : this.obfName;
		}
		
		/**
		 * @return the deobfuscated name of this field
		 */
		public String getDeobfName() {
			return this.deobfName;
		}
		
		/**
		 * @return the obfuscated name of this field
		 */
		public String getObfName() {
			return this.obfName;
		}
		
		/**
		 * @return the environment name of this field
		 */
		public String getEnvName() {
			return this.envName;
		}
		
		/**
		 * @return the owner class of this field
		 */
		public Class<O> getOwnerClass() {
			return this.owner;
		}
		
		/**
		 * @return the type class of this field
		 */
		public Class<T> getTypeClass() {
			return this.retClass;
		}
	}
	
	/**
	 * A class that contains information about methods.
	 * (Owner, return type, parameter types, deobfuscated and obfuscated name)
	 * 
	 * @author MrNobody98
	 *
	 * @param <O> the owner type
	 * @param <R> the return type
	 */
	public static final class ObfuscatedMethod<O, R> {
		public static final ObfuscatedMethod<MinecraftServer, Void> MinecraftServer_saveAllWorlds = new ObfuscatedMethod
				<MinecraftServer, Void>("saveAllWorlds", "func_71267_a", MinecraftServer.class, void.class, boolean.class);
		public static final ObfuscatedMethod<MinecraftServer, Void> MinecraftServer_loadAllWorlds = new ObfuscatedMethod
				<MinecraftServer, Void>("loadAllWorlds", "func_71247_a", MinecraftServer.class, void.class, String.class, String.class, long.class, WorldType.class, String.class);
		public static final ObfuscatedMethod<EntityHorse, Void> EntityHorse_initHorseChest = new ObfuscatedMethod
				<EntityHorse, Void>("initHorseChest", "func_110226_cD", EntityHorse.class, void.class);
		public static final ObfuscatedMethod<EntityHorse, Void> EntityHorse_updateHorseSlots = new ObfuscatedMethod
				<EntityHorse, Void>("updateHorseSlots", "func_110232_cE", EntityHorse.class, void.class);
		//This method is generated dynamically via com.mrnbobody.morecommands.asm.transform.TransformBlockRailBase
		public static final ObfuscatedMethod<BlockRailBase, Void> BlockRailBase_setMaxRailSpeed = new ObfuscatedMethod
				<BlockRailBase, Void>("setMaxRailSpeed", "setMaxRailSpeed", BlockRailBase.class, void.class, float.class);
		
		private final String obfName, deobfName; private String envName;
		private final Class<R> retClass; private final Class<O> owner;
		private final Class<?>[] parameters;
		
		/**
		 * Constructs a new {@link ObfuscatedMethod}
		 * 
		 * @param deobfName the deobfuscated name of the method
		 * @param obfName the obfuscated name of the method
		 * @param owner the owner class of the method
		 * @param retClass the type class of the method
		 * @param parameters the parameter types of the method
		 */
		public ObfuscatedMethod(String deobfName, String obfName, Class<O> owner, Class<R> retClass, Class<?>... parameters) {
			this.obfName = obfName;
			this.deobfName = deobfName;
			this.retClass = retClass;
			this.owner = owner;
			this.parameters = parameters;
			if (ObfuscatedNames.deobfSet) this.setEnvName(ObfuscatedNames.isDeobf);
		}
		
		/**
		 * Sets the environment name of this field depending on whether minecraft is running in a deobfusctated environment or not
		 * @param isDeobf whether minecraft is running in a deobfuscated environment
		 */
		public void setEnvName(boolean isDeobf) {
			this.envName = isDeobf ? this.deobfName : this.obfName;
		}
		
		/**
		 * @return the deobfuscated name of this field
		 */
		public String getDeobfName() {
			return this.deobfName;
		}
		
		/**
		 * @return the obfuscated name of this field
		 */
		public String getObfName() {
			return this.obfName;
		}
		
		/**
		 * @return the environment name of this field
		 */
		public String getEnvName() {
			return this.envName;
		}
		
		/**
		 * @return the owner class of this field
		 */
		public Class<O> getOwnerClass() {
			return this.owner;
		}
		
		/**
		 * @return the class of the return type
		 */
		public Class<R> getReturnClass() {
			return this.retClass;
		}
		
		/**
		 * @return the parameter type classes
		 */
		public Class<?>[] getParameters() {
			return this.parameters;
		}
	}
}
