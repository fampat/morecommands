package com.mrnobody.morecommands.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
 * The (de)serialization process is implemented by {@link SettingsSerializable}.
 * Because {@link SettingsSerializable} works with {@link AbstractElement}s, the
 * {@link NBTBase}s read from the settings file will be converted to
 * {@link AbstractElement}s.<br><br>
 * 
 * A {@link NBTBase} corresponds to an {@link AbstractElement}.<br>
 * A {@link NBTTagCompound} corresponds to a {@link ObjectElement}.<br>
 * A {@link NBTTagString} corresponds to a {@link StringElement}.<br>
 * A {@link NBTPrimitive} corresponds to a {@link NumericElement}.<br>
 * {@link NBTTagList}, {@link NBTTagIntArray} and {@link NBTTagByteArray} correspond to a {@link ListElement}.<br>
 * 
 * @author MrNobody98
 */
public class NBTSettingsManager extends SettingsManager {
	/**
	 * Converts a {@link NBTBase} into an {@link AbstractElement}
	 * 
	 * @param element the {@link NBTBase} to convert
	 * @return the converted {@link AbstractElement}
	 */
    public static AbstractElement toAbstractElement(NBTBase element) {
		if (element instanceof NBTTagByte) return new NumericElement(((NBTTagByte) element).getByte());
		else if (element instanceof NBTTagShort) return new NumericElement(((NBTTagShort) element).getShort());
		else if (element instanceof NBTTagInt) return new NumericElement(((NBTTagInt) element).getInt());
		else if (element instanceof NBTTagLong) return new NumericElement(((NBTTagLong) element).getLong());
		else if (element instanceof NBTTagFloat) return new NumericElement(((NBTTagFloat) element).getFloat());
		else if (element instanceof NBTTagDouble) return new NumericElement(((NBTTagDouble) element).getDouble());
		else if (element instanceof NBTTagString) return new StringElement(((NBTTagString) element).getString());
		else if (element instanceof NBTTagByteArray) {
			ListElement e = new ListElement();
			for (byte b : ((NBTTagByteArray) element).getByteArray()) e.add(new NumericElement(b));
			return e;
		}
		else if (element instanceof NBTTagIntArray) {
			ListElement e = new ListElement();
			for (int i : ((NBTTagIntArray) element).getIntArray()) e.add(new NumericElement(i));
			return e;
		}
		else if (element instanceof NBTTagList) {
			ListElement e = new ListElement();
			for (int i = 0; i < ((NBTTagList) element).tagCount(); i++) e.add(toAbstractElement(((NBTTagList) element).get(i)));
			return e;
		}
		else if (element instanceof NBTTagCompound) {
			ObjectElement e = new ObjectElement();
			for (String key : (Set<String>) ((NBTTagCompound) element).getKeySet()) e.add(key, toAbstractElement(((NBTTagCompound) element).getTag(key)));
			return e;
		}
		else return null;
	}
	
	/**
	 * Converts a {@link AbstractElement} into an {@link NBTBase}
	 * 
	 * @param element the {@link AbstractElement} to convert
	 * @return the converted {@link NBTBase}
	 */
	public static NBTBase toNBTElement(AbstractElement element) {
		if (element.isList()) {
			NBTTagList list = new NBTTagList();
			for (AbstractElement elem : element.asList()) list.appendTag(toNBTElement(elem));
			return list;
		}
		else if (element.isObject()) {
			NBTTagCompound compound = new NBTTagCompound();
			for (Map.Entry<String, AbstractElement> entry : element.asObject().entrySet()) compound.setTag(entry.getKey(), toNBTElement(entry.getValue()));
			return compound;
		}
		else if (element.isNumeric()) {
			if (element.asNumericElement().asNumber() instanceof Byte) return new NBTTagByte(element.asNumericElement().asNumber().byteValue());
			else if (element.asNumericElement().asNumber() instanceof Short) return new NBTTagShort(element.asNumericElement().asNumber().shortValue());
			else if (element.asNumericElement().asNumber() instanceof Integer) return new NBTTagInt(element.asNumericElement().asNumber().intValue());
			else if (element.asNumericElement().asNumber() instanceof Long) return new NBTTagLong(element.asNumericElement().asNumber().longValue());
			else if (element.asNumericElement().asNumber() instanceof Float) return new NBTTagFloat(element.asNumericElement().asNumber().floatValue());
			else if (element.asNumericElement().asNumber() instanceof Double) return new NBTTagDouble(element.asNumericElement().asNumber().doubleValue());
			else return new NBTTagDouble(element.asNumericElement().asNumber().doubleValue());
		}
		else if (element.isString()) return new NBTTagString(element.asStringElement().asString());
		else return null;
	}
    
	private boolean failed = false, loaded = false;
	
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
	 * @param load whether to read the settings immediately
	 */
	public NBTSettingsManager(File file, boolean load) {
		this(file, load, true);
	}
	
	/**
	 * Constructs a new NBTSettingsManager
	 * 
	 * @param file the file from which settings are read and to which settings are written. Must be in NBT format
	 * @param load whether to read the settings immediately
	 * @param useServer whether to read and write server dependencies of settings (See {@link Setting} for more details)
	 */
	public NBTSettingsManager(File file, boolean load, boolean useServer) {
		super(file, load, useServer);
	}
	
	/**
	 * Constructs a new NBTSettingsManager
	 * 
	 * @param file the file from which settings are read and to which settings are written. Must be in NBT format
	 * @param load whether to read the settings immediately
	 * @param defaultSerializable the default (de)serializer used if a {@link Setting} occurs for which 
	 *        a {@link Serializable} has not been registered
	 * @param useServer whether to read and write server dependencies of settings (See {@link Setting} for more details)
	 */
	public NBTSettingsManager(File file, boolean load, Serializable<Object> defaultSerializer, boolean useServer) {
		super(file, load, defaultSerializer, useServer);
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
	public synchronized void loadSettings() {
		FileInputStream fis = null;
		this.loaded = true;
		this.failed = false;
		
		try {
			if (!this.file.exists() || !this.file.isFile()) return;
			fis = new FileInputStream(this.file);
			NBTTagCompound data = CompressedStreamTools.readCompressed(fis);
			this.settings = this.serializable.deserialize(toAbstractElement(data));
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
			NBTBase data = toNBTElement(this.serializable.serialize(this.settings));
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
