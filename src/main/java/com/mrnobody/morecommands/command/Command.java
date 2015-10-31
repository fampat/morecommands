package com.mrnobody.morecommands.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation containing metadata on a command, mainly used by the help command
 * 
 * @author MrNobody98
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Command {
	   String name();
	   String description();
	   String syntax();
	   String example();
	   String videoURL();
}
