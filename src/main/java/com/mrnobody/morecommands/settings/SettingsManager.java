package com.mrnobody.morecommands.settings;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;

/**
 * A class managing settings which are constrained by several {@link SettingsProperty}s
 * and represented by {@link Setting} objects
 * <p>
 * Also reads and writes from/to setting files.
 * <p>
 * The process of serialization works the following way:
 * </p>
 * For each setting there can be multiple concrete values of this setting. Which settings are
 * valid is determined by the constraints defined in {@link SettingsProperty}.
 * <p>
 * The serialization simply creates a root JsonObject where each key corresponds to
 * the setting's name. Each such key has a JsonArray as value. Each entry in this JsonArray
 * is a JsonObject representing a concrete setting value with their {@link SettingsProperty}s. 
 * This JsonObject has one basic key "value" which maps the actual value to a JsonElement returned by a
 * {@link SettingsSerializer}. Optionally (if it is >= 0), there is a key "priority" which is a numeric 
 * JsonPrimitive which is equal to {@link Setting#getCustomPriorityLevel()} if this number is >= 0.
 * Otherwise this key is omitted. Also optionally (if it is false), there is a key "allowMerge" which 
 * is a boolean JsonPrimitive which is equal to {@link Setting#allowMerge()}. If this value is true, the
 * key is omitted since true is the default value. At last, for every {@link SettingsProperty} of the setting, 
 * there is one key named {@link SettingsProperty#getName()} which has a JsonArray as value which consists 
 * of string values corresponding to the accepted property values of the setting.
 * </p>
 * <p>
 * The deserialization process works exactly the other way round.
 * </p>
 * A visualized example in JSON notation looks like this:<br>
 * <pre>
 * {
 * 	"A setting name" : [
 *   {
 * 	  "server" : ["bla", "some server"],
 * 	  "world" : "A world",
 * 	  "dimension" : ["The End", "Nether"],
 * 	  "priority" : 3,
 * 	  "value" : "Insert any value here"
 * 	 },
 *   {
 * 	  "server" : ["singleplayer"],
 * 	  "value" : "A value"
 * 	 }
 * 	],
 *  "Another setting" : [
 *    {
 *     "value": "I am a setting"
 *    }
 *  ],
 *  "Another setting 2" : {
 *   "allowMerge" : false,
 *  
 *	 "value" : {
 *	  "F" : "fly",
 *	  "K" : "kill"
 *	 }
 *  }
 * }
 * </pre>
 * 
 * @author MrNobody98
 * @see SettingsProperty
 * @see Setting
 * @see SettingsSerializer
 */
public abstract class SettingsManager {
	/** The root settings (de)serializer */
	private SettingsSerializer<SetMultimap<String, Setting<?>>> settingsSerializer;
	
	/**
	 * Constructs a new server side SettingsManager
	 */
	public SettingsManager() {
		this(false);
	}
	
	/**
	 * Constructs a new SettingsManager
	 * 
	 * @param isClient whether this is a client side settings manager
	 */
	public SettingsManager(boolean isClient) {
		this.settingsSerializer = new RootSettingsSerializer(isClient);
	}
	
	/**
	 * Constructs a new SettingsManager
	 * 
	 * @param defaultSerializer the default (de)serializer used if a {@link Setting} occurs for which 
	 *        a {@link SettingsSerializer} has not been registered
	 * @param isClient whether this is a client side settings manager
	 */
	public SettingsManager(SettingsSerializer<Object> defaultSerializer, boolean isClient) {
		this.settingsSerializer = new RootSettingsSerializer(defaultSerializer, isClient);
	}
	
	/**
	 * Gets all settings.
	 * @return A {@link SetMultimap} mapping a setting name to a list of settings (See {@link Setting} for more details)
	 */
	protected abstract SetMultimap<String, Setting<?>> getSettings();
	
	/**
	 * Sets all settings.
	 * @param settings A {@link SetMultimap} mapping a setting name to a list of settings (See {@link Setting} for more details)
	 */
	protected abstract void setSettings(SetMultimap<String, Setting<?>> settings);
	
	/**
	 * Loads the settings<br>
	 * The settings that are read will be stored in this settings manager 
	 * so you can access them with the get() methods
	 */
	public abstract void loadSettings();
	
	/**
	 * Writes the settings<br>
	 * If you follow the recommendations stated for {@link Setting} it is not
	 * required to store settings via the store() methods because changes are
	 * reflected in this settings manager
	 */
	public abstract void saveSettings();
	
	/**
	 * @return whether the settings have been loaded from the settings file at least once
	 */
	public abstract boolean isLoaded();
	
	/**
	 * Serializes all settings into a {@link JsonElement}
	 * 
	 * @return the serialized settings
	 */
	public final JsonElement serializeSettings() {
		return this.settingsSerializer.serialize(this.getSettings());
	}
	
	/**
	 * Deserializes all settings from a {@link JsonElement}
	 * 
	 * @param input the serialized settings
	 */
	public final void deserializeSettings(JsonElement input) {
		this.setSettings(this.settingsSerializer.deserialize(input));
	}
	
	/**
	 * Gets a setting from the settings that have be read from the settings file via {@link #loadSettings()}
	 * 
	 * <p>
	 * Since it is possible for a setting to have different values depending on the {@link SettingsProperty}s
	 * associated, this method does not return a single {@link Setting} but a DESCENDING priority-ordered
	 * list of all settings of which the properties match the given properties.
	 * <p>
	 * The ordering process is done using {@link Collections#sort(List)} using the comparison described in {@link Setting#PRIORITY_COMPARATOR}
	 * 
	 * @param <T> the setting's type
	 * @param type the setting's name
	 * @param properties A map from {@link SettingsProperty}s to values which every setting must match to be returned
	 * @param classOfT the class of the setting's type
	 * @return the priority-ordered settings
	 */
	public final synchronized <T> List<Setting<T>> getSetting(String type, Map<SettingsProperty, String> properties, Class<T> classOfT) {
		return sortByPriority(getSettingsAccepting(type, properties, classOfT), false);
	}
	
	/**
	 * Stores a setting in the list of settings that will be written to the settings file via {@link #saveSettings()}
	 * 
	 * <p>
	 * If there is already a setting that requires exactly the same {@link SettingsProperty}s,
	 * this setting will be replaced by the given setting
	 * </p>
	 * 
	 * @param <T> the setting's type
	 * @param type the setting's name
	 * @param setting the setting to store
	 */
	public final synchronized <T> void storeSetting(String type, Setting<T> setting) {
		if (this.getSettings().containsEntry(type, setting))
			this.getSettings().remove(type, setting);
		
		this.getSettings().put(type, setting);
	}
	
	/**
	 * Removes a setting from the list of settings that will be written to the settings file via {@link #saveSettings()}
	 * 
	 * @param <T> the setting's type
	 * @param type the setting's name
	 * @param setting the setting to remove
	 */
	public final synchronized <T> void removeSetting(String type, Setting<T> setting) {
		this.getSettings().remove(type, setting);
	}
	
	/**
	 * Gets settings from this settings manager whose value is of type {@link Map}
	 * and merges them into {@link MergedMappedSettings}
	 * 
	 * @param <T> the map's value type (key type is always String)
	 * @param type the setting's name
	 * @param props A map from {@link SettingsProperty}s to values which every setting must match to be returned
	 * @param putProps the default {@link SettingsProperty} used when the {@link MergedMappedSettings#put(Object, Object)} is used. See that method for more details
	 * @param classOfT the map's value class (key class is always String)
	 * @return all map values of the settings merged into {@link MergedMappedSettings}
	 */
	public final synchronized <T> MergedMappedSettings<String, T> getMergedMappedSettings(String type, Map<SettingsProperty, String> props, Map<SettingsProperty, String> putProps, Class<T> classOfT) {
		return new MergedMappedSettings<String, T>(this, type, sortByPriority(getMappedSettingsAccepting(type, props, classOfT), true), putProps);
	}
	
	/**
	 * Stores {@link MergedMappedSettings} into this settings manager
	 * This method is actually not required, since all changes made to {@link MergedMappedSettings}
	 * are reflected in the settings manager.
	 * 
	 * @param type the setting's name
	 * @param settings the {@link MergedMappedSettings} to store
	 */
	public final synchronized <T> void storeMergedMappedSettings(String type, MergedMappedSettings<String, T> settings) {
		for (MergedMappedSettings.SettingsContainer setting : settings.getSettings())
			storeSetting(type, setting.getSetting());
	}

	/**
	 * Removes {@link MergedMappedSettings} from this settings manager
	 * This method is actually not required, since all changes made to {@link MergedMappedSettings}
	 * are reflected in the settings manager.
	 * 
	 * @param type the setting's name
	 * @param settings the {@link MergedMappedSettings} to remove
	 */
	public final synchronized <T> void removeMergedMappedSettings(String type, MergedMappedSettings<String, T> settings) {
		for (MergedMappedSettings.SettingsContainer setting : settings.getSettings())
			removeSetting(type, setting.getSetting());
	}
	
	/**
	 * Sorts a set of settings according to the algorithm described in {@link #getSetting(String, Map, Class)}
	 * 
	 * @param settings the settings to sort
	 * @param stripUnallowedMergeSettings whether to exclude all settings of sublist from i to list.size() where i is the first setting for which {@link Setting#allowMerge()} is not true
	 * @return the sorted settings
	 */
	private <T> List<Setting<T>> sortByPriority(Collection<Setting<T>> settings, boolean stripUnallowedMergeSettings) {
		List<Setting<T>> list = Lists.newArrayList(settings);
		Collections.sort(list, Setting.PRIORITY_COMPARATOR);
		
		if (stripUnallowedMergeSettings) {
			int idx = list.size() - 1;
			
			for (int i = 0; i < list.size(); i++) {
				if (!list.get(i).allowMerge()) {
					idx = i; break;
				}
			}
			
			return Lists.newArrayList(list.subList(0, idx + 1));
		}
		else
			return list;
	}
	
	/**
	 * Returns a set of all setting which match some given {@link SettingsProperty}s
	 * 
	 * @param settingName the name of the settings
	 * @param props the properties to match
	 * @param classOfT the value class of the settings
	 * @return the setting accepting the given properties
	 */
	private <T> Set<Setting<T>> getSettingsAccepting(String settingName, final Map<SettingsProperty, String> props, final Class<T> classOfT) {
		Set<Setting<T>> filtered = Sets.newHashSet(); 
		
		for (Setting<?> setting : getSettings().get(settingName)) {
			if (!classOfT.isInstance(setting.getValue())) continue;
			Map<SettingsProperty, Set<String>> settingProps = setting.getProperties();
			boolean accepts = true;
			
			for (Map.Entry<SettingsProperty, String> prop : props.entrySet()) {
				Set<String> accepted = settingProps.get(prop.getKey());
				
				if (accepted == null || accepted.isEmpty()) continue;
				if (!accepted.contains(prop.getValue())) {accepts = false; break;}
			}
			
			if (accepts) filtered.add((Setting<T>) setting);
		}
		
		return filtered;
	}

	/**
	 * Returns a set of all setting of type Map<String, T> which match some given {@link SettingsProperty}s
	 * 
	 * @param settingName the name of the settings
	 * @param props the properties to match
	 * @param classOfT the value class of the map
	 * @return the setting accepting the given properties
	 */
	private <T> Set<Setting<Map<String, T>>> getMappedSettingsAccepting(String settingName, final Map<SettingsProperty, String> props, final Class<T> classOfMapValue) {
		Set<Setting<Map<String, T>>> filtered = Sets.newHashSet(); 
		
		for (Setting<?> setting : getSettings().get(settingName)) {
			if (!(setting.getValue() instanceof Map<?, ?>)) continue;
			if (((Map<?, ?>) setting.getValue()).isEmpty()) continue;
			
			
			Map.Entry<?, ?> entry = ((Map<?, ?>) setting.getValue()).entrySet().iterator().next();
			if (!(entry.getKey() instanceof String)) continue;
			if (!classOfMapValue.isInstance(entry.getValue())) continue;
			
			Map<SettingsProperty, Set<String>> settingProps = setting.getProperties();
			boolean accepts = true;
			
			for (Map.Entry<SettingsProperty, String> prop : props.entrySet()) {
				Set<String> accepted = settingProps.get(prop.getKey());
				
				if (accepted == null || accepted.isEmpty()) continue;
				if (!accepted.contains(prop.getValue())) {accepts = false; break;}
			}
			
			if (accepts) filtered.add((Setting<Map<String, T>>) setting);
		}
		
		return filtered;
	}
}