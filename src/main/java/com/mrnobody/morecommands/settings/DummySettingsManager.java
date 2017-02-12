package com.mrnobody.morecommands.settings;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

/**
 * A dummy SettingsManager that has no load/save capabilities
 * 
 * @author MrNobody98
 */
public class DummySettingsManager extends SettingsManager {
	public DummySettingsManager(boolean isClient) {
		super(isClient);
	}
	
	@Override 
	public void loadSettings() {}
	
	@Override 
	public void saveSettings() {}
	
	@Override 
	public boolean isLoaded() {return false;}
	
	@Override 
	protected SetMultimap<String, Setting<?>> getSettings() {return HashMultimap.create();}
	
	@Override 
	protected void setSettings(SetMultimap<String, Setting<?>> settings) {}
}
