package com.mrnobody.morecommands.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * A class helping to do reflections
 * 
 * @author MrNobody98
 *
 */
public class ReflectionHelper {
	/**
	 * @return the field or null
	 */
	public static Field getField(Class<?> clazz, String ... fieldNames) {
		if(clazz == null) {
			System.err.println("No class specified.");
			return null;
		}
		if(fieldNames == null || fieldNames.length < 1) {
			System.err.println("No field name(s) specified.");
			return null;
		}
		
		Field field = null;
		String unobfuscatedName = null;
		for(String fieldName : fieldNames) {
			try {field = clazz.getDeclaredField(fieldName); break;} 
			catch (NoSuchFieldException nsfe) {
				unobfuscatedName = ObfuscationHelper.getObfuscatedName(fieldName);
				
				if (unobfuscatedName != null) {
					try {field = clazz.getDeclaredField(unobfuscatedName); break;}
					catch (NoSuchFieldException e) {continue;}
				}
			}
		}
		
		if(field == null) {
			System.err.println(clazz.getName() + " does not have field " + fieldNames[0]);
			return null; 
		} else {
			try {
				field.setAccessible(true);
			} catch (SecurityException se) {}
				return field;
		}
	}
	
	/**
	 * @return the field or null
	 */
	public static Field getField(Object instance, String ... fieldNames) {
		if(instance != null) {
			return getField(instance.getClass(), fieldNames);
		} 
		else {
			return null;
		}
	}
	
	/**
	 * Sets a field
	 * 
	 * @return whether the field was set successfully
	 */
	public static boolean setField(Field field, Object instance, Object value) {
		if (field == null) {
			System.err.println("Null field");
			return false;
		}
		
		try {
			field.set(instance, value);
		} catch (Exception e) {
			System.err.println(field.getType() + " not assignable from " + value.getClass());
			return false;
		}
		return true;
	}
	
	/**
	 * @return the method or null
	 */
	public static Method getMethod(Class<?> clazz, String methodName, Class<?> ... params) {
		return getMethod(clazz, new String[]{methodName}, params);
	}
	
	/**
	 * @return the method or null
	 */
	public static Method getMethod(Class<?> clazz, String[] methodNames, Class<?> ... params) {
		if(clazz == null) {
			System.err.println("No class specified.");
			return null;
		}
		if(methodNames == null || methodNames.length < 1) {
			System.err.println("No methodNames specified.");
			return null;
		}
		Method method = null;
		String unobfuscatedName;
		for(String methodName : methodNames) {
			try {
				method = clazz.getDeclaredMethod(methodName, params);
				break;
			} catch (NoSuchMethodException nsfe) {
				unobfuscatedName = ObfuscationHelper.getObfuscatedName(methodName);
				
				if (unobfuscatedName != null) {
					try {method = clazz.getDeclaredMethod(unobfuscatedName, params); break;}
					catch (NoSuchMethodException e) {continue;}
				}
			} catch (NoClassDefFoundError ncdfe) {
				continue;
			}
		}
		if(method == null) {
			System.err.println(clazz.getName() + " does not have method " + methodNames[0]);
			return null; 
		} else {
			try {
				method.setAccessible(true);
			} catch (SecurityException se) {}
				return method;
		}
	} 
}
