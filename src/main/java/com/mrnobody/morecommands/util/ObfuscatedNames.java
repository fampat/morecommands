package com.mrnobody.morecommands.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import com.mrnobody.morecommands.core.MoreCommands;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockStem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.command.ICommandManager;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.inventory.AnimalChest;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.BlockPos;
import net.minecraft.world.WorldType;
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
		public static final ObfuscatedField<Minecraft, MusicTicker> Minecraft_mcMusicTicker;
		public static final ObfuscatedField<MusicTicker, Integer> MusicTicker_timeUntilNextMusic;
		public static final ObfuscatedField<NetHandlerPlayClient, GuiScreen> NetHandlerPlayClient_guiScreenServer;
		public static final ObfuscatedField<ClientCommandHandler, ClientCommandHandler> ClientCommandHandler_instance;
		public static final ObfuscatedField<NetHandlerPlayClient, WorldClient> NetHandlerPlayClient_clientWorldController;
		public static final ObfuscatedField<SimpleReloadableResourceManager, List<?>> SimpleReloadableResourceManager_reloadListeners;
		
		static {if (FMLCommonHandler.instance().getSide().isClient()) {
			Minecraft_mcMusicTicker = new ObfuscatedField<Minecraft, MusicTicker>("mcMusicTicker", "field_147126_aw", Minecraft.class, MusicTicker.class);
			MusicTicker_timeUntilNextMusic = new ObfuscatedField<MusicTicker, Integer>("timeUntilNextMusic", "field_147676_d", MusicTicker.class, int.class);
			NetHandlerPlayClient_guiScreenServer = new ObfuscatedField<NetHandlerPlayClient, GuiScreen>("guiScreenServer", "field_147307_j", NetHandlerPlayClient.class, GuiScreen.class);
			ClientCommandHandler_instance = new ObfuscatedField<ClientCommandHandler, ClientCommandHandler>("instance", "instance", ClientCommandHandler.class, ClientCommandHandler.class);
			NetHandlerPlayClient_clientWorldController = new ObfuscatedField<NetHandlerPlayClient, WorldClient>("clientWorldController", "field_147300_g", NetHandlerPlayClient.class, WorldClient.class);
			SimpleReloadableResourceManager_reloadListeners = new ObfuscatedField<SimpleReloadableResourceManager, List<?>>("reloadListeners", "field_110546_b", SimpleReloadableResourceManager.class, (Class<List<?>>) (Class<?>) List.class);
		} else {
			Minecraft_mcMusicTicker = null;
			MusicTicker_timeUntilNextMusic = null;
			NetHandlerPlayClient_guiScreenServer = null;
			ClientCommandHandler_instance = null;
			NetHandlerPlayClient_clientWorldController = null;
			SimpleReloadableResourceManager_reloadListeners = null;
		
		}}
		
		public static final ObfuscatedField<NetHandlerPlayServer, Double> NetHandlerPlayServer_lastPosX = new ObfuscatedField
				<NetHandlerPlayServer, Double>("lastPosX", "field_147373_o", NetHandlerPlayServer.class, double.class);
		public static final ObfuscatedField<NetHandlerPlayServer, Double> NetHandlerPlayServer_lastPosY = new ObfuscatedField
				<NetHandlerPlayServer, Double>("lastPosY", "field_147382_p", NetHandlerPlayServer.class, double.class);
		public static final ObfuscatedField<NetHandlerPlayServer, Double> NetHandlerPlayServer_lastPosZ = new ObfuscatedField
				<NetHandlerPlayServer, Double>("lastPosZ", "field_147381_q", NetHandlerPlayServer.class, double.class);
		public static final ObfuscatedField<NetHandlerPlayServer, Boolean> NetHandlerPlayServer_hasMoved = new ObfuscatedField
				<NetHandlerPlayServer, Boolean>("hasMoved", "field_147380_r", NetHandlerPlayServer.class, boolean.class);
		public static final ObfuscatedField<NetHandlerPlayServer, Boolean> NetHandlerPlayServer_field_147366_g = new ObfuscatedField
				<NetHandlerPlayServer, Boolean>("field_147366_g", "field_147366_g", NetHandlerPlayServer.class, boolean.class);
		public static final ObfuscatedField<NetHandlerPlayServer, Integer> NetHandlerPlayServer_field_175090_f = new ObfuscatedField
				<NetHandlerPlayServer, Integer>("field_175090_f", "field_175090_f", NetHandlerPlayServer.class, int.class);
		public static final ObfuscatedField<NetHandlerPlayServer, Integer> NetHandlerPlayServer_networkTickCount = new ObfuscatedField
				<NetHandlerPlayServer, Integer>("networkTickCount", "field_147368_e", NetHandlerPlayServer.class, int.class);
		public static final ObfuscatedField<NetHandlerPlayServer, Integer> NetHandlerPlayServer_floatingTickCount = new ObfuscatedField
				<NetHandlerPlayServer, Integer>("floatingTickCount", "field_147365_f", NetHandlerPlayServer.class, int.class);
		public static final ObfuscatedField<MinecraftServer, ICommandManager> MinecraftServer_commandManager = new ObfuscatedField
				<MinecraftServer, ICommandManager>("commandManager", "field_71321_q", MinecraftServer.class, ICommandManager.class);
		public static final ObfuscatedField<StatList, Map<String, StatBase>> StatList_oneShotStats = new ObfuscatedField
				<StatList, Map<String, StatBase>>("oneShotStats", "field_75942_a", StatList.class, (Class<Map<String, StatBase>>) (Class<?>) Map.class);
		public static final ObfuscatedField<BlockStem, Block> BlockStem_crop = new ObfuscatedField
				<BlockStem, Block>("crop", "field_149877_a", BlockStem.class, Block.class);
		public static final ObfuscatedField<PlayerCapabilities, Float> PlayerCapabilities_walkSpeed = new ObfuscatedField
				<PlayerCapabilities, Float>("walkSpeed", "field_75097_g", PlayerCapabilities.class, float.class);
		public static final ObfuscatedField<PlayerCapabilities, Float> PlayerCapabilities_flySpeed = new ObfuscatedField
				<PlayerCapabilities, Float>("flySpeed", "field_75096_f", PlayerCapabilities.class, float.class);
		public static final ObfuscatedField<EntityMinecart, Float> EntityMinecart_currentSpeedRail = new ObfuscatedField
				<EntityMinecart, Float>("currentSpeedRail", "currentSpeedRail", EntityMinecart.class, float.class);
		public static final ObfuscatedField<EntityPlayerMP, String> EntityPlayerMP_translator = new ObfuscatedField
				<EntityPlayerMP, String>("translator", "field_71148_cg", EntityPlayerMP.class, String.class);
		public static final ObfuscatedField<EntityHorse, AnimalChest> EntityHorse_horseChest = new ObfuscatedField
				<EntityHorse, AnimalChest>("horseChest", "field_110296_bG", EntityHorse.class, AnimalChest.class);
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
		public static final ObfuscatedMethod<EntityDragon, Void> EntityDragon_generatePortal = new ObfuscatedMethod
				<EntityDragon, Void>("generatePortal", "func_175499_a", EntityDragon.class, void.class, BlockPos.class);
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
