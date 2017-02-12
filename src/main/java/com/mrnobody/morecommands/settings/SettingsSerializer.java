package com.mrnobody.morecommands.settings;

import com.google.gson.JsonElement;

/**
 * An interface for (de)serializing values from and to
 * a {@link JsonElement}
 * 
 * @author MrNobody98
 * @param <T> the type this Serializer (de)serializes
 */
public interface SettingsSerializer<T> {
	/**
	 * Deserializes a value of type T from an {@link JsonElement}
	 * 
	 * @param element the {@link JsonElement} used to deserialize the value
	 * @return the deserialized value
	 */
	public T deserialize(JsonElement element);
	
	/**
	 * Serializes a value of type T to an {@link JsonElement}
	 * 
	 * @param element the element to serialize
	 * @return the serialized element
	 */
	public JsonElement serialize(T element);
	
	/**
	 * @return The class of the type this Serializable (de)serializes
	 */
	public Class<T> getTypeClass();
}
