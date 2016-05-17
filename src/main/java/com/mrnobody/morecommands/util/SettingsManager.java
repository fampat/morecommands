package com.mrnobody.morecommands.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

/**
 * A class managing every kind of settings of this mod.
 * Also writes and reads to setting files.
 * 
 * The (de)serialization process is implemented by {@link SettingsSerializable}
 * 
 * @author MrNobody98
 * @see SettingsSerializable
 */
public abstract class SettingsManager {	
	/**
	 * Every kind of setting is wrapped into this class.
	 * Every setting can be dependent on a server (except for server settings, they
	 * of course are saved on the server so they will always have the same server address
	 * so making them depending on the server is quite useless), a world and a dimension.
	 * <p>
	 * Dependent means that this setting is only intended to be available on a server of
	 * which the server address is contained in the list of server address this setting
	 * allows, in a world of which the name is contained in the list of worlds names this
	 * setting allows and in a dimension of which the name is contained in the list of
	 * dimension names this setting allows.
	 * </p><p>
	 * If a list of any of these three dependencies is empty, this means that every
	 * server/world/dimension is allowed.
	 * </p><p>
	 * A Setting is immutable.
	 * </p>
	 * 
	 * @author MrNobody98
	 * @param <T> the type of the value this setting contains
	 */
	public static final class Setting<T> {
		private T value;
		private final ImmutableSet<String> server;
		private final ImmutableSet<String> world;
		private final ImmutableSet<String> dims;
		private final boolean allowMerge;
		
		/**
		 * Constructs a new Setting object
		 * 
		 * @param value the setting value
		 * @param server the server address on which this setting is allowed to be available. null means any server
		 * @param world the name of the world in which this setting is allowed to be available. null means any world
		 * @param dim the name of the dimension in which this setting is allowed to be available. null means any dimension
		 * @param allowMerge if the value this setting contains is a list or a map, this boolean indicates whether it's allowed
		 *                   to merge the list/map this setting contains with a list/map from a setting which has less requirements
		 *                   (e.g. no requirements at all/does not depend on anything)
		 */
		public Setting(T value, String server, String world, String dim, boolean allowMerge) {
			this.value = value;
			this.server = server == null ? ImmutableSet.<String>of() : ImmutableSet.of(server);
			this.world = world == null ? ImmutableSet.<String>of() : ImmutableSet.of(world);
			this.dims = dim == null ? ImmutableSet.<String>of() : ImmutableSet.of(dim);
			this.allowMerge = allowMerge;
		}
		
		/**
		 * Constructs a new Setting object
		 * 
		 * @param value the setting value
		 * @param server the server addresses on which this setting is allowed to be available. null or empty means any server
		 * @param world the names of the worlds in which this setting is allowed to be available. null or empty means any world
		 * @param dim the names of the dimensions in which this setting is allowed to be available. null or empty means any dimension
		 * @param allowMerge if the value this setting contains is a list or a map, this boolean indicates whether it's allowed
		 *                   to merge the list/map this setting contains with a list/map from a setting which has less requirements
		 *                   (e.g. no requirements at all/does not depend on anything)
		 */
		public Setting(T value, Set<String> server, Set<String> world, Set<String> dims, boolean allowMerge) {
			this.value = value;
			this.server = server == null ? ImmutableSet.<String>of() : ImmutableSet.<String>builder().addAll(server).build();
			this.world = world == null ? ImmutableSet.<String>of() : ImmutableSet.<String>builder().addAll(world).build();
			this.dims = dims == null ? ImmutableSet.<String>of() : ImmutableSet.<String>builder().addAll(dims).build();
			this.allowMerge = allowMerge;
		}
		
		/**
		 * @return The value of this setting
		 */
		public T getValue() {
			return this.value;
		}
		
		/**
		 * Sets the value
		 * 
		 * @param value the value
		 */
		public void setValue(T value) {
			this.value = value;
		}
		
		/**
		 * @return The addresses of server on which this setting should be available. Empty means any server
		 */
		public ImmutableSet<String> getServer() {
			return this.server;
		}
		
		/**
		 * @return The world names of worlds in which this setting should be available. Empty means any world
		 */
		public ImmutableSet<String> getWorld() {
			return this.world;
		}
		
		/**
		 * @return The dimension names of dimensions in which this setting should be available. Empty means any dimensions
		 */
		public ImmutableSet<String> getDim() {
			return this.dims;
		}
		
		/**
		 * @return If the value of this setting is a list/map, this method indicates that it is allowed to merge the
		 * list/map of this setting with a list/map of a setting that has less requirements (e.g. no requirements at all/
		 * a setting that does not depend on anything)
		 */
		public boolean allowMerge() {
			return this.allowMerge;
		}
		
		/**
		 * IMPORTANT: The equals method of a Setting does NOT check the equality of the values. This is required because
		 * otherwise a player could store settings with completely different values but exactly the same requirements 
		 * in a set of which every value should be unique in terms of setting requirements but not in terms of the value.
		 * You have to compare the value for yourself.
		 * <br>
		 * Generally it's recommended to use this class only to read and write settings but not to store them. You should
		 * extract the value and store it somewhere else, especially if you want uniqueness in terms of the value.
		 */
		@Override
		public boolean equals(Object o) {
			if (o == this) return true;
			else if (!(o instanceof Setting)) return false;
			else {
				Setting s = (Setting) o;
				return //((s.value == null && this.value == null) || (s.value != null && s.value.equals(this.value))) &&
						s.server.equals(this.server) && s.world.equals(this.world) && s.dims.equals(this.dims);
			}
		}
		
		@Override
		public int hashCode() {
			int hashCode = 17;
			//hashCode += hashCode * 37 + (this.value == null ? 0 : this.value.hashCode());
			hashCode += hashCode * 37 + this.server.hashCode();
			hashCode += hashCode * 37 + this.world.hashCode();
			hashCode += hashCode * 37 + this.dims.hashCode();
			return hashCode;
		}
		
		@Override
		public String toString() {
			return "server: " + this.server + "; worlds: " + this.world + "; dimensions: " + this.dims + "; allowMerge: " + this.allowMerge + "; value: " + this.value;
		}
	}
	
	/**
	 * An interface for (de)serializing values from and to
	 * an {@link AbstractElement}
	 * 
	 * @author MrNobody98
	 * @param <T> the type this Serializable (de)serializes
	 */
	public static interface Serializable<T> {
		/**
		 * Deserializes a value of type T from an {@link AbstractElement}
		 * 
		 * @param element the {@link AbstractElement} used to deserialize the value
		 * @return the deserialized value
		 */
		public T deserialize(AbstractElement element);
		
		/**
		 * Serializes a value of type T to an {@link AbstractElement}
		 * 
		 * @param element the element to serialize
		 * @return the serialized element
		 */
		public AbstractElement serialize(T element);
		
		/**
		 * @return The class of the type this Serializable (de)serializes
		 */
		public Class<T> getTypeClass();
	}
	
	private static final Map<String, Pair<Serializable<?>, Boolean>> serializables = new HashMap<String, Pair<Serializable<?>, Boolean>>();
	
	/**
	 * Registers a {@link Serializable} that is used to (de)serialize a setting
	 * 
	 * @param <T> the type the given {@link Serializable} (de)serializes
	 * @param name the setting that is (de)serialized with that {@link Serializable}
	 * @param serializable the {@link Serializable} that is used to (de)serialize that setting
	 * @param useServer whether this setting can depend on the server (See {@link Setting}). Settings that
	 *        are used server side obviously can't be depending on the server because they are on stored on
	 *        the server itself. If this is false, any server dependencies are not read or written during
	 *        (de)serialization. Set this to false for server side settings and true for client side settings.
	 */
	public static final <T> void registerSerializable(String name, Serializable<T> serializable, boolean useServer) {
		serializables.put(name, ImmutablePair.<Serializable<?>, Boolean>of(serializable, useServer));
	}
	
	/** The settings (de)serializer */
	private SettingsSerializable serializable;
	
	/**
	 * Constructs a new SettingsManager
	 * 
	 * @param load whether to read the settings immediately
	 * @param useServer whether to read server dependencies of settings (See {@link Setting} for more details)
	 */
	public SettingsManager(boolean load, boolean useServer) {
		this.serializable = new SettingsSerializable(useServer);
		if (load) loadSettings();
	}
	
	/**
	 * Constructs a new SettingsManager
	 * 
	 * @param load whether to read the settings immediately
	 * @param defaultSerializable the default (de)serializer used if a {@link Setting} occurs for which 
	 *        a {@link Serializable} has not been registered
	 * @param useServer whether to read and write server dependencies of settings (See {@link Setting} for more details)
	 */
	public SettingsManager(boolean load, Serializable<Object> defaultSerializable, boolean useServer) {
		this.serializable = new SettingsSerializable(defaultSerializable, useServer);
		if (load) loadSettings();
	}
	
	/**
	 * Gets all settings.
	 * @return A {@link SetMultimap} mapping a setting name to a list of settings (See {@link Setting} for more details)
	 */
	public abstract SetMultimap<String, Setting<?>> getSettings();
	
	/**
	 * Sets all settings.
	 * @param settings A {@link SetMultimap} mapping a setting name to a list of settings (See {@link Setting} for more details)
	 */
	public abstract void setSettings(SetMultimap<String, Setting<?>> settings);
	
	/**
	 * Loads the settings<br>
	 * The settings that are read will be stored in this object so you can
	 * access them with the getXXX() methods
	 */
	public abstract void loadSettings();
	
	/**
	 * Writes the settings<br>
	 * <b>IMPORTANT:</b> Unless you removed overwrote a setting
	 * with a storeXXX() or removeXXX() method, the settings that have
	 * been read from the settings file via {@link #loadSettings()} will be
	 * written back to the settings file because they are stored in this object.
	 */
	public abstract void saveSettings();
	
	/**
	 * @return whether the settings have been loaded from the settings file at least once
	 */
	public abstract boolean isLoaded();
	
	/**
	 * Serializes all settings into an {@link AbstractElement}
	 * 
	 * @return the serialized settings
	 */
	public final AbstractElement serializeSettings() {
		return this.serializable.serialize(this.getSettings());
	}
	
	/**
	 * Deserializes all settings from an {@link AbstractElement}
	 * 
	 * @param input the serialized settings
	 */
	public final void deserializeSettings(AbstractElement input) {
		this.setSettings(this.serializable.deserialize(input));
	}
	
	/**
	 * Gets a setting from the settings that have be read from the settings file via {@link #loadSettings()}
	 * 
	 * <p>
	 * This method assigns a priority to every {@link Setting} that has been read from the file and that corresponds to
	 * the setting's name. The priority is a sum that is constructed the following way: 
	 * 
	 * <ul>
	 * <li>If the setting accepts any server add 4 to the sum.
	 * <li>If the setting accepts any world add 2 to the sum. 
	 * <li>If the setting accepts any dimension add 1 to the sum. 
	 * </ul>
	 * 
	 * The lower the sum the higher the priority. This method returns the setting with the 
	 * highest priority that accepts the given server address, world name and dimension name.
	 * </p>
	 * 
	 * @param <T> the setting's type
	 * @param type the setting's name
	 * @param server the server address that the setting must accept, null if none
	 * @param world the world name that the setting must accept, null if none
	 * @param dim the dimension name that the setting must accept, null if none
	 * @param classOfT the class of the setting's type
	 * @return the setting with the highest priority that accepts server, world and dimension
	 */
	public final synchronized <T> Setting<T> getSetting(String type, String server, String world, String dim, Class<T> classOfT) {
		Set<Setting<?>> settings = this.getSettings().get(type);
		Object[] candidates = new Object[8];
		
		for (Setting<?> setting : settings) {
			if (!classOfT.isInstance(setting.getValue())) continue;
			
			if (accepts(setting, server, world, dim)) {
				int i = 0; if (setting.getServer().isEmpty()) i |= 4; if (setting.getWorld().isEmpty()) i |= 2; if (setting.getDim().isEmpty()) i |= 1;
				candidates[i] = new Setting(classOfT.cast(setting.getValue()), setting.getServer(), setting.getWorld(), setting.getDim(), setting.allowMerge());
			}
		}
		
		for (Object candidate : candidates) if (candidate != null) return (Setting<T>) candidate;
		return null;
	}
	
	/**
	 * Stores a setting in the list of settings that will be written to the settings file via {@link #saveSettings()}
	 * 
	 * <p>
	 * If there is already a setting that requires the given server, world and dimension, the value of
	 * that setting will be replaced with the value handed over to this method otherwise a new
	 * setting that requires the given server, world and dimension will be created
	 * </p>
	 * 
	 * @param <T> the setting's type
	 * @param type the setting's name
	 * @param value the value of this setting
	 * @param server the server address that this setting requires, null if none
	 * @param world the world name that this setting requires, null if none
	 * @param dim the dimension name that this setting requires, null if none
	 * @param classOfT the class of the setting's type
	 */
	public final synchronized <T> void storeSetting(String type, T value, String server, String world, String dim, Class<T> classOfT) {
		Setting<?> found = findValue(type, server, world, dim, classOfT);
		
		if (found != null) {
			 this.getSettings().remove(type, found);
			 this.getSettings().put(type, new Setting<T>(value, found.getServer(), found.getWorld(), found.getDim(), found.allowMerge()));
		}
		else this.getSettings().put(type, new Setting<T>(value, server, world, dim, true));
	}
	
	/**
	 * Removes a setting from the list of settings that will be written to the settings file via {@link #saveSettings()}
	 * 
	 * @param <T> the setting's type
	 * @param type the setting's name
	 * @param server the server that this setting requires, null if none
	 * @param world the world that this setting requires, null if none
	 * @param dim the dimension that this setting requires, null if none
	 * @param classOfT the setting's type class
	 */
	public final synchronized <T> void removeSetting(String type, String server, String world, String dim, Class<T> classOfT) {
		Setting<?> found = findValue(type, server, world, dim, classOfT);
		if (found != null) this.getSettings().remove(type, found);
	}
	
	/**
	 * Gets settings whose values are of type {@link Map} 
	 * from the settings that have be read from the settings file via {@link #loadSettings()}
	 * 
	 * <p>
	 * This method assigns a priority to every {@link Setting} that has been read from the file and that corresponds to
	 * the setting's name and of which the value is a {@link Map}. The priority is a sum that is constructed the following way: 
	 * 
	 * <ul>
	 * <li>If the setting accepts any server add 4 to the sum.
	 * <li>If the setting accepts any world add 2 to the sum. 
	 * <li>If the setting accepts any dimension add 1 to the sum. 
	 * </ul>
	 * 
	 * The lower the sum the higher the priority.<br><br> This method returns a list of {@link Setting}s that accept the given
	 * server, world and dimension and that is ordered by the priority in a descending order.<br><br>
	 * If, while adding the settings to the returned list, the {@link Setting#allowMerge()} method of a {@link Setting} 
	 * returns false, all following settings won't be added to the list.<br><br>
	 * For example if there are settings with the priorities 7, 5, 3 and 0 and the {@link Setting#allowMerge()} method of any of
	 * the settings that have priority 3 returns false, any subsequent settings with a lower priority won't be added to the list.
	 * For this example, this would be the settings with priority 7 and 5. The returned list would only have settings with
	 * priorities 0 and 3. As 0 is the higher priority, settings with that priority would come first in the list.
	 * </p>
	 * 
	 * @param <T> the value type of the map that the returned settings have as their value
	 * @param type the setting's name
	 * @param server the server address that the setting must accept, null if none
	 * @param world the world name that the setting must accept, null if none
	 * @param dim the dimension name that the setting must accept, null if none
	 * @param classOfT the class of the value type of the map that the returned settings have as their value
	 * @return a list of settings ordered descending by their priority which accept server, world and dimension
	 */
	public final synchronized <T> List<Setting<Map<String, T>>> getMappedSettings(String type, String server, String world, String dim, Class<T> classOfT) {
		Set<Setting<?>> settings = this.getSettings().get(type);
		Object[] candidates = new Object[8];
		
		for (Setting<?> setting : settings) {
			if (!(setting.getValue() instanceof Map<?, ?>)) continue;
			Setting<Map<String, T>> copy = new Setting<Map<String, T>>(new HashMap<String, T>(((Map<?, ?>) setting.getValue()).size()), setting.getServer(), setting.getWorld(), setting.getDim(), setting.allowMerge());
			int i = 0; if (setting.getServer().isEmpty()) i |= 4; if (setting.getWorld().isEmpty()) i |= 2; if (setting.getDim().isEmpty()) i |= 1;
			
			if (accepts(setting, server, world, dim)) {
				for (Map.Entry<?, ?> entry : ((Map<?, ?>) setting.getValue()).entrySet()) {
					if (entry.getKey() instanceof String && classOfT.isInstance(entry.getValue()))
						copy.getValue().put((String) entry.getKey(), classOfT.cast(entry.getValue()));
				}
				
				if (candidates[i] == null) candidates[i] = new HashSet<Setting<Map<String, T>>>();
				((Set<Setting<Map<String, T>>>) candidates[i]).add(copy);
			}
		}
		
		List<Setting<Map<String, T>>> finalList = new ArrayList<Setting<Map<String, T>>>();
		for (Object set : candidates) if (set != null) {
			boolean disallowMerge = false;
			for (Setting<Map<String, T>> setting : (Set<Setting<Map<String, T>>>) set) 
				if (!setting.allowMerge()) {disallowMerge = true; break;}
			
			finalList.addAll((Set<Setting<Map<String, T>>>) set);
			if (disallowMerge) return finalList;
		}
		
		return finalList;
	}
	
	/**
	 * Stores a setting that has a map as its value in the list of settings that will be written to the settings
	 * file via {@link #saveSettings()}
	 * 
	 * <p>
	 * If there is already a setting that requires the given server, world and dimension and of which the value
	 * is of type {@link Map}, the entries of the given map will be put into this map otherwise a new setting
	 * that requires the given server, world and dimension having the given map as value will be created.
	 * </p>
	 * 
	 * @param <T> the map's value type
	 * @param type the setting's name
	 * @param value the map that is the value of this setting
	 * @param server the server address that this setting requires, null if none
	 * @param world the world name that this setting requires, null if none
	 * @param dim the dimension name that this setting requires, null if none
	 * @param classOfT the map's value class
	 */
	public final synchronized <T> void storeMappedSettings(String type, Map<String, T> value, String server, String world, String dim, Class<T> classOfT) {
		Setting<?> found = findValue(type, server, world, dim, Map.class);
		
		if (found != null) {
			 this.getSettings().remove(type, found);
			 Map<String, T> map = Maps.newHashMapWithExpectedSize(((Map<?, ?>) found.getValue()).size() + value.size());
			 
			 for (Map.Entry<?, ?> entry : ((Map<?, ?>) found.getValue()).entrySet()) {
				 if (entry.getKey() instanceof String && classOfT.isInstance(entry.getValue()))
					 map.put((String) entry.getKey(), classOfT.cast(entry.getValue()));
			 }
			 
			 map.putAll(value);
			 this.getSettings().put(type, new Setting<Map<String, T>>(map, found.getServer(), found.getWorld(), found.getDim(), found.allowMerge()));
		}
		else this.getSettings().put(type, new Setting<Map<String, T>>(value, server, world, dim, true));
	}
	
	/**
	 * Removes entries from a setting that has a map as value and that will be written to the settings file via {@link #saveSettings()}
	 * 
	 * @param type the setting's name
	 * @param toRemove the keys of the entries to remove from the map that this setting has as value
	 * @param removeAll whether to remove all entries. If true <i>toRemove</i> may be empty or null
	 * @param server the server that this setting requires, null if none
	 * @param world the world that this setting requires, null if none
	 * @param dim the dimension that this setting requires, null if none
	 */
	public final synchronized void removeMappedSettings(String type, Collection<String> toRemove, boolean removeAll, String server, String world, String dim) {
		Setting<?> found = findValue(type, server, world, dim, Map.class);
		
		if (found != null) {
			 if (removeAll) ((Map<?, ?>) found.getValue()).clear();
			 else ((Map<?, ?>) found.getValue()).keySet().removeAll(toRemove);
			 
			 if (((Map<?, ?>) found.getValue()).isEmpty()) this.getSettings().remove(type, found);
		}
	}
	
	/**
	 * Checks if a setting accepts a given server, world and dimension
	 * 
	 * @param setting the setting to check
	 * @param server the server that the given setting must accept, null if none
	 * @param world the world that the given setting must accept, null if none
	 * @param dim the dimension that the given setting must accept, null if none
	 * @return whether the given setting accepts the given server, world and dimension
	 */
	private boolean accepts(Setting<?> setting, String server, String world, String dim) {
		boolean acceptsServer = setting.getServer().isEmpty() || (server != null && setting.getServer().contains(server));
		boolean acceptsWorld = setting.getWorld().isEmpty() || (world != null && setting.getWorld().contains(world));
		boolean acceptsDim = setting.getDim().isEmpty() || (dim != null && setting.getDim().contains(dim));
		
		return acceptsServer && acceptsWorld && acceptsDim;
	}
	
	/**
	 * Finds a value from the settings that have been read via {@link #loadSettings()} and that
	 * requires a given server, world and dimension and that has a value of a certain type
	 * 
	 * @param type the name of the setting to find
	 * @param server the server that the given setting requires, null if none
	 * @param world the world that the given setting requires, null if none
	 * @param dim the dimension that the given setting requires, null if none
	 * @param requiredClass the class of which the value of the setting must be an instance of
	 * @return the first setting that was found and that matches the given requirements
	 */
	private Setting<?> findValue(String type, String server, String world, String dim, Class<?> requiredClass) {
		Set<Setting<?>> settings = this.getSettings().get(type);
		
		for (Setting<?> setting : settings) {
			if (!requiredClass.isInstance(setting.getValue())) continue;
			
			boolean acceptsServer = (server == null && setting.getServer().isEmpty()) || (server != null && setting.getServer().contains(server));
			boolean acceptsWorld = (world == null && setting.getWorld().isEmpty()) || (world != null && setting.getWorld().contains(world));
			boolean acceptsDim = (dim == null && setting.getDim().isEmpty()) || (dim != null && setting.getDim().contains(dim));
			
			if (acceptsServer && acceptsWorld && acceptsDim) return setting;
		}
		
		return null;
	}
	
	/**
	 * The abstract base class for an element that was read from a settings file.
	 * It could either be a {@link ListElement}, a {@link ObjectElement}, a {@link StringElement}
	 * or a {@link NumericElement}
	 * 
	 * @author MrNobody98
	 */
	public static abstract class AbstractElement {
		/**
		 * @return Whether this element is a {@link ListElement}
		 */
		public final boolean isList() {
			return this instanceof ListElement;
		}
		
		/**
		 * @return Whether this element is a {@link ObjectElement}
		 */
		public final boolean isObject() {
			return this instanceof ObjectElement;
		}
		
		/**
		 * @return Whether this element is a {@link StringElement}
		 */
		public final boolean isString() {
			return this instanceof StringElement;
		}
		
		/**
		 * @return Whether this element is a {@link NumericElement}
		 */
		public final boolean isNumeric() {
			return this instanceof NumericElement;
		}
		
		/**
		 * @return This element as a {@link ListElement}
		 * @throws ClassCastException if this element is not a {@link ListElement}
		 */
		public final ListElement asList() {
			return (ListElement) this;
		}
		
		/**
		 * @return This element as a {@link ObjectElement}
		 * @throws ClassCastException if this element is not a {@link ObjectElement}
		 */
		public final ObjectElement asObject() {
			return (ObjectElement) this;
		}
		
		/**
		 * @return This element as a {@link StringElement}
		 * @throws ClassCastException if this element is not a {@link StringElement}
		 */
		public final StringElement asStringElement() {
			return (StringElement) this;
		}
		
		/**
		 * @return This element as a {@link NumericElement}
		 * @throws ClassCastException if this element is not a {@link NumericElement}
		 */
		public final NumericElement asNumericElement() {
			return (NumericElement) this;
		}
	}
	
	/**
	 * This element represents a list of {@link AbstractElement}s
	 * 
	 * @author MrNobody98
	 */
	public static final class ListElement extends AbstractElement implements Iterable<AbstractElement> {
		private final List<AbstractElement> values = Lists.newArrayList();
		
		/**
		 * Constructs a new {@link ListElement}
		 */
		public ListElement() {}
		
		/**
		 * Constructs a new {@link ListElement} having the given elements
		 * @param values the elements of this {@link ListElement}
		 */
		public ListElement(List<AbstractElement> values) {
			this.values.addAll(values);
		}
		
		/**
		 * @return an iterator over the elements of this {@link ListElement}
		 */
		@Override
		public Iterator<AbstractElement> iterator() {
			return this.values.iterator();
		}
		
		/**
		 * @return The elements of this {@link ListElement} as an array
		 */
		public AbstractElement[] toArray() {
			return this.values.toArray(new AbstractElement[this.values.size()]);
		}
		
		/**
		 * @return The size of this {@link ListElement}
		 */
		public int size() {
			return this.values.size();
		}
		
		/**
		 * @param idx the index
		 * @return the element at the given index
		 * @throws IndexOutOfBoundsException if idx < 0 || idx >= {@link #size()}
		 */
		public AbstractElement get(int idx) throws IndexOutOfBoundsException {
			return this.values.get(idx);
		}
		
		/**
		 * @param idx the index
		 * @param element the element to be stored at the given index
		 * @throws IndexOutOfBoundsException if idx < 0 || idx >= {@link #size()}
		 */
		public void set(int idx, AbstractElement element) throws IndexOutOfBoundsException {
			this.values.set(idx, element);
		}
		
		/**
		 * @param element the element to be added to the list
		 */
		public void add(AbstractElement element) {
			this.values.add(element);
		}
		
		/**
		 * @param the index at which the element is inserted (subsequent elements are right-shifted)
		 * @param element the element to be added to the list
		 * @throws IndexOutOfBoundsException if idx < 0 || idx >= {@link #size()}
		 */
		public void add(int idx, AbstractElement element) throws IndexOutOfBoundsException {
			this.values.add(idx, element);
		}
		
		/**
		 * @param elements the elements to add to this {@link ListElement}
		 */
		public void addAll(ListElement elements) {
			for (AbstractElement e : elements) add(e);
		}
		
		/**
		 * @param idx the index to remove
		 * @return the removed element
		 * @throws IndexOutOfBoundsException if idx < 0 || idx >= {@link #size()}
		 */
		public AbstractElement remove(int idx) throws IndexOutOfBoundsException {
			return this.values.remove(idx);
		}
		
		/**
		 * @param element the element to remove (removes the first occurrence)
		 * @return whether this {@link ListElement} contained the specified element
		 */
		public boolean remove(AbstractElement element) {
			return this.values.remove(element);
		}
		
		/**
		 * @param toRemove the elements to remove
		 * @return whether this {@link ListElement} contained the specified element
		 */
		public void removeAll(Collection<AbstractElement> toRemove) {
			this.values.removeAll(toRemove);
		}
		
		/**
		 * @param element the element to check if it is contained in this {@link ListElement]
		 * @return whether this {@link ListElement} contains the specified element
		 */
		public boolean contains(AbstractElement element) {
			return this.values.contains(element);
		}
		
		/**
		 * @param element the element to of which to get the index
		 * @return the index or -1 if not contained
		 */
		public int indexOf(AbstractElement element) {
			return this.values.indexOf(element);
		}
		
		@Override
		public String toString() {
			return this.values.toString();
		}
		
		@Override
		public boolean equals(Object o) {
			return o instanceof ListElement ? ((ListElement) o).values.equals(this.values) : super.equals(o);
		}
		
		@Override
		public int hashCode() {
			return this.values.hashCode();
		}
	}
	
	/**
	 * This class represents a map that maps {@link AbstractElement}s to a string key
	 * 
	 * @author MrNobody98
	 */
	public static final class ObjectElement extends AbstractElement {
		private final Map<String, AbstractElement> values = Maps.newHashMap();
		
		/**
		 * Constructs a new {@link ObjectElement}
		 */
		public ObjectElement() {}
		
		/**
		 * Constructs a new {@link ObjectElement} having the given entries
		 * @param values the entries of this {@link ObjectElement}
		 */
		public ObjectElement(Map<String, AbstractElement> values) {
			this.values.putAll(values);
		}
		
		/**
		 * @return a set of entries of this {@link ObjectElement}
		 */
		public Set<Map.Entry<String, AbstractElement>> entrySet() {
			return this.values.entrySet();
		}
		
		/**
		 * @param key the key of the value to get
		 * @return the {@link AbstractElement} mapped to the given key or null if there was no mapping
		 */
		public AbstractElement get(String key) {
			return this.values.get(key);
		}
		
		/**
		 * @param key the key of the mapping to add
		 * @param value the value of the mapping to add
		 */
		public void add(String key, AbstractElement value) {
			this.values.put(key, value);
		}
		
		/**
		 * @param key the key of the mapping to remove
		 */
		public void remove(String key) {
			this.values.remove(key);
		}
		
		/**
		 * @param key the key used to check if a mapping exists
		 * @return whether a mapping for the given key exists
		 */
		public boolean has(String key) {
			return this.values.containsKey(key);
		}
		
		@Override
		public String toString() {
			return this.values.toString();
		}
		
		@Override
		public boolean equals(Object o) {
			return o instanceof ObjectElement ? ((ObjectElement) o).values.equals(this.values) : super.equals(o);
		}
		
		@Override
		public int hashCode() {
			return this.values.hashCode();
		}
	}
	
	/**
	 * This element represents a String
	 * 
	 * @author MrNobody98
	 */
	public static class StringElement extends AbstractElement {
		private final String string;
		
		/**
		 * Constructs a new {@link StringElement} containing the given string
		 * @param string the string this element contains
		 */
		public StringElement(String string) {
			this.string = string;
		}
		
		/**
		 * @return the string this element contains
		 */
		public String asString() {
			return this.string;
		}
		
		@Override
		public String toString() {
			return this.string;
		}
		
		@Override
		public boolean equals(Object o) {
			return o instanceof StringElement ? ((StringElement) o).string.equals(this.string) : super.equals(o);
		}
		
		@Override
		public int hashCode() {
			return this.string.hashCode();
		}
	}
	
	/**
	 * This element represents a Number
	 * 
	 * @author MrNobody98
	 */
	public static final class NumericElement extends StringElement {
		private final Number number;
		
		/**
		 * Constructs a new {@link NumericElement} with the given Number
		 * @param number the number this element contains
		 */
		public NumericElement(Number number) {
			super(number.toString());
			this.number = number;
		}
		
		/**
		 * @return the number this element contains
		 */
		public Number asNumber() {
			return this.number;
		}
		
		@Override
		public String toString() {
			return this.number.toString();
		}
		
		@Override
		public boolean equals(Object o) {
			return o instanceof NumericElement ? ((NumericElement) o).number.equals(this.number) : super.equals(o);
		}
		
		@Override
		public int hashCode() {
			return this.number.hashCode();
		}
	}
	
	/**
	 * This class is used to (de)serialize settings from/into an {@link AbstractElement}.
	 * "settings" here means the {@link SettingsManager#settings} field.
	 * 
	 * <p>
	 * The process of serialization works the following way:
	 * </p>
	 * <p>
	 * A setting is basically a key-values pair (not only one value but multiple values). 
	 * The idea behind such a setting was that a setting can have different values depending on the server, the world
	 * and the dimension of a player. So a setting actually maps the setting name to different values. The value that
	 * is used is selected by the server, world and dimension of player. These values of a setting are represented by
	 * a {@link Setting} object. Therefore a setting is simply a name which is mapped to several values that are 
	 * {@link Setting} objects (This is actually the {@link SettingsManager#settings} field which is 
	 * a {@link SetMultimap} mapping a setting's name to its values).
	 * </p>
	 * <p>
	 * A {@link Setting} object itself consists of five fields that are serialized into one to five key-value pairs:
	 * <ul>
	 * <li>The {@link Setting#value} field is serialized into a key-value pair of which the key is named "value" and
	 *     of which the value is the actual value of the setting.
	 * <li>The {@link Setting#server} field is a list of server addresses. It is only serialized if it is not empty.
	 *     Otherwise it will be serialized into a key-value pair whose key is the string "server". The value will be
	 *     the server address at index 0 of the list if the list's size is 1 else the value will be the whole list.
	 * <li>The {@link Setting#world} field is a list of world names. It is serialized exactly the same way as the
	 *     {@link Setting#server} field except that the key name of the key-value pair is "world".
	 * <li>The {@link Setting#dims} field is a list of dimension names. It is serialized exactly the same way as the
	 *     {@link Setting#server} field except that the key name of the key-value pair is "dimension".
	 * <li>The {@link Setting#allowMerge} field is a boolean that indicates whether to allow to merge that setting.
	 *     (See {@link Setting#allowMerge()} for more details). It is only serialized if it is false. It will be
	 *     serialized into a key-value pair having "allowMerge" as its key and the value of the field as value.
	 * </ul>
	 * </p>
	 * <p>
	 * The final serialized form of a {@link Setting} object is an {@link ObjectElement} whose key-value pairs are those
	 * mentioned above unless there is only one serialized key-value pair (That means that {@link Setting#server}, 
	 * {@link Setting#world} and {@link Setting#dims} are empty and {@link Setting#allowMerge} is true) and the serialized
	 * form of {@link Setting#value} itself is not an {@link ObjectElement}. If that is true, the serialized form of the
	 * {@link Setting#value} field will be used as serialized form of the {@link Setting} object.
	 * </p>
	 * <p>
	 * As mentioned in the beginning, a setting is a name<->{@link Setting}s pair. Therefore the serialized form of multiple
	 * setting is an {@link ObjectElement} of those pairs. As explained above {@link Setting} objects are serialized into
	 * {@link ObjectElement}s. If there is only one {@link Setting} mapped to the setting name, the serialized form of that
	 * {@link Setting} will be used as value of the name<->{@link Setting}s pair, otherwise the value will be a {@link ListElement}
	 * consisting of the serialized forms of the {@link Setting} elements.
	 * </p>
	 * <p>
	 * All settings together (all name<->{@link Setting}s pairs) will be stored in an {@link ObjectElement} with
	 * the names as keys and the serialized form of the {@link Setting}s as values. This is the final serialized form
	 * of all settings.
	 * </p>
	 * <p>
	 * The deserialization process works exactly the other way round.
	 * </p>
	 * A visualized example in JSON notation looks like this (In this example,
	 * a json object corresponds to an {@link ObjectElement},
	 * a json array corresponds to a {@link ListElement}):<br>
	 * <pre>
	 * {
	 * 	"A setting name" : [
	 *   {
	 * 	  "server" : ["bla", "some server"],
	 * 	  "world" : "A world",
	 * 	  "dimension" : [1, "Nether"],
	 * 	  "allowMerge" : false,
	 * 	  "value" : "Insert any value here"
	 * 	 },
	 * 	 "A value without any dependency",
	 * 	],
	 *  "Another setting" : "I am a setting",
	 *  "Another setting 2" : {
	 *	 value : {
	 *	  "F" : "fly",
	 *	  "K" : "kill"
	 *	 }
	 *  }
	 * }
	 * </pre>
	 * 
	 * @author MrNobody98
	 */
	protected static final class SettingsSerializable implements Serializable<SetMultimap<String, Setting<?>>> {
		/**
		 * @param arr the {@link ListElement}
		 * @return a Set of Strings contained in the given {@link ListElement}
		 */
		private static final Set<String> toStringSet(ListElement arr) {
			return toStringSet(arr, false);
		}
		
		/**
		 * @param arr the {@link ListElement}
		 * @param convertDimIDs if there are integers in the given {@link ListElement}, interpret them
		 *        as dimension id's and add their corresponding dimension name to the returned set.<br>
		 *        <b>IMPORTANT:</b> Only the default dimension id's are converted (0: Overworld, 1: End, -1: Nether)
		 * @return a Set of Strings contained in the given {@link ListElement}
		 */
		private static final Set<String> toStringSet(ListElement arr, boolean convertDimIDs) {
			Set<String> list = Sets.newHashSet();
			
			for (int i = 0; i < arr.size(); i++) {
				if (convertDimIDs && arr.get(i).isNumeric()) {
					int dim = arr.get(i).asNumericElement().asNumber().intValue();
					
					if (dim == 0) list.add("Overworld");
					else if (dim == 1) list.add("The End");
					else if (dim == -1) list.add("Nether");
				}
				else if (arr.get(i).isString()) list.add(arr.get(i).asStringElement().asString());
			}
			
			return list;
		}
		
		/**
		 * @param list the iterable to convert
		 * @return A {@link ListElement} containing the elements of the given iterable
		 */
		private static final ListElement toStringArray(Iterable<String> list) {
			List<AbstractElement> arr = Lists.newArrayList();
			for (String s : list) arr.add(new StringElement(s));
			return new ListElement(arr);
		}
		
		/**
		 * This is the default (de)serializer for values of settings which don't have
		 * a {@link Serializable} registered to them.<br>
		 * The default implementation (de)serializes Strings, Numbers, Lists/Arrays, Maps and Strings.
		 */
		private Serializable<Object> defaulSerializable = new Serializable<Object>() {

			@Override
			public Object deserialize(AbstractElement element) {
				if (element == null) return null;
				else if (element.isString()) return element.asStringElement().asString();
				else if (element.isNumeric()) return element.asNumericElement().asNumber();
				else if (element.isList()) {
		    		List<Object> list = Lists.newArrayList();
		    		for (AbstractElement e : element.asList()) list.add(deserialize(e));
		    		return list;
		    	}
				else if (element.isObject()) {
					Map<String, Object> map = Maps.newHashMap();
					for (Map.Entry<String, AbstractElement> entry : element.asObject().entrySet()) map.put(entry.getKey(), deserialize(entry.getValue()));
					return map;
				}
		    	else return null;
			}

			@Override
			public AbstractElement serialize(Object obj) {
				if (obj instanceof Number) return new NumericElement((Number) obj);
		    	else if (obj instanceof String) return new StringElement((String) obj);
		    	else if (obj != null && obj.getClass().isArray()) {
		    		ListElement e = new ListElement();
		    		for (int i = 0; i < Array.getLength(obj); i++)  e.add(serialize(Array.get(obj, i)));
		    		return e;
		    	}
		    	else if (obj instanceof List<?>) {
		    		List<?> list = (List<?>) obj;
		    		ListElement l = new ListElement();
		    		
		    		for (Object o : list) {
		    			AbstractElement serialized = serialize(o);
		    			if (serialized != null) l.add(serialized);
		    		}
		    		
		    		return l;
		    	}
		    	else if (obj instanceof Map<?, ?>) {
		    		Map<?, ?> map = (Map<?, ?>) obj;
		    		ObjectElement e = new ObjectElement();
		    		
		    		for (Map.Entry<?, ?> entry : map.entrySet()) {
		    			if (!(entry.getKey() instanceof String)) continue;
		    			AbstractElement serialized = serialize(entry.getValue());
		    			if (serialized != null) e.add((String) entry.getKey(), serialized);
		    		}
		    		
		    		return e;
		    	}
		    	else return null;
			}

			@Override
			public Class<Object> getTypeClass() {
				return Object.class;
			}
		};
		
		/** whether to (de)serialize the {@link Setting#server} field */
		private boolean useServer = true;
		
		/** Constructs a new {@link SettingsSerializable} */
		public SettingsSerializable() {}
		
		/**
		 * Constructs a new {@link SettingsSerializable}
		 * @param useServer whether to (de)serialize the {@link Setting#server} field
		 */
		public SettingsSerializable(boolean useServer) {
			this.useServer = useServer;
		}
		
		/**
		 * Constructs a new {@link SettingsSerializable}
		 * @param defaultSerializable the default (de)serializer for values of {@link Setting}s which don't have a
		 *        {@link Serializable} registered to them
		 * @param useServer whether to (de)serialize the {@link Setting#server} field
		 */
		public SettingsSerializable(Serializable<Object> defaultSerializable, boolean useServer) {
			this.defaulSerializable = defaultSerializable;
			this.useServer = useServer;
		}
		
		/**
		 * Deserializes an {@link AbstractElement} that contains settings
		 * into a {@link SetMultimap} that contains settings
		 * @see SettingsSerializable
		 */
		public SetMultimap<String, Setting<?>> deserialize(AbstractElement element) {
			if (element == null || !element.isObject()) return HashMultimap.create();
			ObjectElement obj = element.asObject();
			SetMultimap<String, Setting<?>> settings = HashMultimap.create();
			
			for (Map.Entry<String, AbstractElement> entry : obj.entrySet()) {
				Pair<Serializable<?>, Boolean> config = serializables.get(entry.getKey());
				AbstractElement val = entry.getValue();
				
				if (!val.isList()) {ListElement e = new ListElement(Arrays.asList(val)); val = e;}
				
				for (AbstractElement elem : val.asList()) {
					if (!elem.isObject()) {
						Object value = config == null ? this.defaulSerializable.deserialize(elem) : config.getLeft().deserialize(elem);
						settings.put(entry.getKey(), new Setting<Object>(value, Sets.<String>newHashSet(), Sets.<String>newHashSet(), Sets.<String>newHashSet(), true));
					}
					else {
						ObjectElement setting = elem.asObject();
						
						AbstractElement val2 = setting.get("value");
						AbstractElement server = this.useServer && (config == null ? true : config.getRight()) ? setting.get("server") : null;
						AbstractElement world = setting.get("world");
						AbstractElement dim = setting.get("dimension");
						Object value = config == null ? this.defaulSerializable.deserialize(val2) : config.getLeft().deserialize(val2);
						boolean allowMerge = setting.has("allowMerge") ? setting.get("allowMerge").isString() ? Boolean.parseBoolean(setting.get("allowMerge").asStringElement().asString()) : true : true;
						
						Set<String> servers = server instanceof StringElement ? Sets.newHashSet(((StringElement) server).asString()) : server instanceof ListElement ? toStringSet((ListElement) server) : Sets.<String>newHashSet();
						Set<String> worlds = world instanceof StringElement ? Sets.newHashSet(((StringElement) world).asString()) : world instanceof ListElement ? toStringSet((ListElement) world) : Sets.<String>newHashSet();
						Set<String> dims = dim instanceof StringElement ? Sets.newHashSet(((StringElement) dim).asString()) : dim instanceof ListElement ? toStringSet((ListElement) dim, true) : Sets.<String>newHashSet();
						
						settings.put(entry.getKey(), new Setting<Object>(value, servers, worlds, dims, allowMerge));
					}
				}
			}
			
			return settings;
		}
		
		/**
		 * Serializes an {@link SetMultimap} that contains settings
		 * into an {@link AbstractElement} that contains settings
		 * @see SettingsSerializable
		 */
		public AbstractElement serialize(SetMultimap<String, Setting<?>> src) {
			ObjectElement finalObj = new ObjectElement();
			if (src == null) return finalObj;
			
			for (String key : src.keySet()) {
				Pair<Serializable<?>, Boolean> config = serializables.get(key);
				Set<Setting<?>> set = src.get(key);
				ListElement settings = new ListElement();
				
				for (Setting<?> setting : set) {
					if (setting.getValue() == null || (setting.getValue() instanceof Collection<?> 
						&& ((Collection<?>) setting.getValue()).isEmpty()) || (setting.getValue() instanceof Map<?, ?> 
						&& ((Map<?, ?>) setting.getValue()).isEmpty()) || (setting.getValue().getClass().isArray() &&
							Array.getLength(setting.getValue()) == 0)) continue;
					
					AbstractElement value; try {
					value = config == null || !config.getLeft().getTypeClass().isInstance(setting.getValue()) ? 
					this.defaulSerializable.serialize(setting.getValue()) : ((Serializable) config.getLeft()).serialize(setting.getValue());
					} catch (ClassCastException ex) {value = this.defaulSerializable.serialize(setting.getValue());}
					
					if (value.isObject() || !setting.getServer().isEmpty() || !setting.getWorld().isEmpty() || !setting.getDim().isEmpty() || !setting.allowMerge()) {
						ObjectElement obj = new ObjectElement();
						
						if (this.useServer && (config == null ? true : config.getRight()) && !setting.getServer().isEmpty()) if (setting.getServer().size() == 1)
						obj.add("server", new StringElement(Iterables.get(setting.getServer(), 0))); else obj.add("server", toStringArray(setting.getServer()));
					
						if (!setting.getWorld().isEmpty()) if (setting.getWorld().size() == 1)
						obj.add("world", new StringElement(Iterables.get(setting.getWorld(), 0))); else obj.add("world", toStringArray(setting.getWorld()));
						
						if (!setting.getDim().isEmpty()) if (setting.getDim().size() == 1)
						obj.add("dimension", new StringElement(Iterables.get(setting.getDim(), 0))); else obj.add("dimension", toStringArray(setting.getDim()));
						
						if (!setting.allowMerge())
							obj.add("allowMerge", new StringElement("false"));
						
						obj.add("value", value);
						settings.add(obj);
					}
					else settings.add(value);
				}
				
				if (settings.size() == 1) finalObj.add(key, settings.get(0));
				else finalObj.add(key, settings);
			}
			
			return finalObj;
		}
		
		@Override
		public Class<SetMultimap<String, Setting<?>>> getTypeClass() {
			return (Class<SetMultimap<String, Setting<?>>>) (Class<?>) SetMultimap.class;
		}
	}
	
	/**
	 * A dummy SettingsManager that has no load/save capabilities
	 * 
	 * @author MrNobody98
	 */
	public static final class DummySettingsManager extends SettingsManager {
		public DummySettingsManager() {super(false, false);}
		@Override public void loadSettings() {}
		@Override public void saveSettings() {}
		@Override public boolean isLoaded() {return false;}
		@Override public SetMultimap<String, Setting<?>> getSettings() {return HashMultimap.create();}
		@Override public void setSettings(SetMultimap<String, Setting<?>> settings) {}
	}
}