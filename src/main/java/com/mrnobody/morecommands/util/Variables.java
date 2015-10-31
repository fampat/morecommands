package com.mrnobody.morecommands.util;

public final class Variables {
	private Variables() {}
	
	public static class VarCouldNotBeResolvedException extends Exception {
		private String var;
		
		public VarCouldNotBeResolvedException(String var) {
			this.var = var;
		}
		
		public String getVar() {
			return this.var;
		}
	}
	
	private static String replaceVars(String string, boolean serverSide, ServerPlayerSettings settings, boolean throwException) throws VarCouldNotBeResolvedException {
		String varIdentifier = "";
		String newString = "";
		boolean isReadingVarIdentifier = false;
		
		for (char ch : string.toCharArray()) {
			if (ch == '%') {
				if (isReadingVarIdentifier) {
					isReadingVarIdentifier = false;
					
					if (varIdentifier.isEmpty()) newString += "%";
					else if (serverSide) {
						if (!settings.varMapping.containsKey(varIdentifier)) {
							if (throwException) throw new VarCouldNotBeResolvedException(varIdentifier);
							else newString += "%" + varIdentifier + "%";
						}
						else newString += settings.varMapping.get(varIdentifier);
					}
					else {
						if (!ClientPlayerSettings.varMapping.containsKey(varIdentifier)) {
							if (throwException) throw new VarCouldNotBeResolvedException(varIdentifier);
							else newString += "%" + varIdentifier + "%";
						}
						else newString += ClientPlayerSettings.varMapping.get(varIdentifier);
					}
					
					varIdentifier = "";
				}
				else isReadingVarIdentifier = true;
			}
			else {
				if (isReadingVarIdentifier) varIdentifier += ch;
				else newString += ch;
			}
		}
		
		return newString;
	}
	
	public static String replaceVars(String string, ServerPlayerSettings settings) throws VarCouldNotBeResolvedException {
		return replaceVars(string, true, settings, true);
	}
	
	public static String replaceVarsSafe(String string, ServerPlayerSettings settings) {
		try {return replaceVars(string, true, settings, false);}
		catch (VarCouldNotBeResolvedException vcnbr) {return null;} //will never be thrown
	}
	
	public static String replaceVars(String string) throws VarCouldNotBeResolvedException {
		return replaceVars(string, false, null, true);
	}
	
	public static String replaceVarsSafe(String string) {
		try {return replaceVars(string, false, null, false);}
		catch (VarCouldNotBeResolvedException vcnbr) {return null;} //will never be thrown
	}
}
