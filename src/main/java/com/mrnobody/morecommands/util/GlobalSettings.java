package com.mrnobody.morecommands.util;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.mrnobody.morecommands.event.EventHandler;
import com.mrnobody.morecommands.event.Listeners.EventListener;
import com.mrnobody.morecommands.util.SettingsManager.Setting;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.world.WorldEvent;

/**
 * A class containing global settings
 * 
 * @author MrNobody98
 *
 */
public final class GlobalSettings implements EventListener<WorldEvent.Load> {
	/** The global command settings */
	public static final ImmutableSet<String> GLOBAL_SETTINGS = ImmutableSet.of("aliases", "variables");
	
	private static GlobalSettings INSTANCE;
	
	/**
	 * initializes the global command settings
	 */
	public static void init() {
		INSTANCE = new GlobalSettings();
	}
	
	private SettingsManager manager = new JsonSettingsManager(new File(Reference.getModDir(), "settings_global.json"), false, false);
	
	private GlobalSettings() {
		EventHandler.LOAD_WORLD.register(this);
		this.manager.loadSettings();
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				store(aliases, "aliases", String.class);
				store(variables, "variables", String.class);
				INSTANCE.manager.saveSettings();
			}
		}));
	}
	
	@Override
	public void onEvent(WorldEvent.Load event) {
		if (event.getWorld().isRemote) return;
		
		put(aliases, this.manager.getMappedSettings("aliases", null, event.getWorld().getMinecraftServer().getFolderName(), event.getWorld().provider.getDimensionType().getName(), String.class));
		put(variables, this.manager.getMappedSettings("variables", null, event.getWorld().getMinecraftServer().getFolderName(), event.getWorld().provider.getDimensionType().getName(), String.class));
		registerGlobalAliases(event.getWorld().getMinecraftServer());
	}
	
	/**
	 * Registers global aliases to the command handler
	 */
	private static void registerGlobalAliases(MinecraftServer server) {
		net.minecraft.command.CommandHandler commandHandler = (net.minecraft.command.CommandHandler) server.getCommandManager();
		
		for (Map.Entry<String, String> entry: GlobalSettings.getAllAliases().entrySet()) {
			if (!entry.getValue().equalsIgnoreCase(entry.getKey()) && !commandHandler.getCommands().containsKey(entry.getKey())) {
				DummyCommand cmd = new DummyCommand(entry.getKey(), false);
				commandHandler.getCommands().put(entry.getKey(), cmd);
			}
		}
	}
	
	/**
	 * Stores a "save properties<->settings map" map in the settings manager
	 * 
	 * @param <T> the setting's type
	 * @param map the settings map
	 * @param type the setting's name
	 * @param classOfT the setting's type class
	 */
	private static <T> void store(Map<Pair<String, String>, Map<String, T>> map, String type, Class<T> classOfT) {
		for (Map.Entry<Pair<String, String>, Map<String, T>> entry : map.entrySet()) {
			INSTANCE.manager.removeMappedSettings(type, null, true, null, entry.getKey().getLeft(), entry.getKey().getRight());
			INSTANCE.manager.storeMappedSettings(type, entry.getValue(), null, entry.getKey().getLeft(), entry.getKey().getRight(), classOfT);
		}
	}
	
	/**
	 * Puts the settings that are read from the settings manager into a "save properties<->settings map" map
	 * 
	 * @param <T> the the value type of the settings map
	 * @param map the map into that the settings are put
	 * @param settings the settings that are read from the settings manager
	 */
	private static <T> void put(Map<Pair<String, String>, Map<String, T>> map, List<Setting<Map<String, T>>> settings) {
		Pair<String, String> nullPair = ImmutablePair.<String, String>of(null, null);
		
		for (Setting<Map<String, T>> setting : settings) {
			if (setting.getWorld().isEmpty() && setting.getDim().isEmpty()) {
				if (!map.containsKey(nullPair)) map.put(nullPair, Maps.<String, T>newHashMap());
				map.get(nullPair).putAll(setting.getValue());
			}
			else if (setting.getDim().isEmpty()) {
				for (String world : setting.getWorld()) {
					Pair<String, String> pair = ImmutablePair.<String, String>of(world, null);
					if (!map.containsKey(pair)) map.put(pair, Maps.<String, T>newHashMap());
					map.get(pair).putAll(setting.getValue());
				}
			}
			else if (setting.getWorld().isEmpty()) {
				for (String dim : setting.getDim()) {
					Pair<String, String> pair = ImmutablePair.<String, String>of(null, dim);
					if (!map.containsKey(pair)) map.put(pair, Maps.<String, T>newHashMap());
					map.get(pair).putAll(setting.getValue());
				}
			}
			else {
				for (String world : setting.getWorld()) {
					for (String dim : setting.getDim()) {
						Pair<String, String> pair = ImmutablePair.<String, String>of(world, dim);
						if (!map.containsKey(pair)) map.put(pair, Maps.<String, T>newHashMap());
						map.get(pair).putAll(setting.getValue());
					}
				}
			}
		}
	}
	
	/**
	 * Gets the actual settings map from a "save propertyies<->settings map" map
	 * 
	 * @param map the "save property<->settings map" map
	 * @param world the folder name of the world for which the settings should be loaded
	 * @param dim the dimension for which the settings map should be loaded
	 * @return the settings map
	 */
	private static <T> Map<String, T> get(Map<Pair<String, String>, Map<String, T>> map, String world, String dim) {
		int size = 0;
		Map<String, T> map1 = map.get(ImmutablePair.<String, String>of(null, null));   if (map1 != null) size += map1.size();
		Map<String, T> map2 = map.get(ImmutablePair.<String, String>of(null, dim));    if (map2 != null) size += map2.size();
		Map<String, T> map3 = map.get(ImmutablePair.<String, String>of(world, null));  if (map3 != null) size += map3.size();
		Map<String, T> map4 = map.get(ImmutablePair.<String, String>of(world, dim));   if (map4 != null) size += map4.size();
		
		Map<String, T> finalMap = Maps.newHashMapWithExpectedSize(size);
		if (map1 != null) finalMap.putAll(map1); if (map2 != null) finalMap.putAll(map2);
		if (map3 != null) finalMap.putAll(map3); if (map4 != null) finalMap.putAll(map4);
		
		return finalMap;
	}
	
	/**
	 * Removes an entry from a settings map
	 * 
	 * @param map the "save properties<->settings map" map
	 * @param world the world folder name for which the entry should be removed
	 * @param dim the dimension name for which the entry should be removed
	 * @param key the key of the entry that should be removed
	 */
	private static <T> void remove(Map<Pair<String, String>, Map<String, T>> map, String world, String dim, String key) {
		Map<String, T> map1 = map.get(ImmutablePair.<String, String>of(null, null));
		Map<String, T> map2 = map.get(ImmutablePair.<String, String>of(null, dim));
		Map<String, T> map3 = map.get(ImmutablePair.<String, String>of(world, null));
		Map<String, T> map4 = map.get(ImmutablePair.<String, String>of(world, dim));
		
		if (key == null) {
			if (map4 != null) map4.clear(); else if (map3 != null) map3.clear();
			else if (map2 != null) map2.clear(); else if (map1 != null) map1.clear();
		}
		else {
			if (map4 != null && map4.containsKey(key)) map4.remove(key); else if (map3 != null && map3.containsKey(key)) map3.remove(key);
			else if (map2 != null && map2.containsKey(key)) map2.remove(key); else if (map1 != null && map1.containsKey(key)) map1.remove(key);
		}
	}
	
	private static final Map<String, Pair<Boolean, Boolean>> saveProps = Maps.newHashMap();
	
	/**
	 * Sets whether to save a setting depending on the world's name and/or
	 * on the dimension name by default.
	 * 
	 * @param type the setting's name
	 * @param worldDependent whether to save depending on the world name by default
	 * @param dimDependendent whether to save depending on the dimension name by default
	 */
	public static void putSaveProp(String type, boolean worldDependent, boolean dimDependendent) {
		saveProps.put(type, ImmutablePair.of(worldDependent, dimDependendent));
	}
	
	/**
	 * Gets a {@link Pair} representing the save properties of a setting. The left value indicates whether to save
	 * depending on the world name, the right value indicates whether to save depending on the dimension name
	 * 
	 * @param type the setting's name
	 * @return a {@link Pair} representing the save properties for this setting
	 */
	public static Pair<Boolean, Boolean> getSaveProp(String type) {
		return saveProps.containsKey(type) ? saveProps.get(type) : ImmutablePair.of(false, false);
	}
	
	private static final Map<Pair<String, String>, Map<String, String>> aliases = Maps.newHashMap();
	private static final Map<Pair<String, String>, Map<String, String>> variables = Maps.newHashMap();
	
	/**
	 * @return all global aliases
	 */
	public static Map<String, String> getAllAliases() {
		Map<String, String> allAliases = Maps.newHashMap();
		for (Map<String, String> val : aliases.values()) allAliases.putAll(val);
		return allAliases;
	}
	
	/**
	 * Returns all aliases that are valid on the given world and dimension.
	 * 
	 * @param world the world name
	 * @param dim the dimension name
	 * @return the aliases that are valid for the given world and dimension
	 */
	public static Map<String, String> getAliases(String world, String dim) {
		return get(aliases, world, dim);
	}
	
	/**
	 * Creates a global alias that is valid only on the given world and on the given
	 * dimension. Using null allows every world/dimension
	 * 
	 * @param world the world that this alias requires
	 * @param dim the dimension this alias requires
	 * @param alias the alias
	 * @param command the actual command of this alias
	 */
	public static void putAlias(String world, String dim, String alias, String command) {
		Pair<String, String> pair = ImmutablePair.<String, String>of(world, dim);
		if (!aliases.containsKey(pair)) aliases.put(pair, Maps.<String, String>newHashMap());
		aliases.get(pair).put(alias, command);
	}
	
	/**
	 * Removes an alias that depends on the given world and on the given dimension.
	 * 
	 * @param world the world that the given alias depends on
	 * @param dim the dimension that the given alias depends on
	 * @param alias the alias to remove
	 */
	public static void removeAlias(String world, String dim, String alias) {
		remove(aliases, world, dim, alias);
	}
	
	/**
	 * Returns all variables that are valid on the given world and dimension
	 * 
	 * @param world the world name
	 * @param dim the dimension name
	 * @return the variables that are valid for the given world and dimension
	 */
	public static Map<String, String> getVariables(String world, String dim) {
		return get(variables, world, dim);
	}
	
	/**
	 * Creates a global variable that is valid only on the given world and on the given
	 * dimension. Using null allows every world/dimension
	 * 
	 * @param world the world that this variable requires
	 * @param dim the dimension this variable requires
	 * @param variable the variable
	 * @param value the actual content of the variable
	 */
	public static void putVariable(String world, String dim, String variable, String value) {
		Pair<String, String> pair = ImmutablePair.<String, String>of(world, dim);
		if (!variables.containsKey(pair)) variables.put(pair, Maps.<String, String>newHashMap());
		variables.get(pair).put(variable, value);
	}
	
	/**
	 * Removes a variable that depends on the given world and on the given dimension.
	 * 
	 * @param world the world that the given variable depends on
	 * @param dim the dimension that the given variable depends on
	 * @param alias the variable to remove
	 */
	public static void removeVariable(String world, String dim, String var) {
		remove(variables, world, dim, var);
	}
	
	/**
	 * Removes all variables that depend on the given world and dimension
	 * 
	 * @param world the world that the variables depend on
	 * @param dim the dimension that the variables depend on
	 */
	public static void removeAllVariables(String world, String dim) {
		remove(variables, world, dim, null);
	}
	
	/** Whether to send a welcome message in the chat when a player joins */
	public static boolean welcome_message = true;
	/** Whether to retry handshake if they failed */
	public static boolean retryHandshake = true;
	/** The timeout to wait for a handshake before startup commands are executed (in seconds) */
	public static int startupTimeout = 10;
	/** The timeout after which a handshake should be retried (in seconds) */
	public static int handshakeTimeout = 3;
	/** How often a handshake should be retried */
	public static int handshakeRetries = 3;
	/** Whether MoreCommands should look for updates */
	public static boolean searchUpdates = true;
	/** The update rate for xray (in seconds) */
	public static int xrayUPS = 1;
	/** Whether to use the regex calc parser for parsing calculations. See {@link CalculationParser} */
	public static boolean useRegexCalcParser = true;
	
	/** Whether to enable player variables */
	public static boolean enablePlayerVars = true, enablePlayerVarsOriginal = true;
	/** Whether to enable global variables */
	public static boolean enableGlobalVars = true;
	/** Whether to enable player aliases */
	public static boolean enablePlayerAliases = true, enablePlayerAliasesOriginal = true;
	/** Whether to enable global aliases */
	public static boolean enableGlobalAliases = true;
	
	/** Whether to allow creepers to explode */
	public static boolean creeperExplosion = true;
	/** Whether to allow item drops */
	public static boolean dodrops = true;
	/** Whether to allow endermen to pickup blocks */
	public static boolean endermanpickup = true;
	/** Whether to allow endermen to teleport */
	public static boolean endermanteleport = true;
	/** Whether to allow any kind of explosion */
	public static boolean explosions = true;
	
	/** Whether ALL clients are required to have MoreCommands installed */
	public static boolean clientMustHaveMod = false;
	/** Whether the server is required to have MoreCommands installed */
	public static boolean serverMustHaveMod = false;
	
	/** A map that maps a command name to a permission level (works only for MoreCommands server commands, client commands do always have permission level 0) */
	public static final Map<String, Integer> permissionMapping = new HashMap<String, Integer>();
	
	/**
	 * Reads settings and permissions from the settings file except for aliases and variables
	 * (The are read automatically as soon as {@link GlobalSettings#init()} was called)
	 */
	public static void readSettings() {
		Settings settings = new Settings(new File(Reference.getModDir(), "config.cfg"), true);
		
		GlobalSettings.welcome_message = settings.getBoolean("welcome_message", true);
		GlobalSettings.enablePlayerVars = GlobalSettings.enablePlayerVarsOriginal = settings.getBoolean("enablePlayerVars", true);
		GlobalSettings.enableGlobalVars = settings.getBoolean("enableGlobalVars", true);
		GlobalSettings.enablePlayerAliases = GlobalSettings.enablePlayerAliasesOriginal = settings.getBoolean("enablePlayerAliases", true);
		GlobalSettings.enableGlobalAliases = settings.getBoolean("enableGlobalAliases", true);
		GlobalSettings.retryHandshake = settings.getBoolean("retryHandshake", true);
		GlobalSettings.startupTimeout = settings.getInteger("startupTimeout", 10);
		GlobalSettings.handshakeTimeout = settings.getInteger("handshakeTimeout", 3);
		GlobalSettings.handshakeRetries = settings.getInteger("handshakeRetries", 3);
		GlobalSettings.searchUpdates = settings.getBoolean("searchUpdates", true);
		GlobalSettings.xrayUPS = settings.getInteger("xrayUPS", 1);
		GlobalSettings.useRegexCalcParser = settings.getBoolean("useRegexCalcParser", true);
		GlobalSettings.clientMustHaveMod = settings.getBoolean("clientMustHaveMod", false);
		GlobalSettings.serverMustHaveMod = settings.getBoolean("serverMustHaveMod", false);
		
		Settings permissions = new Settings(new File(Reference.getModDir(), "permissions.cfg"), true);
		
		for (Map.Entry<Object, Object> entry : permissions.entrySet()) {
			if (entry.getKey() instanceof String && entry.getValue() instanceof String) {
				int perm = MathHelper.parseIntWithDefault((String) entry.getValue(), -1);
				permissionMapping.put((String) entry.getKey(), perm < 0 ? 0 : perm > 4 ? 4 : perm);
			}
		}
	}
	
	/**
	 * Writes settings to the settings file except for aliases and variables
	 * (The are written automatically as soon as {@link GlobalSettings#init()} was called)
	 */
	public static void writeSettings() {
		Settings settings = new Settings(new File(Reference.getModDir(), "config.cfg"), true);
		
		settings.set("welcome_message", GlobalSettings.welcome_message);
		settings.set("enablePlayerVars", GlobalSettings.enablePlayerVarsOriginal);
		settings.set("enableGlobalVars", GlobalSettings.enableGlobalVars);
		settings.set("enablePlayerAliases", GlobalSettings.enablePlayerAliasesOriginal);
		settings.set("enableGlobalAliases", GlobalSettings.enableGlobalAliases);
		settings.set("retryHandshake", GlobalSettings.retryHandshake);
		settings.set("startupTimeout", GlobalSettings.startupTimeout);
		settings.set("handshakeTimeout", GlobalSettings.handshakeTimeout);
		settings.set("handshakeRetries", GlobalSettings.handshakeRetries);
		settings.set("searchUpdates", GlobalSettings.searchUpdates);
		settings.set("xrayUPS", GlobalSettings.xrayUPS);
		settings.set("useRegexCalcParser", GlobalSettings.useRegexCalcParser);
		settings.set("clientMustHaveMod", GlobalSettings.clientMustHaveMod);
		settings.set("serverMustHaveMod", GlobalSettings.serverMustHaveMod);
		
		settings.save();
		
		Settings permissions = new Settings(new File(Reference.getModDir(), "permissions.cfg"), true);
		
		for (Map.Entry<String, Integer> entry : permissionMapping.entrySet()) { 
			permissions.set(entry.getKey(), entry.getValue());
		}
		
		permissions.save();
	}
}
