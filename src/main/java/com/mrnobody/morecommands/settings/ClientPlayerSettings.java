package com.mrnobody.morecommands.settings;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.util.DummyCommand;

import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.capabilities.Capability;

/**
 * A settings class for settings that are used client side
 * 
 * @author MrNobody98
 */
public final class ClientPlayerSettings extends PlayerSettings {
	private static ClientPlayerSettings instance;
	
	/**
	 * @param player the client player
	 * @return the {@link ClientPlayerSettings} instance
	 */
	public static ClientPlayerSettings getInstance(EntityPlayerSP player) {
		if (instance == null) instance = new ClientPlayerSettings(player);
		return instance;
	}
	
	/**
	 * Gets the {@link ClientPlayerSettings} instance if it has been initialized via {@link #getInstance(EntityClientPlayerMP)}
	 * before. Returns null otherwise
	 * 
	 * @return The {@link ClientPlayerSettings} instance if it has been initialized via {@link #getInstance(EntityClientPlayerMP)} before else null
	 */
	public static ClientPlayerSettings getInstance() {
		return instance;
	}
	
	/** A set of client side settings */
	public static final ImmutableSet<String> CLIENT_SETTINGS = ImmutableSet.of("bindings", "xray");
	
	/**
	 * A class containing settings for xray
	 * 
	 * @author MrNobody98
	 */
	public static final class XrayInfo {
		public int radius;
		public Map<Block, Integer> colors;
		
		public XrayInfo(int radius, Map<Block, Integer> colors) {
			this.radius = radius;
			this.colors = colors;
		}
	}
	
	private final List<String> startupCommandsList = Lists.newArrayList();
	/** The client side startup commands. This list is unmodifiable! */
	public final List<String> startupCommands = Collections.unmodifiableList(this.startupCommandsList);
	
	/** A map storing the key bindings the player created */
	public MergedMappedSettings<String, String> bindings;
	/** A map storing xray settings */
	public MergedMappedSettings<String, XrayInfo> xray;
	
	private ClientPlayerSettings(EntityPlayerSP player) {
		super(MoreCommands.getProxy().createSettingsManagerForPlayer(player), true);
	}
	
	protected ClientPlayerSettings(SettingsManager manager, boolean load) {
		super(manager, load);
	}
	
	@Override
	protected void registerAliases() {
		net.minecraft.command.CommandHandler commandHandler = ClientCommandHandler.instance;
		String command;
		
		for (String alias : this.aliases.keySet()) {
			command = this.aliases.get(alias).split(" ")[0];
			
			if (!command.equalsIgnoreCase(alias) && commandHandler.getCommands().get(alias) == null) {
				DummyCommand cmd = new DummyCommand(alias, true);
				commandHandler.getCommands().put(alias, cmd);
			}
		}
	}
	
	@Override
	protected synchronized void readSettings() {
		super.readSettings();
		this.xray = super.readMergedMappedSettings("xray", XrayInfo.class, SettingsProperty.SERVER_PROPERTY);
		this.bindings = super.readMergedMappedSettings("bindings", String.class, SettingsProperty.SERVER_PROPERTY);
		
		List<Setting<List<String>>> startupCommands = this.getManager().getSetting("startupCommands", 
				this.getSettingsProperties(), (Class<List<String>>) (Class<?>) List.class);
		
		this.startupCommandsList.clear();
		if (startupCommands != null && !startupCommands.isEmpty()) 
			this.startupCommandsList.addAll(startupCommands.get(0).getValue());
	}
	
	@Override
	public synchronized boolean setPutProperties(String setting, SettingsProperty... props) {
		if (super.setPutProperties(setting, props)) return true;
		if (!CLIENT_SETTINGS.contains(setting)) return false;
		
		Map<SettingsProperty, Set<String>> sProps = Maps.newEnumMap(SettingsProperty.class);
		for (SettingsProperty prop : props) sProps.put(prop, Sets.newHashSet(this.getSettingsProperties().get(prop)));
		
		if ("xray".equals(setting))
			this.xray.setPutSetting(new Setting<Map<String, XrayInfo>>(Maps.<String, XrayInfo>newHashMap(), sProps));
		else if ("bindings".equals(setting))
			this.bindings.setPutSetting(new Setting<Map<String, String>>(Maps.<String, String>newHashMap(), sProps));
		
		return true;
	}
	
	@Override
	protected void copyProperties(PlayerSettings psettings) {
		if (psettings instanceof ClientPlayerSettings) {
			ClientPlayerSettings settings = (ClientPlayerSettings) psettings;
			
			this.bindings = settings.bindings;
			this.xray = settings.xray;
		}
		
		super.copyProperties(psettings);
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == SETTINGS_CAP_CLIENT;
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return capability == SETTINGS_CAP_CLIENT ? SETTINGS_CAP_CLIENT.<T>cast(this) : null;
	}
	
	protected static final SettingsSerializer<Map<String, XrayInfo>> MAP_STRING_XRAY_SERIALIZER = new SettingsSerializer<Map<String, XrayInfo>>() {
		@Override
		public JsonElement serialize(Map<String, XrayInfo> src) {
			JsonObject obj = new JsonObject();
			
			for (Map.Entry<String, XrayInfo> entry : src.entrySet()) {
				JsonObject data = new JsonObject();
				JsonObject colors = new JsonObject();
				
				for (Map.Entry<Block, Integer> entry2 : entry.getValue().colors.entrySet()) {
					Object name = Block.blockRegistry.getNameForObject(entry2.getKey());
					if (name == null) continue; int color = entry2.getValue();
					
					JsonArray rgb = new JsonArray(); rgb.add(new JsonPrimitive(color & 0xFF));
					rgb.add(new JsonPrimitive((color >>> 8) & 0xFF)); rgb.add(new JsonPrimitive((color >>> 16) & 0xFF));
					
					colors.add(name.toString(), rgb);
				}
				
				data.add("radius", new JsonPrimitive(entry.getValue().radius));
				data.add("colors", colors);
			}
			
			return obj;
		}

		@Override
		public Map<String, XrayInfo> deserialize(JsonElement element) {
			if (!element.isJsonObject()) return Maps.newHashMap();
			
			JsonObject obj = element.getAsJsonObject();
			Map<String, XrayInfo> map = Maps.newHashMap();
			
			for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
				if (!entry.getValue().isJsonObject()) continue;
				JsonObject data = entry.getValue().getAsJsonObject();
				
				int radius = 25;
				if (data.has("radius") && data.get("radius").isJsonPrimitive() && data.get("radius").getAsJsonPrimitive().isNumber())
					try {radius = data.get("radius").getAsJsonPrimitive().getAsNumber().intValue();}
					catch (NumberFormatException nfe) {}
				
				JsonElement colors = data.get("colors");
				if (colors == null || !colors.isJsonObject()) continue;
				Map<Block, Integer> cols = new HashMap<Block, Integer>();
				
				for (Map.Entry<String, JsonElement> entry2 : colors.getAsJsonObject().entrySet()) {
					if (!entry2.getValue().isJsonArray()) continue;
					JsonArray rgb = entry2.getValue().getAsJsonArray();
					
					if (rgb.size() != 3 || 
						!rgb.get(0).isJsonPrimitive() || !rgb.get(0).getAsJsonPrimitive().isNumber() ||
						!rgb.get(1).isJsonPrimitive() || !rgb.get(1).getAsJsonPrimitive().isNumber() ||
						!rgb.get(2).isJsonPrimitive() || !rgb.get(2).getAsJsonPrimitive().isNumber()) continue;
					
					int r = rgb.get(0).getAsInt(), g = rgb.get(1).getAsInt(), b = rgb.get(2).getAsInt();
					int color = b << 16 | g << 8 | r;;
					
					Block block = Block.getBlockFromName(entry2.getKey());
					if (block != null)
						try {block = Block.getBlockById(Integer.parseInt(entry2.getKey()));}
						catch (NumberFormatException nfe) {}
					
					if (block == null) continue;
					else cols.put(block, color);
				}
				
				map.put(entry.getKey(), new XrayInfo(radius, cols));
			}
			
			return map;
		}
		
		@Override
		public Class<Map<String, XrayInfo>> getTypeClass() {
			return (Class<Map<String, XrayInfo>>) (Class<?>) Map.class;
		}
	};
	
	protected static final SettingsSerializer<List<String>> STRING_LIST_SERIALIZER = new SettingsSerializer<List<String>>() {
		@Override
		public List<String> deserialize(JsonElement element) {
			if (!element.isJsonArray()) return Lists.newArrayList();
			List<String> list = Lists.newArrayList();
			
			for (JsonElement e : element.getAsJsonArray()) {
				if (!e.isJsonPrimitive()) continue;
				else list.add(e.getAsString());
			}
			
			return list;
		}

		@Override
		public JsonElement serialize(List<String> element) {
			JsonArray arr = new JsonArray();
			for (String s : element) arr.add(new JsonPrimitive(s));
			return arr;
		}

		@Override
		public Class<List<String>> getTypeClass() {
			return (Class<List<String>>) (Class<?>) List.class;
		}
	};
	
	static {
		RootSettingsSerializer.registerSerializer("bindings", MAP_STRING_STRING_SERIALIZER);
		RootSettingsSerializer.registerSerializer("xray", MAP_STRING_XRAY_SERIALIZER);
		RootSettingsSerializer.registerSerializer("startupCommands", STRING_LIST_SERIALIZER);
	}
}
