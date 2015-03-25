package com.mrnobody.morecommands.core;

import net.minecraft.command.ICommandSender;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;

import com.mrnobody.morecommands.command.CommandBase.ServerType;
/**
 * Abstract base class for client and server proxies
 * 
 * @author MrNobody98
 *
 */
public abstract class CommonProxy {
	/**
	 * @return Whether this proxy loaded the commands successfully
	 */
	public abstract boolean commandsLoaded();
	
	/**
	 * Registers Handlers
	 */
	public abstract boolean registerHandlers();
	
	/**
	 * @return The running Server Type
	 */
	public abstract ServerType getRunningServerType();
	
	/**
	 * Called from the main mod class to do pre initialization
	 */
	public abstract void preInit(FMLPreInitializationEvent event);
	
	/**
	 * Called from the main mod class to do initialization
	 */
	public abstract void init(FMLInitializationEvent event);
	
	/**
	 * Called from the main mod class to do post initialization
	 */
	public abstract void postInit(FMLPostInitializationEvent event);
	
	/**
	 * Called from the main mod class to do stuff before the server starts
	 */
	public abstract void serverStart(FMLServerAboutToStartEvent event);
	
	/**
	 * Called from the main mod class to handle the server start
	 */
	public abstract void serverInit(FMLServerStartingEvent event);
	
	/**
	 * Called from the main mod class to handle the server stop
	 */
	public abstract void serverStop(FMLServerStoppedEvent event);
	
	/**
	 * Called from the main mod class to get the language
	 */
	public abstract String getLang(ICommandSender player);
	
	/**
	 * Called from the main mod class to get the patcher instance
	 */
	public abstract Patcher getPatcher();
}
