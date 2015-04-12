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
		Map<String, String> input = new HashMap<String, String>();
		
		input.put("netClientHandler", "field_78774_b");
		input.put("lastPosX", "field_147373_o");
		input.put("lastPosY", "field_147382_p");
		input.put("lastPosZ", "field_147381_q");
		input.put("hasMoved", "field_147380_r");
		input.put("guiScreenServer", "field_147307_j");
		input.put("myNetworkManager", "field_71453_ak");
		input.put("renderBlocksRg", "field_147592_B");
		input.put("updateFallState", "func_70064_a");
		input.put("gameController", "field_147299_f");
		input.put("guiScreenServer", "field_147307_j");
		input.put("clientWorldController", "field_147300_g");
		input.put("createEnderPortal", "func_70975_a");
		input.put("net.minecraft.block.Block", "aji");
		input.put("theWorld", "field_72769_h");
		input.put("prevSortX", "field_72758_d");
		input.put("prevSortY", "field_72759_e");
		input.put("prevSortZ", "field_72756_f");
		input.put("prevRenderSortX", "field_147596_f");
		input.put("prevRenderSortY", "field_147597_g");
		input.put("prevRenderSortZ", "field_147602_h");
		input.put("prevChunkSortX", "field_147603_i");
		input.put("prevChunkSortY", "field_147600_j");
		input.put("prevChunkSortZ", "field_147601_k");
		input.put("renderDistanceChunks", "field_72739_F");
		input.put("worldRenderers", "field_72765_l");
		input.put("renderChunksWide", "field_72766_m");
		input.put("renderChunksTall", "field_72763_n");
		input.put("renderChunksDeep", "field_72764_o");
		input.put("sortedWorldRenderers", "field_72768_k");
		input.put("minBlockX", "field_72780_y");
		input.put("minBlockY", "field_72779_z");
		input.put("minBlockZ", "field_72741_A");
		input.put("maxBlockX", "field_72742_B");
		input.put("maxBlockY", "field_72743_C");
		input.put("maxBlockZ", "field_72737_D");
		input.put("worldRenderersToUpdate", "field_72767_j");
		input.put("glRenderListBase", "field_72778_p");
		input.put("occlusionEnabled", "field_72774_t");
		input.put("glOcclusionQueryBase", "field_72775_s");
		input.put("renderEntitiesStartupCounter", "field_72740_G");
		input.put("markRenderersForNewPosition", "func_72722_c");
		input.put("bytesDrawn", "field_78917_C");
		input.put("vertexState", "field_147894_y");
		input.put("tileEntities", "field_147893_C");
		input.put("isInitialized", "field_78915_A");
		input.put("preRenderBlocks", "func_147890_b");
		input.put("postRenderBlocks", "func_147891_a");
		input.put("lightOpacity", "field_149786_r");
		input.put("integratedServerIsRunning", "field_71455_al");
		input.put("theIntegratedServer", "field_71437_Z");
		input.put("commandManager", "field_71321_q");
		input.put("invulnerable", "field_83001_bt");
		input.put("mcMusicTicker", "field_147126_aw");
		input.put("commandSet", "field_71561_b");
		input.put("saveAllWorlds", "func_71267_a");
		input.put("loadAllWorlds", "func_71247_a");
		input.put("foodLevel", "field_75127_a");
		input.put("translator", "field_71148_cg");
		input.put("flySpeed", "field_75096_f");
		input.put("walkSpeed", "field_75097_g");
		//Add more fields
		
		ObfuscationMap = Collections.unmodifiableMap(input);
	}
	
	public static String getObfuscatedName(String unobfuscatedName) {
		return ObfuscationMap.get(unobfuscatedName);
	}
}
