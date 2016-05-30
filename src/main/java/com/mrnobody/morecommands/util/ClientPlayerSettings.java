package com.mrnobody.morecommands.util;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.util.SettingsManager.AbstractElement;
import com.mrnobody.morecommands.util.SettingsManager.ListElement;
import com.mrnobody.morecommands.util.SettingsManager.NumericElement;
import com.mrnobody.morecommands.util.SettingsManager.ObjectElement;
import com.mrnobody.morecommands.util.SettingsManager.Serializable;

import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.EnumFacing;
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
	
	/** A map storing the key bindings the player created */
	public Map<Integer, String> bindings = new HashMap<Integer, String>();
	/** A map storing xray settings */
	public Map<String, XrayInfo> xray = new HashMap<String, XrayInfo>();
	
	public ClientPlayerSettings(EntityPlayerSP player) {
		super(MoreCommands.getProxy().createSettingsManagerForPlayer(player), true);
	}
	
	public ClientPlayerSettings(SettingsManager manager, boolean load) {
		super(manager, load);
	}
	
	@Override
	public synchronized void readSettings(String server, String world, String dim) {
		super.readSettings(server, world, dim);
		this.xray = readMappedSetting("xray", server, world, dim, XrayInfo.class);
		
		Map<String, String> bindings = readMappedSetting("bindings", server, world, dim, String.class);
		this.bindings = new HashMap<Integer, String>(bindings.size());
		for (Map.Entry<String, String> entry : bindings.entrySet()) {
			int key = Keyboard.getKeyIndex(entry.getKey());
			if (key != Keyboard.KEY_NONE) this.bindings.put(key, entry.getValue());
		}
	}
	
	@Override
	public synchronized void saveSettings(String server, String world, String dim) {
		super.saveSettings(server, world, dim);
		writeMappedSetting("xray", this.xray, server, world, dim, XrayInfo.class);
		
		Map<String, String> bindings = new HashMap<String, String>();
		for (Map.Entry<Integer, String> entry : this.bindings.entrySet()) {
			String keyName = Keyboard.getKeyName(entry.getKey());
			if (keyName != null) bindings.put(keyName, entry.getValue());
		}
		writeMappedSetting("bindings", bindings, server, world, dim, String.class);
	}
	
	@Override
	public void cloneSettings(PlayerSettings settings) {
		if (settings instanceof ClientPlayerSettings) {
			ClientPlayerSettings csettings = (ClientPlayerSettings) settings;
			
			this.bindings = new HashMap<Integer, String>(csettings.bindings);
			this.xray = new HashMap<String, XrayInfo>(csettings.xray);
		}
		
		super.cloneSettings(settings);
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == SETTINGS_CAP_CLIENT;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return capability == SETTINGS_CAP_CLIENT ? SETTINGS_CAP_CLIENT.<T>cast(this) : null;
	}
	
	protected static final Serializable<Map<String, XrayInfo>> MAP_STRING_XRAY_SERIALIZABLE = new Serializable<Map<String, XrayInfo>>() {
		@Override
		public AbstractElement serialize(Map<String, XrayInfo> src) {
			ObjectElement obj = new ObjectElement();
			for (Map.Entry<String, XrayInfo> entry : src.entrySet()) {
				ObjectElement data = new ObjectElement();
				ObjectElement colors = new ObjectElement();
				
				for (Map.Entry<Block, Integer> entry2 : entry.getValue().colors.entrySet()) {
					Object name = Block.REGISTRY.getNameForObject(entry2.getKey());
					if (name == null) continue; int color = entry2.getValue();
					
					ListElement rgb = new ListElement(); rgb.add(new NumericElement(color & 0xFF));
					rgb.add(new NumericElement((color >>> 8) & 0xFF)); rgb.add(new NumericElement((color >>> 16) & 0xFF));
					
					colors.add(name.toString(), rgb);
				}
				
				data.add("radius", new NumericElement(entry.getValue().radius));
				data.add("colors", colors);
			}
			return obj;
		}

		@Override
		public Map<String, XrayInfo> deserialize(AbstractElement element) {
			if (!element.isObject()) return Maps.newHashMap();
			ObjectElement obj = element.asObject();
			Map<String, XrayInfo> map = Maps.newHashMap();
			
			for (Map.Entry<String, AbstractElement> entry : obj.entrySet()) {
				if (!entry.getValue().isObject()) continue;
				ObjectElement data = entry.getValue().asObject();
				
				int radius = 25;
				if (data.has("radius") && data.get("radius").isNumeric())
					try {radius = data.get("radius").asNumericElement().asNumber().intValue();}
					catch (NumberFormatException nfe) {}
				
				AbstractElement colors = data.get("colors");
				if (colors == null || !colors.isObject()) continue;
				Map<Block, Integer> cols = new HashMap<Block, Integer>();
				
				for (Map.Entry<String, AbstractElement> entry2 : colors.asObject().entrySet()) {
					if (!entry2.getValue().isList()) continue;
					ListElement rgb = entry2.getValue().asList();
					if (rgb.size() != 3 || !rgb.get(0).isNumeric() || !rgb.get(1).isNumeric() || !rgb.get(2).isNumeric()) continue;
					NumericElement r = rgb.get(0).asNumericElement(), g = rgb.get(1).asNumericElement(), b = rgb.get(2).asNumericElement();
					
					int color = 0;
					try {color = b.asNumber().intValue() << 16 | g.asNumber().intValue() << 8 | r.asNumber().intValue();}
					catch (NumberFormatException nfe) {continue;}
					
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
	
	static {
		SettingsManager.registerSerializable("bindings", MAP_STRING_STRING_SERIALIZABLE, true);
		SettingsManager.registerSerializable("xray", MAP_STRING_XRAY_SERIALIZABLE, true);
	}
}
