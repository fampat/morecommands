package com.mrnobody.morecommands.wrapper;

/**
 * Custom exception type for command exceptions
 * 
 * @author MrNobody98
 *
 */
public class CommandException extends Exception {
	public CommandException() {
		super();
	}
   
	public CommandException(String message) {
		super(message);
	}
   
	public CommandException(Throwable t) {
		super(t);
	}
}
