package com.mrnobody.morecommands.command;

import net.minecraft.command.ICommandSender;

/**
 * An interface indicating that the command which implements this interface
 * should be processed the server side.
 * 
 * @author MrNobody98
 */
public interface ServerCommandProperties {
    /**
     * @param commandName the name of this command
     * @param sender the command sender who executed this command
     * @param params the parameters which are used to execute this command
     * @return Whether the command sender can use this command
     */
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params);
}