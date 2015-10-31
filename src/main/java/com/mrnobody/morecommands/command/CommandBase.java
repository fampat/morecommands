package com.mrnobody.morecommands.command;

import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;

/**
 * Base class for all commands
 * 
 * @author MrNobody98
 */
public abstract class CommandBase extends net.minecraft.command.CommandBase {
	/**
	 * An enum of requirements for a command to be executed properly
	 * 
	 * @author MrNobody98
	 */
	public static enum Requirement {
		PATCH_NETHANDLERPLAYSERVER,
		PATCH_ENTITYPLAYERSP,
		PATCH_ENTITYPLAYERMP,
		MODDED_CLIENT,
		HANDSHAKE_FINISHED,
		HANDSHAKE_FINISHED_IF_CLIENT_MODDED,
		PATCH_SERVERCONFIGMANAGER,
		PATCH_CLIENTCOMMANDHANDLER,
		PATCH_SERVERCOMMANDHANDLER,
		PATCH_RENDERGLOBAL
	}
	
	/**
	 * @return The required permission level
	 */
	@Override
    public int getRequiredPermissionLevel() {return this.getPermissionLevel();}
	
	/**
	 * @return The command name
	 */
    public abstract String getName();

	/**
	 * @return The command usage
	 */
    public final String getCommandUsage(ICommandSender sender) {return this.getUsage();}
    
	/**
	 * @return The command usage
	 */
    public abstract String getUsage();
    
	/**
	 * Executes the command
	 */
    public abstract void execute(CommandSender sender, String[] params) throws CommandException;
    
	/**
	 * @return The requirements for a command to be executed
	 */
    public abstract Requirement[] getRequirements();
    
	/**
	 * processes the command
	 */
    public abstract void execute(ICommandSender sender, String[] params);
    
	/**
	 * @return Whether this command is enabled
	 */
    public abstract boolean isEnabled(ICommandSender sender);
    
	/**
	 * @return The Server Type on which this command can be executed
	 */
    public abstract ServerType getAllowedServerType();
    
	/**
	 * @return The permission level
	 */
    public abstract int getPermissionLevel();
    
    public static boolean parseTrueFalse(String[] params, int index, boolean default_) throws IllegalArgumentException {
        if (params.length > index) {
        	if (params[index].equalsIgnoreCase("enable") || params[index].equalsIgnoreCase("1")
            	|| params[index].equalsIgnoreCase("on") || params[index].equalsIgnoreCase("true")) {
        		return true;
            }
            else if (params[index].equalsIgnoreCase("disable") || params[index].equalsIgnoreCase("0")
            		|| params[index].equalsIgnoreCase("off") || params[index].equalsIgnoreCase("false")) {
            	return false;
            }
            else throw new IllegalArgumentException("Invalid Argument");
        }
        else return !default_;
    }
}
