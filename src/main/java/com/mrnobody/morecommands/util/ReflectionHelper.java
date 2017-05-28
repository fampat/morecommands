package com.mrnobody.morecommands.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.google.common.base.Joiner;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.util.ObfuscatedNames.ObfuscatedField;
import com.mrnobody.morecommands.util.ObfuscatedNames.ObfuscatedMethod;

/**
 * A class helping to do reflections
 * 
 * @author MrNobody98
 *
 */
public final class ReflectionHelper {
	private static final Joiner commanJoiner = Joiner.on(',');
	
	private ReflectionHelper() {}
	
	/**
	 * Gets the actual {@link Field} object from the owner class of an {@link ObfuscatedField}
	 * 
	 * @param <O> the field's owner type
	 * @param <T> the field's type
	 * @param obfusctatedField the {@link ObfuscatedField} from which to get the {@link Field} object
	 * @return the {@link Field} object or null if any exception occurred
	 */
	public static <O, T> Field getField(ObfuscatedField<O, T> obfusctatedField) {
		try {
			Field field = null;
			
			if (obfusctatedField.getEnvName() == null) {
				try {field = obfusctatedField.getOwnerClass().getDeclaredField(obfusctatedField.getDeobfName());}
				catch (Exception ex) {field = obfusctatedField.getOwnerClass().getDeclaredField(obfusctatedField.getObfName());}
			}
			else field = obfusctatedField.getOwnerClass().getDeclaredField(obfusctatedField.getEnvName());
			
			field.setAccessible(true);
			return field;
		}
		catch (Exception ex) {
			MoreCommands.INSTANCE.getLogger().trace("Failed to obtain field " + obfusctatedField.getOwnerClass().getName() + "#" + 
					(obfusctatedField.getEnvName() == null ? obfusctatedField.getDeobfName() + "/" + obfusctatedField.getObfName() : obfusctatedField.getEnvName()), ex);
			
			return null;
		}
	}
	
	/**
	 * Gets the current value of an {@link ObfuscatedField} from an instance of the fields owner type 
	 * 
	 * @param <O> the field's owner type
	 * @param <T> the field's type
	 * @param obfusctatedField the {@link ObfuscatedField} from which to get the current value
	 * @param instance the instance from which to get the field value, null if static
	 * @return the current value of the field for the given instance
	 */
	public static <O, T> T get(ObfuscatedField<O, T> obfusctatedField, O instance) {
		Field field = getField(obfusctatedField);
		if (field == null) return  null;
		else return get(obfusctatedField, field, instance);
	}
	
	/**
	 * Gets the current value of an {@link ObfuscatedField} from an instance of the fields owner type 
	 * 
	 * @param <O> the field's owner type
	 * @param <T> the field's type
	 * @param obfusctatedField the {@link ObfuscatedField} from which to get the current value
	 * @param field the field that is represented by <i>obfuscatedField</i> (can be fetched via {@link #getField(ObfuscatedField)})
	 * @param instance the instance from which to get the field value, null if static
	 * @return the current value of the field for the given instance
	 */
	public static <O, T> T get(ObfuscatedField<O, T> obfusctatedField, Field field, O instance) {
		if (field.getDeclaringClass() != obfusctatedField.getOwnerClass()) {
				MoreCommands.INSTANCE.getLogger().trace("Invalid field object: " + field.getDeclaringClass().getName() + 
						"#" + field.getName() + ", required: " + obfusctatedField.getOwnerClass().getName() + "#" + 
						(obfusctatedField.getEnvName() == null ? obfusctatedField.getDeobfName() + "/" + obfusctatedField.getObfName() : obfusctatedField.getEnvName()));
				
				return null;
		}
		else {
			try {return obfusctatedField.getTypeClass().cast(field.get(instance));}
			catch (Exception ex) {
				MoreCommands.INSTANCE.getLogger().trace("Failed to obtain field value from field " + obfusctatedField.getOwnerClass().getName() + "#" + 
						(obfusctatedField.getEnvName() == null ? obfusctatedField.getDeobfName() + "/" + obfusctatedField.getObfName() : obfusctatedField.getEnvName()), ex);
				
				return null;
			}
		}
	}
	
	/**
	 * Sets the current value of an {@link ObfuscatedField} for an instance of the fields owner type 
	 * 
	 * @param <O> the field's owner type
	 * @param <T> the field's type
	 * @param obfusctatedField the {@link ObfuscatedField} of which the value should be set
	 * @param instance the instance for which the field value should be set, null if static
	 * @param value the value to set the field to
	 * @return true if success, false if failure
	 */
	public static <O, T> boolean set(ObfuscatedField<O, T> obfusctatedField, O instance, T value) {
		Field field = getField(obfusctatedField);
		if (field == null) return false;
		else return set(obfusctatedField, field, instance, value);
	}
	
	/**
	 * Sets the current value of an {@link ObfuscatedField} for an instance of the fields owner type 
	 * 
	 * @param <O> the field's owner type
	 * @param <T> the field's type
	 * @param obfusctatedField the {@link ObfuscatedField} of which the value should be set
	 * @param field the field that is represented by <i>obfuscatedField</i> (can be fetched via {@link #getField(ObfuscatedField)})
	 * @param instance the instance for which the field value should be set, null if static
	 * @param value the value to set the field to
	 * @return true if success, false if failure
	 */
	public static <O, T> boolean set(ObfuscatedField<O, T> obfusctatedField, Field field, O instance, T value) {
		if (field.getDeclaringClass() != obfusctatedField.getOwnerClass()) {
				MoreCommands.INSTANCE.getLogger().trace("Invalid field object: " + field.getDeclaringClass().getName() + 
						"#" + field.getName() + ", required: " + obfusctatedField.getOwnerClass().getName() + "#" + 
						(obfusctatedField.getEnvName() == null ? obfusctatedField.getDeobfName() + "/" + obfusctatedField.getObfName() : obfusctatedField.getEnvName()));
		
				return false;
		}
		else {
			try {field.set(instance, value); return true;}
			catch (Exception ex) {
				MoreCommands.INSTANCE.getLogger().trace("Failed to set field value for field " + obfusctatedField.getOwnerClass().getName() + "#" + 
						(obfusctatedField.getEnvName() == null ? obfusctatedField.getDeobfName() + "/" + obfusctatedField.getObfName() : obfusctatedField.getEnvName()), ex);
			
				return false;
			}
		}
	}
	
	/**
	 * Gets the actual {@link Method} object from the owner class of an {@link ObfuscatedMethod}
	 * 
	 * @param <O> the method's owner type
	 * @param <R> the method's return type
	 * @param obfuscatedMethod the {@link ObfuscatedMethod} from which to get the {@link Method} object
	 * @return the {@link Method} object or null if any exception occurred
	 */
	public static <O, R> Method getMethod(ObfuscatedMethod<O, R> obfuscatedMethod) {
		try {
			Method method = null;
			
			if (obfuscatedMethod.getEnvName() == null) {
				try {method = obfuscatedMethod.getOwnerClass().getDeclaredMethod(obfuscatedMethod.getDeobfName(), obfuscatedMethod.getParameters());}
				catch (Exception ex) {method = obfuscatedMethod.getOwnerClass().getDeclaredMethod(obfuscatedMethod.getObfName(), obfuscatedMethod.getParameters());}
			}
			else method = obfuscatedMethod.getOwnerClass().getDeclaredMethod(obfuscatedMethod.getEnvName(), obfuscatedMethod.getParameters());
			
			method.setAccessible(true);
			return method;
		}
		catch (Exception ex) {
			MoreCommands.INSTANCE.getLogger().trace("Failed to obtain method " + obfuscatedMethod.getOwnerClass().getName() + "#" + 
					(obfuscatedMethod.getEnvName() == null ? obfuscatedMethod.getDeobfName() + "/" + obfuscatedMethod.getObfName() : 
						obfuscatedMethod.getEnvName()) + toString(obfuscatedMethod.getParameters()), ex);
			
			return null;
		}
	}
	
	/**
	 * Invokes the method that is represented by an {@link ObfuscatedMethod} on an instance of the methods owner type 
	 * 
	 * @param <O> the method's owner type
	 * @param <T> the method's return type
	 * @param obfuscatedMethod the {@link ObfuscatedMethod} to invoke
	 * @param instance the instance on which to invoke the method, null if static
	 * @param args the method's arguments. Their types must match {@link ObfuscatedMethod#getParameters()}
	 * @return the return value of the method
	 */
	public static <O, R> R invoke(ObfuscatedMethod<O, R> obfuscatedMethod, O instance, Object... args) {
		Method method = getMethod(obfuscatedMethod);
		if (method == null) return  null;
		else return invoke(obfuscatedMethod, method, instance, args);
	}
	
	/**
	 * Invokes the method that is represented by an {@link ObfuscatedMethod} on an instance of the methods owner type 
	 * 
	 * @param <O> the method's owner type
	 * @param <T> the method's return type
	 * @param obfuscatedMethod the {@link ObfuscatedMethod} to invoke
	 * @param method the method that is represented by <i>obfuscatedMethod</i> (can be fetched via {@link #getMethod(ObfuscatedMethod)})
	 * @param instance the instance on which to invoke the method, null if static
	 * @param args the method's arguments. Their types must match {@link ObfuscatedMethod#getParameters()}
	 * @return the return value of the method
	 */
	public static <O, R> R invoke(ObfuscatedMethod<O, R> obfuscatedMethod, Method method, O instance, Object... args) {
		if (method.getDeclaringClass() != obfuscatedMethod.getOwnerClass()) {
				MoreCommands.INSTANCE.getLogger().trace("Invalid method object: " + method.getDeclaringClass().getName() + 
						"#" + method.getName() + toString(method.getParameterTypes()) + ", required: " + obfuscatedMethod.getOwnerClass().getName() + "#" + 
						(obfuscatedMethod.getEnvName() == null ? obfuscatedMethod.getDeobfName() + "/" + obfuscatedMethod.getObfName() : 
							obfuscatedMethod.getEnvName()) + toString(obfuscatedMethod.getParameters()));
				
				return null;
		}
		else {
			try {return obfuscatedMethod.getReturnClass().cast(method.invoke(instance, args));}
			catch (Exception ex) {
				MoreCommands.INSTANCE.getLogger().trace("Failed to invoke method " + obfuscatedMethod.getOwnerClass().getName() + "#" + 
						(obfuscatedMethod.getEnvName() == null ? obfuscatedMethod.getDeobfName() + "/" + obfuscatedMethod.getObfName() : 
							obfuscatedMethod.getEnvName()) + toString(obfuscatedMethod.getParameters()), ex);
				
				return null;
			}
		}
	}
	
	/**
	 * Returns a string that represents an array of classes that are parameter types
	 * of a method. Looks like this : "(cls1, cls2, cls3, ...)" where cls1, cls2, cls3 etc.
	 * are the class names
	 * 
	 * @param classes the classes
	 * @return the representation string
	 */
	private static String toString(Class<?>[] classes) {
		StringBuilder builder = new StringBuilder("(");
		for (int i = 0; i < classes.length - 1; i++) builder.append(classes[i].getName() + ", ");
		builder.append(classes.length != 0 ? classes[classes.length - 1].getName() + ")" : ")");
		return builder.toString();
	}
}
