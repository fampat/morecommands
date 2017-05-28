package com.mrnobody.morecommands.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import com.mrnobody.morecommands.core.MoreCommands;

import cpw.mods.fml.client.ExtendedServerListData;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockStem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ServerSelectionList;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.command.ICommandManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.inventory.AnimalChest;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.NetworkSystem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.FoodStats;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.client.ClientCommandHandler;

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
		public static final ObfuscatedField<Minecraft, MusicTicker> Minecraft_mcMusicTicker;
		public static final ObfuscatedField<MusicTicker, Integer> MusicTicker_field_147676_d;
		public static final ObfuscatedField<NetHandlerPlayClient, GuiScreen> NetHandlerPlayClient_guiScreenServer;
		public static final ObfuscatedField<ClientCommandHandler, ClientCommandHandler> ClientCommandHandler_instance;
		public static final ObfuscatedField<NetHandlerPlayClient, WorldClient> NetHandlerPlayClient_clientWorldController;
		public static final ObfuscatedField<GuiChat, String> GuiChat_defaultInputFieldText;
		public static final ObfuscatedField<Minecraft, String> Minecraft_serverName;
		public static final ObfuscatedField<Minecraft, Integer> Minecraft_serverPort;
		public static final ObfuscatedField<Minecraft, NetworkManager> Minecraft_myNetworkManager;
		public static final ObfuscatedField<GuiMultiplayer, GuiScreen> GuiMultiplayer_field_146798_g;
		public static final ObfuscatedField<GuiMultiplayer, ServerSelectionList> GuiMultiplayer_field_146803_h;
		public static final ObfuscatedField<GuiMultiplayer, Boolean> GuiMultiplayer_field_146813_x;
		public static final ObfuscatedField<GuiMultiplayer, ServerData> GuiMultiplayer_field_146811_z;
		public static final ObfuscatedField<FMLClientHandler, CountDownLatch> FMLClientHandler_startupConnectionData;
		public static final ObfuscatedField<FMLClientHandler, CountDownLatch> FMLClientHandler_playClientBlock;
		public static final ObfuscatedField<FMLClientHandler, Map<ServerData, ExtendedServerListData>> FMLClientHandler_serverDataTag;
		public static final ObfuscatedField<GuiConnecting, AtomicInteger> GuiConnecting_field_146372_a;
		
		static {if (FMLCommonHandler.instance().getSide().isClient()) {
			Minecraft_mcMusicTicker = new ObfuscatedField<Minecraft, MusicTicker>("mcMusicTicker", "field_147126_aw", Minecraft.class, MusicTicker.class);
			MusicTicker_field_147676_d = new ObfuscatedField<MusicTicker, Integer>("field_147676_d", "field_147676_d", MusicTicker.class, int.class);
			NetHandlerPlayClient_guiScreenServer = new ObfuscatedField<NetHandlerPlayClient, GuiScreen>("guiScreenServer", "field_147307_j", NetHandlerPlayClient.class, GuiScreen.class);
			ClientCommandHandler_instance = new ObfuscatedField<ClientCommandHandler, ClientCommandHandler>("instance", "instance", ClientCommandHandler.class, ClientCommandHandler.class);
			NetHandlerPlayClient_clientWorldController = new ObfuscatedField<NetHandlerPlayClient, WorldClient>("clientWorldController", "field_147300_g", NetHandlerPlayClient.class, WorldClient.class);
			GuiChat_defaultInputFieldText = new ObfuscatedField<GuiChat, String>("defaultInputFieldText", "field_146409_v", GuiChat.class, String.class);
			Minecraft_serverName = new ObfuscatedField<Minecraft, String>("serverName", "field_71475_ae", Minecraft.class, String.class);
			Minecraft_serverPort = new ObfuscatedField<Minecraft, Integer>("serverPort", "field_71477_af", Minecraft.class, int.class);
			Minecraft_myNetworkManager = new ObfuscatedField<Minecraft, NetworkManager>("myNetworkManager", "field_71453_ak", Minecraft.class, NetworkManager.class);
			GuiMultiplayer_field_146798_g = new ObfuscatedField<GuiMultiplayer, GuiScreen>("field_146798_g", "field_146798_g", GuiMultiplayer.class, GuiScreen.class);
			GuiMultiplayer_field_146803_h = new ObfuscatedField<GuiMultiplayer, ServerSelectionList>("field_146803_h", "field_146803_h", GuiMultiplayer.class, ServerSelectionList.class);
			GuiMultiplayer_field_146813_x = new ObfuscatedField<GuiMultiplayer, Boolean>("field_146813_x", "field_146813_x", GuiMultiplayer.class, boolean.class);
			GuiMultiplayer_field_146811_z = new ObfuscatedField<GuiMultiplayer, ServerData>("field_146811_z", "field_146811_z", GuiMultiplayer.class, ServerData.class);
			FMLClientHandler_startupConnectionData = new ObfuscatedField<FMLClientHandler, CountDownLatch>("startupConnectionData", "startupConnectionData", FMLClientHandler.class, CountDownLatch.class);
			FMLClientHandler_playClientBlock = new ObfuscatedField<FMLClientHandler, CountDownLatch>("playClientBlock", "playClientBlock", FMLClientHandler.class, CountDownLatch.class);
			FMLClientHandler_serverDataTag = new ObfuscatedField<FMLClientHandler, Map<ServerData, ExtendedServerListData>>("serverDataTag", "serverDataTag", FMLClientHandler.class, (Class<Map<ServerData, ExtendedServerListData>>) (Class<?>) Map.class);
			GuiConnecting_field_146372_a = new ObfuscatedField<GuiConnecting, AtomicInteger>("field_146372_a", "field_146372_a", GuiConnecting.class, AtomicInteger.class);
		} else {
			Minecraft_mcMusicTicker = null;
			MusicTicker_field_147676_d = null;
			NetHandlerPlayClient_guiScreenServer = null;
			ClientCommandHandler_instance = null;
			NetHandlerPlayClient_clientWorldController = null;
			GuiChat_defaultInputFieldText = null;
			Minecraft_serverName = null;
			Minecraft_serverPort = null;
			Minecraft_myNetworkManager = null;
			GuiMultiplayer_field_146798_g = null;
			GuiMultiplayer_field_146803_h = null;
			GuiMultiplayer_field_146813_x = null;
			GuiMultiplayer_field_146811_z = null;
			FMLClientHandler_startupConnectionData = null;
			FMLClientHandler_playClientBlock = null;
			FMLClientHandler_serverDataTag = null;
			GuiConnecting_field_146372_a = null;
		}}
		
		public static final ObfuscatedField<MinecraftServer, ICommandManager> MinecraftServer_commandManager = new ObfuscatedField
				<MinecraftServer, ICommandManager>("commandManager", "field_71321_q", MinecraftServer.class, ICommandManager.class);
		public static final ObfuscatedField<StatList, Map<String, StatBase>> StatList_oneShotStats = new ObfuscatedField
				<StatList, Map<String, StatBase>>("oneShotStats", "field_75942_a", StatList.class, (Class<Map<String, StatBase>>) (Class<?>) Map.class);
		public static final ObfuscatedField<BlockStem, Block> BlockStem_field_149877_a = new ObfuscatedField
				<BlockStem, Block>("field_149877_a", "field_149877_a", BlockStem.class, Block.class);
		public static final ObfuscatedField<FoodStats, Integer> FoodStats_foodLevel = new ObfuscatedField
				<FoodStats, Integer>("foodLevel", "field_75127_a", FoodStats.class, int.class);
		public static final ObfuscatedField<PlayerCapabilities, Float> PlayerCapabilities_walkSpeed = new ObfuscatedField
				<PlayerCapabilities, Float>("walkSpeed", "field_75097_g", PlayerCapabilities.class, float.class);
		public static final ObfuscatedField<PlayerCapabilities, Float> PlayerCapabilities_flySpeed = new ObfuscatedField
				<PlayerCapabilities, Float>("flySpeed", "field_75096_f", PlayerCapabilities.class, float.class);
		public static final ObfuscatedField<EntityMinecart, Float> EntityMinecart_currentSpeedRail = new ObfuscatedField
				<EntityMinecart, Float>("currentSpeedRail", "currentSpeedRail", EntityMinecart.class, float.class);
		public static final ObfuscatedField<EntityPlayerMP, String> EntityPlayerMP_translator = new ObfuscatedField
				<EntityPlayerMP, String>("translator", "field_71148_cg", EntityPlayerMP.class, String.class);
		public static final ObfuscatedField<NBTTagList, List<NBTBase>> NBTTagList_tagList = new ObfuscatedField
				<NBTTagList, List<NBTBase>>("tagList", "field_74747_a", NBTTagList.class, (Class<List<NBTBase>>) (Class<?>) List.class);
		public static final ObfuscatedField<EntityHorse, AnimalChest> EntityHorse_horseChest = new ObfuscatedField
				<EntityHorse, AnimalChest>("horseChest", "field_110296_bG", EntityHorse.class, AnimalChest.class);
		public static final ObfuscatedField<World, WorldInfo> World_worldInfo = new ObfuscatedField
				<World, WorldInfo>("worldInfo", "field_72986_A", World.class, WorldInfo.class);
		
		public static final ObfuscatedField<EntityList, Map<String, Class<? extends Entity>>> EntityList_stringToClassMapping = new ObfuscatedField
				<EntityList, Map<String, Class<? extends Entity>>>("stringToClassMapping", "field_75625_b", EntityList.class, (Class<Map<String, Class<? extends Entity>>>) (Class<?>) Map.class);
		public static final ObfuscatedField<EntityList, Map<Class<? extends Entity>, String>> EntityList_classToStringMapping = new ObfuscatedField
				<EntityList, Map<Class<? extends Entity>, String>>("classToStringMapping", "field_75626_c", EntityList.class, (Class<Map<Class<? extends Entity>, String>>) (Class<?>) Map.class);
		public static final ObfuscatedField<EntityList, Map<Integer, Class<? extends Entity>>> EntityList_IDtoClassMapping = new ObfuscatedField
				<EntityList, Map<Integer, Class<? extends Entity>>>("IDtoClassMapping", "field_75623_d", EntityList.class, (Class<Map<Integer, Class<? extends Entity>>>) (Class<?>) Map.class);
		public static final ObfuscatedField<EntityList, Map<Class<? extends Entity>, Integer>> EntityList_classToIDMapping = new ObfuscatedField
				<EntityList, Map<Class<? extends Entity>, Integer>>("classToIDMapping", "field_75624_e", EntityList.class, (Class<Map<Class<? extends Entity>, Integer>>) (Class<?>) Map.class);
		public static final ObfuscatedField<EntityList, Map<String, Integer>> EntityList_stringToIDMapping = new ObfuscatedField
				<EntityList, Map<String, Integer>>("stringToIDMapping", "field_75622_f", EntityList.class, (Class<Map<String, Integer>>) (Class<?>) Map.class);
		public static final ObfuscatedField<NetworkSystem, List<NetworkManager>> NetworkSystem_networkManagers = new ObfuscatedField
				<NetworkSystem, List<NetworkManager>>("networkManagers", "field_151272_f", NetworkSystem.class, (Class<List<NetworkManager>>) (Class<?>) List.class);
		
		//This field is generated dynamically via com.mrnbobody.morecommands.asm.transform.TransformChatStyle
		public static final ObfuscatedField<ChatStyle, ChatStyle> ChatStyle_defaultChatStyle = new ObfuscatedField
				<ChatStyle, ChatStyle>("defaultChatStyle", "defaultChatStyle", ChatStyle.class, ChatStyle.class);
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
		public static final ObfuscatedMethod<EntityHorse, Void> EntityHorse_func_110226_cD = new ObfuscatedMethod
				<EntityHorse, Void>("func_110226_cD", "func_110226_cD", EntityHorse.class, void.class);
		public static final ObfuscatedMethod<EntityHorse, Void> EntityHorse_func_110232_cE = new ObfuscatedMethod
				<EntityHorse, Void>("func_110232_cE", "func_110232_cE", EntityHorse.class, void.class);
		public static final ObfuscatedMethod<EntityDragon, Void> EntityDragon_createEnderPortal = new ObfuscatedMethod
				<EntityDragon, Void>("createEnderPortal", "func_70975_a", EntityDragon.class, void.class, int.class, int.class);
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
