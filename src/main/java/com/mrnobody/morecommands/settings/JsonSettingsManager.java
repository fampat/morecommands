package com.mrnobody.morecommands.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
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
 * @author MrNobody98
 */
public class JsonSettingsManager extends SettingsManager {
	private boolean failed = false, loaded = false;
	private SetMultimap<String, Setting<?>> settings = HashMultimap.create();
	private final File file;
	
	/**
	 * Constructs a new JsonSettingsManager
	 * 
	 * @param file the file from which settings are read and to which settings are written. Must be in JSON format
	 */
	public JsonSettingsManager(File file) {
		this(file, false);
	}
	
	/**
	 * Constructs a new JsonSettingsManager
	 * 
	 * @param file the file from which settings are read and to which settings are written. Must be in JSON format
	 * @param isClient whether this is a client side settings manager
	 */
	public JsonSettingsManager(File file, boolean isClient) {
		this(file, isClient, false);
	}
	
	/**
	 * Constructs a new JsonSettingsManager
	 * 
	 * @param file the file from which settings are read and to which settings are written. Must be in JSON format
	 * @param isClient whether this is a client side settings manager
	 * @param load whether to read the settings immediately
	 */
	public JsonSettingsManager(File file, boolean isClient, boolean load) {
		super(isClient);
		this.file = file;
		if (load) loadSettings();
	}
	
	/**
	 * Constructs a new JsonSettingsManager
	 * 
	 * @param file the file from which settings are read and to which settings are written. Must be in JSON format
	 * @param isClient whether this is a client side settings manager
	 * @param defaultSerializer the default (de)serializer used if a {@link Setting} occurs for which 
	 *        a {@link SettingsSerializer} has not been registered
	 * @param load whether to read the settings immediately
	 */
	public JsonSettingsManager(File file, boolean isClient, SettingsSerializer<Object> defaultSerializer, boolean load) {
		super(defaultSerializer, isClient);
		this.file = file;
		if (load) loadSettings();
	}
	
	@Override
	protected SetMultimap<String, Setting<?>> getSettings() {
		return this.settings;
	}

	@Override
	protected void setSettings(SetMultimap<String, Setting<?>> settings) {
		this.settings = settings;
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
			this.deserializeSettings(root);
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
		
		String out = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(this.serializeSettings());
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
