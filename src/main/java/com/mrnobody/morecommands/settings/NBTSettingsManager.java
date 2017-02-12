package com.mrnobody.morecommands.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mrnobody.morecommands.core.MoreCommands;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTBase.NBTPrimitive;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;

/**
 * This {@link SettingsManager} implementation reads and writes settings
 * from files using the NBT format.
 * 
 * The (de)serialization process is implemented by {@link RootSettingsSerializer}.
 * Because {@link RootSettingsSerializer} works with {@link JsonElement}s, the
 * {@link NBTBase}s read from the settings file will be converted to
 * {@link JsonElement}s.<br><br>
 * 
 * A {@link NBTBase} corresponds to an {@link JsonElement}.<br>
 * A {@link NBTTagCompound} corresponds to a {@link JsonObject}.<br>
 * A {@link NBTTagString} corresponds to a {@link JsonPrimitive}.<br>
 * A {@link NBTPrimitive} corresponds to a {@link JsonPrimitive}.<br>
 * {@link NBTTagList}, {@link NBTTagIntArray} and {@link NBTTagByteArray} correspond to a {@link JsonArray}.<br>
 * 
 * @author MrNobody98
 */
public class NBTSettingsManager extends SettingsManager {
	/**
	 * Converts a {@link NBTBase} into a {@link JsonElement}
	 * 
	 * @param element the {@link NBTBase} to convert
	 * @return the converted {@link JsonElement}
	 */
    public static JsonElement toJsonElement(NBTBase element) {
		if (element instanceof NBTTagByte) return new JsonPrimitive(((NBTTagByte) element).getByte());
		else if (element instanceof NBTTagShort) return new JsonPrimitive(((NBTTagShort) element).getShort());
		else if (element instanceof NBTTagInt) return new JsonPrimitive(((NBTTagInt) element).getInt());
		else if (element instanceof NBTTagLong) return new JsonPrimitive(((NBTTagLong) element).getLong());
		else if (element instanceof NBTTagFloat) return new JsonPrimitive(((NBTTagFloat) element).getFloat());
		else if (element instanceof NBTTagDouble) return new JsonPrimitive(((NBTTagDouble) element).getDouble());
		else if (element instanceof NBTTagString) return new JsonPrimitive(((NBTTagString) element).getString());
		else if (element instanceof NBTTagByteArray) {
			JsonArray e = new JsonArray();
			for (byte b : ((NBTTagByteArray) element).getByteArray()) e.add(new JsonPrimitive(b));
			return e;
		}
		else if (element instanceof NBTTagIntArray) {
			JsonArray e = new JsonArray();
			for (int i : ((NBTTagIntArray) element).getIntArray()) e.add(new JsonPrimitive(i));
			return e;
		}
		else if (element instanceof NBTTagList) {
			JsonArray e = new JsonArray();
			for (int i = 0; i < ((NBTTagList) element).tagCount(); i++) e.add(toJsonElement(((NBTTagList) element).get(i)));
			return e;
		}
		else if (element instanceof NBTTagCompound) {
			JsonObject e = new JsonObject();
			for (String key : (Set<String>) ((NBTTagCompound) element).getKeySet()) e.add(key, toJsonElement(((NBTTagCompound) element).getTag(key)));
			return e;
		}
		else return null;
	}
	
	/**
	 * Converts a {@link JsonElement} into an {@link NBTBase}
	 * 
	 * @param element the {@link JsonElement} to convert
	 * @return the converted {@link NBTBase}
	 */
	public static NBTBase toNBTElement(JsonElement element) {
		if (element.isJsonArray()) {
			NBTTagList list = new NBTTagList();
			for (JsonElement elem : element.getAsJsonArray()) list.appendTag(toNBTElement(elem));
			return list;
		}
		else if (element.isJsonObject()) {
			NBTTagCompound compound = new NBTTagCompound();
			for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) compound.setTag(entry.getKey(), toNBTElement(entry.getValue()));
			return compound;
		}
		else if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
			Number num = element.getAsJsonPrimitive().getAsNumber();
			
			if (num instanceof Byte) return new NBTTagByte(num.byteValue());
			else if (num instanceof Short) return new NBTTagShort(num.shortValue());
			else if (num instanceof Integer) return new NBTTagInt(num.intValue());
			else if (num instanceof Long) return new NBTTagLong(num.longValue());
			else if (num instanceof Float) return new NBTTagFloat(num.floatValue());
			else if (num instanceof Double) return new NBTTagDouble(num.doubleValue());
			else return new NBTTagDouble(num.doubleValue());
		}
		else if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) 
			return new NBTTagString(element.getAsJsonPrimitive().getAsString());
		else return null;
	}
    
	private boolean failed = false, loaded = false;
	private SetMultimap<String, Setting<?>> settings = HashMultimap.create();
	private final File file;
	
	/**
	 * Constructs a new NBTSettingsManager
	 * 
	 * @param file the file from which settings are read and to which settings are written. Must be in NBT format
	 */
	public NBTSettingsManager(File file) {
		this(file, false);
	}
	
	/**
	 * Constructs a new NBTSettingsManager
	 * 
	 * @param file the file from which settings are read and to which settings are written. Must be in NBT format
	 * @param isClient whether this is a client side settings manager
	 */
	public NBTSettingsManager(File file, boolean isClient) {
		this(file, isClient, false);
	}
	
	/**
	 * Constructs a new NBTSettingsManager
	 * 
	 * @param file the file from which settings are read and to which settings are written. Must be in NBT format
	 * @param isClient whether this is a client side settings manager
	 * @param load whether to read the settings immediately
	 */
	public NBTSettingsManager(File file, boolean isClient, boolean load) {
		super(isClient);
		this.file = file;
		if (load) loadSettings();
	}
	
	/**
	 * Constructs a new NBTSettingsManager
	 * 
	 * @param file the file from which settings are read and to which settings are written. Must be in NBT format
	 * @param isClient whether this is a client side settings manager
	 * @param defaultSerializer the default (de)serializer used if a {@link Setting} occurs for which 
	 *        a {@link SettingsSerializer} has not been registered
	 * @param load whether to read the settings immediately
	 */
	public NBTSettingsManager(File file, boolean isClient, SettingsSerializer<Object> defaultSerializer, boolean load) {
		super(defaultSerializer, isClient);
		this.file = file;
		if (load) loadSettings();
	}
	
	/**
	 * Gets a player by his UUID
	 * 
	 * @param uuid the player's UUID
	 * @return the player or null if there is none with this id
	 */
	private EntityPlayerMP getPlayerByUUID(UUID uuid) {
		for (EntityPlayerMP player : (List<EntityPlayerMP>) MinecraftServer.getServer().getConfigurationManager().playerEntityList)
			if (uuid.equals(player.getUniqueID())) return player;
		return null;
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
		FileInputStream fis = null;
		this.loaded = true;
		this.failed = false;
		
		try {
			if (!this.file.exists() || !this.file.isFile()) return;
			fis = new FileInputStream(this.file);
			NBTTagCompound data = CompressedStreamTools.readCompressed(fis);
			this.deserializeSettings(toJsonElement(data));
		}
		catch (IOException ex) {
			UUID uuid = null; try {uuid = UUID.fromString(this.file.getName().split("\\.")[0]);} catch (IllegalArgumentException e) {}
			EntityPlayerMP player = uuid == null ? null : getPlayerByUUID(uuid);
			
			MoreCommands.INSTANCE.getLogger().info("Error reading command config " + 
					(player == null ? "from file " + this.file.getName() : "for player " + player.getName()));
			
			this.failed = true;
		}
		finally {
			try {if (fis != null) fis.close();}
			catch (IOException ex) {}
		}
	}
	
	@Override
	public synchronized void saveSettings() {
		//if no settings have been loaded, writing them will result in an empty file and all settings are gone
		if (!this.loaded) return;
		
		if (this.failed) {
			MoreCommands.INSTANCE.getLogger().info("Command Config won't be saved, because reading failed");
			return;
		}
		
		FileOutputStream fos = null;
		
		try {
			if (!this.file.exists() || !this.file.isFile()) this.file.createNewFile();
			fos = new FileOutputStream(this.file);
			NBTBase data = toNBTElement(this.serializeSettings());
			if (data instanceof NBTTagCompound) CompressedStreamTools.writeCompressed((NBTTagCompound) data, fos);
			else MoreCommands.INSTANCE.getLogger().info("Failed to write settings to file " + this.file.getName() + ": settings is not a NBTTagCompound");
		}
		catch (IOException ex) {
			UUID uuid = null; try {uuid = UUID.fromString(this.file.getName().split("\\.")[0]);} catch (IllegalArgumentException e) {}
			EntityPlayerMP player = uuid == null ? null : getPlayerByUUID(uuid);
			
			MoreCommands.INSTANCE.getLogger().info("Error writing command config " + 
					(player == null ? "to file " + this.file.getName() : "for player " + player.getName()));
		}
		finally {
			try {if (fos != null) fos.close();}
			catch (IOException ex) {}
		}
	}
	
	@Override
	public boolean isLoaded() {
		return this.loaded;
	}
}
