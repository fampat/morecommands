package com.mrnobody.morecommands.settings;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mrnobody.morecommands.event.EventHandler;
import com.mrnobody.morecommands.event.Listeners.TwoEventListener;
import com.mrnobody.morecommands.util.DummyCommand;
import com.mrnobody.morecommands.util.Reference;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.world.WorldEvent;

/**
 * A class containing global settings
 * 
 * @author MrNobody98
 *
 */
public final class GlobalSettings implements TwoEventListener<WorldEvent.Load, WorldEvent.Unload> {
	/** A list of global settings */
	public static final ImmutableSet<String> GLOBAL_SETTINGS = ImmutableSet.of("aliases", "variables");
	
	private SettingsManager manager;
	private static GlobalSettings instance;
	
	/** This map maps a pair whose left value represents a world name and whose right value represents a dimension name to the actual settings map */
	public final Map<Pair<String, String>, MergedMappedSettings<String, String>> aliases = Maps.newHashMap();
	/** This map maps a pair whose left value represents a world name and whose right value represents a dimension name to the actual settings map */
	public final Map<Pair<String, String>, MergedMappedSettings<String, String>> variables = Maps.newHashMap();
	
	/**
	 * Creates a GlobalSettings intance if neccessary and returns it
	 */
	public static GlobalSettings getInstance() {
		if (instance == null) instance = new GlobalSettings();
		return instance;
	}
	
	private GlobalSettings() {
		EventHandler.LOAD_WORLD.registerFirst(this);
		EventHandler.UNLOAD_WORLD.registerSecond(this);
		
		this.manager = new JsonSettingsManager(new File(Reference.getModDir(), "settings_global.json"), false, false);
		this.manager.loadSettings();
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				GlobalSettings.this.manager.saveSettings();
			}
		}));
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
	public boolean setPutProperties(String type, boolean world, boolean dim) {
		if (!"variables".equals(type) && !"aliases".equals(type)) return false;
		Map<Pair<String, String>, MergedMappedSettings<String, String>> map = "variables".equals(type) ? this.variables : this.aliases;
		
		for (Map.Entry<Pair<String, String>, MergedMappedSettings<String, String>> entry : map.entrySet()) {
			Map<SettingsProperty, Set<String>> putProps = Maps.newEnumMap(SettingsProperty.class);
			
			if (world) putProps.put(SettingsProperty.WORLD_PROPERTY, Sets.newHashSet(entry.getKey().getLeft()));
			if (dim) putProps.put(SettingsProperty.DIMENSION_POPERTY, Sets.newHashSet(entry.getKey().getRight()));
			
			entry.getValue().setPutSetting(new Setting<Map<String, String>>(
					Maps.<String, String>newHashMap(), 
					putProps));
		}
		
		return true;
	}
	
	/**
	 * Invoked when a new world loads. Loads the settings for this world from the settings manager
	 */
	@Override
	public void onEvent1(WorldEvent.Load event) {
		if (event.world.isRemote) return;
		
		loadSetting("aliases", String.class, this.aliases, event.world.getSaveHandler().getWorldDirectoryName(), event.world.provider.getDimensionName());
		loadSetting("variables", String.class, this.variables, event.world.getSaveHandler().getWorldDirectoryName(), event.world.provider.getDimensionName());
	}

	/**
	 * Invoked when a new world unloads. Stores the settings for this in the settings manager.
	 */
	@Override
	public void onEvent2(WorldEvent.Unload event) {
		if (event.world.isRemote) return;
		Pair<String, String> pair = ImmutablePair.of(event.world.getSaveHandler().getWorldDirectoryName(), event.world.provider.getDimensionName());
		
		this.aliases.remove(pair);
		this.variables.remove(pair);
	}
	
	/**
	 * Loads a global setting
	 * 
	 * @param setting the setting name
	 * @param classOfT the setting's type class
	 * @param map the map to store the settings into
	 * @param world the world name
	 * @param dim the dimension name
	 */
	private <T> void loadSetting(String setting, Class<T> classOfT, Map<Pair<String, String>, MergedMappedSettings<String, T>> map, String world, String dim) {
		Pair<String, String> pair = ImmutablePair.of(world, dim);
		
		Map<SettingsProperty, String> props = Maps.newEnumMap(SettingsProperty.class);
		props.put(SettingsProperty.WORLD_PROPERTY, world);
		props.put(SettingsProperty.DIMENSION_POPERTY, dim);
		
		map.put(pair, this.manager.getMergedMappedSettings(setting, props, Maps.<SettingsProperty, String>newEnumMap(SettingsProperty.class), classOfT));
		
		if (setting.equals("aliases"))
			registerGlobalAliases((MergedMappedSettings<String, String>) map.get(pair));
	}
	
	/**
	 * Registers global aliases to the command handler
	 */
	private void registerGlobalAliases(MergedMappedSettings<String, String> aliases) {
		net.minecraft.command.CommandHandler commandHandler = (net.minecraft.command.CommandHandler) MinecraftServer.getServer().getCommandManager();
		
		for (Map.Entry<String, String> entry: aliases.entrySet()) {
			if (!entry.getValue().equalsIgnoreCase(entry.getKey()) && !commandHandler.getCommands().containsKey(entry.getKey())) {
				DummyCommand cmd = new DummyCommand(entry.getKey(), false);
				commandHandler.getCommands().put(entry.getKey(), cmd);
			}
		}
	}
}
