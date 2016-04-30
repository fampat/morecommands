package com.mrnobody.morecommands.util;

import java.io.File;
import java.util.Date;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * A class containing information on the mod, such as mod id, version <br>
 * paths, etc.
 * 
 * @author MrNobody98
 *
 */
public final class Reference {
	/** the mod id */
	public static final String MODID = "mrnobody_morecommands";
	/** the mod version */
	public static final String VERSION = "3.0";
	/** the mod name */
	public static final String NAME = "MoreCommands";
	/** the mod network channel name */
	public static final String CHANNEL = "mrnobody_cmd";
	/** The website URL */
	public static final String WEBSITE = "http://bit.ly/morecommands";
	/** The build date */
    public static final Date BUILD = new Date(System.currentTimeMillis()); //gets replaced during build process
	
    /** @return the mod configuration directory */
	public static final File getModDir() {return Reference.INSTANCE.MOD_DIR;}
	/** @return the directory where the mod saves server player data */
	public static final File getSettingsDirServer() {return Reference.INSTANCE.SETTINGS_DIR_SERVER;}
	
	private static Reference INSTANCE;
	
	private final File SETTINGS_DIR_SERVER;
	private final File MOD_DIR;
	
	private Reference(FMLPreInitializationEvent event) {
		this.MOD_DIR = new File(event.getModConfigurationDirectory(), "morecommands");
		if (!this.MOD_DIR.exists()) this.MOD_DIR.mkdirs();
		
		this.SETTINGS_DIR_SERVER = new File(this.MOD_DIR, "settings_server");
		if (!this.SETTINGS_DIR_SERVER.exists()) this.SETTINGS_DIR_SERVER.mkdirs();
	}
	/**
	 * Initializes the the configuration directories from
	 * {@link FMLPreInitializationEvent#getModConfigurationDirectory()}
	 */
	public static final void init(FMLPreInitializationEvent event) {
		if (INSTANCE == null) Reference.INSTANCE = new Reference(event);
	}
}
