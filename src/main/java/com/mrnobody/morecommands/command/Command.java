package com.mrnobody.morecommands.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation providing metadata for a command.
 * Each {@link StandardCommand} implementation should have
 * this annotation. It is mainly used by the help command to
 * provide information on a command to the user.
 * 
 * @author MrNobody98
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
	/**
	 * @return the name of this command
	 */
	String name();
	
	/**
	 * @return The description of this command. It can be a language file entry
	 */
	String description();
	
	/**
	 * @return The syntax of this command. It can be a language file entry
	 */
	String syntax();
	
	/**
	 * @return An example of how to use this command. It can be a language file entry
	 */
	String example();
	
	/**
	 * @return A link to a video of this command. It can be a language file entry
	 */
	String videoURL();
	   
	/**
	 * A version of {@link Command} that is intended to be used by
	 * implementations of {@link MultipleCommands}. All properties
	 * of this command are arrays which must have the same size as
	 * {@link MultipleCommands#getCommandNames()}. The index of
	 * each property corresponds to the index of each other property.
	 * 
	 * @see Command
	 * @author MrNobody98
	 */
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface MultipleCommand {
		/**
		 * @return the names of this command
		 */
		String[] name();
		
		/**
		 * @return The descriptions of this command. It can be a language file entries
		 */
		String[] description();
		
		/**
		 * @return The syntaxes of this command. It can be a language file entries
		 */
		String[] syntax();
		
		/**
		 * @return Examples of how to use this command. It can be a language file entries
		 */
		String[] example();
		
		/**
		 * @return Links to videos of this command. It can be a language file entries
		 */
		String[] videoURL();
	}
}
