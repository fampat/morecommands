package com.mrnobody.morecommands.settings;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mrnobody.morecommands.settings.ChangeNotifyingMap.ChangeListener;

/**
 * <b>IMPORTANT NOTE:</b><br>
 * Although being an implementation of Map, this class clearly violates the map contract (continue
 * reading to see why). This class is only intended to be used as a storage of setting values which
 * are "proper" maps but want some additional behavior which is not consistent with the map contract. 
 * The only reason why this class is a Map is that it shares lots of functionality and it is essentially
 * a kind of a map with some different properties which is why Map is implemented - for simple 
 * convenience reasons. You should be really clear about what you do with objects of this class and use them
 * only as storage for setting values.
 * 
 * <p>
 * This class manages the access to multiple {@link Setting}s which have a map as their value.<br>
 * This class represents a <i>merged</i> version of these maps. The merging works the following way:
 * </p><p>
 * Given a List&ltSetting&ltMap&ltK, V&gt&gt&gt which is priority-ordered according to {@link SettingsManager#getSetting(String, Map, Class)}
 * Then the merged map is constructed the following way:<br>
 * Create a new Map&ltK, V&gt and iteratively starting from the END of the list
 * put the entries of each map in the list into this new map which will then be the merged map.
 * </p><p>
 * As already said this class represents such a merged map but additionally keeps the feature of {@link Setting}s
 * that every change made to the map is reflected in the settings manager.<br>
 * This has several effects:
 * <ul>
 * <li>When a value is removed from this map, it is possible that a value for the SAME setting (that means the same key)
 *     with a lower priority exists in the settings manager. In this case, instead of being removed
 *     the value will be replaced with that new value. This is also the case for the clear() method
 *     This is obviously a violation of the Map contract so please be aware what you do with objects
 *     of this class and only use them for the intended purposes
 * <li>Every change made in this map is also reflected in the {@link Setting}s used to construct an
 *     object of this class and therefore also in the settings manager
 * <li>For the <i>put</i> method there MUST be a {@link Setting} to which the value should be written
 *     Therefore objects of this class always require a "put setting" which is used for the <i>put</i>
 *     method. This is also the way how you can specify which {@link SettingsProperty}s are used when
 *     new setting values are to be stored in the settings manager and ultimately in setting files.
 * <ul>
 * </p>
 * 
 * @author MrNobody
 *
 * @param <K> the key type of the map
 * @param <V> the value type of the map
 */
public class MergedMappedSettings<K, V> implements Map<K, V> {
	private List<SettingsContainer> settings = Lists.newArrayList();
	private Map<K, MutablePair<V, Integer>> mergedMap = new HashMap<K, MutablePair<V, Integer>>();
	
	private SettingsManager manager;
	private String settingName;
	private int putIndex;
	
	private Collection<V> values;
	private Set<K> keySet;
	private Set<Map.Entry<K, V>> entrySet;
	
	/**
	 * Constructs new {@link MergedMappedSettings}
	 * 
	 * @param manager the manager which created this object
	 * @param settingsName the setting's name
	 * @param maps the priority-ordered list of settings
	 * @param putProps the properties used for the <i>put</i> method
	 */
	MergedMappedSettings(SettingsManager manager, String settingsName, List<Setting<Map<K, V>>> maps, Map<SettingsProperty, String> putProps) {
		this.manager = manager;
		this.settingName = settingsName;
		
		for (Setting<Map<K, V>> setting : maps)
			this.settings.add(new SettingsContainer(setting, this.settings.size()));
		
		mergeMaps();
		Map<SettingsProperty, Set<String>> map = Maps.newEnumMap(SettingsProperty.class); 
		
		for (Map.Entry<SettingsProperty, String> prop : putProps.entrySet()) 
			map.put(prop.getKey(), Sets.newHashSet(prop.getValue()));
		
		setPutSetting(new Setting<Map<K, V>>((Map<K, V>) Maps.newHashMap(), map));
	}
	
	/**
	 * @return the settings contained in this MergedMappedSettings
	 */
	List<SettingsContainer> getSettings() {
		return this.settings;
	}
	
	/**
	 * Clears the merged map and re-merges it from the settings
	 */
	private void mergeMaps() {
		this.mergedMap.clear();
		
		for (int idx = this.settings.size() - 1; idx >= 0; idx--)
			for (Map.Entry<K, V> entry : this.settings.get(idx).setting.getValue().entrySet())
				this.mergedMap.put(entry.getKey(), MutablePair.of(entry.getValue(), idx));
	}
	
	/**
	 * Gets the insertion index of a settings by its priority
	 * using binary search
	 * 
	 * @param setting the setting to get the insertion index for
	 * @return the insertion index (positive if equal value found, negative - 1 if not found)
	 */
	private int getInsertionIndex(Setting<Map<K, V>> setting) {
		int low = 0;
		int high = this.settings.size() - 1;
		
		while (low <= high) {
			int mid = (low + high) >>> 1;
			int cmp = Setting.PRIORITY_COMPARATOR.compare(this.settings.get(mid).setting, setting);
			
			if (cmp < 0)
				low = mid + 1;
			else if (cmp > 0)
				high = mid - 1;
			else
				return mid;
		}
		
		return -(low + 1);
	}
	
	/**
	 * Gets the index of a setting in the settings list
	 * (This is required as extra method since SettingsContainer does not implement
	 *  equals, see that class to see why)
	 *  
	 * @param setting the setting
	 * @return the index of the list (-1 if not contained)
	 */
	private int getIndex(Setting<Map<K, V>> setting) {
		for (int i = 0; i < this.settings.size(); i++)
			if (this.settings.get(i).setting.equals(setting)) return i;
		
		return -1;
	}
	
	/**
	 * Sets the setting to which is written via the <i>put</i> method
	 * @param setting the setting
	 */
	public synchronized void setPutSetting(Setting<Map<K, V>> setting) {
		boolean alreadyContained = false;
		
		for (Setting<?> s : this.manager.getSettings().get(this.settingName))
			if (setting.equals(s)) {setting  = (Setting<Map<K, V>>) s; alreadyContained = true; break;}
		
		int idx = getIndex(setting);
		
		if (idx < 0) {
			idx = getInsertionIndex(setting);
			idx = idx < 0 ? -idx - 1 : idx;
			
			this.settings.add(idx, new SettingsContainer(setting, idx));
			if (!alreadyContained) this.manager.storeSetting(this.settingName, setting);
			
			adjustIndices(idx, 1);
		}
		
		this.putIndex = idx;
	}
	
	/**
	 * Adjusts the indices starting with a certain index by a certain amount.
	 * This is needed when a setting is removed or inserted because
	 * subsequent settings are shifted
	 * 
	 * @param startIdx the index from which on the indices should be adjusted
	 * @param amount the amount to adjust the indices
	 */
	private synchronized void adjustIndices(int startIdx, final int amount) {
		for (Map.Entry<K, MutablePair<V, Integer>> entry : this.mergedMap.entrySet())
			if (entry.getValue().getRight() >= startIdx)
				entry.getValue().setRight(entry.getValue().getRight() + amount);
		
		for (int i = startIdx; i < this.settings.size(); i++)
			this.settings.get(i).listIndex = i;
		
		if (this.putIndex >= startIdx)
			this.putIndex += amount;
	}
	
	/**
	 * Removes the setting at an index
	 * 
	 * @param idx the index of the setting to remove
	 */
	private synchronized void removeSetting(int idx) {
		SettingsContainer sc = this.settings.get(idx);
		
		ChangeNotifyingMap<K, V> map = (ChangeNotifyingMap<K, V>) sc.setting.getValue();
		map.unregisterChangeListener(sc);
		
		if (map.getChangeListeners().isEmpty()) {
			sc.setting.setValueRaw(map.getBackingMap());
			this.manager.removeSetting(this.settingName, sc.setting);
		}
		
		this.settings.remove(idx);
	}
	
	/**
	 * Removes a setting if the map value of that setting is empty
	 * 
	 * @param idx the index of the setting
	 */
	private synchronized void removeMapIfEmpty(int idx) {
		if (idx != this.putIndex && this.settings.get(idx).setting.getValue().isEmpty()) {
			removeSetting(idx);
			adjustIndices(idx, -1);
		}
	}
	
	/**
	 * Returns the value and the index of the setting with the highest priority
	 * which has a mapping for a key
	 * 
	 * @param key the key
	 * @return the value mapped to that key and the index of the setting containing this mapping (null if there is none)
	 */
	private synchronized Pair<V, Integer> getIndexAndValueForKey(Object key) {
		for (int i = 0; i < this.settings.size(); i++)
			if (this.settings.get(i).setting.getValue().containsKey(key)) 
				return ImmutablePair.<V, Integer>of(this.settings.get(i).setting.getValue().get(key), i);
	
		return null;
	}
	
	/**
	 * Invoked when an entry of a map of one of the settings is removed.<br>
	 * This is used to adjust the merged map accordingly
	 * 
	 * @param sc the setting of which the entry was removed
	 * @param entry the removed entry
	 */
	private synchronized void onEntryRemove(SettingsContainer sc, Map.Entry<K, V> entry) {
		MutablePair<V, Integer> pair = this.mergedMap.get(entry.getKey());
		
		if (pair != null && pair.getRight() == sc.listIndex) {
			Pair<V, Integer> newVal = getIndexAndValueForKey(entry.getKey());
			
			if (newVal != null) {
				pair.setLeft(newVal.getLeft());
				pair.setRight(newVal.getRight());
			}
		}
		
		removeMapIfEmpty(sc.listIndex);
	}
	
	/**
	 * Invoked when an entry of a map of one of the settings was set to a new value.<br>
	 * This is used to adjust the merged map accordingly
	 * 
	 * @param sc the setting of which the entry value was changed
	 * @param entry the changed entry
	 * @param value the new value
	 */
	private synchronized void onEntrySet(SettingsContainer sc, Map.Entry<K, V> entry, V value) {
		MutablePair<V, Integer> pair = this.mergedMap.get(entry.getKey());
		
		if (pair != null && pair.getRight() == sc.listIndex)
			pair.setLeft(value);
	}
	
	/**
	 * Invoked when a mapping of a map of one of the settings is removed.<br>
	 * This is used to adjust the merged map accordingly
	 * 
	 * @param sc the setting of which the entry was removed
	 * @param key the key of the mapping to be removed
	 */
	private synchronized void onRemove(SettingsContainer sc, Object key) {
		MutablePair<V, Integer> pair = this.mergedMap.get(key);
		
		if (pair != null && pair.getRight() == sc.listIndex) {
			Pair<V, Integer> newVal = getIndexAndValueForKey(key);
			
			if (newVal != null) {
				pair.setLeft(newVal.getLeft());
				pair.setRight(newVal.getRight());
			}
			else this.mergedMap.remove(key);
		}
		
		removeMapIfEmpty(sc.listIndex);
	}
	
	/**
	 * Invoked when a mapping was put into a map of one of the settings.<br>
	 * This is used to adjust the merged map accordingly
	 * 
	 * @param sc the setting into which the mapping was put
	 * @param key the key of the mapping
	 * @param value the value of the mapping
	 */
	private synchronized void onPut(SettingsContainer sc, K key, V value) {
		MutablePair<V, Integer> pair = this.mergedMap.get(key);
		
		if (pair == null)
			this.mergedMap.put(key, MutablePair.of(value, sc.listIndex));
		else if (pair.getRight() >= sc.listIndex) {
			pair.setLeft(value);
			pair.setRight(sc.listIndex);
		}
	}
	
	/**
	 * Invoked when the map of one of the settings is cleared.<br>
	 * This is used to adjust the merged map accordingly
	 * 
	 * @param sc the setting of which the map was cleared
	 */
	private synchronized void onClear(SettingsContainer sc) {
		for (Map.Entry<K, MutablePair<V, Integer>> entry : Sets.newHashSet(this.mergedMap.entrySet()))
			this.onRemove(sc, entry.getKey());
	}
	
	/**
	 * Invoked when the map of one of the settings was replaced by a new map.<br>
	 * This is used to adjust the merged map accordingly
	 * 
	 * @param sc the setting of which the map was set to a new map
	 * @param map the new map
	 */
	private synchronized void onSetBackingMap(SettingsContainer sc, Map<K, V> map) {
		for (Map.Entry<K, V> entry : map.entrySet())
			this.onPut(sc, entry.getKey(), entry.getValue());
	}
	
	/**
	 * Clears this {@link MergedMappedSettings}.<br>
	 * <b>IMPORTANT:</b><br>
	 * This does not ensure that this map will be empty afterwards.
	 * The only thing it does is removing all the setting values currently
	 * contained in the merged map. However there may be settings with the 
	 * same map key but a lower priority. In this case, these settings 
	 * will form a new merged map.
	 */
	@Override
	public synchronized void clear() {
		for (Map.Entry<K, MutablePair<V, Integer>> entry : Sets.newHashSet(this.mergedMap.entrySet()))
			this.settings.get(entry.getValue().getRight()).setting.getValue().remove(entry.getKey());
	}
	
	/**
	 * Puts a value to the default setting set via {@link #setSelectedPutSetting(Setting)}
	 * Also removes any mappings of settings with a higher priority than the default setting
	 */
	@Override
	public synchronized V put(K key, V value) {
		List<SettingsContainer> remList = Lists.newArrayList(this.settings.subList(0, this.putIndex));
		
		for (SettingsContainer sc : remList)
			sc.setting.getValue().remove(key);
		
		return this.settings.get(this.putIndex).setting.getValue().put(key, value);
	}
	
	/**
	 * Removes a map value by its key<br>
	 * <b>IMPORTANT:</b><br>
	 * This does not ensure that there will be no mapping with this key afterwards.
	 * The only thing it does is removing the setting values currently associated
	 * to this key in the merged map. However there may be a setting with the same 
	 * map key but a lower priority. In this case, this settings will be put into
	 * the merged map.
	 */
	@Override
	public synchronized V remove(Object key) {
		MutablePair<V, Integer> val = this.mergedMap.get(key);
		if (val == null) return null;
		
		return this.settings.get(val.getRight()).setting.getValue().remove(key);
	}
	
	/**
	 * Delegates to {@link #put(Object, Object)} for all entries of the map. See that method for more details
	 */
	@Override
	public synchronized void putAll(Map<? extends K, ? extends V> m) {
		for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) 
			put(entry.getKey(), entry.getValue());
	}
	
	/**
	 * Returns a set of map entries of this merged map<br>
	 * All changes to that set are reflected in this map.
	 */
	@Override
	public synchronized Set<Map.Entry<K, V>> entrySet() {
		return this.entrySet == null ? (this.entrySet = new EntrySet()) : this.entrySet;
	}
	
	/**
	 * Returns a set of map keys of this merged map<br>
	 * All changes to that set are reflected in this map.
	 */
	@Override
	public synchronized Set<K> keySet() {
		return this.keySet == null ? (this.keySet = new KeySet()) : this.keySet;
	}
	
	/**
	 * Returns a set of map values of this merged map<br>
	 * All changes to that set are reflected in this map.
	 */
	@Override
	public synchronized Collection<V> values() {
		return this.values == null ? (this.values = new Values()) : this.values;
	}
	
	/**
	 * @see {@link Map#containsKey(Object)}
	 */
	@Override
	public synchronized boolean containsKey(Object key) {
		return this.mergedMap.containsKey(key);
	}
	
	/**
	 * @see {@link Map#containsValue(Object)}
	 */
	@Override
	public synchronized boolean containsValue(Object value) {
		for (V val : this.values())
			if (val == null ? value == null : val.equals(value)) return true;
		
		return false;
	}
	
	/**
	 * @see {@link Map#get(Object)}
	 */
	@Override
	public synchronized V get(Object key) {
		MutablePair<V, Integer> pair = this.mergedMap.get(key);
		return pair == null ? null : pair.getLeft();
	}
	
	/**
	 * @see {@link Map#isEmpty()}
	 */
	@Override
	public synchronized boolean isEmpty() {
		return this.mergedMap.isEmpty();
	}
	
	/**
	 * @see {@link Map#size()}
	 */
	@Override
	public synchronized int size() {
		return this.mergedMap.size();
	}
	
	/**
	 * @see {@link Map#equals(Object)}
	 */
	@Override
	public boolean equals(Object o) {
		return this.mergedMap.equals(o);
	}
	
	/**
	 * @see {@link Map#hashCode()}
	 */
	@Override
	public int hashCode() {
		return this.mergedMap.hashCode();
	}
	
	/**
	 * @see {@link Map#toString()}
	 */
	@Override
	public String toString() {
		return this.mergedMap.toString();
	}
	
	private class Values extends AbstractCollection<V> {
		@Override
		public Iterator<V> iterator() {
			return newValuesIterator();
		}

		@Override
		public int size() {
			return MergedMappedSettings.this.size();
		}
		
		private Iterator<V> newValuesIterator() {
			return new EntryIterator<V>() {
				@Override
				public V next() {
					return nextEntry().getValue();
				}
			};
		}
	}
	
	private class KeySet extends AbstractSet<K> {
		@Override
		public Iterator<K> iterator() {
			return newKeyIterator();
		}

		@Override
		public int size() {
			return MergedMappedSettings.this.size();
		}
		
		private Iterator<K> newKeyIterator() {
			return new EntryIterator<K>() {
				@Override
				public K next() {
					return nextEntry().getKey();
				}
			};
		}
	}
	
	private class EntrySet extends AbstractSet<Map.Entry<K, V>> {
		@Override
		public Iterator<Map.Entry<K, V>> iterator() {
			return newEntryIterator();
		}

		@Override
		public int size() {
			return MergedMappedSettings.this.size();
		}
		
		private Iterator<Map.Entry<K, V>> newEntryIterator() {
			return new EntryIterator<Map.Entry<K, V>>() {
				@Override
				public Map.Entry<K, V> next() {
					return nextEntry();
				}
			};
		}
	}
	
	private abstract class EntryIterator<E> implements Iterator<E> {
		private Iterator<Map.Entry<K, MutablePair<V, Integer>>> actualIterator = Sets.newHashSet(MergedMappedSettings.this.mergedMap.entrySet()).iterator();
		private Entry current = null;
		
		@Override
		public final boolean hasNext() {
			return this.actualIterator.hasNext();
		}
		
		protected final Map.Entry<K, V> nextEntry() {
			if (!hasNext())
				throw new NoSuchElementException();
			
			return this.current = new Entry(this.actualIterator.next());
		}
		
		public final void remove() {
			if (this.current == null)
				throw new IllegalStateException();
			
			this.current.setting.getValue().remove(this.current.key);
			this.current.setRemoved();
			this.current = null;
		}
	}
	
	private final class Entry implements Map.Entry<K, V> {
		private K key;
		private V value;
		private Setting<Map<K, V>> setting;
		private boolean removed = false;
		
		private Entry(Map.Entry<K, MutablePair<V, Integer>> actualEntry) {
			this.key = actualEntry.getKey();
			this.value = actualEntry.getValue().getLeft();
			this.setting = MergedMappedSettings.this.settings.get(actualEntry.getValue().getRight()).setting;
		}
		
		private void setRemoved() {
			this.removed = true;
		}
		
		@Override 
		public K getKey() {
			return this.key;
		}
		
		@Override
		public V getValue() {
			return this.value;
		}
		
		@Override
		public V setValue(V value) {
			V ret = this.value;;
			this.value = value;
			
			if (!this.removed)
				this.setting.getValue().put(this.key, value);
			
			return ret;
		}
		
		@Override 
		public boolean equals(Object o) {
			if (o == this) return true;
			if (!(o instanceof Map.Entry)) return false;
			Map.Entry<?, ?> that = (Map.Entry<?, ?>) o;
			
			return (this.key == null ? that.getKey() == null : this.key.equals(that.getKey())) &&
					(this.value == null ? that.getValue() == null : this.value.equals(that.getValue()));
		}
		
		@Override 
		public int hashCode() {
			return (this.key == null ? 0 : this.key.hashCode()) ^ (this.value == null ? 0 : this.value.hashCode());
		}
		
		@Override 
		public String toString() {
			return this.key + "=" + this.value;
		}
	}
	
	/**
	 * A wrapper class for settings which implements
	 * {@link ChangeListener}
	 * <p>
	 * The constructor sets the value of a setting to a {@link ChangeNotifyingMap}
	 * to be notified about changes to adjust the merged map
	 * accordingly
	 * <p>
	 * This class doesn't implement equals or hashCode because
	 * two instances of this class would be considered equal
	 * if their contained settings are equal. However this would
	 * break symmetry of the equivalence relation of equals
	 * because {@link Setting} is not equal to any instance
	 * of this class even if it contains exactly that setting.
	 * Since this class is not intended to be used only as wrapper
	 * this is not a problem.
	 * 
	 * @author MrNobody98
	 */
	final class SettingsContainer implements ChangeListener<K, V> {
		private Setting<Map<K, V>> setting;
		private int listIndex;
		
		/**
		 * Constructs a new settings container
		 * 
		 * @param setting the setting
		 * @param listIndex the list index of the list in which this object is stored.
		 * 		  Used to determine the corresponding mappings in the merged map
		 * 		  (You could obviously iterate over the list and check for identity
		 * 		   equality to get the index but that is obviously slower)
		 */
		private SettingsContainer(Setting<Map<K, V>> setting, int listIndex) {
			this.setting = setting;
			this.listIndex = listIndex;
			
			if (!(setting.getValue() instanceof ChangeNotifyingMap))
				setting.setValueRaw(new ChangeNotifyingMap<K, V>(setting.getValue()));
			
			((ChangeNotifyingMap<K, V>) setting.getValue()).registerChangeListener(this);
		}
		
		/**
		 * @return the setting of this container
		 */
		Setting<Map<K, V>> getSetting() {
			return this.setting;
		}
		
		/**
		 * Delegates to {@link MergedMappedSettings#onEntryRemove(SettingsContainer, java.util.Map.Entry)}
		 */
		@Override
		public void onEntryRemove(ChangeNotifyingMap<K, V> map, Map.Entry<K, V> entry) {
			MergedMappedSettings.this.onEntryRemove(this, entry);
		}
		

		/**
		 * Delegates to {@link MergedMappedSettings#onEntrySet(SettingsContainer, java.util.Map.Entry, Object)}
		 */
		@Override
		public void onEntrySet(ChangeNotifyingMap<K, V> map, Map.Entry<K, V> entry, V value) {
			MergedMappedSettings.this.onEntrySet(this, entry, value);
		}
		
		/**
		 * Delegates to {@link MergedMappedSettings#onRemove(SettingsContainer, Object)}
		 */
		@Override
		public void onRemove(ChangeNotifyingMap<K, V> map, Object key) {
			MergedMappedSettings.this.onRemove(this, key);
		}

		/**
		 * Delegates to {@link MergedMappedSettings#onPut(SettingsContainer, Object, Object)}
		 */
		@Override
		public void onPut(ChangeNotifyingMap<K, V> map, K key, V value) {
			MergedMappedSettings.this.onPut(this, key, value);
		}
		
		/**
		 * Delegates to {@link MergedMappedSettings#onClear(SettingsContainer)}
		 */
		@Override
		public void onClear(ChangeNotifyingMap<K, V> map) {
			MergedMappedSettings.this.onClear(this);
		}
		
		/**
		 * Delegates to {@link MergedMappedSettings#onSetBackingMap(SettingsContainer, Map)}
		 */
		@Override
		public void onSetBackingMap(ChangeNotifyingMap<K, V> map, Map<K, V> backingMap) {
			MergedMappedSettings.this.onSetBackingMap(this, backingMap);
		}
		
		/**
		 * Delegates to {@link Setting#toString()}
		 */
		@Override
		public String toString() {
			return this.setting.toString();
		}
	}
}
