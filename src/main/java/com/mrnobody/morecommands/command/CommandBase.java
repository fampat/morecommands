package com.mrnobody.morecommands.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

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
		PATCH_SERVERCONFIGMANAGER,
		PATCH_CLIENTCOMMANDHANDLER,
		PATCH_RENDERGLOBAL
	}
	
	/**
	 * An enum of allowed server types for a command
	 * 
	 * @author MrNobody98
	 */
	public static enum ServerType {INTEGRATED, DEDICATED, ALL}
	
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
    public abstract boolean isEnabled(EntityPlayer player);
    
	/**
	 * @return The Server Type on which this command can be executed
	 */
    public abstract ServerType getAllowedServerType();
    
	/**
	 * @return The permission level
	 */
    public abstract int getPermissionLevel();
    
    public static final EntityPlayerMP parseCommandBlockPlayer(CommandBlockLogic logic) {
    	return null;
    }
}
