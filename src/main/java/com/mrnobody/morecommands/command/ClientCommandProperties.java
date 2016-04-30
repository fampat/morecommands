package com.mrnobody.morecommands.command;

/**
 * An interface indicating that the command which implements this interface
 * should be processed on client side.
 * 
 * @author MrNobody98
 */
public interface ClientCommandProperties {
    /**
     * @return Whether this command shall be registered if the server has this mod installed
     */
    public boolean registerIfServerModded();
}
