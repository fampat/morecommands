package com.mrnobody.morecommands.settings;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.MutablePair;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.util.CalculationParser;
import com.mrnobody.morecommands.util.Reference;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectIntProcedure;
import net.minecraft.util.math.MathHelper;

/**
 * A class containing various configurations
 * 
 * @author MrNobody98
 *
 */
public final class MoreCommandsConfig {
	private MoreCommandsConfig() {}
	
	/** Whether to send a welcome message in the chat when a player joins */
	public static boolean welcome_message = true;
	/** Whether to retry handshakes if they failed */
	public static boolean retryHandshake = true;
	/** The timeout to wait for a handshake before startup commands are executed (in seconds) */
	public static int startupTimeout = 10;
	/** An artificial delay before startup commands are executed (in seconds) */
	public static int startupDelay;
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
	/** The timeout after which a remotely executed command must return a result (in milliseconds) */
	public static int remoteCommandsTimeout = 500;
	
	/** Whether to enable player variables */
	public static boolean enablePlayerVars = true, enablePlayerVarsOriginal = true;
	/** Whether to enable global variables */
	public static boolean enableGlobalVars = true;
	/** Whether to enable player aliases */
	public static boolean enablePlayerAliases = true, enablePlayerAliasesOriginal = true;
	/** Whether to enable global aliases */
	public static boolean enableGlobalAliases = true;
	
	/** Whether enchantments should do various applicability checks */
	public static boolean strictEnchanting = true;
	/** Whether the execute command should require a confirmation before the command is executed */
	public static boolean useExecRequests = false;
	/** The time to confirm a command execution request (in seconds) */
	public static int execRequestTimeout = 10;
	/** Maximum of execution requests for a player */
	public static int maxExecRequests = 5;
	/** Minimum level required to be able to confirm or deny execution requests */
	public static int minExecRequestLevel = 2;
	/** Whether to prefix the name of a chat channel for all chat messages */
	public static boolean prefixChannelName = true;
	
	/** Whether ALL clients are required to have MoreCommands installed */
	public static boolean clientMustHaveMod = false;
	/** Whether the server is required to have MoreCommands installed */
	public static boolean serverMustHaveMod = false;
	
	/** A map that maps a command name to a base permission level and to several permission levels for various actions (this works only for morecommands server commands) */
	public static final Map<String, MutablePair<Integer, TObjectIntMap<String>>> permissionMapping = Maps.newHashMap();
	
	private static String startupCommandsString; //The original string read from the file
	private static final List<String> startupCommandsList = Lists.newArrayList(); //The actual list
	/** This list contains commands to be executed on server startup. This list is unmodifiable! */
	public static final List<String> startupCommands = Collections.unmodifiableList(startupCommandsList);
	
	/**
	 * Reads the configuration from the config file
	 */
	public static void readConfig() {
		Config config = new Config(new File(Reference.getModDir(), "config.cfg"), true);
		
		MoreCommandsConfig.welcome_message = config.getBoolean("welcome_message", true);
		MoreCommandsConfig.enablePlayerVars = MoreCommandsConfig.enablePlayerVarsOriginal = config.getBoolean("enablePlayerVars", true);
		MoreCommandsConfig.enableGlobalVars = config.getBoolean("enableGlobalVars", true);
		MoreCommandsConfig.enablePlayerAliases = MoreCommandsConfig.enablePlayerAliasesOriginal = config.getBoolean("enablePlayerAliases", true);
		MoreCommandsConfig.enableGlobalAliases = config.getBoolean("enableGlobalAliases", true);
		MoreCommandsConfig.retryHandshake = config.getBoolean("retryHandshake", true);
		MoreCommandsConfig.startupTimeout = config.getInteger("startupTimeout", 10);
		MoreCommandsConfig.handshakeTimeout = config.getInteger("handshakeTimeout", 3);
		MoreCommandsConfig.handshakeRetries = config.getInteger("handshakeRetries", 3);
		MoreCommandsConfig.searchUpdates = config.getBoolean("searchUpdates", true);
		MoreCommandsConfig.xrayUPS = config.getInteger("xrayUPS", 1);
		MoreCommandsConfig.strictEnchanting = config.getBoolean("strictEnchanting", true);
		MoreCommandsConfig.useRegexCalcParser = config.getBoolean("useRegexCalcParser", true);
		MoreCommandsConfig.useExecRequests = config.getBoolean("useExecRequests", false);
		MoreCommandsConfig.execRequestTimeout = config.getInteger("execRequestTimeout", 10);
		MoreCommandsConfig.maxExecRequests = config.getInteger("maxExecRequests", 5);
		MoreCommandsConfig.prefixChannelName = config.getBoolean("prefixChannelName", true);
		MoreCommandsConfig.minExecRequestLevel = config.getInteger("minExecRequestLevel", 2);
		MoreCommandsConfig.clientMustHaveMod = config.getBoolean("clientMustHaveMod", false);
		MoreCommandsConfig.serverMustHaveMod = config.getBoolean("serverMustHaveMod", false);
		
		MoreCommandsConfig.startupDelay = config.getInteger("startupDelay", 0);
		MoreCommandsConfig.startupCommandsString = config.getString("startupCommands", null);
		MoreCommandsConfig.startupCommandsList.addAll(splitStartupCommands(MoreCommandsConfig.startupCommandsString));
		
		Config permissions = new Config(new File(Reference.getModDir(), "permissions.cfg"), true);
		
		for (Map.Entry<Object, Object> entry : permissions.entrySet()) {
			if (entry.getKey() instanceof String && entry.getValue() instanceof String) {
				String[] actions = ((String) entry.getValue()).split(",");
				
				int perm = -1;
				TObjectIntMap<String> actionPerms = new TObjectIntHashMap<String>(gnu.trove.impl.Constants.DEFAULT_CAPACITY, gnu.trove.impl.Constants.DEFAULT_LOAD_FACTOR, -1);
				
				for (String action : actions) {
					if (action.contains(":")) {
						String[] split = action.split(":");
						if (split.length != 2) continue;
						
						int val = MathHelper.getInt(split[1], -1);
						if (val >= 0) actionPerms.put(split[0].trim(), val);
					}
					else perm = MathHelper.getInt(action.trim(), -1);
				}
				
				permissionMapping.put((String) entry.getKey(), MutablePair.of(perm, actionPerms));
			}
		}
	}
	

	/**
	 * Writes the configuration to the config file
	 */
	public static void writeConfig() {
		Config config = new Config(new File(Reference.getModDir(), "config.cfg"), true);
		
		config.set("welcome_message", MoreCommandsConfig.welcome_message);
		config.set("enablePlayerVars", MoreCommandsConfig.enablePlayerVarsOriginal);
		config.set("enableGlobalVars", MoreCommandsConfig.enableGlobalVars);
		config.set("enablePlayerAliases", MoreCommandsConfig.enablePlayerAliasesOriginal);
		config.set("enableGlobalAliases", MoreCommandsConfig.enableGlobalAliases);
		config.set("retryHandshake", MoreCommandsConfig.retryHandshake);
		config.set("startupTimeout", MoreCommandsConfig.startupTimeout);
		config.set("handshakeTimeout", MoreCommandsConfig.handshakeTimeout);
		config.set("handshakeRetries", MoreCommandsConfig.handshakeRetries);
		config.set("searchUpdates", MoreCommandsConfig.searchUpdates);
		config.set("xrayUPS", MoreCommandsConfig.xrayUPS);
		config.set("strictEnchanting", MoreCommandsConfig.strictEnchanting);
		config.set("useRegexCalcParser", MoreCommandsConfig.useRegexCalcParser);
		config.set("useExecRequests", MoreCommandsConfig.useExecRequests);
		config.set("execRequestTimeout", MoreCommandsConfig.execRequestTimeout);
		config.set("maxExecRequests", MoreCommandsConfig.maxExecRequests);
		config.set("minExecRequestLevel", MoreCommandsConfig.minExecRequestLevel);
		config.set("prefixChannelName", MoreCommandsConfig.prefixChannelName);
		config.set("clientMustHaveMod", MoreCommandsConfig.clientMustHaveMod);
		config.set("serverMustHaveMod", MoreCommandsConfig.serverMustHaveMod);
		
		config.set("startupDelay", MoreCommandsConfig.startupDelay);
		if (startupCommandsString != null)
			config.set("startupCommands", startupCommandsString);
		
		config.save();
		
		Config permissions = new Config(new File(Reference.getModDir(), "permissions.cfg"), true);
		
		for (Map.Entry<String, MutablePair<Integer, TObjectIntMap<String>>> entry : permissionMapping.entrySet()) { 
			final StringBuilder out = new StringBuilder();
			if (entry.getValue().getLeft() >= 0) out.append(entry.getValue().getLeft());
			
			entry.getValue().getRight().forEachEntry(new TObjectIntProcedure<String>() {
				@Override
				public boolean execute(String action, int level) {
					out.append(",").append(action).append(":").append(level);
					return true;
				}
			});
			
			String w = out.toString();
			if (!w.isEmpty()) permissions.set(entry.getKey(), w.startsWith(",") ? w.substring(1) : w);
		}
		
		permissions.save();
	}
	
	//Splits the startupCommands string into single startup commands
	private static List<String> splitStartupCommands(String startupCommands) {
		if (startupCommands == null || startupCommands.isEmpty()) 
			return Lists.newArrayList();
		
		JsonParser parser = new JsonParser();
		JsonElement parsed;
		
		try {parsed = parser.parse(startupCommands);}
		catch (JsonSyntaxException ex) {MoreCommands.INSTANCE.getLogger().warn("Invalid syntax for startup commands"); return Lists.newArrayList();}
		catch (JsonParseException ex) {MoreCommands.INSTANCE.getLogger().warn("Exception during parsing of startup commands"); return Lists.newArrayList();}
		
		if (!parsed.isJsonArray())
			return Lists.newArrayList();
		
		List<String> list = Lists.newArrayList();
		
		for (JsonElement e : parsed.getAsJsonArray()) {
			if (!e.isJsonPrimitive()) continue;
			else list.add(e.getAsString());
		}
		
		return list;
	}
}
