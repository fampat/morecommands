package com.mrnobody.morecommands.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.mrnobody.morecommands.core.MoreCommands;

/**
 * This {@link SettingsManager} implementation reads and writes settings
 * from files using the JSON format.
 * 
 * The (de)serialization process is implemented by {@link SettingsSerializable}.
 * Because {@link SettingsSerializable} works with {@link AbstractElement}s, the
 * {@link JsonElement}s read from the settings file will be converted to
 * {@link AbstractElement}s.<br><br>
 * 
 * A {@link JsonElement} corresponds to an {@link AbstractElement}.<br>
 * A {@link JsonObject} corresponds to a {@link ObjectElement}.<br>
 * A {@link JsonArray} corresponds to a {@link ListElement}.<br>
 * A {@link JsonPrimitive} corresponds to a {@link StringElement} if it contains a String
 * else if it contains a Number, it corresponds to a {@link NumericElement}.
 * 
 * @author MrNobody98
 */
public class JsonSettingsManager extends SettingsManager {
	/**
	 * Converts a {@link JsonElement} into an {@link AbstractElement}
	 * 
	 * @param element the {@link JsonElement} to convert
	 * @return the converted {@link AbstractElement}
	 */
	public static AbstractElement toAbstractElement(JsonElement element) {
		if (element.isJsonArray()) {
			ListElement e = new ListElement();
			for (JsonElement elem : element.getAsJsonArray()) e.add(toAbstractElement(elem));
			return e;
		}
		else if (element.isJsonObject()) {
			ObjectElement e = new ObjectElement();
			for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) e.add(entry.getKey(), toAbstractElement(entry.getValue()));
			return e;
		}
		else if (element.isJsonPrimitive()) {
			if (element.getAsJsonPrimitive().isString()) return new StringElement(element.getAsString());
			else if (element.getAsJsonPrimitive().isNumber()) return new NumericElement(element.getAsNumber());
			else return null;
		}
		else return null;
	}
	
	/**
	 * Converts a {@link AbstractElement} into an {@link JsonElement}
	 * 
	 * @param element the {@link AbstractElement} to convert
	 * @return the converted {@link JsonElement}
	 */
	public static JsonElement toJsonElement(AbstractElement element) {
		if (element.isList()) {
			JsonArray e = new JsonArray();
			for (AbstractElement elem : element.asList()) e.add(toJsonElement(elem));
			return e;
		}
		else if (element.isObject()) {
			JsonObject e = new JsonObject();
			for (Map.Entry<String, AbstractElement> entry : element.asObject().entrySet()) e.add(entry.getKey(), toJsonElement(entry.getValue()));
			return e;
		}
		else if (element.isNumeric()) return new JsonPrimitive(element.asNumericElement().asNumber());
		else if (element.isString()) return new JsonPrimitive(element.asStringElement().asString());
		else return JsonNull.INSTANCE;
	}
	
	private boolean failed = false, loaded = false;
	
	/**
	 * Constructs a new JsonSettingsManager
	 * 
	 * @param file the file from which settings are read and to which settings are written. Must be in JSON format
	 * @param load whether to read the settings immediately
	 */
	public JsonSettingsManager(File file) {
		this(file, false);
	}
	
	/**
	 * Constructs a new JsonSettingsManager
	 * 
	 * @param file the file from which settings are read and to which settings are written. Must be in JSON format
	 * @param load whether to read the settings immediately
	 */
	public JsonSettingsManager(File file, boolean load) {
		this(file, load, true);
	}
	
	/**
	 * Constructs a new JsonSettingsManager
	 * 
	 * @param file the file from which settings are read and to which settings are written. Must be in JSON format
	 * @param load whether to read the settings immediately
	 * @param useServer whether to read and write server dependencies of settings (See {@link Setting} for more details)
	 */
	public JsonSettingsManager(File file, boolean load, boolean useServer) {
		super(file, load, useServer);
	}
	
	/**
	 * Constructs a new JsonSettingsManager
	 * 
	 * @param file the file from which settings are read and to which settings are written. Must be in JSON format
	 * @param load whether to read the settings immediately
	 * @param defaultSerializable the default (de)serializer used if a {@link Setting} occurs for which 
	 *        a {@link Serializable} has not been registered
	 * @param useServer whether to read and write server dependencies of settings (See {@link Setting} for more details)
	 */
	public JsonSettingsManager(File file, boolean load, Serializable<Object> defaultSerializable, boolean useServer) {
		super(file, load, defaultSerializable, useServer);
	}
	
	@Override
	public synchronized void loadSettings() {
		JsonReader reader = null;
		this.loaded = true;
		this.failed = false;
		
		try {
			if (!this.file.exists() || !this.file.isFile()) return;
			
			JsonParser p = new JsonParser();
			JsonElement root = p.parse(reader = new JsonReader(new InputStreamReader(new FileInputStream(this.file))));
			
			this.settings = this.serializable.deserialize(toAbstractElement(root));
		}
		catch (IOException ex) {MoreCommands.INSTANCE.getLogger().info("Error reading command config"); this.failed = true;}
		catch (JsonIOException ex) {MoreCommands.INSTANCE.getLogger().info("Error reading command config"); this.failed = true;}
		catch (JsonSyntaxException ex)  {MoreCommands.INSTANCE.getLogger().info("Invalid syntax in command config file: " + ex.getMessage()); this.failed = true;}
		finally {try {if (reader != null) reader.close();} catch (IOException ex) {}}
	}
	
	@Override
	public synchronized void saveSettings() {
		//if no settings have been loaded, writing them will result in an empty file and all settings are gone
		if (!this.loaded) return;
		
		if (this.failed) {
			MoreCommands.INSTANCE.getLogger().info("Command Config won't be saved, because reading failed");
			return;
		}
		
		String out = new GsonBuilder().setPrettyPrinting().create().toJson(toJsonElement(this.serializable.serialize(this.settings)));
		OutputStreamWriter w = null;
		
		try {
			if (!this.file.exists() || !this.file.isFile()) this.file.createNewFile();
			w = new OutputStreamWriter(new FileOutputStream(this.file), "UTF-8");
			
			w.write(out); w.flush(); w.close();
		}
		catch (IOException ex) {MoreCommands.INSTANCE.getLogger().info("Error saving command config");}
		finally {try {if (w != null) w.close();} catch (IOException ex) {}}
	}
	
	@Override
	public boolean isLoaded() {
		return this.loaded;
	}
}
