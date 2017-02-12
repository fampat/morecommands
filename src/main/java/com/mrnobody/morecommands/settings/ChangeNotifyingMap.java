package com.mrnobody.morecommands.settings;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.WeakHashMap;

import com.google.common.collect.Sets;

/**
 * A map implementation which notifies several
 * change listeners about any change of the map.
 * 
 * @author MrNobody98
 *
 * @param <K> the key type of the map
 * @param <V> the value type of the map
 */
class ChangeNotifyingMap<K, V> implements Map<K, V> {
	/**
	 * A method of a change listener is invoked
	 * if a change to the map occurs.
	 * However, this doesn't allow to prevent changes
	 * 
	 * @author MrNobody98
	 *
	 * @param <K> the key type of the map
	 * @param <V> the value type of the map
	 */
	public static interface ChangeListener<K, V> {
		/**
		 * Invoked when a map entry was removed via {@link Iterator#remove()}.
		 * This iterator is either the iterator of {@link Map#entrySet()}, {@link Map#keySet()} or {@link Map#values()}
		 * 
		 * @param map the map from which the entry was removed
		 * @param entry the removed entry
		 */
		void onEntryRemove(ChangeNotifyingMap<K, V> map, Map.Entry<K, V> entry);
		
		/**
		 * Invoked when a map entry was set to a new value via {@link Map.Entry#setValue(Object)}
		 * 
		 * @param map the map for which the entry value was changed
		 * @param entry the changed entry
		 * @param value the new value
		 */
		void onEntrySet(ChangeNotifyingMap<K, V> map, Map.Entry<K, V> entry, V value);
		
		/**
		 * Invoked when a mapping was to be removed
		 * 
		 * @param map the map from which the mapping to the given key was to be removed
		 * @param key the key of the mapping to be removed
		 */
		void onRemove(ChangeNotifyingMap<K, V> map, Object key);
		
		/**
		 * Invoked when a mapping was added or replaced
		 * 
		 * @param map the map in which the new mapping is stored
		 * @param key the key of the new mapping
		 * @param value the value of the new mapping
		 */
		void onPut(ChangeNotifyingMap<K, V> map, K key, V value);
		
		/**
		 * Invoked when the map was cleared
		 * 
		 * @param map the map which was cleared
		 */
		void onClear(ChangeNotifyingMap<K, V> map);
		
		/**
		 * Invoked when the backing map was changed
		 * 
		 * @param map the map which was cleared
		 * @param backingMap the new backing map
		 */
		void onSetBackingMap(ChangeNotifyingMap<K, V> map, Map<K, V> backingMap);
	}
	
	private Set<ChangeListener<K, V>> changeListeners = Collections.newSetFromMap(new WeakHashMap<ChangeListener<K, V>, Boolean>());
	private Map<K, V> map;
	private EntrySet entrySet;
	private KeySet keySet;
	private Values values;
	
	/**
	 * Constructs a new {@link ChangeNotifyingMap}
	 * 
	 * @param map the backing map
	 */
	public ChangeNotifyingMap(Map<K, V> map) {
		this.map = map;
	}
	
	/**
	 * @return the backing map
	 */
	public Map<K, V> getBackingMap() {
		return this.map;
	}
	
	/**
	 * Sets the backing map
	 * 
	 * @param map the new backing map
	 */
	public void setBackingMap(Map<K, V> map) {
		this.map = map;
		
		for (ChangeListener<K, V> changeListener : Sets.newHashSet(this.changeListeners))
			changeListener.onSetBackingMap(this, map);
	}
	
	/**
	 * @return all change listeners registered to this map
	 */
	public Set<ChangeListener<K, V>> getChangeListeners() {
		return this.changeListeners;
	}
	
	/**
	 * Registers a change listener
	 * 
	 * @param listener the listener to register
	 */
	public void registerChangeListener(ChangeListener<K, V> listener) {
		this.changeListeners.add(listener);
	}
	
	/**
	 * Explicitly unregisters a change listener
	 * 
	 * @param listener the listener to unregister
	 */
	public void unregisterChangeListener(ChangeListener<K, V> listener) {
		this.changeListeners.remove(listener);
	}
	
	@Override
	public void clear() {
		this.map.clear();
		
		for (ChangeListener<K, V> changeListener : Sets.newHashSet(this.changeListeners))
			changeListener.onClear(this);
	}

	@Override
	public boolean containsKey(Object key) {
		return this.map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return this.map.containsValue(value);
	}

	@Override
	public V get(Object key) {
		return this.map.get(key);
	}

	@Override
	public boolean isEmpty() {
		return this.map.isEmpty();
	}

	@Override
	public V put(K key, V value) {
		V ret = this.map.put(key, value);
		
		for (ChangeListener<K, V> changeListener : Sets.newHashSet(this.changeListeners))
			changeListener.onPut(this, key, value);
		
		return ret;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for (Map.Entry<? extends K, ? extends V> entry : m.entrySet())
			put(entry.getKey(), entry.getValue());
	}

	@Override
	public V remove(Object key) {
		V ret = this.map.remove(key);
		
		for (ChangeListener<K, V> changeListener : Sets.newHashSet(this.changeListeners))
			changeListener.onRemove(this, key);
		
		return ret;
	}

	@Override
	public int size() {
		return this.map.size();
	}
	
	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		return this.entrySet == null ? new EntrySet() : this.entrySet;
	}
	
	@Override
	public Set<K> keySet() {
		return this.keySet == null ? new KeySet() : this.keySet;
	}
	
	@Override
	public Collection<V> values() {
		return this.values == null ? new Values() : this.values;
	}
	
	@Override
	public boolean equals(Object o) {
		return this.map.equals(o);
	}
	
	@Override
	public int hashCode() {
		return this.map.hashCode();
	}
	
	@Override
	public String toString() {
		return this.map.toString();
	}
	
	private class Values extends AbstractCollection<V> {
		@Override
		public Iterator<V> iterator() {
			return newValuesIterator();
		}

		@Override
		public int size() {
			return ChangeNotifyingMap.this.size();
		}
		
		@Override
		public void clear() {
			ChangeNotifyingMap.this.clear();
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
			return ChangeNotifyingMap.this.size();
		}
		
		@Override
		public void clear() {
			ChangeNotifyingMap.this.clear();
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
			return ChangeNotifyingMap.this.size();
		}
		
		@Override
		public void clear() {
			ChangeNotifyingMap.this.clear();
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
		private Iterator<Map.Entry<K, V>> actualIterator = ChangeNotifyingMap.this.map.entrySet().iterator();
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
			
			this.actualIterator.remove();
			
			for (ChangeListener<K, V> changeListener : Sets.newHashSet(ChangeNotifyingMap.this.changeListeners))
				changeListener.onEntryRemove(ChangeNotifyingMap.this, this.current.actualEntry);
			
			this.current = null;
		}
	}
	
	private final class Entry implements Map.Entry<K, V> {
		private Map.Entry<K, V> actualEntry;
		
		private Entry(Map.Entry<K, V> actualEntry) {
			this.actualEntry = actualEntry;
		}
		
		@Override 
		public K getKey() {
			return this.actualEntry.getKey();
		}
		
		@Override
		public V getValue() {
			return this.actualEntry.getValue();
		}
		
		@Override
		public V setValue(V value) {
			V ret = this.actualEntry.setValue(value);
			
			for (ChangeListener<K, V> changeListener : Sets.newHashSet(ChangeNotifyingMap.this.changeListeners))
				changeListener.onEntrySet(ChangeNotifyingMap.this, this.actualEntry, value);
			
			return ret;
		}
		
		@Override 
		public boolean equals(Object o) {
			return this.actualEntry.equals(o);
		}
		
		@Override 
		public int hashCode() {
			return this.actualEntry.hashCode();
		}
		
		@Override 
		public String toString() {
			return this.actualEntry.toString();
		}
	}
}
