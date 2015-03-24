package com.mrnobody.morecommands.command;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An annotation containing metadata on a command, mainly used by the help command
 * 
 * @author MrNobody98
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
	   String name();
	   String description();
	   String syntax();
	   String example();
	   String videoURL();
	   //String version() default "";
	   //boolean enabled() default true;
	   //boolean async() default false;
	   //String[] alias() default {};
}
