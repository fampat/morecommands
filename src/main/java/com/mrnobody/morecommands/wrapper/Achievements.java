package com.mrnobody.morecommands.wrapper;

import java.lang.reflect.Field;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mrnobody.morecommands.util.ReflectionHelper;

import net.minecraft.stats.Achievement;
import net.minecraft.stats.AchievementList;

/**
 * A wrapper for achievements
 * 
 * @author MrNobody98
 */
public class Achievements
{
	/**
	 * The name<->achievement mapping
	 */
	private static final BiMap<String, Achievement> achievements;
	
	static {
		ImmutableBiMap.Builder<String, Achievement> builder = ImmutableBiMap.builder();
		Field name = ReflectionHelper.getField(Achievement.class, "achievementDescription");
		
		try {
			Object achievement;
			Object desc;
			
			for (Field field : AchievementList.class.getFields()) {
				achievement = field.get(null);
				
				if (achievement != null && achievement instanceof Achievement) {
					desc = name.get(achievement);
					
					if (desc instanceof String) {
						builder.put(((String) desc).split("\\.")[1], (Achievement) achievement);
					}
				}
			}
		}
		catch (Exception ex) {ex.printStackTrace();}
		
		achievements = builder.build();
	}
	
	/**
	 * Gets an achievement by its name
	 * 
	 * @return The achievement or null if it wasn't found
	 */
	public static Achievement getAchievementByName(String name) {
		return achievements.getOrDefault(name, null);
	}
	
	/**
	 * Gets the parent achievement name
	 * 
	 * @return The parent achievement name or null if it wasn't found
	 */
	public static String getAchievementRequirement(String name) {
		return achievements.getOrDefault(name, null) != null ? achievements.inverse().getOrDefault(achievements.get(name).parentAchievement, null) : null;
	}
	
	/**
	 * Gets the achievement list
	 * 
	 * @return the achievement array
	 */
	public static String[] getAchievementNameList() {
		return achievements.keySet().toArray(new String[achievements.keySet().size()]);
	}
}
