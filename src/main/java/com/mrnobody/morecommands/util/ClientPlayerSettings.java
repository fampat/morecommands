package com.mrnobody.morecommands.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import com.mrnobody.morecommands.core.MoreCommands;

/**
 * Reads and writes the client settings
 * 
 * @author MrNobody98
 *
 */
public class ClientPlayerSettings {
	public static Map<Integer, String> keybindMapping = new HashMap<Integer, String>();
	public static Map<String, String> aliasMapping = new HashMap<String, String>();
	public static Map<String, String> varMapping = new HashMap<String, String>();
	public static Map<String, Map<Block, Integer>> xrayColorMapping = new HashMap<String, Map<Block, Integer>>();
	public static Map<String, Integer> xrayRadiusMapping = new HashMap<String, Integer>();
	
	/**
	 * Read settings
	 * 
	 * @return Whether the settings were read successfully
	 */
	public static boolean readSettings(String socketAddress) {
		File playerSettings = new File(Reference.getClientPlayerDir(), "localplayer.dat");
		NBTTagCompound playerData;
		
		if (!playerSettings.exists() || !playerSettings.isFile()) return false;
		
		try {
			playerData = CompressedStreamTools.readCompressed(new FileInputStream(playerSettings));
			
			//those settings are not intended to be read or written
			
			//settings.climb = playerData.hasKey("climb") ? playerData.getBoolean("climb") : false;
			//settings.clouds = playerData.hasKey("clouds") ? playerData.getBoolean("clouds") : true;
			//settings.creeperExplosion = playerData.hasKey("creeperExplosion") ? playerData.getBoolean("creeperExplosion") : true;
			//settings.dodrops = playerData.hasKey("dodrops") ? playerData.getBoolean("dodrops") : true;
			//settings.falldamage = playerData.hasKey("falldamage") ? playerData.getBoolean("falldamage") : true;
			//settings.firedamage = playerData.hasKey("firedamage") ? playerData.getBoolean("firedamage") : true;
			//settings.fly = playerData.hasKey("fly") ? playerData.getBoolean("fly") : false;
			//settings.noFall = playerData.hasKey("noFall") ? playerData.getBoolean("noFall") : false;
			//settings.justDisabled = playerData.hasKey("justDisabled") ? playerData.getBoolean("justDisabled") : false;
			//settings.freecam = playerData.hasKey("freecam") ? playerData.getBoolean("freecam") : false;
			//settings.freeezecam = playerData.hasKey("freeezecam") ? playerData.getBoolean("freeezecam") : false;
			//settings.xrayBlockRadius = playerData.hasKey("xrayBlockRadius") ? playerData.getInteger("xrayBlockRadius") : 32;
			//settings.xrayEnabled = playerData.hasKey("xrayEnabled") ? playerData.getBoolean("xrayEnabled") : false;
			//settings.breakSpeedEnabled = playerData.hasKey("breakSpeedEnabled") ? playerData.getBoolean("breakSpeedEnabled") : false;
			//settings.breakspeed = playerData.hasKey("breakspeed") ? playerData.getFloat("breakspeed") : 1.0F;
			//settings.infiniteitems = playerData.hasKey("infiniteitems") ? playerData.getBoolean("infiniteitems") : false;
			//settings.instantgrow = playerData.hasKey("instantgrow") ? playerData.getBoolean("instantgrow") : false;
			//settings.instantkill = playerData.hasKey("instantkill") ? playerData.getBoolean("instantkill") : false;
			//settings.keepinventory = playerData.hasKey("keepinventory") ? playerData.getBoolean("keepinventory") : false;
			//settings.killattacker = playerData.hasKey("killattacker") ? playerData.getBoolean("killattacker") : false;
			//settings.lightWorld = playerData.hasKey("lightWorld") ? playerData.getBoolean("lightWorld") : false;
			//settings.mobdamage = playerData.hasKey("mobdamage") ? playerData.getBoolean("mobdamage") : true;
			//settings.scuba = playerData.hasKey("scuba") ? playerData.getBoolean("scuba") : false;
			//settings.output = playerData.hasKey("output") ? playerData.getBoolean("output") : true;
			
			NBTTagCompound socket = playerData.hasKey(socketAddress) ? playerData.getCompoundTag(socketAddress) : null;
			
			if (socket == null) return true;
			
			NBTTagCompound bindings = socket.hasKey("bindings") ? socket.getCompoundTag("bindings") : null;
			
			if (bindings != null) {
				for (Object o : bindings.func_150296_c()) {
					ClientPlayerSettings.keybindMapping.put(Integer.parseInt((String) o), bindings.getString((String) o));
				}
			}
			
			NBTTagCompound aliases = socket.hasKey("aliases") ? socket.getCompoundTag("aliases") : null;
			
			if (aliases != null) {
				for (Object o : aliases.func_150296_c()) {
					ClientPlayerSettings.aliasMapping.put((String) o, aliases.getString((String) o));
				}
			}
			
			NBTTagCompound vars = socket.hasKey("vars") ? socket.getCompoundTag("vars") : null;
			
			if (vars != null) {
				for (Object o : vars.func_150296_c()) {
					ClientPlayerSettings.varMapping.put((String) o, vars.getString((String) o));
				}
			}
			
			NBTTagCompound xray = socket.hasKey("xray") ? socket.getCompoundTag("xray") : null;
			
			if (xray != null) {
				for (Object o : xray.func_150296_c()) {
					NBTTagCompound setting = xray.getCompoundTag((String) o);
					NBTTagCompound colors = setting.getCompoundTag("colors");
					int radius = setting.getInteger("radius");
					
					HashMap<Block, Integer> map = new HashMap<Block, Integer>();
					
					for (Object o2 : colors.func_150296_c()) {
						Block block = Block.getBlockById(Integer.parseInt((String) o2));
						int color = colors.getInteger((String) o2);
						map.put(block, color);
					}
					
					ClientPlayerSettings.xrayColorMapping.put((String) o, map);
					ClientPlayerSettings.xrayRadiusMapping.put((String) o, radius);
				}
			}
			
			return true;
		}
		catch (Exception ex) {
			ex.printStackTrace();
			MoreCommands.getMoreCommands().getLogger().warn("Failed to read player settings for player '" + Minecraft.getMinecraft().thePlayer.getCommandSenderName() + "'");
			return false;
		}
	}
	
	/**
	 * Saves settings
	 * 
	 * @return Whether the settings were saved successfully
	 */
	public static boolean saveSettings() {
		try{
			NBTTagCompound data = new NBTTagCompound();
			NBTTagCompound socket = new NBTTagCompound();
			
			NBTTagCompound bindings = new NBTTagCompound();
		
			for (int keyid : ClientPlayerSettings.keybindMapping.keySet()) {
				bindings.setString(String.valueOf(keyid), ClientPlayerSettings.keybindMapping.get(keyid));
			}
		
			socket.setTag("bindings", bindings);
			
			NBTTagCompound aliases = new NBTTagCompound();
			
			for (String alias : ClientPlayerSettings.aliasMapping.keySet()) {
				aliases.setString(alias, ClientPlayerSettings.aliasMapping.get(alias));
			}
		
			socket.setTag("aliases", aliases);
			
			NBTTagCompound vars = new NBTTagCompound();
			
			for (String var : ClientPlayerSettings.varMapping.keySet()) {
				vars.setString(var, ClientPlayerSettings.varMapping.get(var));
			}
		
			socket.setTag("vars", vars);
			
			NBTTagCompound xray = new NBTTagCompound();
			
			for (String conf : ClientPlayerSettings.xrayColorMapping.keySet()) {
				NBTTagCompound setting = new NBTTagCompound();
				NBTTagCompound colors = new NBTTagCompound();
				Map<Block, Integer> map = ClientPlayerSettings.xrayColorMapping.get(conf);
				
				for (Block block : map.keySet())
					colors.setInteger(String.valueOf(Block.getIdFromBlock(block)), map.get(block));
				
				setting.setInteger("radius", ClientPlayerSettings.xrayRadiusMapping.containsKey(conf) ? ClientPlayerSettings.xrayRadiusMapping.get(conf) : 32);
				setting.setTag("colors", colors);
				
				xray.setTag(conf, setting);
			}
		
			socket.setTag("xray", xray);
			
			data.setTag(Minecraft.getMinecraft().getNetHandler().getNetworkManager().getSocketAddress().toString(), socket);
		
			File playerSettingsTmp = new File(Reference.getClientPlayerDir(), "localplayer.dat.tmp");
			File playerSettings = new File(Reference.getClientPlayerDir(), "localplayer.dat");
			CompressedStreamTools.writeCompressed(data, new FileOutputStream(playerSettingsTmp));
		
			if (playerSettings.exists()) playerSettings.delete();
			playerSettingsTmp.renameTo(playerSettings);
			
			return true;
		}
		catch (Exception ex) {
			ex.printStackTrace();
			MoreCommands.getMoreCommands().getLogger().warn("Failed to write player settings for player '" + Minecraft.getMinecraft().thePlayer.getCommandSenderName() + "'");
			return false;
		}
	}
}
