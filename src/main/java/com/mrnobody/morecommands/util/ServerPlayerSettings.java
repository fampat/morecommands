package com.mrnobody.morecommands.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;

import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.wrapper.Coordinate;

/**
 * Reads and writes the settings for each player on a server
 * 
 * @author MrNobody98
 *
 */
public class ServerPlayerSettings {
	/**
	 * A map from the player uuid to the player object
	 */
	public static final Map<UUID, EntityPlayerMP> playerUUIDMapping = new HashMap<UUID, EntityPlayerMP>();
	
	/**
	 * A map containing the settings for each player
	 */
	public static final Map<EntityPlayerMP, ServerPlayerSettings> playerSettingsMapping = new HashMap<EntityPlayerMP, ServerPlayerSettings>();
	
	/**
	 * Gets the settings for a player
	 * 
	 * @return a ServerPlayerSettings object
	 */
	public static ServerPlayerSettings getPlayerSettings(EntityPlayerMP player) {
		File playerSettings = new File(Reference.getServerPlayerDir(), player.getUniqueID().toString() + ".dat");
		ServerPlayerSettings settings = new ServerPlayerSettings(player);
		NBTTagCompound playerData;
		
		if (!playerSettings.exists() || !playerSettings.isFile()) return settings;
		
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
			
			NBTTagCompound world = playerData.hasKey(MinecraftServer.getServer().getFolderName()) ? playerData.getCompoundTag(MinecraftServer.getServer().getFolderName()) : null;
			
			if (world == null) return settings;
			
			NBTTagCompound serverbindings = world.hasKey("server_bindings") ? world.getCompoundTag("server_bindings") : null;
			
			if (serverbindings != null) {
				for (Object o : serverbindings.func_150296_c()) {
					settings.serverKeybindMapping.put(Integer.parseInt((String) o), serverbindings.getString((String) o));
				}
			}
			
			NBTTagCompound clientbindings = world.hasKey("client_bindings") ? world.getCompoundTag("client_bindings") : null;
			
			if (clientbindings != null) {
				for (Object o : clientbindings.func_150296_c()) {
					settings.clientKeybindMapping.put(Integer.parseInt((String) o), clientbindings.getString((String) o));
				}
			}
			
			NBTTagCompound serveraliases = world.hasKey("server_aliases") ? world.getCompoundTag("server_aliases") : null;
			
			if (serveraliases != null) {
				for (Object o : serveraliases.func_150296_c()) {
					settings.serverAliasMapping.put((String) o, serveraliases.getString((String) o));
				}
			}
			
			NBTTagCompound clientaliases = world.hasKey("client_aliases") ? world.getCompoundTag("client_aliases") : null;
			
			if (clientaliases != null) {
				for (Object o : clientaliases.func_150296_c()) {
					settings.clientAliasMapping.put((String) o, clientaliases.getString((String) o));
				}
			}
			
			NBTTagCompound waypoints = world.hasKey("waypoints") ? world.getCompoundTag("waypoints") : null;
			
			if (waypoints != null) {
				for (Object o : waypoints.func_150296_c()) {
					NBTTagCompound waypointdata = waypoints.getCompoundTag((String) o);
					
					if (waypointdata.hasKey("posX") && waypointdata.hasKey("posX") && waypointdata.hasKey("posX") && waypointdata.hasKey("yaw") && waypointdata.hasKey("pitch")) {
						settings.waypoints.put((String) o, new double[] {
								waypointdata.getDouble("posX"),
								waypointdata.getDouble("posY"),
								waypointdata.getDouble("posZ"),
								waypointdata.getDouble("yaw"),
								waypointdata.getDouble("pitch"),
						});
					}
				}
			}
			
			NBTTagCompound inventories = world.hasKey("inventories") ? world.getCompoundTag("inventories") : null;
			
			if (inventories != null) {
				for (Object o : inventories.func_150296_c()) {
					settings.inventories.put((String) o, inventories.getTagList((String) o, 10));
				}
			}
			
			return settings;
		}
		catch (Exception ex) {
			ex.printStackTrace();
			MoreCommands.getMoreCommands().getLogger().warn("Failed to read player settings for player '" + player.getCommandSenderName() + "'");
			return settings;
		}
	}
	
	public ServerPlayerSettings(EntityPlayer player) {
		this.player = player;
	}
	
	private EntityPlayer player;
	public Set<String> clientCommands = new HashSet<String>();
	
	public Map<Integer, String> serverKeybindMapping = new HashMap<Integer, String>();
	public Map<Integer, String> clientKeybindMapping = new HashMap<Integer, String>();
	public Map<String, String> serverAliasMapping = new HashMap<String, String>();
	public Map<String, String> clientAliasMapping = new HashMap<String, String>();
	public Map<String, double[]> waypoints = new HashMap<String, double[]>();
	public Map<String, NBTTagList> inventories = new HashMap<String, NBTTagList>();
	
	public boolean climb = false;
	public boolean clouds = true;
	public boolean creeperExplosion = true;
	public boolean dodrops = true;
	public boolean falldamage = true;
	public boolean firedamage = true;
	public boolean fly = false;
	public boolean noFall = false;
	public boolean justDisabled = false;
	public boolean freecam = false;
	public boolean freeezecam = false;
	public int xrayBlockRadius = 32;
	public boolean xrayEnabled = false;
	public boolean breakSpeedEnabled = false;
	public float breakspeed = 1.0F;
	public boolean infiniteitems = false;
	public boolean instantgrow = false;
	public boolean instantkill = false;
	public boolean keepinventory = false;
	public boolean killattacker = false;
	public boolean lightWorld = false;
	public boolean mobdamage = true;
	public boolean scuba = false;
	public boolean output = true;
	public Coordinate lastPos = null;
	public int superpunch = -1;
	public boolean waterdamage = true;
	
	/**
	 * Saves the settings
	 * 
	 * @return whether the settings were saved successfully
	 */
	public boolean saveSettings() {
		try{
			NBTTagCompound data = new NBTTagCompound();
			NBTTagCompound world = new NBTTagCompound();
		
			NBTTagCompound serverbindings = new NBTTagCompound();
		
			for (int keyid : this.serverKeybindMapping.keySet()) {
				serverbindings.setString(String.valueOf(keyid), this.serverKeybindMapping.get(keyid));
			}
		
			world.setTag("server_bindings", serverbindings);
			
			NBTTagCompound clientbindings = new NBTTagCompound();
			
			for (int keyid : this.clientKeybindMapping.keySet()) {
				clientbindings.setString(String.valueOf(keyid), this.clientKeybindMapping.get(keyid));
			}
		
			world.setTag("client_bindings", clientbindings);
			
			NBTTagCompound serveraliases = new NBTTagCompound();
			
			for (String alias : this.serverAliasMapping.keySet()) {
				serveraliases.setString(alias, this.serverAliasMapping.get(alias));
			}
		
			world.setTag("server_aliases", serveraliases);
			
			NBTTagCompound clientaliases = new NBTTagCompound();
			
			for (String alias : this.clientAliasMapping.keySet()) {
				clientaliases.setString(alias, this.clientAliasMapping.get(alias));
			}
		
			world.setTag("client_aliases", clientaliases);
		
			NBTTagCompound waypoints = new NBTTagCompound();
			
			for (String waypoint : this.waypoints.keySet()) {
				double[] wdata = this.waypoints.get(waypoint);
				NBTTagCompound waypointdata = new NBTTagCompound();
			
				waypointdata.setDouble("posX", wdata[0]);
				waypointdata.setDouble("posY", wdata[1]);
				waypointdata.setDouble("posZ", wdata[2]);
				waypointdata.setDouble("yaw", wdata[3]);
				waypointdata.setDouble("pitch", wdata[4]);
			
				waypoints.setTag(waypoint, waypointdata);
			}
		
			world.setTag("waypoints", waypoints);
			
			NBTTagCompound inventories = new NBTTagCompound();
			
			for (String name : this.inventories.keySet()) {
				inventories.setTag(name, this.inventories.get(name));
			}
		
			world.setTag("inventories", inventories);
			
			data.setTag(MinecraftServer.getServer().getFolderName(), world);
		
			File playerSettingsTmp = new File(Reference.getServerPlayerDir(), player.getUniqueID().toString() + ".dat.tmp");
			File playerSettings = new File(Reference.getServerPlayerDir(), player.getUniqueID().toString()  + ".dat");
			CompressedStreamTools.writeCompressed(data, new FileOutputStream(playerSettingsTmp));
		
			if (playerSettings.exists()) playerSettings.delete();
			playerSettingsTmp.renameTo(playerSettings);
			
			return true;
		}
		catch (Exception ex) {
			ex.printStackTrace();
			MoreCommands.getMoreCommands().getLogger().warn("Failed to write player settings for player '" + player.getCommandSenderName() + "'");
			return false;
		}
	}
}
