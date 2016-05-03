package com.mrnobody.morecommands.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.util.SettingsManager.AbstractElement;
import com.mrnobody.morecommands.util.SettingsManager.NumericElement;
import com.mrnobody.morecommands.util.SettingsManager.ObjectElement;
import com.mrnobody.morecommands.util.SettingsManager.Serializable;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;

/**
 * A settings class for settings that are used server side
 * 
 * @author MrNobody98
 */
public final class ServerPlayerSettings extends PlayerSettings {
	/** A set of server side settings */
	public static final ImmutableSet<String> SERVER_SETTINGS = ImmutableSet.of("waypoints", "inventories");
	
	/** The waypoints a player created */
	public Map<String, double[]> waypoints = new HashMap<String, double[]>();
	/** The inventories a player saved */
	public Map<String, NBTTagList> inventories = new HashMap<String, NBTTagList>();
	/** The entity classes which are not allowed to attack a player */
	public Set<Class<? extends EntityLiving>> disableAttacks = new HashSet<Class<? extends EntityLiving>>();
	/** The items which are not allowed to be picked up */
	public Set<Item> disablePickups = new HashSet<Item>();
	/** The items of which damage is disabled */
	public Set<Item> disableDamage = new HashSet<Item>();
	
	/** Data for the path command. Contains {block id, meta, path size, last x, last y, last z} */
	public int[] pathData = new int[6];
	/** Whether to allow creepers to explode if they targeted the player this settings correspond to */
	public boolean creeperExplosion = true;
	/** Whether to enable/disable damage */
	public boolean damage = true;
	/** Whether to enable/disable fall damage */
	public boolean falldamage = true;
	/** Whether to enable/disable fire damage */
	public boolean firedamage = true;
	/** Whether to enable/disable hunger */
	public boolean hunger = true;
	/** Whether to enable/disable fly mode */
	public boolean fly = false;
	/** Whether to enable/disable falling (used to cancel fall damage if the player is flying) */
	public boolean noFall = false;
	/** Indicates whether fly mode was just disabled (used to cancel fall damage after flying was disabled but the player is still in air) */
	public boolean justDisabled = false;
	/** Whether to enable/disable freecam */
	public boolean freecam = false;
	/** Whether to enable/disable freezecam */
	public boolean freeezecam = false;
	/** The current xray block radius */
	public int xrayBlockRadius = 32;
	/** Whether to xray is enabled/disabled */
	public boolean xrayEnabled = false;
	/** The current block break speed */
	public float breakspeed = -1F;
	/** Whether to enable/disable infinite items */
	public boolean infiniteitems = false;
	/** Whether to enable/disable instant plant growing */
	public boolean instantgrow = false;
	/** Whether to keep the inventory on death */
	public boolean keepinventory = false;
	/** Whether to kill entities that attack the player corresponding to this settings */
	public boolean killattacker = false;
	/** Whether the world is lit */
	public boolean lightWorld = false;
	/** Whether to enable/disable mob damage */
	public boolean mobdamage = true;
	/** Whether to allow player to breathe under water */
	public boolean scuba = false;
	/** Whether to enable/disable chat output */
	public boolean output = true;
	/** The last player position before a death or teleport */
	public BlockPos lastPos = null;
	/** The last player position before a death  */
	public BlockPos deathpoint = null;
	/** The last player position before a teleport */
	public BlockPos lastTeleport = null;
	/** The knockback factor for a player's punch */
	public int superpunch = -1;
	/** Whether to enable/disable instant water damage */
	public boolean waterdamage = true;
	/** Whether the player has a modified compass target (not the spawn point) */
	public boolean hasModifiedCompassTarget = false;
	/** If the compass target is a waypoint, this is the waypoint's name */
	public String waypointCompassTarget = null;
	
	/** The lazy SettingsManager loader that fetches the SettingsManager for a player only when settings are loaded or saved */
	private final LazySettingsManagerLoader loader;
	/** whether common PlayerSettings should be read and/or written */
	private final boolean useCommonSettings;
	
	public ServerPlayerSettings(EntityPlayerMP player) {
		super(new LazySettingsManagerLoader(player), false);
		this.loader = (LazySettingsManagerLoader) this.getManager();
		this.useCommonSettings = false;
	}
	
	public ServerPlayerSettings(SettingsManager manager, boolean load, boolean useCommonSettings) {
		super(manager, load);
		this.loader = null;
		this.useCommonSettings = useCommonSettings;
	}
	
	@Override
	public synchronized void readSettings(String server, String world, String dim) {
		if (this.loader == null ? this.useCommonSettings : this.loader.useCommonSettings()) super.readSettings(server, world, dim);
		this.inventories = readMappedSetting("inventories", server, world, dim, NBTTagList.class);
		this.waypoints = readMappedSetting("waypoints", server, world, dim, double[].class);
	}
	
	@Override
	public synchronized void saveSettings(String server, String world, String dim) {
		if (this.loader == null ? this.useCommonSettings : this.loader.useCommonSettings()) super.saveSettings(server, world, dim);
		writeMappedSetting("inventories", this.inventories, server, world, dim, NBTTagList.class);
		writeMappedSetting("waypoints", this.waypoints, server, world, dim, double[].class);
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == SETTINGS_CAP_SERVER;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return capability == SETTINGS_CAP_SERVER ? SETTINGS_CAP_SERVER.<T>cast(this) : null;
	}
	
	protected static final Serializable<Map<String, NBTTagList>> INVENTORIES_SERIALIZABLE = new Serializable<Map<String, NBTTagList>>() {
		@Override
		public AbstractElement serialize(Map<String, NBTTagList> src) {
			ObjectElement obj = new ObjectElement();
			for (Map.Entry<String, NBTTagList> entry : src.entrySet()) obj.add(entry.getKey(), NBTSettingsManager.toAbstractElement(entry.getValue()));
			return obj;
		}
		
		@Override
		public Map<String, NBTTagList> deserialize(AbstractElement element) {
			if (!element.isObject()) return Maps.newHashMap();
			ObjectElement obj = element.asObject();
			Map<String, NBTTagList> map = Maps.newHashMap();
			
			for (Map.Entry<String, AbstractElement> entry : obj.entrySet()) {
				NBTBase nbt = NBTSettingsManager.toNBTElement(entry.getValue());
				if (!(nbt instanceof NBTTagList)) continue;
				map.put(entry.getKey(), (NBTTagList) nbt);
			}
			
			return map;
		}
		
		@Override
		public Class<Map<String, NBTTagList>> getTypeClass() {
			return (Class<Map<String, NBTTagList>>) (Class<?>) Map.class;
		}
	};
	
	protected static final Serializable<Map<String, double[]>> WAYPOINT_SERIALIZABLE = new Serializable<Map<String, double[]>>() {
		@Override
		public AbstractElement serialize(Map<String, double[]> src) {
			ObjectElement obj = new ObjectElement();
			for (Map.Entry<String, double[]> entry : src.entrySet()) {
				ObjectElement data = new ObjectElement();
				data.add("posX", new NumericElement(entry.getValue()[0]));
				data.add("posY", new NumericElement(entry.getValue()[1]));
				data.add("posZ", new NumericElement(entry.getValue()[2]));
				data.add("yaw", new NumericElement(entry.getValue()[3]));
				data.add("pitch", new NumericElement(entry.getValue()[4]));
				obj.add(entry.getKey(), data);
			}
			return obj;
		}

		@Override
		public Map<String, double[]> deserialize(AbstractElement element) {
			if (!element.isObject()) return Maps.newHashMap();
			ObjectElement obj = element.asObject();
			Map<String, double[]> map = Maps.newHashMap();
			
			for (Map.Entry<String, AbstractElement> entry : obj.entrySet()) {
				if (!entry.getValue().isObject()) continue;
				ObjectElement data = entry.getValue().asObject();
				
				AbstractElement posX = data.get("posX");
				AbstractElement posY = data.get("posY");
				AbstractElement posZ = data.get("posZ");
				AbstractElement yaw = data.get("yaw");
				AbstractElement pitch = data.get("pitch");
				
				if (posX == null || posY == null || posZ == null || !posX.isNumeric() || !posY.isNumeric() || !posZ.isNumeric()) continue;
				double[] wdata = new double[5];
				
				try {
					wdata[0] = posX.asNumericElement().asNumber().doubleValue();
					wdata[1] = posY.asNumericElement().asNumber().doubleValue();
					wdata[2] = posZ.asNumericElement().asNumber().doubleValue();
					wdata[3] = yaw == null || !yaw.isNumeric() ? 0 : yaw.asNumericElement().asNumber().doubleValue();
					wdata[4] = pitch == null || !pitch.isNumeric() ? 0 : pitch.asNumericElement().asNumber().doubleValue();
				}
				catch (NumberFormatException nfe) {continue;}
				
				map.put(entry.getKey(), wdata);
			}
			
			return map;
		}
		
		@Override
		public Class<Map<String, double[]>> getTypeClass() {
			return (Class<Map<String, double[]>>) (Class<?>) Map.class;
		}
	};
	
	@Override
	public void cloneSettings(PlayerSettings settings) {
		if (settings instanceof ServerPlayerSettings) {
			ServerPlayerSettings ssettings = (ServerPlayerSettings) settings;
			
			this.breakspeed = ssettings.breakspeed;
			this.creeperExplosion = ssettings.creeperExplosion;
			this.damage = ssettings.damage;
			this.deathpoint = ssettings.deathpoint == null ? null : new BlockPos(ssettings.deathpoint.getX(), ssettings.deathpoint.getY(), ssettings.deathpoint.getZ());
			this.disableAttacks = new HashSet<Class<? extends EntityLiving>>(ssettings.disableAttacks);
			this.disableDamage = new HashSet<Item>(ssettings.disableDamage);
			this.disablePickups = new HashSet<Item>(ssettings.disablePickups);
			this.falldamage = ssettings.falldamage;
			this.firedamage = ssettings.firedamage;
			this.fly = ssettings.fly;
			this.freecam = ssettings.freecam;
			this.freeezecam = ssettings.freeezecam;
			this.hasModifiedCompassTarget = ssettings.hasModifiedCompassTarget;
			this.hunger = ssettings.hunger;
			this.infiniteitems = ssettings.infiniteitems;
			this.instantgrow = ssettings.instantgrow;
			this.inventories = new HashMap<String, NBTTagList>(ssettings.inventories);
			this.justDisabled = ssettings.justDisabled;
			this.keepinventory = ssettings.keepinventory;
			this.killattacker = ssettings.killattacker;
			this.lastPos = ssettings.lastPos == null ? null : new BlockPos(ssettings.lastPos.getX(), ssettings.lastPos.getY(), ssettings.lastPos.getZ());
			this.lastTeleport = ssettings.lastTeleport == null ? null : new BlockPos(ssettings.lastTeleport.getX(), ssettings.lastTeleport.getY(), ssettings.lastTeleport.getZ());
			this.lightWorld = ssettings.lightWorld;
			this.mobdamage = ssettings.mobdamage;
			this.noFall = ssettings.noFall;
			this.output = ssettings.output;
			this.pathData = Arrays.copyOf(ssettings.pathData, ssettings.pathData.length);
			this.scuba = ssettings.scuba;
			this.superpunch = ssettings.superpunch;
			this.waterdamage = ssettings.waterdamage;
			this.waypointCompassTarget = ssettings.waypointCompassTarget;
			this.waypoints = new HashMap<String, double[]>(ssettings.waypoints);
			this.xrayBlockRadius = ssettings.xrayBlockRadius;
			this.xrayEnabled = ssettings.xrayEnabled;
		}
		
		super.cloneSettings(settings);
	}
	
	static {
		SettingsManager.registerSerializable("inventories", INVENTORIES_SERIALIZABLE, false);
		SettingsManager.registerSerializable("waypoints", WAYPOINT_SERIALIZABLE, false);
	}
	
	/**
	 * This class lazy loads the settings manager for a PlayerSettings
	 * object when a call to <i>saveSettings</i> or <i>loadSettings</i>
	 * occurs. The reason why this class is need is simply that the constructor 
	 * <i>ServerPlayerSettings(EntityPlayerMP)</i>
	 * fetches the actual SettingsManager from <i>MoreCommands.getProxy().
	 * createSettingsManagerForPlayer(EntityPlayer)</i>. As being a Capability
	 * Provider, it is likely that a new ServerPlayerSettings object
	 * is attached to an <i>AttachCapabilitiesEvent</i>. The problem with this is that
	 * many important fields of the player object are not yet initialized when
	 * this event is fired. Using any of those fields
	 * (<i>ComonProxy.createSettingsManagerForPlayer(EntityPlayer)</i> does that, e.g.
	 * by using <i>EntityPlayerMP.getName()</i> which is not initialized) will then
	 * result in a <i>NullPointerException</i> being thrown and minecraft will
	 * crash. Using lazy loading ensures that the actual SettingsManager is
	 * created at the right time to avoid a NPE.
	 * 
	 * @author MrNobody98
	 */
	private static class LazySettingsManagerLoader extends SettingsManager {
		private SettingsManager manager;
		private boolean useCommonSettings = true;
		private EntityPlayerMP player;
		
		/**
		 * Constructs a new {@link LazySettingsManagerLoader} for the given player
		 * 
		 * @param player the player
		 */
		public LazySettingsManagerLoader(EntityPlayerMP player) {
			super(null, false, false);
			this.player = player;
		}

		@Override
		public void loadSettings() {
			checkLoaded();
			this.manager.loadSettings();
		}

		@Override
		public void saveSettings() {
			checkLoaded();
			this.manager.saveSettings();
		}

		@Override
		public boolean isLoaded() {
			return this.manager == null ? false : this.manager.isLoaded();
		}
		
		/**
		 * @return whether common settings should be read/written for the player to which this SettingsManager belongs
		 */
		public boolean useCommonSettings() {
			checkLoaded();
			return this.useCommonSettings;
		}
		
		private void checkLoaded() {
			if (this.manager == null && this.player != null) {
				this.manager = MoreCommands.getProxy().createSettingsManagerForPlayer(this.player);
				this.useCommonSettings = MoreCommands.isServerSide() || !this.player.getName().equals(this.player.getServer().getServerOwner());
				if (!this.manager.isLoaded()) this.manager.loadSettings();
				MoreCommands.getProxy().updateWorld(this.player);
				this.player = null;
			}
		}
	}
}
