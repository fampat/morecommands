package com.mrnobody.morecommands.settings;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Implements the actual (de)serializing process of a {@link SettingsManager}
 * 
 * @author MrNobody98
 */
class RootSettingsSerializer implements SettingsSerializer<SetMultimap<String, Setting<?>>> {
	private static final Map<String, SettingsSerializer<?>> serializers = new HashMap<String, SettingsSerializer<?>>();
	
	/**
	 * Registers a (de)serializer for a setting
	 * 
	 * @param name the setting name
	 * @param serializer the (de)serializer
	 */
	static final <T> void registerSerializer(String name, SettingsSerializer<T> serializer) {
		serializers.put(name, serializer);
	}
	
	/**
	 * @param arr the {@link JsonArray}
	 * @return a Set of Strings contained in the given {@link JsonArray}
	 */
	private static final Set<String> toStringSet(JsonArray arr) {
		Set<String> list = Sets.newHashSet();
		
		for (int i = 0; i < arr.size(); i++)
			if (arr.get(i).isJsonPrimitive())
				list.add(arr.get(i).getAsString());
		
		return list;
	}
	
	/**
	 * @param list the iterable to convert
	 * @return A {@link JsonArray} containing the elements of the given iterable
	 */
	private static final JsonArray toStringArray(Iterable<String> list) {
		JsonArray arr = new JsonArray();
		for (String s : list) arr.add(new JsonPrimitive(s));
		return arr;
	}
	
	/**
	 * This is the default (de)serializer for values of settings which don't have
	 * a {@link SettingsSerializer} registered to them.<br>
	 * The default implementation (de)serializes Strings, Numbers, Lists/Arrays, Maps and Strings.
	 */
	private SettingsSerializer<Object> defaultSerializer = new SettingsSerializer<Object>() {
		@Override
		public Object deserialize(JsonElement element) {
			if (element == null) return null;
			else if (element.isJsonPrimitive()) {
				if (element.getAsJsonPrimitive().isNumber()) return element.getAsNumber();
				else return element.getAsString();
			}
			else if (element.isJsonArray()) {
	    		List<Object> list = Lists.newArrayList();
	    		for (JsonElement e : element.getAsJsonArray()) list.add(deserialize(e));
	    		return list;
	    	}
			else if (element.isJsonObject()) {
				Map<String, Object> map = Maps.newHashMap();
				for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) map.put(entry.getKey(), deserialize(entry.getValue()));
				return map;
			}
	    	else return null;
		}
		
		@Override
		public JsonElement serialize(Object obj) {
			if (obj instanceof Number) return new JsonPrimitive((Number) obj);
	    	else if (obj instanceof String) return new JsonPrimitive((String) obj);
	    	else if (obj != null && obj.getClass().isArray()) {
	    		JsonArray e = new JsonArray();
	    		for (int i = 0; i < Array.getLength(obj); i++)  e.add(serialize(Array.get(obj, i)));
	    		return e;
	    	}
	    	else if (obj instanceof List<?>) {
	    		List<?> list = (List<?>) obj;
	    		JsonArray l = new JsonArray();
	    		
	    		for (Object o : list) {
	    			JsonElement serialized = serialize(o);
	    			if (serialized != null) l.add(serialized);
	    		}
	    		
	    		return l;
	    	}
	    	else if (obj instanceof Map<?, ?>) {
	    		Map<?, ?> map = (Map<?, ?>) obj;
	    		JsonObject e = new JsonObject();
	    		
	    		for (Map.Entry<?, ?> entry : map.entrySet()) {
	    			if (!(entry.getKey() instanceof String)) continue;
	    			JsonElement serialized = serialize(entry.getValue());
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
	private boolean isClient = true;
	
	/** Constructs a new {@link RootSettingsSerializer} */
	RootSettingsSerializer() {}
	
	/**
	 * Constructs a new {@link RootSettingsSerializer}
	 * @param isClient whether client properties should be serialized
	 */
	RootSettingsSerializer(boolean isClient) {
		this.isClient = isClient;
	}
	
	/**
	 * Constructs a new {@link RootSettingsSerializer}
	 * @param defaultSerializer the default (de)serializer for values of {@link Setting}s which don't have a
	 *        {@link SettingsSerializer} registered to them
	 * @param isClient whether client properties should be serialized
	 */
	RootSettingsSerializer(SettingsSerializer<Object> defaultSerializer, boolean isClient) {
		this.defaultSerializer = defaultSerializer;
		this.isClient = isClient;
	}
	
	/**
	 * Deserializes a {@link JsonElement} that contains settings
	 * into a {@link SetMultimap} that contains settings
	 * @see RootSettingsSerializer
	 */
	public SetMultimap<String, Setting<?>> deserialize(JsonElement element) {
		if (element == null || !element.isJsonObject()) return HashMultimap.create();
		JsonObject obj = element.getAsJsonObject();
		SetMultimap<String, Setting<?>> settings = HashMultimap.create();
		
		for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
			SettingsSerializer<?> deserializer = serializers.get(entry.getKey());
			JsonElement val = entry.getValue();
			
			if (!val.isJsonArray()) {
				JsonElement tmp = val;
				val = new JsonArray();
				val.getAsJsonArray().add(tmp);
			}
			
			for (JsonElement elem : val.getAsJsonArray()) {
				if (!elem.isJsonObject()) {
					Object value = deserializer == null ? this.defaultSerializer.deserialize(elem) : deserializer.deserialize(elem);
					settings.put(entry.getKey(), new Setting<Object>(value, new EnumMap<SettingsProperty, Set<String>>(SettingsProperty.class)));
				}
				else {
					JsonObject setting = elem.getAsJsonObject();
					
					Map<SettingsProperty, Set<String>> properties = Maps.newEnumMap(SettingsProperty.class);
					JsonElement val2 = setting.remove("value");
					JsonElement allowMerge = setting.remove("allowMerge");
					JsonElement priority = setting.remove("priority");
					
					if (val2 == null) continue;
					
					for (SettingsProperty property : SettingsProperty.values()) {
						if (property.isClientOnly() && !this.isClient) continue;
						
						JsonElement propValue = setting.get(property.getName());
						if (propValue == null) continue;
						
						Set<String> values = properties.get(property);
						if (values == null) properties.put(property, values = Sets.newHashSet());
						
						if (propValue.isJsonPrimitive())
							values.add(propValue.getAsString());
						else if (propValue.isJsonArray())
							values.addAll(toStringSet(propValue.getAsJsonArray()));
					}
					
					Object value = deserializer == null ? this.defaultSerializer.deserialize(val2) : deserializer.deserialize(val2);
					settings.put(entry.getKey(), new Setting<Object>(value, properties, priority == null ? -1 : 
						priority.isJsonPrimitive() && priority.getAsJsonPrimitive().isNumber() ? priority.getAsInt() : -1,
						allowMerge != null && allowMerge.isJsonPrimitive() && allowMerge.getAsJsonPrimitive().isBoolean() ? allowMerge.getAsBoolean() : true));
				}
			}
		}
		
		return settings;
	}
	
	/**
	 * Serializes a {@link SetMultimap} that contains settings
	 * into a {@link JsonElement} that contains settings
	 * @see RootSettingsSerializer
	 */
	public JsonElement serialize(SetMultimap<String, Setting<?>> src) {
		JsonObject finalObj = new JsonObject();
		if (src == null) return finalObj;
		
		for (String key : src.keySet()) {
			if (src.get(key).isEmpty()) continue;
			
			SettingsSerializer<?> serializer = serializers.get(key);
			Set<Setting<?>> set = src.get(key);
			JsonArray settings = new JsonArray();
			
			for (Setting<?> setting : set) {
				if (setting.getValue() == null || (setting.getValue() instanceof Collection<?> 
					&& ((Collection<?>) setting.getValue()).isEmpty()) || (setting.getValue() instanceof Map<?, ?> 
					&& ((Map<?, ?>) setting.getValue()).isEmpty()) || (setting.getValue().getClass().isArray() &&
						Array.getLength(setting.getValue()) == 0)) continue;
				
				JsonElement value; try {
				value = serializer == null || !serializer.getTypeClass().isInstance(setting.getValue()) ? 
				this.defaultSerializer.serialize(setting.getValue()) : ((SettingsSerializer) serializer).serialize(setting.getValue());
				} catch (ClassCastException ex) {value = this.defaultSerializer.serialize(setting.getValue());}
				
				if (value.isJsonObject() || value.isJsonArray() || !setting.getProperties().isEmpty()) {
					JsonObject obj = new JsonObject();
					
					for (Map.Entry<SettingsProperty, Set<String>> entry : setting.getProperties().entrySet()) {
						if (entry.getKey().isClientOnly() && !this.isClient) continue;
						
						if (!entry.getValue().isEmpty()) {
							if (entry.getValue().size() == 1)
								obj.add(entry.getKey().getName(), new JsonPrimitive(Iterables.get(entry.getValue(), 0)));
							else 
								obj.add(entry.getKey().getName(), toStringArray(entry.getValue()));
						}
					}
					
					if (setting.getCustomPriorityLevel() >= 0) 
						obj.add("priority", new JsonPrimitive(setting.getCustomPriorityLevel()));

					if (!setting.allowMerge())
						obj.add("allowMerge", new JsonPrimitive(false));
					
					obj.add("value", value);
					settings.add(obj);
				}
				else settings.add(value);
			}
			
			if (settings.size() == 1) finalObj.add(key, settings.get(0));
			else if (settings.size() != 0) finalObj.add(key, settings);
		}
		
		return finalObj;
	}
	
	@Override
	public Class<SetMultimap<String, Setting<?>>> getTypeClass() {
		return (Class<SetMultimap<String, Setting<?>>>) (Class<?>) SetMultimap.class;
	}
}
