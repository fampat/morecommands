package com.mrnobody.morecommands.patch;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Sets;
import com.mrnobody.morecommands.command.AbstractCommand;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.settings.GlobalSettings;
import com.mrnobody.morecommands.settings.MoreCommandsConfig;
import com.mrnobody.morecommands.settings.PlayerSettings;
import com.mrnobody.morecommands.settings.ServerPlayerSettings;
import com.mrnobody.morecommands.util.ObfuscatedNames.ObfuscatedField;
import com.mrnobody.morecommands.util.ReflectionHelper;
import com.mrnobody.morecommands.util.Variables;

import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLStateEvent;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandNotFoundException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerSelector;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;

/**
 * This patch substitues {@link net.minecraft.command.ServerCommandManager} with a modified subclass<br>
 * This is mainly to handle server side variable replacement.
 * 
 * @author MrNobody98
 *
 */
public class PatchServerCommandManager implements PatchManager.StateEventBasedPatch {
	private String displayName;
	
	PatchServerCommandManager(String displayName) {
		this.displayName = displayName;
	}
	
	@Override
	public <T extends FMLStateEvent> boolean needsToBeApplied(T event) {
		return true;
	}
	
	@Override
	public <T extends FMLStateEvent> boolean printLogFor(T event) {
		return true;
	}

	@Override
	public String getDisplayName() {
		return this.displayName;
	}

	@Override
	public String getFailureConsequences() {
		return "No server side variable replacement for at least commands if " + 
				PatchList.PATCH_NETHANDLERPLAYSERVER + " failed as well.";
	}
	
	@Override
	public Collection<Class<? extends FMLStateEvent>> stateEventClasses() {
		return Sets.<Class<? extends FMLStateEvent>>newHashSet(FMLServerAboutToStartEvent.class);
	}
	
	@Override
	public <T extends FMLStateEvent> boolean applyStateEventPatch(T e) {
		FMLServerAboutToStartEvent event = (FMLServerAboutToStartEvent) e;
		Field commandManager = ReflectionHelper.getField(ObfuscatedField.MinecraftServer_commandManager);
		
		if (commandManager != null) {
			try {
				commandManager.set(event.getServer(), new ServerCommandManager(event.getServer().getCommandManager()));
				
				PatchManager.instance().getGlobalAppliedPatches().setPatchSuccessfullyApplied(this.displayName, true);
				return true;
			}
			catch (Exception ex) {
				PatchManager.instance().getGlobalAppliedPatches().setPatchSuccessfullyApplied(this.displayName, false);
				return false;
			}
		}
		else {
			PatchManager.instance().getGlobalAppliedPatches().setPatchSuccessfullyApplied(this.displayName, false);
			return false;
		}
	}
	
	private static String replaceVariables(ICommandSender sender, String rawCommand) {
    	try {
    		String world = sender.getEntityWorld().getSaveHandler().getWorldDirectory().getName(), dim = sender.getEntityWorld().provider.getDimensionName();
    		
    		if (AbstractCommand.isSenderOfEntityType(sender, EntityPlayerMP.class)) {
    			ServerPlayerSettings settings = MoreCommands.getEntityProperties(ServerPlayerSettings.class, PlayerSettings.MORECOMMANDS_IDENTIFIER, AbstractCommand.getSenderAsEntity(sender, EntityPlayerMP.class));
    			Map<String, String> playerVars = settings == null ? new HashMap<String, String>() : settings.variables;
    			
    			if (MoreCommandsConfig.enableGlobalVars && MoreCommandsConfig.enablePlayerVars)
    				rawCommand = Variables.replaceVars(rawCommand, true, playerVars, GlobalSettings.getInstance().variables.get(ImmutablePair.of(world, dim)));
    			else if (MoreCommandsConfig.enablePlayerVars)
    				rawCommand = Variables.replaceVars(rawCommand, true, playerVars);
    			else if (MoreCommandsConfig.enableGlobalVars)
    				rawCommand = Variables.replaceVars(rawCommand, true, GlobalSettings.getInstance().variables.get(ImmutablePair.of(world, dim)));
    		}
    		else if (MoreCommandsConfig.enableGlobalVars) 
    			rawCommand = Variables.replaceVars(rawCommand, true, GlobalSettings.getInstance().variables.get(ImmutablePair.of(world, dim)));
    	}
        catch (Variables.VariablesCouldNotBeResolvedException vcnbre) {
        	rawCommand = vcnbre.getNewString();
        }
    	
    	return rawCommand;
	}
	
	public static class ServerCommandManager extends net.minecraft.command.ServerCommandManager {
		private static final Logger LOGGER = LogManager.getLogger(net.minecraft.command.ServerCommandManager.class);
		
		ServerCommandManager(net.minecraft.command.ICommandManager parent) {
			for (Object command : parent.getCommands().values()) 
				this.registerCommand((ICommand) command);
		}
		
		@Override
		public int executeCommand(ICommandSender sender, String rawCommand) {
			rawCommand = rawCommand.trim();
			
			if (rawCommand.startsWith("/"))
				rawCommand = rawCommand.substring(1);
	        
	        rawCommand = replaceVariables(sender, rawCommand);
	        
	    	 String[] astring = rawCommand.split(" ");
	         String s = astring[0];
	         astring = dropFirstString(astring);
	         ICommand icommand = (ICommand) this.getCommands().get(s);
	         int i = this.getUsernameIndex(icommand, astring);
	         int j = 0;
	         
	         try {
	        	 if (icommand == null)
	        		 throw new CommandNotFoundException();
	        	 
	        	 if (icommand.canCommandSenderUseCommand(sender)) {
	        		 CommandEvent event = new CommandEvent(icommand, sender, astring);
	        		 
	        		 if (MinecraftForge.EVENT_BUS.post(event)) {
	        			 if (event.exception != null)
	        				 throw event.exception;
	                     
	        			 return 1;
	        		 }
	        		 
	                 if (i > -1) {
	                	 EntityPlayerMP[] players = PlayerSelector.matchPlayers(sender, astring[i]);
	                	 String s1 = astring[i];
	                	 
	                	 for (int l = 0; l < players.length; ++l) {
	                         astring[i] = players[l].getCommandSenderName();

	                         try {
	                        	 icommand.processCommand(sender, astring);
	                        	 ++j;
	                         }
	                         catch (CommandException ce) {
	                        	 ChatComponentTranslation cct = new ChatComponentTranslation(ce.getMessage(), ce.getErrorOjbects());
	                        	 cct.getChatStyle().setColor(EnumChatFormatting.RED);
	                             sender.addChatMessage(cct);
	                         }
	                     }
	                	 
	                     astring[i] = s1;
	                 }
	                 else {
	                     try {
	                    	 icommand.processCommand(sender, astring);
	                         ++j;
	                     }
	                     catch (CommandException ce) {
	                    	 ChatComponentTranslation cct = new ChatComponentTranslation(ce.getMessage(), ce.getErrorOjbects());
	                    	 cct.getChatStyle().setColor(EnumChatFormatting.RED);
	                         sender.addChatMessage(cct);
	                    }
	                }
	             }
	             else {
	                 ChatComponentTranslation cct = new ChatComponentTranslation("commands.generic.permission");
	                 cct.getChatStyle().setColor(EnumChatFormatting.RED);
	                 sender.addChatMessage(cct);
	             }
	         }
	         catch (WrongUsageException wue) {
	        	 ChatComponentTranslation cct = new ChatComponentTranslation("commands.generic.usage", new Object[] {new ChatComponentTranslation(wue.getMessage(), wue.getErrorOjbects())});
	        	 cct.getChatStyle().setColor(EnumChatFormatting.RED);
	             sender.addChatMessage(cct);
	         }
	         catch (CommandException ce) {
	        	 ChatComponentTranslation cct = new ChatComponentTranslation(ce.getMessage(), ce.getErrorOjbects());
	        	 cct.getChatStyle().setColor(EnumChatFormatting.RED);
	             sender.addChatMessage(cct);
	         }
	         catch (Throwable t) {
	        	 t.printStackTrace();
	        	 
	        	 ChatComponentTranslation cct = new ChatComponentTranslation("commands.generic.exception");
	        	 cct.getChatStyle().setColor(EnumChatFormatting.RED);
	             sender.addChatMessage(cct);
	             
	             LOGGER.error("Couldn\'t process command: \'" + rawCommand + "\'");
	         }
	         
	         return j;
	    }
		
		private static String[] dropFirstString(String[] input) {
			String[] astring1 = new String[input.length - 1];
			System.arraycopy(input, 1, astring1, 0, input.length - 1);
			return astring1;
		}
		
		private int getUsernameIndex(ICommand command, String[] args) {
	        if (command == null)
	        	return -1;
	        else {
	        	for (int i = 0; i < args.length; ++i)
	        		if (command.isUsernameIndex(args, i) && PlayerSelector.matchesMultiplePlayers(args[i]))
	                    return i;
	        	
	            return -1;
	        }
	    }
	}
}
