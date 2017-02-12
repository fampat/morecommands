package com.mrnobody.morecommands.settings;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mrnobody.morecommands.core.ClientProxy;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.event.EventHandler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;

/**
 * An enum of possible properties a setting can have. These properties
 * are actually a sort of constraints which must be fulfilled by settings
 * to be valid for given values.<br><br>
 * 
 * E.g. the {@link #SERVER_PROPERTY} property tells
 * the settings manager to only return settings which have a certain
 * value for this property. In this case only settings which are valid
 * on a certain server would be returned by the settings managers get() methods.<br><br>
 * 
 * It is important to note that these properties have a priority ordering which
 * corresponds to the order of the enum declarations. This allows to associate
 * an integer value to a combination of properties which represents their priority.
 * 
 * @author MrNobody
 */
public enum SettingsProperty {
	/**
	 * This property restricts settings to a certain server (identified by ip)
	 */
	SERVER_PROPERTY(null, "server", true) {
		@Override
		public String getSettingsString(EntityPlayer player) {
			return MoreCommands.getProxy().getCurrentServerNetAddress();
		}
	},
	/**
	 * This property restricts settings to a certain world
	 */
	WORLD_PROPERTY(MinecraftForge.EVENT_BUS, "world", false, PlayerRespawnEvent.class) {
		@Override
		public String getSettingsString(EntityPlayer player) {
			return MoreCommands.isClientSide() ? ((ClientProxy) MoreCommands.getProxy()).getRemoteWorldName() : 
					player.world.getSaveHandler().getWorldDirectory().getName();
		}
	},
	/**
	 * This property restricts settings to a certain dimension
	 */
	DIMENSION_POPERTY(MinecraftForge.EVENT_BUS, "dimension", false, PlayerChangedDimensionEvent.class) {
		@Override
		public String getSettingsString(EntityPlayer player) {
			return player.world.provider.getDimensionType().getName();
		}
	};
	
	private final Class<? extends PlayerEvent>[] eventClasses;
	private final boolean clientOnly;
	private String name;
	
	/**
	 * 
	 * @param bus the event bus on which changes to a property value are fired
	 * @param name a "friendly" name of this property (used to store in files)
	 * @param clientOnly whether this is a client-side only property
	 * @param eventClasses the events which are fired on a change of a this property
	 */
	private <T extends PlayerEvent> SettingsProperty(EventBus bus, String name, boolean clientOnly, Class<T>... eventClasses) {
		for (Class<T> eventClass : eventClasses)
			EventHandler.registerMethodToEventBus(bus, eventClass, this, UpdateSettingsContainer.updateSettings, Loader.instance().activeModContainer());
		
		this.eventClasses = eventClasses;
		this.name = name;
		this.clientOnly = clientOnly;
	}
	
	/**
	 * Extracts this property from a player as a string representation
	 * 
	 * @param player the player
	 * @return the string representation
	 */
	public abstract String getSettingsString(EntityPlayer player);
	
	/**
	 * @return the "friendly" name of this property
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * @return whether this is a client only property
	 */
	public boolean isClientOnly() {
		return this.clientOnly;
	}
	
	/**
	 * @return returns an integer representing the priority of this property. This is equal to 1 << ordinal()
	 */
	public int getPropertyBit() {
		return 1 << ordinal();
	}
	
	/**
	 * This method is invoked when a certain property of a player changes
	 * and this property is represented by this object. Changes of such
	 * properties are communicated through PlayerEvents. This method will
	 * invoke the {@link PlayerSettings#updateSettingsProperty(SettingsProperty, String)}
	 * method to notify the actual settings object of a player of a property change.
	 * 
	 * @param event
	 */
	@SubscribeEvent
	public void updateSetting(PlayerEvent event) {
		if (!(event.player instanceof EntityPlayerMP)) return;
		
		for (Class<? extends PlayerEvent> eventClass : this.eventClasses) {
			if (eventClass.isInstance(event)) {
				updateSetting((EntityPlayerMP) event.player, getSettingsString(event.player));
				break;
			}
		}
	}
	
	/**
	 * Updates the value of this property for a player
	 * 
	 * @param player the player for which this property is to be changed
	 * @param setting the new property value
	 */
	public void updateSetting(EntityPlayerMP player, String setting) {
		if (setting == null) return;
		ServerPlayerSettings settings = player.getCapability(PlayerSettings.SETTINGS_CAP_SERVER, null);
		
		if (setting != null)
			settings.updateSettingsProperty(this, setting);
	}
	
	/**
	 * Returns the property for which {@link #getName()} is equal to the given name
	 * 
	 * @param name the property name
	 * @return the corresponding {@link SettingsProperty}
	 */
	public static SettingsProperty getByName(String name) {
		for (SettingsProperty prop : values())
			if (prop.getName().equalsIgnoreCase(name)) return prop;
		
		return null;
	}
	
	/**
	 * Returns the properties corresponding to a bit field.
	 * This bit field equals the priority of the combination of properties
	 * 
	 * @param bits the bit field
	 * @return the corresponding properties
	 */
	public static EnumSet<SettingsProperty> getPropertiesByBits(int bits) {
		EnumSet<SettingsProperty> props = EnumSet.noneOf(SettingsProperty.class);
		
		for (SettingsProperty prop : values())
			if ((bits & prop.getPropertyBit()) != 0) props.add(prop);
		
		return props;
	}
	
	/**
	 * Returns an integer which is a bit field representing some properties
	 * This integer is equal the priority of the given properties
	 * 
	 * @param properties the properties
	 * @return the bit field
	 */
	public static int getPropertyBits(Iterable<SettingsProperty> properties) {
		int priority = 0;
		
		for (SettingsProperty property : properties)
			priority |= property.getPropertyBit();
		
		return priority;
	}
	
	/**
	 * Fetches the configuration of all properties for a player and returns them as a map
	 * 
	 * @param player the player
	 * @param exclude which properties are to be excluded
	 * @return a map from the {@link SettingsProperty} to the actual value for the given player
	 */
	public static Map<SettingsProperty, String> getPropertyMap(EntityPlayer player, SettingsProperty... exclude) {
		Set<SettingsProperty> excl = Sets.newEnumSet(Arrays.asList(exclude), SettingsProperty.class);
		Map<SettingsProperty, String> props = Maps.newEnumMap(SettingsProperty.class);
		
		for (SettingsProperty prop : values())
			if (!excl.contains(prop)) props.put(prop, prop.getSettingsString(player));
		
		return props;
	}
	
	/**
	 * A simple container class which contains the <i>updateSettings</i> method.
	 * This is required to be in an extra class since the static initializer
	 * of the SettingsProperty enum will first initialize the enum
	 * constants. However in the constructor, the <i>updateSettings</i>
	 * method is required which wouldn't be initialized at that point.
	 * To force the initialization of this field, it has to be
	 * put into an extra class.
	 * 
	 * @author MrNobody
	 */
	private static class UpdateSettingsContainer {
		/**
		 * Gets the {@link SettingsProperty#updateSetting(PlayerEvent)} method
		 */
		private static final Method getUpdateSettingsMethod() {
			Method updateSettings;
			
			try {updateSettings = SettingsProperty.class.getMethod("updateSetting", PlayerEvent.class);}
			catch (Exception ex) {throw new IllegalStateException("Couldn't fetch updateSetting method", ex);}
			
			return updateSettings;
		}
		
		/** The updateSettings Method registered to event buses */
		private static final Method updateSettings = getUpdateSettingsMethod();
	}
}
