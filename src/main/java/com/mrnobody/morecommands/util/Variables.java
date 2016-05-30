package com.mrnobody.morecommands.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants.NBT;

/**
 * This class is used to replace variables in strings.
 * A variable looks like this: "%VAR_NAME%" where "VAR_NAME" is the
 * name of a variable. To be able to use percent signs, use two percent
 * signs to get one. E.g the string "%%VAR_NAME%%" will result in the
 * string "%VAR_NAME%" and a variable won't be replaced. So the escape
 * character for the percent sign is a percent sign, two percent signs
 * will be replace by one, a single percent sign marks the beginning or
 * the end of a variable identifier.
 * 
 * @author MrNobody98
 */
public final class Variables {
	private Variables() {}
	
	/**
	 * An exception that will be thrown when a variable could not be resolved/a variable
	 * doesn't exist.
	 * 
	 * @author MrNobody98
	 */
	public static final class VariablesCouldNotBeResolvedException extends Exception {
		private final Set<String> variables;
		private final String newString;
		
		public VariablesCouldNotBeResolvedException(Set<String> variables, String newString) {
			this.variables = variables;
			this.newString = newString;
		}
		
		/**
		 * @return the set of variables which couldn't be resolved
		 */
		public Set<String> getVariables() {
			return this.variables;
		}
		
		/**
		 * @return The string with the existing variables replaced 
		 */
		public String getNewString() {
			return this.newString;
		}
	}
	
	/**
	 * Replaces all the variables in a string
	 * 
	 * @param string the string for which the variables should be replaced
	 * @param ignoreUnresolvableVars whether to ignore variables that don't exist (the string won't be modified for those variables at all)
	 * @param varMappings the variable mappings (name<->value maps that are used to look up variables)
	 * @return the string with replaced variables
	 * @throws VariablesCouldNotBeResolvedException if <i>ignoreUnresolvableVars</i> is false and a variable could not be resolved
	 */
	private static String replaceVars(String string, boolean ignoreUnresolvableVars, Map<String, String>... varMappings) throws VariablesCouldNotBeResolvedException {
		if (!string.contains("%")) return string;
		
		StringBuilder varIdentifier = new StringBuilder("");
		StringBuilder newString = new StringBuilder("");
		boolean isReadingVarIdentifier = false;
		Set<String> unresolvableVariables = new HashSet<String>();
		
		for (char ch : string.toCharArray()) {
			if (ch == '%') {
				if (isReadingVarIdentifier) {
					isReadingVarIdentifier = false;
					String identifier = varIdentifier.toString();
					
					if (identifier.isEmpty()) newString.append("%");
					else {
						boolean found = false; String value;
						
						for (Map<String, String> varMap : varMappings) {
							if ((value = varMap.get(identifier)) != null) {
								newString.append(value);
								found = true; break;
							}
						}
						
						if (!found) {
							if (!ignoreUnresolvableVars) unresolvableVariables.add(identifier);
							newString.append("%" + identifier + "%");
						}
					}
					
					varIdentifier = new StringBuilder("");
				}
				else isReadingVarIdentifier = true;
			}
			else {
				if (isReadingVarIdentifier) varIdentifier.append(ch);
				else newString.append(ch);
			}
		}
		
		if (!unresolvableVariables.isEmpty())
			throw new VariablesCouldNotBeResolvedException(unresolvableVariables, newString.toString());
		
		return newString.toString();
	}
	
	/**
	 * Replaces all the variables in a string of a {@link NBTTagString}.
	 * This method works recursively and looks for string tags in {@link NBTTagCompound}s
	 * and {@link NBTTagList}s.
	 * 
	 * @param nbt the {@link NBTTagCompound}
	 * @param ignoreUnresolvableVars whether to ignore variables that don't exist (the string won't be modified for those variables at all)
	 * @param varMappings the variable mappings (name<->value maps that are used to look up variables)
	 * @return the compound tag with replaced string tags
	 * @throws VariablesCouldNotBeResolvedException if <i>ignoreUnresolvableVars</i> is false and a variable could not be resolved
	 */
	private static void replaceVars(NBTTagCompound nbt, boolean ignoreUnresolvableVars, Map<String, String>... varMappings) throws VariablesCouldNotBeResolvedException {
		Set<String> unresolvableVariables = new HashSet<String>();
		Map<String, String> temp = new HashMap<String, String>();
		
		for (String key : nbt.getKeySet()) {
			byte id = nbt.getTagId(key);
			
			if (id == NBT.TAG_COMPOUND) 
				replaceVars(nbt.getCompoundTag(key), ignoreUnresolvableVars, varMappings);
			else if (id == NBT.TAG_LIST && ((NBTTagList) nbt.getTag(key)).getTagType() == NBT.TAG_COMPOUND) {
				NBTTagList list = nbt.getTagList(key, NBT.TAG_COMPOUND);
				
				for (int i = 0; i < list.tagCount(); i++) 
					replaceVars(list.getCompoundTagAt(i), ignoreUnresolvableVars, varMappings);
			}
			else if (id == NBT.TAG_LIST && ((NBTTagList) nbt.getTag(key)).getTagType() == NBT.TAG_STRING) {
				NBTTagList list = nbt.getTagList(key, NBT.TAG_STRING);
				
				for (int i = 0; i < list.tagCount(); i++) {
					String replaced;
					
					try {replaced = replaceVars(list.getStringTagAt(i), ignoreUnresolvableVars, varMappings);}
					catch (VariablesCouldNotBeResolvedException vcnbre) {
						unresolvableVariables.addAll(vcnbre.getVariables()); 
						replaced = vcnbre.getNewString();
					}
					
					list.set(i, new NBTTagString(replaced));
				}
			}
			else if (id == NBT.TAG_STRING) {
				try {temp.put(key, replaceVars(nbt.getString(key), ignoreUnresolvableVars, varMappings));}
				catch (VariablesCouldNotBeResolvedException vcnbre) {
					unresolvableVariables.addAll(vcnbre.getVariables()); 
					temp.put(key, vcnbre.getNewString());
				}
			}
		}
		
		for (Map.Entry<String, String> replace : temp.entrySet())
			nbt.setString(replace.getKey(), replace.getValue());
		
		if (!unresolvableVariables.isEmpty())
			throw new VariablesCouldNotBeResolvedException(unresolvableVariables, "");
	}
	
	/**
	 * Replaces all the variables in a string
	 * 
	 * @param string the string for which the variables should be replaced
	 * @param varMappings the variable mappings (name<->value maps that are used to look up variables)
	 * @return the string with replaced variables
	 * @throws VariablesCouldNotBeResolvedException a variable could not be resolved
	 */
	public static String replaceVars(String string, Map<String, String>... varMappings) throws VariablesCouldNotBeResolvedException {
		return replaceVars(string, false, varMappings);
	}
	
	/**
	 * Replaces all the variables in a string
	 * 
	 * @param string the string for which the variables should be replaced
	 * @param varMappings the variable mappings (name<->value maps that are used to look up variables)
	 * @return the string with replaced variables. If a variable could not replaced, the string is not modified for that variable
	 */
	public static String replaceVarsSafe(String string, Map<String, String>... varMappings) {
		try {return replaceVars(string, true, varMappings);}
		catch (VariablesCouldNotBeResolvedException vcnbr) {return null;} //will never be thrown
	}
	
	/**
	 * Replaces all the variables of a string contained in {@link NBTTagString}s
	 * 
	 * @param nbt the compound tag which is searched for string tags
	 * @param varMappings the variable mappings (name<->value maps that are used to look up variables)
	 * @return the compound tag with replaced string tags
	 * @throws VariablesCouldNotBeResolvedException a variable could not be resolved
	 */
	public static void replaceVars(NBTTagCompound nbt, Map<String, String>... varMappings) throws VariablesCouldNotBeResolvedException {
		replaceVars(nbt, false, varMappings);
	}
	
	/**
	 * Replaces all the variables of a string contained in {@link NBTTagString}s
	 * 
	 * @param nbt the compound tag which is searched for string tags
	 * @param varMappings the variable mappings (name<->value maps that are used to look up variables)
	 * @return the compound tag with replaced string tags. If a variable could not replaced, the string is not modified for that variable
	 */
	public static void replaceVarsSafe(NBTTagCompound nbt, Map<String, String>... varMappings) {
		try {replaceVars(nbt, true, varMappings);}
		catch (VariablesCouldNotBeResolvedException vcnbr) {} //will never be thrown
	}
}
