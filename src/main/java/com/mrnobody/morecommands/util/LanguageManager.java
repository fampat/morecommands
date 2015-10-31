package com.mrnobody.morecommands.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.mrnobody.morecommands.core.MoreCommands;

/**
 * A class managing languages included in this mod.
 * Forge registers languages only client side. <br>
 * Clients which don't have this mod installed wouldn't
 * get a translation.
 * 
 * @author MrNobody98
 *
 */
public class LanguageManager {
	public static final String defaultLanguage = "en_US";
	private static final Splitter equalSignSplitter = Splitter.on('=').limit(2);
	
	private static final FilenameFilter filter = new FilenameFilter() {
		@Override 
		public boolean accept(File dir, String name) {
			return name.endsWith(".lang");
		}
	};
	
	public static final Map<String, Map<String, String>> languages = new HashMap<String, Map<String, String>>();
	
	/**
	 * Reads translations
	 */
	public static void readTranslations() {
		Map<String, File> langs = MoreCommands.CLASSLOADER.getResources("assets." + Reference.MODID + ".lang", filter);
		if (langs == null) return;
		Map<String, String> map;
		BufferedReader reader;
		String line;
		String[] translate;
		File lang;
	
		for (String filename : langs.keySet()) {
			try {
				map = new HashMap<String, String>();
				lang = langs.get(filename);
				languages.put(filename.split("\\.")[0], map);
				
				reader = new BufferedReader(new FileReader(lang));
					
				while ((line = reader.readLine()) != null) {
					translate = (String[]) Iterables.toArray(equalSignSplitter.split(line), String.class);
					if (translate.length > 1) map.put(translate[0], translate[1]);
				}
				
				reader.close();
				MoreCommands.getMoreCommands().getLogger().info("Language '" + filename.split("\\.")[0] + "' successfully loaded");
			}
			catch (Exception ex) {ex.printStackTrace();}
		}
	}
	
	/**
	 * Gets a translation
	 */
	public static String translate(String locale, String untranlated, Object... formatArgs) {
		Map<String, String> translation;
		
		if (languages.containsKey(locale)) translation = languages.get(locale);
		else translation = languages.get(defaultLanguage);
		
		if (translation != null && translation.containsKey(untranlated)) 
			return String.format(translation.get(untranlated), formatArgs);
		else return untranlated;
	}
}
