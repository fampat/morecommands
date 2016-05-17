package com.mrnobody.morecommands.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.util.ObfuscatedNames.ObfuscatedField;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTBase.NBTPrimitive;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagEnd;
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
	/** The field of the list list in which the tags in a {@link NBTTagList} are stored */
    private static final Field list = ReflectionHelper.getField(ObfuscatedField.NBTTagList_tagList);
    
    /**
     * Gets the {@link NBTBase} of a {@link NBTTagList} at a specified index
     * 
     * @param tagList the tag list
     * @param idx the index
     * @return the tag or null, if the index is out of bounds or the tag list could not be accessed
     */
    private static final NBTBase getTag(NBTTagList tagList, int idx) {
    	try {
    		if (NBTSettingsManager.list == null) return null;
    		List list = (List) NBTSettingsManager.list.get(tagList);
    		if (list == null) return null;
    		return (NBTBase)(idx >= 0 && idx < list.size() ? (NBTBase)list.get(idx) : new NBTTagEnd());
    	}
    	catch (Exception ex) {return null;}
    }
    
	/**
	 * Converts a {@link NBTBase} into an {@link AbstractElement}
	 * 
	 * @param element the {@link NBTBase} to convert
	 * @return the converted {@link AbstractElement}
	 */
    public static AbstractElement toAbstractElement(NBTBase element) {
		if (element instanceof NBTTagByte) return new NumericElement(((NBTTagByte) element).func_150290_f());
		else if (element instanceof NBTTagShort) return new NumericElement(((NBTTagShort) element).func_150289_e());
		else if (element instanceof NBTTagInt) return new NumericElement(((NBTTagInt) element).func_150287_d());
		else if (element instanceof NBTTagLong) return new NumericElement(((NBTTagLong) element).func_150291_c());
		else if (element instanceof NBTTagFloat) return new NumericElement(((NBTTagFloat) element).func_150288_h());
		else if (element instanceof NBTTagDouble) return new NumericElement(((NBTTagDouble) element).func_150286_g());
		else if (element instanceof NBTTagString) return new StringElement(((NBTTagString) element).func_150285_a_());
		else if (element instanceof NBTTagByteArray) {
			ListElement e = new ListElement();
			for (byte b : ((NBTTagByteArray) element).func_150292_c()) e.add(new NumericElement(b));
			return e;
		}
		else if (element instanceof NBTTagIntArray) {
			ListElement e = new ListElement();
			for (int i : ((NBTTagIntArray) element).func_150302_c()) e.add(new NumericElement(i));
			return e;
		}
		else if (element instanceof NBTTagList) {
			ListElement e = new ListElement();
			for (int i = 0; i < ((NBTTagList) element).tagCount(); i++) e.add(toAbstractElement(getTag((NBTTagList) element, i)));
			return e;
		}
		else if (element instanceof NBTTagCompound) {
			ObjectElement e = new ObjectElement();
			for (String key : (Set<String>) ((NBTTagCompound) element).func_150296_c()) e.add(key, toAbstractElement(((NBTTagCompound) element).getTag(key)));
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
		super(load, useServer);
		this.file = file;
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
		super(load, defaultSerializer, useServer);
		this.file = file;
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
	public SetMultimap<String, Setting<?>> getSettings() {
		return this.settings;
	}

	@Override
	public void setSettings(SetMultimap<String, Setting<?>> settings) {
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
			this.deserializeSettings(toAbstractElement(data));
		}
		catch (IOException ex) {
			UUID uuid = null; try {uuid = UUID.fromString(this.file.getName().split("\\.")[0]);} catch (IllegalArgumentException e) {}
			EntityPlayerMP player = uuid == null ? null : getPlayerByUUID(uuid);
			
			MoreCommands.INSTANCE.getLogger().info("Error reading command config " + 
					(player == null ? "from file " + this.file.getName() : "for player " + player.getCommandSenderName()));
			
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
					(player == null ? "to file " + this.file.getName() : "for player " + player.getCommandSenderName()));
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
