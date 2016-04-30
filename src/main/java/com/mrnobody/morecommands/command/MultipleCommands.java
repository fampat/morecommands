package com.mrnobody.morecommands.command;

import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

/**
 * The base class for a command having multiple names. Such a command can
 * provide different functionalities which are still similar. Each name
 * corresponds to a functionality.
 * 
 * @author MrNobody98
 */
public abstract class MultipleCommands extends StandardCommand {
	private final int typeIndex;
	private final String commandName, commandUsage;
	
	/**
	 * Constructs a {@link MultipleCommands} object of which the
	 * name it uses and therefore the functionality has not been set
	 */
	public MultipleCommands() {
		this.typeIndex = -1;
		this.commandName = this.commandUsage = null;
	}
	
	/**
	 * Constructs a {@link MultipleCommands} object which uses a certain
	 * name. The name is determined by
	 * <b>{@link MultipleCommands#getCommandNames()}[typeIndex]</b>
	 * 
	 * @param typeIndex the index of the name which determines the functionality
	 * 		  of this command. The name which will be used is determined by
	 * 		  <b>{@link MultipleCommands#getCommandNames()}[typeIndex]</b>
	 * @throws ArrayIndexOutOfBoundsException if typeIndex >= <b>{@link MultipleCommands#getCommandNames()}.length</b>
	 * 			or typeIndex >= <b>{@link MultipleCommands#getUsages()}.length</b>
	 * @throws NegativeArraySizeException if typeIndex < 0
	 */
	public MultipleCommands(int typeIndex) {
		this.typeIndex = typeIndex;
		this.commandName = this.getNames()[this.typeIndex];
		this.commandUsage = this.getUsages()[this.typeIndex];
	}
	
	/**
	 * The index of {@link MultipleCommands#getCommandNames()} and
	 * {@link MultipleCommands#getUsages()} that is used to determine
	 * the name of this command
	 * 
	 * @return
	 */
	public final int getTypeIndex() {
		return this.typeIndex;
	}
	
	@Override
	public final String getName() {
		return this.commandName;
	}

	@Override
	public final String getUsage() {
		return this.commandUsage;
	}

	@Override
	public final void execute(CommandSender sender, String[] params) throws CommandException {
		if (this.commandName == null || this.commandUsage == null) throw new CommandException("command.generic.multiple.null", sender);
		else this.execute(this.commandName, sender, params);
	}
	
	/**
	 * @return The names of this command
	 */
	public abstract String[] getNames();
	
	/**
	 * @return The usages of this command. The array size must be equal to the size of {@link MultipleCommands#getCommandNames()}
	 */
	public abstract String[] getUsages();
	
	/**
	 * Executes this command using the functionality corresponding to the given name
	 * 
	 * @param command the name of this command. This determines the functionality that is used during execution
	 * @param sender the command sender
	 * @param params the command parameters
	 * @throws CommandException if the command can't be processed for some reason
	 */
	public abstract void execute(String command, CommandSender sender, String[] params) throws CommandException;
}
