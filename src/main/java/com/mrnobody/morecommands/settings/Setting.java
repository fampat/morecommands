package com.mrnobody.morecommands.settings;

import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Every kind of setting which is managed by a {@link SettingsManager}
 * is wrapped into this class. Every settings can depend on different 
 * {@link SettingsProperty}s.
 * <p>
 * Dependent means that this setting is only intended to be available in
 * an environment where the properties of the environment match the
 * properties of this setting.
 * </p><p>
 * For each {@link SettingsProperty} there can be multiple allowed
 * values. These values are stored in a set. If there is no mapping
 * from a property to such a set or the set is empty, this means that
 * every value is allowed.
 * </p><p>
 * Since all the settings objects returned by a settings manager are
 * stored in that manager, all changes made to a settings object are
 * reflected in the settings manager. This way you can avoid calls
 * to store()-Methods. It is recommended to use the {@link Setting}
 * objects directly instead of extracting the value.
 * </p><p>
 * <b>IMPORTANT:</b><br>
 * Generally it's recommended to use this class only as a wrapper or 
 * as an accessor for settings. This means that this class works as a "gate"
 * that forwards any change to the settings manager so that changes are
 * reflected in the settings manager. This class is not meant to be compared, 
 * so don't use any collections or other methods which rely on equality
 * especially in terms of the equality of the value. See the {@link #equals(Object)}
 * Method to get more details.
 * 
 * @author MrNobody98
 * @param <T> the type of the value this setting contains
 */
public final class Setting<T> {
	/**
	 * Compares two settings by their priority.<br>
	 * This works the following way:<p>
	 * 
	 * Let Setting one be s1 and Setting two be s2.<br>
	 * A negative return value means higher priority, a positive means lower priority, zero means equality.
	 * 
	 * <ol>
	 * <li>The {@link Setting#getPropertyPriorityLevel()} values of s1 and s2 are calculated<br>
	 *     If they are not equal, the setting with the higher value has a higher priority.
	 * <li>If this is not the case, then {@link Setting#getCustomPriorityLevel()} of s1 and s2 is calculated<br>
	 *     If this is not < 0 for at least one of these two settings, the the one setting with the higher value
	 *     has a higher priority.
	 * <li>If both of these values are < 0, there is a last comparison. For each {@link SettingsProperty},
	 *     the number of actual values for that property is compared. The setting with less values
	 *     has a higher priority.
	 * <li>If even this results in total equality the settings are finally considered equal.
	 * </ol>
	 */
	public static final Comparator<Setting<?>> PRIORITY_COMPARATOR = new Comparator<Setting<?>>() {
		@Override
		public int compare(Setting<?> s1, Setting<?> s2) {
			int levelDiff = s2.propPriorityLevel - s1.propPriorityLevel;
			
			if (levelDiff != 0) return levelDiff;
			else {
				if (s1.customPriorityLevel >= 0 || s2.customPriorityLevel >= 0) {
					if (s1.customPriorityLevel >= 0 && s2.customPriorityLevel >= 0) 
						return s2.customPriorityLevel - s1.customPriorityLevel;
					else if (s1.customPriorityLevel >= 0) 
						return -s1.customPriorityLevel;
					else 
						return s2.customPriorityLevel;
				}
				
				for (Map.Entry<SettingsProperty, Set<String>> entry : s1.properties.entrySet()) {
					Set<String> p1 = entry.getValue(), p2 = s2.properties.get(entry.getKey());
					
					if (p1.size() - p2.size() == 0) continue;
					else return p1.size() - p2.size();
				}
				
				return 0;
			}
		}
		
	};
	
	private T value;
	private final EnumMap<SettingsProperty, Set<String>> properties;
	private boolean allowMerge;
	private final int propPriorityLevel, customPriorityLevel;
	
	/**
	 * Constructs a new Setting object
	 * 
	 * @param value the setting value
	 * @param properties a map which maps a {@link SettingsProperty} to a set of allowed values
	 */
	public Setting(T value, Map<SettingsProperty, Set<String>> properties) {
		this(value, properties, -1, true);
	}
	
	/**
	 * Constructs a new Setting object
	 * 
	 * @param value the setting value
	 * @param properties a map which maps a {@link SettingsProperty} to a set of allowed values
	 * @param customPriorityLevel It is possible that two {@link Setting}s with different values
	 *                            can be allowed in the same environment. If this situation occurs
	 *                            this custom priority level is used to compare two settings and
	 *                            decide which value will be used.
	 */
	public Setting(T value, Map<SettingsProperty, Set<String>> properties, int customPriorityLevel) {
		this(value, properties, customPriorityLevel, true);
	}
	
	/**
	 * Constructs a new Setting object
	 * 
	 * @param value the setting value
	 * @param properties a map which maps a {@link SettingsProperty} to a set of allowed values
	 * @param customPriorityLevel It is possible that two {@link Setting}s with different values
	 *                            can be allowed in the same environment. If this situation occurs
	 *                            this custom priority level is used to compare two settings and
	 *                            decide which value will be used.
	 * @param allowMerge If this setting has a map as value, determines whether this settings allows merging with settings of lower priority
	 */
	public Setting(T value, Map<SettingsProperty, Set<String>> properties, int customPriorityLevel, boolean allowMerge) {
		this.value = value;
		this.properties = new EnumMap<SettingsProperty, Set<String>>(properties);
		this.propPriorityLevel = SettingsProperty.getPropertyBits(this.properties.keySet());
		this.customPriorityLevel = customPriorityLevel;
		
		this.properties.values().remove(Collections.EMPTY_SET);
	}
	
	/**
	 * @return The value of this setting. Be aware of the behavior described in {@link #setValue(Object)}
	 */
	public T getValue() {
		return this.value;
	}
	
	/**
	 * Sets the value.
	 * <p>
	 * <b>IMPORTANT</b>: It is not guaranteed that getValue() will afterwards
	 * return exactly the same object that was passed to this method. It is
	 * possible that the given value is wrapped in another type of the same
	 * interface or superclass. This is required for managing data properly,
	 * e.g. for {@link MergedMappedSettings}. However you can expect the
	 * actual stored value to behave exactly the same way as the given value.
	 * 
	 * @param value the value
	 */
	public void setValue(T value) {
		if (this.value instanceof ChangeNotifyingMap && value instanceof Map)
			((ChangeNotifyingMap) this.value).setBackingMap((Map) value);
		else
			this.value = value;
	}
	
	/**
	 * @return The properties which an environment must match for this setting to be valid in that environment
	 */
	public EnumMap<SettingsProperty, Set<String>> getProperties() {
		return new EnumMap<SettingsProperty, Set<String>>(this.properties);
	}
	
	/**
	 * @return The priority level defined by the {@link SettingsProperty}s
	 */
	public int getPropertyPriorityLevel() {
		return this.propPriorityLevel;
	}
	
	/**
	 * @return A custom priority level which decides which of two settings has a higher priority if the require the same {@link SettingsProperty}s
	 *         This may be -1 to indicate that no priority level has been set. Otherwise a higher value means higher priority.
	 */
	public int getCustomPriorityLevel() {
		return this.customPriorityLevel;
	}
	
	/**
	 * @return If this setting has a map as value, determines whether this settings allows merging with settings of lower priority
	 */
	public boolean allowMerge() {
		return this.allowMerge;
	}
	
	/**
	 * IMPORTANT: The equals method of a Setting does NOT check the equality of the values. This is required because
	 * otherwise a it would be possible to store settings with completely different values but exactly the same required properties 
	 * in a set of which every value should be unique in terms of required setting properties but not in terms of the value,
	 * which is the desired behavior for this class since it's not meant to be equal to the value but a container/accessor
	 * of the value. If you want to compare the values as well, use {@link #equalsIncludingValue(Object)}
	 * <p>
	 * Generally it's recommended to use this class only as a wrapper for settings. This means that this class works
	 * as a "gate" that forwards any change to the settings manager so that changes are reflected in the settings manager.
	 * This class is not meant to be compared, so don't use any collections or other methods which rely on equality
	 * especially in terms of the equality of the value.
	 */
	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		else if (!(o instanceof Setting)) return false;
		else return this.properties.equals(((Setting<?>) o).properties);
	}
	
	/**
	 * Tests whether an object is equal to this one including the value
	 * 
	 * @param o the other object
	 * @return true if equal
	 */
	public boolean equalsIncludingValue(Object o) {
		if (o == this) return true;
		else if (!(o instanceof Setting<?>)) return false;
		else {
			Setting<?> s = (Setting<?>) o;
			return this.properties.equals(s.properties) && (this.value == null ? s.value == null : this.value.equals(s.value));
		}
	}
	
	/**
	 * Returns a hashcode for this object. Be aware: This class will probably not behave how you
	 * want it to in a collection. Don't use it for such purposes.<br>
	 * See the {@link #equals(Object)} method for more details.
	 */
	@Override
	public int hashCode() {
		return this.properties.hashCode();
	}
	
	/**
	 * Returns a hashcode including the value
	 */
	public int hashCodeIncludingValue() {
		return this.properties.hashCode() + 17 * (this.value == null ? 0 : this.value.hashCode());
	}
	
	/**
	 * A string representation of this setting
	 */
	@Override
	public String toString() {
		return "(value: " + this.value + ", properties: " + this.properties + ")";
	}
	
	/**
	 * Directly sets the value
	 * @param value the value
	 */
	void setValueRaw(T value) {
		this.value = value;
	}
}
