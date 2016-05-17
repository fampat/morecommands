package com.mrnobody.morecommands.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.mrnobody.morecommands.util.SettingsManager.AbstractElement;
import com.mrnobody.morecommands.util.SettingsManager.ListElement;
import com.mrnobody.morecommands.util.SettingsManager.ObjectElement;
import com.mrnobody.morecommands.util.SettingsManager.Serializable;
import com.mrnobody.morecommands.util.SettingsManager.Setting;
import com.mrnobody.morecommands.util.SettingsManager.StringElement;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

/**
 * A class keeping track of player settings.
 * Contains some common client/server side settings too.
 * 
 * @author MrNobody98
 */
public abstract class PlayerSettings implements ICapabilityProvider {
	/** The server and client side common settings */
	public static final ImmutableSet<String> COMMON_SETTINGS = ImmutableSet.of("aliases", "variables", "macros");
	/** The {@link IExtendedEntityProperties} identifier with that an object of this class should be registered to a player */
	public static final ResourceLocation SETTINGS_IDENTIFIER = new ResourceLocation(Reference.MODID, "settings");
	/** The client player settings capability */
	@CapabilityInject(ClientPlayerSettings.class) 
	public static final Capability<ClientPlayerSettings> SETTINGS_CAP_CLIENT = null;
	/** The server player settings capability */
	@CapabilityInject(ServerPlayerSettings.class) 
	public static final Capability<ServerPlayerSettings> SETTINGS_CAP_SERVER = null;
	
	public static final void registerCapabilities() {
		CapabilityManager.INSTANCE.register(ClientPlayerSettings.class, new Capability.IStorage<ClientPlayerSettings>() {
			@Override public NBTBase writeNBT(Capability<ClientPlayerSettings> capability, ClientPlayerSettings instance, EnumFacing side) {return null;}
			@Override public void readNBT(Capability<ClientPlayerSettings> capability, ClientPlayerSettings instance, EnumFacing side, NBTBase nbt) {}
		}, new Callable<ClientPlayerSettings>() {
			@Override public ClientPlayerSettings call() throws Exception {return new ClientPlayerSettings(new SettingsManager.DummySettingsManager(), false);}
		});
		
		CapabilityManager.INSTANCE.register(ServerPlayerSettings.class, new Capability.IStorage<ServerPlayerSettings>() {
			@Override public NBTBase writeNBT(Capability<ServerPlayerSettings> capability, ServerPlayerSettings instance, EnumFacing side) {return null;}
			@Override public void readNBT(Capability<ServerPlayerSettings> capability, ServerPlayerSettings instance, EnumFacing side, NBTBase nbt) {}
		}, new Callable<ServerPlayerSettings>() {
			@Override public ServerPlayerSettings call() throws Exception {return new ServerPlayerSettings(new SettingsManager.DummySettingsManager(), false, true);}
		});
	}
	
	private Triple<Boolean, Boolean, Boolean> DEFAULT_SAVE_PROPERTIES = MutableTriple.of(true, false, false);
	private Map<String, Triple<Boolean, Boolean, Boolean>> SAVE_PROPERTIES = new HashMap<String, Triple<Boolean, Boolean, Boolean>>();
	private Map<String, Map<String, Triple<Boolean, Boolean, Boolean>>> MAPPED_SAVE_PROPERTIES = new HashMap<String, Map<String, Triple<Boolean, Boolean, Boolean>>>();
	
	/**
	 * Gets the save properties of a map setting.
	 * The save properties are a {@link Triple} of three booleans that indicate whether to save dependent 
	 * on the server address (left value), dependent on the world name (middle value) and dependent 
	 * on the dimension name (right value).
	 * 
	 * @param type the name of the setting
	 * @param key as this method fetches save properties for a setting that is a map, the map key
	 * @return the save properties triple
	 */
	public final Triple<Boolean, Boolean, Boolean> getSaveProperties(String type, String key) {
		Triple<Boolean, Boolean, Boolean> props = null;
		
		if (MAPPED_SAVE_PROPERTIES.containsKey(type)) props = MAPPED_SAVE_PROPERTIES.get(type).get(key);
		if (props == null) props = SAVE_PROPERTIES.get(type);
		
		return props == null ? DEFAULT_SAVE_PROPERTIES : props; 
	}
	
	/**
	 * Gets the save properties of a regular setting.
	 * The save properties are a {@link Triple} of three booleans that indicate whether to save dependent 
	 * on the server address (left value), dependent on the world name (middle value) and dependent 
	 * on the dimension name (right value).
	 * 
	 * @param type the name of the setting
	 * @return the save properties triple
	 */
	public final Triple<Boolean, Boolean, Boolean> getSaveProperties(String type) {
		if (!SAVE_PROPERTIES.containsKey(type)) return DEFAULT_SAVE_PROPERTIES;
		else return SAVE_PROPERTIES.get(type);
	}
	
	/** A map of aliases a player created */
	public Map<String, String> aliases = new HashMap<String, String>();
	/** A map of variables a player created */
	public Map<String, String> variables = new HashMap<String, String>();
	/** A map of macros (list of commands) a player created */
	public Map<String, List<String>> macros = new HashMap<String, List<String>>();
	
	protected final SettingsManager manager;
	private boolean loadedOnce = false;
	private String lastServer, lastWorld, lastDim;
	
	public PlayerSettings(SettingsManager manager, boolean load) {
		this.manager = manager;
		if (load && !this.manager.isLoaded()) this.manager.loadSettings();
	}
	
	/**
	 * Updates this player settings if a server, world or dimension change occurs
	 * 
	 * @param server the server address of the server the player is currently playing on
	 * @param world the folder name of the world the player is currently playing in
	 * @param dim the dimension name of the dimension the player is currently in
	 */
	public final synchronized void updateSettings(String server, String world, String dim) {
		if (this.loadedOnce) saveSettings(this.lastServer, this.lastWorld, this.lastDim);
		readSettings(server, world, dim);
		
		this.lastServer = server; this.lastWorld = world; this.lastDim = dim;
		this.loadedOnce = true;
	}
	
	/**
	 * Reads the settings from the settings manager
	 * 
	 * @param server the server address used to filter settings accepting only specific servers
	 * @param world the world name used to filter settings accepting only specific worlds
	 * @param dim the dimension name used to filter settings accepting only specific dimensions
	 */
	public synchronized void readSettings(String server, String world, String dim) {
		this.aliases = readMappedSetting("aliases", server, world, dim, String.class);
		this.variables = readMappedSetting("variables", server, world, dim, String.class);
		this.macros = readMappedSetting("macros", server, world, dim, (Class<List<String>>) (Class<?>) List.class);
	}
	
	/**
	 * Read a regular setting from the settings manager.
	 * 
	 * @param <T> the setting's type
	 * @param type the setting's name
	 * @param server the server address used to filter settings accepting only specific servers
	 * @param world the world name used to filter settings accepting only specific worlds
	 * @param dim the dimension name used to filter settings accepting only specific dimensions
	 * @param classOfT the class of the setting
	 * @return the setting
	 */
	public synchronized final <T> T readSetting(String type, String server, String world, String dim, Class<T> classOfT) {
		Setting<T> setting = this.manager.getSetting(type, server, world, dim, classOfT);
		if (setting == null) return null;
		
		SAVE_PROPERTIES.put(type, MutableTriple.of(!setting.getServer().isEmpty(), !setting.getWorld().isEmpty(), !setting.getDim().isEmpty()));
		return setting.getValue();
	}
	
	/**
	 * Read a setting having a map as property from the settings manager.
	 * 
	 * @param <T> the map value type
	 * @param type the setting's name
	 * @param server the server address used to filter settings accepting only specific servers
	 * @param world the world name used to filter settings accepting only specific worlds
	 * @param dim the dimension name used to filter settings accepting only specific dimensions
	 * @param classOfT the class of the list elements
	 * @return the map
	 */
	public synchronized final <T> Map<String, T> readMappedSetting(String type, String server, String world, String dim, Class<T> classOfT) {
		List<Setting<Map<String, T>>> maps = Lists.reverse(this.manager.getMappedSettings(type, server, world, dim, classOfT));
		Map<String, T> finalMap = new HashMap<String, T>();
		
		if (!MAPPED_SAVE_PROPERTIES.containsKey(type))
			MAPPED_SAVE_PROPERTIES.put(type, new HashMap<String, Triple<Boolean, Boolean, Boolean>>());
		
		Map<String, Triple<Boolean, Boolean, Boolean>> props = MAPPED_SAVE_PROPERTIES.get(type);
		
		for (Setting<Map<String, T>> setting : maps) {
			Triple<Boolean, Boolean, Boolean> prop = MutableTriple.of(!setting.getServer().isEmpty(), !setting.getWorld().isEmpty(), !setting.getDim().isEmpty());
			
			for (Map.Entry<String, T> entry : setting.getValue().entrySet()) {
				props.put(entry.getKey(), prop);
				finalMap.put(entry.getKey(), entry.getValue());
			}
		}
		
		return finalMap;
	}
	
	/**
	 * Writes the settings to the settings manager
	 * 
	 * @param server the server address which is used for settings that are accepted only on specific servers
	 * @param world the world name which is used for settings that are accepted only in specific worlds
	 * @param dim the dimension name which is used for settings that are accepted only in specific dimensions
	 */
	public synchronized void saveSettings(String server, String world, String dim) {
		writeMappedSetting("aliases", this.aliases, server, world, dim, String.class);
		writeMappedSetting("variables", this.variables, server, world, dim, String.class);
		writeMappedSetting("macros", this.macros, server, world, dim, (Class<List<String>>) (Class<?>) List.class);
	}
	
	/**
	 * Writes the a regular setting to the settings manager
	 * 
	 * @param <T> the setting's type
	 * @param type the setting's name
	 * @param value the setting's value
	 * @param server the server address which is used for settings that are accepted only on specific servers
	 * @param world the world name which is used for settings that are accepted only in specific worlds
	 * @param dim the dimension name which is used for settings that are accepted only in specific dimensions
	 * @param classOfT the setting's value class
	 */
	public synchronized final <T> void writeSetting(String type, T value, String server, String world, String dim, Class<T> classOfT) {
		if (value == null || (value instanceof Collection<?> && ((Collection<?>) value).isEmpty())) return;
		Triple<Boolean, Boolean, Boolean> props = getSaveProperties(type);
		this.manager.storeSetting(type, value, props.getLeft() && server != null ? server : null,
				props.getMiddle() && world != null ? world : null, props.getRight() && dim != null ? dim : null, classOfT);
	}
	
	/**
	 * Writes a map setting to the settings manager
	 * 
	 * @param <T> the map's value type
	 * @param type the setting's name
	 * @param map the map
	 * @param server the server address which is used for settings that are accepted only on specific servers
	 * @param world the world name which is used for settings that are accepted only in specific worlds
	 * @param dim the dimension name which is used for settings that are accepted only in specific dimensions
	 * @param classOfT the map's value class
	 */
	public synchronized final <T> void writeMappedSetting(String type, Map<String, T> map, String server, String world, String dim, Class<T> classOfT) {
		for (Map.Entry<Triple<Boolean, Boolean, Boolean>, Map<String, T>> entry : splitMap(type, map).entrySet())
			this.manager.storeMappedSettings(type, entry.getValue(), entry.getKey().getLeft() && server != null ? server : null,
			entry.getKey().getMiddle() && world != null ? world : null, entry.getKey().getRight() && dim != null ? dim : null, classOfT);
	}
	
	/**
	 * Each entry of the map can have different save properties ({@link PlayerSettings#getSaveProperties(String, String)}).
	 * This method maps the different save properties of the entries in the given map to maps of which the entries 
	 * have the same save properties as the map key of the returned map indicates.
	 * 
	 * @param <T> the map's value type
	 * @param type the setting's name
	 * @param map the map which is to be split by their save properties
	 * @return a map which maps the save properties of the given map's entries to map of which the entries have the same save properties
	 */
	private final <T> Map<Triple<Boolean, Boolean, Boolean>, Map<String, T>> splitMap(String type, Map<String, T> map) {
		Map<Triple<Boolean, Boolean, Boolean>, Map<String, T>> maps = new HashMap<Triple<Boolean, Boolean, Boolean>, Map<String, T>>();
		
		for (Map.Entry<String, T>  entry : map.entrySet()) {
			Triple<Boolean, Boolean, Boolean> props = getSaveProperties(type, entry.getKey());
			if (!maps.containsKey(props)) maps.put(props, new HashMap<String, T>());
			maps.get(props).put(entry.getKey(), entry.getValue());
		}
		
		return maps;
	}
	
	/**
	 * Stores a regular setting in the settings manager and re-reads it.
	 * Optionally deletes its save properties.
	 * 
	 * @param <T> the setting type
	 * @param type the setting's name
	 * @param value the setting's value
	 * @param classOfT the setting's type class
	 * @param clearProps whether to clear the setting's save properties
	 * @return the re-read setting
	 */
	public final <T> T putAndUpdate(String type, T value, Class<T> classOfT, boolean clearProps) {
		Triple<Boolean, Boolean, Boolean> props = getSaveProperties(type);
		if (clearProps) clearProps(type);
		
		this.manager.storeSetting(type, value, props.getLeft() && this.lastServer != null ? this.lastServer : null, props.getMiddle() && 
				this.lastWorld != null ? this.lastWorld : null, props.getRight() && this.lastDim != null ? this.lastDim : null, classOfT);
		
		return readSetting(type, this.lastServer, this.lastWorld, this.lastDim, classOfT);
	}
	
	/**
	 * Removes a regular setting from the settings manager and re-reads it.
	 * Optionally clears its save properties
	 * 
	 * @param <T> the setting's type
	 * @param type the setting's name
	 * @param classOfT the setting's type class
	 * @param clearProps whether to clear the setting's save properties
	 * @return the re-read setting
	 */
	public final <T> T removeAndUpdate(String type, Class<T> classOfT, boolean clearProps) {
		Triple<Boolean, Boolean, Boolean> props = getSaveProperties(type);
		if (clearProps) clearProps(type);
		
		this.manager.removeSetting(type, props.getLeft() && this.lastServer != null ? this.lastServer : null, props.getMiddle() && 
				this.lastWorld != null ? this.lastWorld : null, props.getRight() && this.lastDim != null ? this.lastDim : null, classOfT);
		
		return readSetting(type, this.lastServer, this.lastWorld, this.lastDim, classOfT);
	}
	
	/**
	 * Puts an entry into the map and the settings manager and re-reads the map.
	 * Optionally clears the entry's save properties
	 * 
	 * @param <T> the map's value type
	 * @param type the setting's name
	 * @param key the entry's key
	 * @param value the entry's value
	 * @param classOfT the map's value type class
	 * @param clearProps whether to clear the save properties associated with the map entry
	 * @return the re-read map
	 */
	public final <T> Map<String, T> putAndUpdate(String type, String key, T value, Class<T> classOfT, boolean clearProps) {
		Map<String, T> map = Maps.newHashMap(); map.put(key, value); 
		return putAndUpdate(type, map, classOfT, clearProps);
	}
	
	/**
	 * Puts multiple entries into the map and the settings manager and re-reads the map.
	 * Optionally clears the entries save properties
	 * 
	 * @param <T> the map's value type
	 * @param type the setting's name
	 * @param key the entry's key
	 * @param value the entry's value
	 * @param classOfT the map's value type class
	 * @param clearProps whether to clear the save properties associated with the map entry
	 * @return the re-read map
	 */
	public final <T> Map<String, T> putAndUpdate(String type, Map<String, T> value, Class<T> classOfT, boolean clearProps) {
		if (clearProps) clearMultipleMappedProps(type, value.keySet());
		Map<Triple<Boolean, Boolean, Boolean>, Map<String, T>> props = splitMap(type, value);
		
		for (Triple<Boolean, Boolean, Boolean> prop : props.keySet())
			this.manager.storeMappedSettings(type, props.get(prop), prop.getLeft() && this.lastServer != null ? this.lastServer : null, 
				prop.getMiddle() && this.lastWorld != null ? this.lastWorld : null, prop.getRight() && this.lastDim != null ? this.lastDim : null, classOfT);
		
		return readMappedSetting(type, this.lastServer, this.lastWorld, this.lastDim, classOfT);
	}
	
	/**
	 * Removes an entry of a map from the settings manager and re-reads the map.
	 * Optionally clears the entry's save properties
	 * 
	 * @param <T> the map's value type
	 * @param type the setting's name
	 * @param key the entry's key
	 * @param classOfT the map's value type class
	 * @param clearProps whether to clear the save properties associated with the map entry
	 * @return the re-read map
	 */
	public final <T> Map<String, T> removeAndUpdate(String type, String key, Class<T> classOfT, boolean clearProps) {
		return removeAndUpdate(type, Sets.newHashSet(key), classOfT, clearProps, false);
	}
	
	/**
	 * Removes several entries from a map from the settings manager. 
	 * Optionally deletes the save properties corresponding to these entries.
	 * 
	 * @param <T> the map's element type
	 * @param type the setting's name
	 * @param keys the keys of the entries to remove.
	 * @param classOfT the map's value type class
	 * @param clearProps whether to clear the save properties of the entries to remove
	 * @param removeAll whether to remove all elements (if true <i>keys</i> may be empty or null)
	 * @return the re-read map
	 */
	public final <T> Map<String, T> removeAndUpdate(String type, Set<String> keys, Class<T> classOfT, boolean clearProps, boolean removeAll) {
		if (clearProps && !removeAll) clearMultipleMappedProps(type, keys);
		else if (clearProps && removeAll) clearMappedProps(type, null);
		SetMultimap<Triple<Boolean, Boolean, Boolean>, String> props = splitMapProps(type, keys, removeAll);
		
		for (Triple<Boolean, Boolean, Boolean> prop : props.keySet())
			this.manager.removeMappedSettings(type, props.get(prop), removeAll, prop.getLeft() && this.lastServer != null ? this.lastServer : null, 
				prop.getMiddle() && this.lastWorld != null ? this.lastWorld : null, prop.getRight() && this.lastDim != null ? this.lastDim : null);
		
		return readMappedSetting(type, this.lastServer, this.lastWorld, this.lastDim, classOfT);
	}
	
	/**
	 * Returns a multimap which maps the save property of all keys
	 * which have the same save property to these keys.
	 * 
	 * @param type the setting's name
	 * @param indices all keys to map
	 * @param all whether to map all keys
	 * @return the save property<->keys map
	 */
	private SetMultimap<Triple<Boolean, Boolean, Boolean>, String> splitMapProps(String type, Set<String> keys, boolean all) {
		SetMultimap<Triple<Boolean, Boolean, Boolean>, String> props = HashMultimap.create();
		
		if (all) props.putAll(getSaveProperties(type), keys);
		else for (String key : keys) props.put(getSaveProperties(type, key), key);
		
		return props;
	}
	
	/**
	 * Sets the default save properties
	 * 
	 * @param serverDependent whether settings should be server dependent
	 * @param worldDependent whether settings should be world dependent
	 * @param dimDependent whether settings should be dimension dependent
	 */
	public final void setDefaultProps(boolean serverDependent, boolean worldDependent, boolean dimDependent) {
		DEFAULT_SAVE_PROPERTIES = MutableTriple.of(serverDependent, worldDependent, dimDependent);
	}
	
	/**
	 * Sets the save properties for a setting
	 * 
	 * @param type the setting's name
	 * @param serverDependent whether this setting should be server dependent
	 * @param worldDependent whether this setting should be world dependent
	 * @param dimDependent whether this setting should be dimension dependent
	 */
	public final void setProps(String type, boolean serverDependent, boolean worldDependent, boolean dimDependent) {
		SAVE_PROPERTIES.put(type, MutableTriple.of(serverDependent, worldDependent, dimDependent));
	}
	
	/**
	 * Clears the save properties for a setting
	 * 
	 * @param type the setting's name
	 */
	public final void clearProps(String type) {
		SAVE_PROPERTIES.remove(type);
	}
	
	/**
	 * Sets the save properties for a mapped setting
	 * 
	 * @param type the setting's name
	 * @param key the map key
	 * @param serverDependent whether this setting should be server dependent
	 * @param worldDependent whether this setting should be world dependent
	 * @param dimDependent whether this setting should be dimension dependent
	 */
	public final void setMappedProps(String type, String key, boolean serverDependent, boolean worldDependent, boolean dimDependent) {
    	if (!MAPPED_SAVE_PROPERTIES.containsKey(type))
    		MAPPED_SAVE_PROPERTIES.put(type, new HashMap<String, Triple<Boolean, Boolean, Boolean>>());
    	
    	MAPPED_SAVE_PROPERTIES.get(type).put(key, MutableTriple.of(serverDependent, worldDependent, dimDependent));
	}
	
	/**
	 * Clears the save properties for a mapped setting
	 * 
	 * @param type the setting's name
	 * @param key the map key
	 */
	public final void clearMappedProps(String type, String key) {
    	if (MAPPED_SAVE_PROPERTIES.containsKey(type)) {
    		if (key == null) MAPPED_SAVE_PROPERTIES.get(type).clear();
    		else MAPPED_SAVE_PROPERTIES.get(type).remove(key);
    	}
	}
	
	/**
	 * Clears multiple save properties for a mapped setting
	 * 
	 * @param type the setting's name
	 * @param keys the map keys
	 */
	public final void clearMultipleMappedProps(String type, Set<String> keys) {
    	if (MAPPED_SAVE_PROPERTIES.containsKey(type)) {
    		MAPPED_SAVE_PROPERTIES.get(type).keySet().removeAll(keys);
    	}
	}
	
	/**
	 * @return the {@link SettingsManager} this {@link PlayerSettings} object uses
	 */
	public final SettingsManager getManager() {
		return this.manager;
	}
	
	protected static final Serializable<Map<String, String>> MAP_STRING_STRING_SERIALIZABLE = new Serializable<Map<String, String>>() {
		@Override
		public AbstractElement serialize(Map<String, String> src) {
			ObjectElement obj = new ObjectElement();
			for (Map.Entry<String, String> entry : src.entrySet()) obj.add(entry.getKey(), new StringElement(entry.getValue()));
			return obj;
		}

		@Override
		public Map<String, String> deserialize(AbstractElement elem){
			if (!elem.isObject()) return Maps.newHashMap();
			ObjectElement obj = elem.asObject();
			Map<String, String> map = Maps.newHashMap();
			
			for (Map.Entry<String, AbstractElement> entry : obj.entrySet()) {
				if (!entry.getValue().isString()) continue;
				else map.put(entry.getKey(), entry.getValue().asStringElement().asString());
			}
			
			return map;
		}
		
		@Override
		public Class<Map<String, String>> getTypeClass() {
			return (Class<Map<String, String>>) (Class<?>) Map.class;
		}
	};
	
	protected static final Serializable<Map<String, List<String>>> MAP_STRING_LIST_STRING_SERIALIZABLE = new Serializable<Map<String, List<String>>>() {
		@Override
		public AbstractElement serialize(Map<String, List<String>> src) {
			ObjectElement obj = new ObjectElement();
			for (Map.Entry<String, List<String>> entry : src.entrySet())  {
				ListElement arr = new ListElement();
				for (String str : entry.getValue()) arr.add(new StringElement(str));
				obj.add(entry.getKey(), arr);
			}
			return obj;
		}

		@Override
		public Map<String, List<String>> deserialize(AbstractElement element) {
			if (!element.isObject()) return Maps.newHashMap();
			ObjectElement obj = element.asObject();
			Map<String, List<String>> map = Maps.newHashMap();
			
			for (Map.Entry<String, AbstractElement> entry : obj.entrySet()) {
				if (!entry.getValue().isList()) continue;
				List<String> list = Lists.newArrayList();
				for (AbstractElement elem : entry.getValue().asList())
					if (elem.isString()) list.add(elem.asStringElement().asString());
				map.put(entry.getKey(), list);
			}
			
			return map;
		}
		
		@Override
		public Class<Map<String, List<String>>> getTypeClass() {
			return (Class<Map<String, List<String>>>) (Class<?>) Map.class;
		}
	};
	
	/**
	 * Copies the settings from a {@link PlayerSettings} into this {@link PlayerSettings}
	 * 
	 * @param settings the {@link PlayerSettings} to copy
	 */
	public void cloneSettings(PlayerSettings settings) {
		this.aliases = new HashMap<String, String>(settings.aliases);
		this.macros = new HashMap<String, List<String>>(settings.macros);
		this.variables = new HashMap<String, String>(settings.variables);
		
		this.DEFAULT_SAVE_PROPERTIES = settings.DEFAULT_SAVE_PROPERTIES;
		this.MAPPED_SAVE_PROPERTIES = new HashMap<String, Map<String, Triple<Boolean, Boolean, Boolean>>>(settings.MAPPED_SAVE_PROPERTIES);
		this.SAVE_PROPERTIES = new HashMap<String, Triple<Boolean, Boolean, Boolean>>(settings.SAVE_PROPERTIES);
		
		this.loadedOnce = settings.loadedOnce;
		this.lastDim = settings.lastDim;
		this.lastServer = settings.lastServer;
		this.lastWorld = settings.lastWorld;
	}
    
	static {
		SettingsManager.registerSerializable("aliases", MAP_STRING_STRING_SERIALIZABLE, true);
		SettingsManager.registerSerializable("variables", MAP_STRING_STRING_SERIALIZABLE, true);
		SettingsManager.registerSerializable("macros", MAP_STRING_LIST_STRING_SERIALIZABLE, true);
	}
}
