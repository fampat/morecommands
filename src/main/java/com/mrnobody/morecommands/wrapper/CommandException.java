package com.mrnobody.morecommands.wrapper;

import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.util.LanguageManager;

import net.minecraft.command.ICommandSender;

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
	
	public CommandException(String message, ICommandSender sender, Object... formatArgs) {
		super(LanguageManager.translate(MoreCommands.getMoreCommands().getCurrentLang(sender), message, formatArgs));
	}
	
	public CommandException(String message, CommandSender sender, Object... formatArgs) {
		this(message, sender.getMinecraftISender(), formatArgs);
	}
   
	public CommandException(Throwable t) {
		super(t);
	}
}
