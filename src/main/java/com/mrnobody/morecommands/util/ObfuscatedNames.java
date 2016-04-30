package com.mrnobody.morecommands.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mrnobody.morecommands.core.MoreCommands;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockStem;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.command.ICommandManager;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.client.ClientCommandHandler;
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
		
		static {if (FMLCommonHandler.instance().getSide().isClient()) {
			NetHandlerPlayClient_guiScreenServer = new ObfuscatedField<NetHandlerPlayClient, GuiScreen>("guiScreenServer", "field_147307_j", NetHandlerPlayClient.class, GuiScreen.class);
			ClientCommandHandler_instance = new ObfuscatedField<ClientCommandHandler, ClientCommandHandler>("instance", "instance", ClientCommandHandler.class, ClientCommandHandler.class);
			NetHandlerPlayClient_clientWorldController = new ObfuscatedField<NetHandlerPlayClient, WorldClient>("clientWorldController", "field_147300_g", NetHandlerPlayClient.class, WorldClient.class);
			SimpleReloadableResourceManager_reloadListeners = new ObfuscatedField<SimpleReloadableResourceManager, List<?>>("reloadListeners", "field_110546_b", SimpleReloadableResourceManager.class, (Class<List<?>>) (Class<?>) List.class);
		} else {
			NetHandlerPlayClient_guiScreenServer = null;
			ClientCommandHandler_instance = null;
			NetHandlerPlayClient_clientWorldController = null;
			SimpleReloadableResourceManager_reloadListeners = null;
		
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
