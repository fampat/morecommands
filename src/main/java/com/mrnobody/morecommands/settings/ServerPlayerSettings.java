package com.mrnobody.morecommands.settings;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mrnobody.morecommands.core.CommonProxy;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.util.ChatChannel;
import com.mrnobody.morecommands.util.DummyCommand;

import net.minecraft.command.CommandHandler;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.common.FMLCommonHandler;

/**
 * A settings class for settings that are used server side
 * 
 * @author MrNobody98
 */
public final class ServerPlayerSettings extends PlayerSettings implements INBTSerializable<NBTTagCompound> {
	/** A set of server side settings */
	public static final ImmutableSet<String> SERVER_SETTINGS = ImmutableSet.of("waypoints", "inventories");
	private static final String NBT_CHATCHANNELS_IDENTIFIER = "ChatChannels";
	
	/** The waypoints a player created */
	public MergedMappedSettings<String, double[]> waypoints;
	/** The inventories a player saved */
	public MergedMappedSettings<String, NBTTagList> inventories;
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
	/** The default chat style for the player's name (the parent style) */
	public Style nameStyle = null;
	/** The default chat style for the player's messages (the parent style) */
	public Style textStyle = null;
	/** The current chat channels of the player */
	public Set<ChatChannel> chatChannels = Sets.newConcurrentHashSet();
	
	private Set<String> playerChannelsToSave = Sets.newHashSet();
	private boolean loggedOut = false;
	
	private EntityPlayerMP player;
	private LazySettingsManagerLoader loader;
	
	public ServerPlayerSettings(EntityPlayerMP player) {
		super(new LazySettingsManagerLoader(player), false);
		this.loader = (LazySettingsManagerLoader) this.getManager();
	}
	
	protected ServerPlayerSettings(SettingsManager manager, boolean load) {
		super(manager, load);
		
		if (manager instanceof LazySettingsManagerLoader) 
			this.loader = (LazySettingsManagerLoader) manager;
	}
	
	/**
	 * Reads the player's chat channels fron nbt and joins them
	 */
	@Override
	public void deserializeNBT(NBTTagCompound settings) {
		MoreCommands.getProxy().ensureChatChannelsLoaded();
		NBTTagList chatChannels = settings.getTagList(NBT_CHATCHANNELS_IDENTIFIER, NBT.TAG_LIST);
		
		if (chatChannels != null) {
			for (int i = 0; i < chatChannels.tagCount(); i++) {
				ChatChannel channel = ChatChannel.getChannel(chatChannels.getStringTagAt(i));
				if (channel != null && !channel.isChannelMember(this.player)) channel.join(this.player);
			}
		}
		
		if (this.chatChannels.isEmpty())
			ChatChannel.getMasterChannel().join(this.player);
		
		this.playerChannelsToSave.clear();
		this.loggedOut = false;
	}
	
	/**
	 * Invoked when a player logs out. ChatChannels have to be left
	 * when a player logs out but leaving them will remove them
	 * from the chatChannels set which in turn means that they
	 * won't be saved because saveNBTData gets called afterwards.
	 * To cope with this problem, this method is used to leave
	 * chat channels. This method stores the name of the chat channels
	 * in a separate set which is then used to save the channels.
	 */
	public void captureChannelsAndLeaveForLogout() {
		for (ChatChannel ch : Sets.newHashSet(this.chatChannels)) {
			ch.leave(this.player);
			this.playerChannelsToSave.add(ch.getName());
		}
		
		this.loggedOut = true;
	}
	
	/**
	 * Saves a player's chat channels to nbt
	 */
	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagList list = new NBTTagList();
		
		if (this.loggedOut) {
			for (String ch : this.playerChannelsToSave)
				list.appendTag(new NBTTagString(ch));
			
			this.playerChannelsToSave.clear();
			this.loggedOut = false;
		}
		else {
			for (ChatChannel ch : this.chatChannels)
				list.appendTag(new NBTTagString(ch.getName()));
		}
		
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setTag(NBT_CHATCHANNELS_IDENTIFIER, list);
		
		return nbt;
	}
	
	@Override
	protected void registerAliases() {
		CommandHandler commandHandler = (CommandHandler) FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager();
		String command;
		
		for (String alias : this.aliases.keySet()) {
			command = this.aliases.get(alias).split(" ")[0];
			
			if (!command.equalsIgnoreCase(alias) && !commandHandler.getCommands().containsKey(alias)) {
				DummyCommand cmd = new DummyCommand(alias, false);
				commandHandler.getCommands().put(alias, cmd);
			}
		}
	}
	
	@Override
	protected synchronized void readSettings() {
		if (this.loader != null)
			this.loader.checkLoaded();
		
		super.readSettings();
		this.inventories = super.readMergedMappedSettings("inventories", NBTTagList.class, SettingsProperty.SERVER_PROPERTY);
		this.waypoints = super.readMergedMappedSettings("waypoints", double[].class, SettingsProperty.SERVER_PROPERTY, SettingsProperty.WORLD_PROPERTY, SettingsProperty.DIMENSION_POPERTY);
	}
	
	@Override
	public synchronized boolean setPutProperties(String setting, SettingsProperty... props) {
		if (super.setPutProperties(setting, props)) return true;
		if (!SERVER_SETTINGS.contains(setting)) return false;
		
		Map<SettingsProperty, Set<String>> sProps = Maps.newEnumMap(SettingsProperty.class);
		for (SettingsProperty prop : props) sProps.put(prop, Sets.newHashSet(this.getSettingsProperties().get(prop)));
		
		if ("inventories".equals(setting))
			this.inventories.setPutSetting(new Setting<Map<String, NBTTagList>>(Maps.<String, NBTTagList>newHashMap(), sProps));
		else if ("waypoints".equals(setting))
			this.waypoints.setPutSetting(new Setting<Map<String, double[]>>(Maps.<String, double[]>newHashMap(), sProps));
		
		return true;
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == SETTINGS_CAP_SERVER;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return capability == SETTINGS_CAP_SERVER ? SETTINGS_CAP_SERVER.<T>cast(this) : null;
	}
	
	protected static final SettingsSerializer<Map<String, NBTTagList>> INVENTORIES_SERIALIZER = new SettingsSerializer<Map<String, NBTTagList>>() {
		@Override
		public JsonElement serialize(Map<String, NBTTagList> src) {
			JsonObject obj = new JsonObject();
			
			for (Map.Entry<String, NBTTagList> entry : src.entrySet()) 
				obj.add(entry.getKey(), NBTSettingsManager.toJsonElement(entry.getValue()));
			
			return obj;
		}
		
		@Override
		public Map<String, NBTTagList> deserialize(JsonElement element) {
			if (!element.isJsonObject()) return Maps.newHashMap();
			
			JsonObject obj = element.getAsJsonObject();
			Map<String, NBTTagList> map = Maps.newHashMap();
			
			for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
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
	
	protected static final SettingsSerializer<Map<String, double[]>> WAYPOINT_SERIALIZER = new SettingsSerializer<Map<String, double[]>>() {
		@Override
		public JsonElement serialize(Map<String, double[]> src) {
			JsonObject obj = new JsonObject();
			
			for (Map.Entry<String, double[]> entry : src.entrySet()) {
				JsonObject data = new JsonObject();
				
				data.add("posX", new JsonPrimitive(entry.getValue()[0]));
				data.add("posY", new JsonPrimitive(entry.getValue()[1]));
				data.add("posZ", new JsonPrimitive(entry.getValue()[2]));
				data.add("yaw", new JsonPrimitive(entry.getValue()[3]));
				data.add("pitch", new JsonPrimitive(entry.getValue()[4]));
				
				obj.add(entry.getKey(), data);
			}
			
			return obj;
		}
		
		@Override
		public Map<String, double[]> deserialize(JsonElement element) {
			if (!element.isJsonObject()) return Maps.newHashMap();
			
			JsonObject obj = element.getAsJsonObject();
			Map<String, double[]> map = Maps.newHashMap();
			
			for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
				if (!entry.getValue().isJsonObject()) continue;
				JsonObject data = entry.getValue().getAsJsonObject();
				
				JsonElement posX = data.get("posX");
				JsonElement posY = data.get("posY");
				JsonElement posZ = data.get("posZ");
				JsonElement yaw = data.get("yaw");
				JsonElement pitch = data.get("pitch");
				
				if (posX == null || posY == null || posZ == null || 
					!posX.isJsonPrimitive() || !posX.getAsJsonPrimitive().isNumber() ||
					!posY.isJsonPrimitive() || !posY.getAsJsonPrimitive().isNumber() || 
					!posZ.isJsonPrimitive() || !posZ.getAsJsonPrimitive().isNumber()) continue;
				
				double[] wdata = new double[5];
				
				try {
					wdata[0] = posX.getAsJsonPrimitive().getAsDouble();
					wdata[1] = posY.getAsJsonPrimitive().getAsDouble();
					wdata[2] = posZ.getAsJsonPrimitive().getAsDouble();
					wdata[3] = yaw == null || !yaw.isJsonPrimitive() || !yaw.getAsJsonPrimitive().isNumber() ? 0 : yaw.getAsDouble();
					wdata[4] = pitch == null || !pitch.isJsonPrimitive() || !pitch.getAsJsonPrimitive().isNumber() ? 0 : pitch.getAsDouble();
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
	
	public void init(EntityPlayerMP newPlayer, ServerPlayerSettings settings) {
		this.player = newPlayer;
		this.properties = settings.properties;
		this.manager = settings.manager;
		
		this.copyProperties(settings);
	}
	
	@Override
	protected void copyProperties(PlayerSettings psettings) {
		if (psettings instanceof ServerPlayerSettings) {
			ServerPlayerSettings settings = (ServerPlayerSettings) psettings;
			
			this.breakspeed = settings.breakspeed;
			this.creeperExplosion = settings.creeperExplosion;
			this.damage = settings.damage;
			this.deathpoint = settings.deathpoint;
			this.disableAttacks = settings.disableAttacks;
			this.disableDamage = settings.disableDamage;
			this.disablePickups = settings.disablePickups;
			this.falldamage = settings.falldamage;
			this.firedamage = settings.firedamage;
			this.fly = settings.fly;
			this.freecam = settings.freecam;
			this.freeezecam = settings.freeezecam;
			this.hasModifiedCompassTarget = settings.hasModifiedCompassTarget;
			this.hunger = settings.hunger;
			this.infiniteitems = settings.infiniteitems;
			this.instantgrow = settings.instantgrow;
			this.inventories = settings.inventories;
			this.justDisabled = settings.justDisabled;
			this.keepinventory = settings.keepinventory;
			this.killattacker = settings.killattacker;
			this.lastPos = settings.lastPos;
			this.lastTeleport = settings.lastTeleport;
			this.lightWorld = settings.lightWorld;
			this.mobdamage = settings.mobdamage;
			this.noFall = settings.noFall;
			this.output = settings.output;
			this.pathData = settings.pathData;
			this.scuba = settings.scuba;
			this.superpunch = settings.superpunch;
			this.waterdamage = settings.waterdamage;
			this.waypointCompassTarget = settings.waypointCompassTarget;
			this.waypoints = settings.waypoints;
			this.xrayBlockRadius = settings.xrayBlockRadius;
			this.xrayEnabled = settings.xrayEnabled;
			this.textStyle = settings.textStyle;
			this.nameStyle = settings.nameStyle;
			this.chatChannels = settings.chatChannels;
		}
		
		super.copyProperties(psettings);
	}
	
	static {
		RootSettingsSerializer.registerSerializer("inventories", INVENTORIES_SERIALIZER);
		RootSettingsSerializer.registerSerializer("waypoints", WAYPOINT_SERIALIZER);
	}
	

	/**
	 * This class lazy loads the settings manager for a PlayerSettings
	 * object when a call to <i>saveSettings</i> or <i>loadSettings</i>
	 * occurs. The reason why this class is need is simply that the constructor 
	 * <i>ServerPlayerSettings(EntityPlayerMP)</i>
	 * fetches the actual SettingsManager from <i>MoreCommands.getProxy().
	 * createSettingsManagerForPlayer(EntityPlayer)</i>. As being a Capability
	 * Provider, it is likely that a new ServerPlayerSettings object
	 * is attached to an <i>EntityConstructing</i>. The problem with this is that
	 * many important fields of the player object are not yet initialized when
	 * this event is fired. Using any of those fields  will then
	 * result in a <i>NullPointerException</i> being thrown and minecraft will
	 * crash. Using lazy loading ensures that the actual SettingsManager is
	 * created at the right time to avoid a NPE.
	 * 
	 * @author MrNobody98
	 */
	private static class LazySettingsManagerLoader extends SettingsManager {
		private SettingsManager manager;
		private EntityPlayerMP player;
		
		/**
		 * Constructs a new {@link LazySettingsManagerLoader} for the given player
		 * 
		 * @param player the player
		 */
		public LazySettingsManagerLoader(EntityPlayerMP player) {
			super(false);
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

		@Override
		protected SetMultimap<String, Setting<?>> getSettings() {
			checkLoaded();
			return this.manager.getSettings();
		}

		@Override
		protected void setSettings(SetMultimap<String, Setting<?>> settings) {
			checkLoaded();
			this.manager.setSettings(settings);
		}
		
		/**
		 * Checks whether the settings manager was fetched from {@link CommonProxy#createSettingsManagerForPlayer(EntityPlayer)}
		 * and does that if not
		 */
		public void checkLoaded() {
			if (this.manager == null && this.player != null) {
				this.manager = MoreCommands.getProxy().createSettingsManagerForPlayer(this.player);
				if (!this.manager.isLoaded()) this.manager.loadSettings();
				this.player = null;
			}
		}
	}
}
