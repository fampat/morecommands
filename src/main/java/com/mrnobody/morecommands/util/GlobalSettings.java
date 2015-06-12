package com.mrnobody.morecommands.util;

import java.io.File;

/**
 * A class containing global settings
 * 
 * @author MrNobody98
 *
 */
public class GlobalSettings {
	public static boolean welcome_message = true;
	public static boolean enableVars = true;
	
	//public static boolean creeperExplosion = true;
	//public static boolean freezeEntities = false;
	public static boolean dodrops = true;
	public static boolean explosions = true;
	public static boolean endermanpickup = true;
	public static boolean clearwater = false;
	public static boolean itemdamage = true;
	
	/**
	 * Reads settings from the settings file
	 */
	public static void readSettings() {
		Settings settings = new Settings(new File(Reference.getModDir(), "settings.cfg"), true);
		
		GlobalSettings.welcome_message = settings.getBoolean("welcome_message", true);
		GlobalSettings.enableVars = settings.getBoolean("enableVars", true);
		
		//those settings are not intended to be read or written
		
		//GlobalSettings.creeperExplosion = settings.getBoolean("creeperExplosion", true);
		//GlobalSettings.dodrops = settings.getBoolean("dodrops", true);
		//GlobalSettings.endermanpickup = settings.getBoolean("endermanpickup", true);
		//GlobalSettings.explosions = settings.getBoolean("explosions", true);
		//GlobalSettings.clearwater = settings.getBoolean("clearwater", false);
		//GlobalSettings.freezeEntities = settings.getBoolean("freezeEntities", false);
		//GlobalSettings.itemdamage = settings.getBoolean("itemdamage", true);
	}
	
	/**
	 * Writes settings to the settings file
	 */
	public static void writeSettings() {
		Settings settings = new Settings(new File(Reference.getModDir(), "settings.cfg"), true);
		
		settings.set("welcome_message", GlobalSettings.welcome_message);
		settings.set("enableVars", GlobalSettings.enableVars);
		
		//those settings are not intended to be read or written
		
		//settings.set("creeperExplosion", GlobalSettings.creeperExplosion);
		//settings.set("dodrops", GlobalSettings.dodrops);
		//settings.set("endermanpickup", GlobalSettings.endermanpickup);
		//settings.set("explosions", GlobalSettings.explosions);
		//settings.set("clearwater", GlobalSettings.clearwater);
		//settings.set("freezeEntities", GlobalSettings.freezeEntities);
		//settings.set("itemdamage", GlobalSettings.itemdamage);
		
		settings.save();
	}
}
