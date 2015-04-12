package com.mrnobody.morecommands.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A class containing a mapping from the deobfuscated field and <br>
 * method names to the obfuscated field and method names.
 * 
 * @author MrNobody98
 *
 */
public class ObfuscationHelper {
	private static final Map<String, String> ObfuscationMap;
	
	static {
		HashMap<String, String> map = new HashMap<String, String>();
		
		map.put("lastPosZ", "field_147381_q");
		map.put("lastPosX", "field_147373_o");
		map.put("lastPosY", "field_147382_p");
		map.put("hasMoved", "field_147380_r");
		map.put("networkTickCount", "field_147368_e");
		map.put("floatingTickCount", "field_147365_f");
		map.put("netClientHandler", "field_78774_b");
		map.put("guiScreenServer", "field_147307_j");
		map.put("myNetworkManager", "field_71453_ak");
		map.put("gameController", "field_147299_f");
		map.put("guiScreenServer", "field_147307_j");
		map.put("clientWorldController", "field_147300_g");
		map.put("commandManager", "field_71321_q");
		map.put("mcMusicTicker", "field_147126_aw");
		map.put("currentMusic", "field_147678_c");
		map.put("timeUntilNextMusic", "field_147676_d");
		map.put("saveAllWorlds", "func_71267_a");
		map.put("foodLevel", "field_75127_a");
		map.put("translator", "field_71148_cg");
		map.put("crop", "field_149877_a");
		map.put("achievementDescription", "field_75996_k");
		map.put("loadAllWorlds", "func_71247_a");
		map.put("flySpeed", "field_75096_f");
		map.put("walkSpeed", "field_75097_g");
		//Add more fields
		
		ObfuscationMap = Collections.unmodifiableMap(map);
	}
	
	public static String getObfuscatedName(String unobfuscatedName) {
		return ObfuscationMap.containsKey(unobfuscatedName) ? ObfuscationMap.get(unobfuscatedName) : unobfuscatedName;
	}
}
