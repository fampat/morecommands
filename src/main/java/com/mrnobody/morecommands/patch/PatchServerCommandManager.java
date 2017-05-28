package com.mrnobody.morecommands.patch;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;

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

import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerSelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLStateEvent;

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
	         
	         if (icommand == null) {
	        	 ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation("commands.generic.notFound");
	        	 chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.RED);
	        	 sender.addChatMessage(chatcomponenttranslation);
	         }
	         else if (icommand.canCommandSenderUse(sender)) {
	        	 net.minecraftforge.event.CommandEvent event = new net.minecraftforge.event.CommandEvent(icommand, sender, astring);
	        	 
	        	 if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event)) {
	        		 if (event.exception != null)
	        			 com.google.common.base.Throwables.propagateIfPossible(event.exception);
	                 
	        		 return 1;
	        	 }
	        	 
	        	 if (i > -1) {
	        		 List<Entity> list = PlayerSelector.matchEntities(sender, astring[i], Entity.class);
	        		 String s1 = astring[i];
	        		 sender.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, list.size());
	        		 
	        		 for (Entity entity : list) {
	        			 astring[i] = entity.getUniqueID().toString();
	        			 
	        			 if (this.tryExecute(sender, astring, icommand, rawCommand))
	        				 ++j;
	        		 }
	        		 
	        		 astring[i] = s1;
	             }
	        	 else {
	        		 sender.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, 1);
	        		 
	        		 if (this.tryExecute(sender, astring, icommand, rawCommand))
	        			 ++j;
	             }
	         }
	         else {
	        	 ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation("commands.generic.permission");
	        	 chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.RED);
	        	 sender.addChatMessage(chatcomponenttranslation);
	         }
	         
	         sender.setCommandStat(CommandResultStats.Type.SUCCESS_COUNT, j);
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
