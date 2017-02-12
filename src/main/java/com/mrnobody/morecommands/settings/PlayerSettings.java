package com.mrnobody.morecommands.settings;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

/**
 * A class keeping track of player settings.
 * Contains some common client/server side settings too.
 * 
 * @author MrNobody98
 */
public abstract class PlayerSettings implements IExtendedEntityProperties {
	/** The server and client side common settings */
	public static final ImmutableSet<String> COMMON_SETTINGS = ImmutableSet.of("aliases", "variables", "macros");
	/** The {@link IExtendedEntityProperties} identifier to be used to register an object of this class to a player */
	public static final String MORECOMMANDS_IDENTIFIER = "morecommands";
	
	protected EnumMap<SettingsProperty, String> properties = new EnumMap<SettingsProperty, String>(SettingsProperty.class);
	
	@Override public void init(Entity entity, World world) {}
	@Override public void saveNBTData(NBTTagCompound compound) {}
	@Override public void loadNBTData(NBTTagCompound compound) {}
	
	/** A map of aliases a player created */
	public MergedMappedSettings<String, String> aliases;
	/** A map of variables a player created */
	public MergedMappedSettings<String, String> variables;
	/** A map of macros (list of commands) a player created */
	public MergedMappedSettings<String, List<String>> macros;
	
	protected final SettingsManager manager;
	
	/**
	 * Creates a new PlayerSettings instance
	 * 
	 * @param manager the manager to read and write from/to
	 * @param load whether to immidiately read the settings
	 */
	public PlayerSettings(SettingsManager manager, boolean load) {
		this.manager = manager;
		
		if (load && !this.manager.isLoaded()) 
			this.manager.loadSettings();
	}
	
	/**
	 * Registers aliases on the respective side
	 */
	protected abstract void registerAliases();
	
	/**
	 * Reads the settings from the settings manager using the current {@link SettingsProperty}s
	 */
	protected synchronized void readSettings() {
		this.aliases = readMergedMappedSettings("aliases", String.class, SettingsProperty.SERVER_PROPERTY);
		this.variables = readMergedMappedSettings("variables", String.class, SettingsProperty.SERVER_PROPERTY);
		this.macros = readMergedMappedSettings("macros", (Class<List<String>>) (Class<?>) List.class, SettingsProperty.SERVER_PROPERTY);
	
		registerAliases();
	}
	
	/**
	 * Re-reads the settings from the settings manager. This is automatically done
	 * when using {@link #updateSettingsProperties(Map)}, {@link #updateSettingsProperty(SettingsProperty, String)}
	 * or {@link #resetSettingsProperties(Map)} so a call to this method is not required
	 */
	public void refresh() {
		readSettings();
	}
	
	/**
	 * @return the settings properties used to load and store setting from/to the settings manager
	 */
	public EnumMap<SettingsProperty, String> getSettingsProperties() {
		return new EnumMap<SettingsProperty, String>(this.properties);
	}
	
	/**
	 * Updates a {@link SettingsProperty}
	 * 
	 * @param property the property to update
	 * @param setting the new value
	 */
	public void updateSettingsProperty(SettingsProperty property, String setting) {
		this.properties.put(property, setting);
		readSettings();
	}
	
	/**
	 * Updates several {@link SettingsProperty}s
	 * 
	 * @param properties a map from the properties to their new values
	 */
	public void updateSettingsProperties(Map<SettingsProperty, String> properties) {
		this.properties.putAll(properties);
		readSettings();
	}
	
	/**
	 * Resets the settings properties.
	 * 
	 * @param properties The new settings properties as a map from property to value. (All old ones are cleared)
	 */
	public void resetSettingsProperties(Map<SettingsProperty, String> properties) {
		this.properties.clear();
		updateSettingsProperties(properties);
	}
	
	/**
	 * This method is used to set the settings saving behavior: It is used to define
	 * which {@link SettingsProperty}s are associated to a setting when using the
	 * {@link MergedMappedSettings#put(Object, Object)} method.
	 * 
	 * @param type the name of the setting. Must be one of {@link #GLOBAL_SETTINGS}
	 * @param world whether the {@link SettingsProperty#WORLD_PROPERTY} should be used
	 * @param dim whether the {@link SettingsProperty#DIMENSION_POPERTY} should be used
	 * @return whether <i>type</i> was a valid setting name
	 * @see MergedMappedSettings#setPutSetting(Setting)
	 */
	
	/**
	 * This method is used to set the settings saving behavior: It is used to define
	 * which {@link SettingsProperty}s are associated to a setting when using the
	 * {@link MergedMappedSettings#put(Object, Object)} method.
	 * 
	 * @param setting the setting name
	 * @param props the properties associated to a setting when using the {@link MergedMappedSettings#put(Object, Object)} method
	 * @return whether <i>setting</i> was a valid settings name
	 */
	public synchronized boolean setPutProperties(String setting, SettingsProperty... props) {
		if (!COMMON_SETTINGS.contains(setting)) return false;
		
		Map<SettingsProperty, Set<String>> sProps = Maps.newEnumMap(SettingsProperty.class);
		for (SettingsProperty prop : props) sProps.put(prop, Sets.newHashSet(this.properties.get(prop)));
		
		if ("aliases".equals(setting))
			this.aliases.setPutSetting(new Setting<Map<String, String>>(Maps.<String, String>newHashMap(), sProps));
		else if ("variables".equals(setting))
			this.variables.setPutSetting(new Setting<Map<String, String>>(Maps.<String, String>newHashMap(), sProps));
		else if ("macros".equals(setting))
			this.macros.setPutSetting(new Setting<Map<String, List<String>>>(Maps.<String, List<String>>newHashMap(), sProps));
	
		return true;
	}
	
	/**
	 * Reads {@link MergedMappedSettings} from the settings manager
	 * 
	 * @param name the name of the setting
	 * @param cls the setting's type class
	 * @param putProps the default properties used for {@link MergedMappedSettings#put(Object, Object)}. See {@link #setPutProperties(String, SettingsProperty...)}
	 * @return the {@link MergedMappedSettings}
	 */
	protected synchronized <T> MergedMappedSettings<String, T> readMergedMappedSettings(String name, Class<T> cls, SettingsProperty... putProps) {
		Map<SettingsProperty, String> map = Maps.newEnumMap(SettingsProperty.class);
		for (SettingsProperty p : putProps) 
			if (this.properties.containsKey(p)) map.put(p, this.properties.get(p));
		
		return this.manager.getMergedMappedSettings(name, this.properties, map, cls);
	}
	
	/**
	 * @return the {@link SettingsManager} this {@link PlayerSettings} object uses
	 */
	public final SettingsManager getManager() {
		return this.manager;
	}
	
	protected static final SettingsSerializer<Map<String, String>> MAP_STRING_STRING_SERIALIZER = new SettingsSerializer<Map<String, String>>() {
		@Override
		public JsonElement serialize(Map<String, String> src) {
			JsonObject obj = new JsonObject();
			
			for (Map.Entry<String, String> entry : src.entrySet()) 
				obj.add(entry.getKey(), new JsonPrimitive(entry.getValue()));
			
			return obj;
		}
		
		@Override
		public Map<String, String> deserialize(JsonElement elem){
			if (!elem.isJsonObject()) return Maps.newHashMap();
			
			JsonObject obj = elem.getAsJsonObject();
			Map<String, String> map = Maps.newHashMap();
			
			for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
				if (!entry.getValue().isJsonPrimitive() || !entry.getValue().getAsJsonPrimitive().isString()) continue;
				else map.put(entry.getKey(), entry.getValue().getAsJsonPrimitive().getAsString());
			}
			
			return map;
		}
		
		@Override
		public Class<Map<String, String>> getTypeClass() {
			return (Class<Map<String, String>>) (Class<?>) Map.class;
		}
	};
	
	protected static final SettingsSerializer<Map<String, List<String>>> MAP_STRING_LIST_STRING_SERIALIZER = new SettingsSerializer<Map<String, List<String>>>() {
		@Override
		public JsonElement serialize(Map<String, List<String>> src) {
			JsonObject obj = new JsonObject();
			
			for (Map.Entry<String, List<String>> entry : src.entrySet())  {
				JsonArray arr = new JsonArray();
				for (String str : entry.getValue()) arr.add(new JsonPrimitive(str));
				obj.add(entry.getKey(), arr);
			}
			
			return obj;
		}
		
		@Override
		public Map<String, List<String>> deserialize(JsonElement element) {
			if (!element.isJsonObject()) return Maps.newHashMap();
			
			JsonObject obj = element.getAsJsonObject();
			Map<String, List<String>> map = Maps.newHashMap();
			
			for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
				if (!entry.getValue().isJsonArray()) continue;
				List<String> list = Lists.newArrayList();
				
				for (JsonElement elem : entry.getValue().getAsJsonArray())
					if (elem.isJsonPrimitive() && elem.getAsJsonPrimitive().isString()) 
						list.add(elem.getAsString());
				
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
	protected void copyProperties(PlayerSettings settings) {
		this.aliases = settings.aliases;
		this.macros = settings.macros;
		this.variables = settings.variables;
	}
    
	static {
		RootSettingsSerializer.registerSerializer("aliases", MAP_STRING_STRING_SERIALIZER);
		RootSettingsSerializer.registerSerializer("variables", MAP_STRING_STRING_SERIALIZER);
		RootSettingsSerializer.registerSerializer("macros", MAP_STRING_LIST_STRING_SERIALIZER);
	}
}
