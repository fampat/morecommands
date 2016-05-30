package com.mrnobody.morecommands.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * A custom settings class extending {@link Properties}
 * 
 * @author MrNobody98
 *
 */
public class Settings extends Properties {
	private File settings;
	
	public Settings() {
		super();
	}
   
	public Settings(File f) {
		this(f, true);
	}

	public Settings(File f, boolean load) {
		super();
		this.settings = f;
		if (load) load(f);
	}
   
	/**
	 * Sets a boolean value
	 */
	public void set(String key, boolean value) {
		setProperty(key, new Boolean(value).toString());
	}
   
	/**
	 * @return a boolean value
	 */
	public boolean getBoolean(String key, boolean base) {
		String value = getProperty(key);
		try {
			return (value == null || value.trim().equalsIgnoreCase("")) ? base : new Boolean(value);
		} catch (Exception e) {
			return base;
		}
	}

	/**
	 * Sets an integer value
	 */
	public void set(String key, int value) {
		setProperty(key,new Integer(value).toString());
	}

	/**
	 * @return an integer value
	 */
	public int getInteger(String key, int base) {
		String value = getProperty(key);
		try {
			return isEmpty(value) ? base : new Integer(value);
		} catch (NumberFormatException e) {
			return base;
		}
	}

	/**
	 * Sets a char value
	 */
	public void set(String key, char value) {
		setProperty(key,new Character(value).toString());
	}

	/**
	 * @return a char value
	 */
	public char getCharacter(String key, char base) {
		String value = getProperty(key);
		try {
			return isEmpty(value) ? base : value.charAt(0);
		} catch (NumberFormatException e) {
			return base;
		}
	}

	/**
	 * Sets a double value
	 */
	public void set(String key, double value) {
		setProperty(key,new Double(value).toString());
	}

	/**
	 * @return a double value
	 */
	public double getDouble(String key, double base) {
		String value = getProperty(key);
		try {
			return isEmpty(value) ? base : new Double(value);
		} catch (NumberFormatException e) {
			return base;
		}
	}

	/**
	 * Sets a float value
	 */
	public void set(String key, float value) {
		setProperty(key,new Float(value).toString());
	}

	/**
	 * @return a float value
	 */
	public float getFloat(String key, float base) {
		String value = getProperty(key);
		try {
			return isEmpty(value) ? base : new Float(value);
		} catch (NumberFormatException e) {
			return base;
		}
	}

	/**
	 * Sets a String value
	 */
	public void set(String key, String value) {
		setProperty(key,value);
	}

	/**
	 * @return a String value
	 */
	public String getString(String key, String base) {
		String value = getProperty(key);
		return isEmpty(value) ? base : value;
	}

	/**
	 * Saves the settings file
	 */
	public boolean save() {
		return this.save("");
	}

	/**
	 * Saves the settings file with a header
	 */
	public boolean save(String header) {
		return this.save(this.settings, header);
	}

	/**
	 * Saves the settings to the given file
	 */
	public boolean save(File file, String header) {
		if (file == null || file.isDirectory()) {
			return false;
		}
		FileOutputStream fos = null;
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
		fos = new FileOutputStream(file);
		super.store(fos, header);
		return true;
		} catch (Exception e) {
			return false;
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {}
			}
      	}
	}

	/**
	 * Loads the settings
	 * 
	 * @return whether the settings were loaded successully
	 */
	public boolean load() {
		return this.load(this.settings);
	}

	/**
	 * Loads the settings from the given file
	 * 
	 * @return whether the settings were loaded successully
	 */
	public boolean load(File file) {
		if (file == null || file.isDirectory()) {
			return false;
		}
		try {
			if (!file.exists()) {
				file.createNewFile();
				return true;
			}
			super.load(new FileInputStream(file));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * @return The settings file
	 */
	public File getFile() {
		return this.settings;
	}

	/**
	 * Sets the settings file
	 */
	public void setFile(File settings) {
		this.settings = settings;
	}

	/**
	 * @return whether the given string is empty
	 */
	private boolean isEmpty(String value) {
		return (value == null || value.trim().equalsIgnoreCase(""));
	}

	@Override
	public Object clone() {
		return new Settings(this.settings);
	}
}
