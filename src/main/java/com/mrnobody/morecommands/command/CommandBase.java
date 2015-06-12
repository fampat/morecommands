package com.mrnobody.morecommands.command;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;

import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventBus;

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
		PATCH_ENTITYCLIENTPLAYERMP,
		PATCH_ENTITYPLAYERMP,
		MODDED_CLIENT,
		PATCH_SERVERCONFIGMANAGER,
		PATCH_CLIENTCOMMANDHANDLER,
		PATCH_SERVERCOMMANDHANDLER
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
    public abstract String getCommandName();

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
    public abstract void processCommand(ICommandSender sender, String[] params);
    
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
}
