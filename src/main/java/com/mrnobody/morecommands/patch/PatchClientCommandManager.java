package com.mrnobody.morecommands.patch;

import static net.minecraft.util.text.TextFormatting.RED;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;

import com.google.common.collect.Sets;
import com.mrnobody.morecommands.settings.ClientPlayerSettings;
import com.mrnobody.morecommands.settings.MoreCommandsConfig;
import com.mrnobody.morecommands.settings.PlayerSettings;
import com.mrnobody.morecommands.util.DummyCommand;
import com.mrnobody.morecommands.util.ObfuscatedNames.ObfuscatedField;
import com.mrnobody.morecommands.util.ReflectionHelper;
import com.mrnobody.morecommands.util.Variables;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLStateEvent;

/**
 * This patch substitues {@link ClientCommandHandler} with a modified subclass<br>
 * This patch is needed for the alias command. An alias is just
 * a dummy command with no function, but an event is sent if it
 * is executed which triggers the original command. The dummy command
 * will be canceled. Therefore the {@link ClientCommandHandler#executeCommand(ICommandSender, String)}
 * method will return 0, which means the command is sent to the server,
 * although it doesn't exist there. This patch changes the return
 * to 1, which makes forge not sending the command to the server.
 * Another aspect why this patch is needed is to use variables.
 * 
 * @author MrNobody98
 *
 */
public class PatchClientCommandManager implements PatchManager.StateEventBasedPatch {
	private String displayName;
	
	PatchClientCommandManager(String displayName) {
		this.displayName = displayName;
	}
	
	@Override
	public Collection<Class<? extends FMLStateEvent>> stateEventClasses() {
		return Sets.<Class<? extends FMLStateEvent>>newHashSet(FMLPostInitializationEvent.class);
	}
	
	@Override
	public <T extends FMLStateEvent> boolean applyStateEventPatch(T event) {
		Field instance = ReflectionHelper.getField(ObfuscatedField.ClientCommandHandler_instance);
		Field modifiers = ReflectionHelper.getField(ObfuscatedField.Field_modifiers);
		
		try {
			modifiers.setInt(instance, instance.getModifiers() & ~Modifier.FINAL);
			instance.set(null, new ClientCommandManager((ClientCommandHandler) instance.get(null)));
			
			PatchManager.instance().getGlobalAppliedPatches().setPatchSuccessfullyApplied(this.displayName, true);
			return true;
		}
		catch (Exception ex)  {
			PatchManager.instance().getGlobalAppliedPatches().setPatchSuccessfullyApplied(this.displayName, false);
			return false;
		}
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
		return "No replacement for client side variables if " + 
				PatchList.PATCH_CHATGUI + " failed as well, no lookup for server side aliases";
	}
	
	private static String replaceVariables(ClientCommandManager manager, ICommandSender sender, String rawCommand) {
		if (MoreCommandsConfig.enablePlayerVars && sender == Minecraft.getMinecraft().player) {
			ClientPlayerSettings settings = Minecraft.getMinecraft().player.getCapability(PlayerSettings.SETTINGS_CAP_CLIENT, null);
			
			if (settings != null) {
				try {rawCommand = Variables.replaceVars(rawCommand, manager.getCommands().containsKey(rawCommand.split(" ")[0]) ? true : 
														!PatchManager.instance().getGlobalAppliedPatches().wasPatchSuccessfullyApplied(PatchList.SERVER_MODDED), settings.variables);}
				catch (Variables.VariablesCouldNotBeResolvedException e) {rawCommand = e.getNewString();}
			}
		}
		
		return rawCommand;
	}
	
	public static class ClientCommandManager extends ClientCommandHandler {
		ClientCommandManager(ClientCommandHandler parent) {
			super();
			
			for (Object command : parent.getCommands().values()) 
				this.registerCommand((ICommand) command);
		}
		
		@Override
	    public int executeCommand(ICommandSender sender, String message) {
	        message = message.trim();

	        if (message.startsWith("/"))
	        	message = message.substring(1);
	        
			message = replaceVariables(this, sender, message);
			
	        String[] temp = message.split(" ");
	        String[] args = new String[temp.length - 1];
	        String commandName = temp[0];
	        System.arraycopy(temp, 1, args, 0, args.length);
	        ICommand icommand = getCommands().get(commandName);
	        
	        try {
	        	if (icommand == null)
	        		return 0;
	        	
	        	if (icommand.checkPermission(this.getServer(), sender)) {
	        		CommandEvent event = new CommandEvent(icommand, sender, args);
	        		
	        		if (MinecraftForge.EVENT_BUS.post(event)) {
	                    if (event.getException() != null)
	                    	throw event.getException();
	                    
	                    if (icommand instanceof DummyCommand) return 1;
	                    else return 0;
	                }
	        		
	        		this.tryExecute(sender, args, icommand, message);
	        		return 1;
	            }
	            else {
	                sender.sendMessage(format(RED, "commands.generic.permission"));
	            }
	        }
	        catch (WrongUsageException wue) {
	            sender.sendMessage(format(RED, "commands.generic.usage", format(RED, wue.getMessage(), wue.getErrorObjects())));
	        }
	        catch (CommandException ce) {
	            sender.sendMessage(format(RED, ce.getMessage(), ce.getErrorObjects()));
	        }
	        catch (Throwable t) {
	            sender.sendMessage(format(RED, "commands.generic.exception"));
	            t.printStackTrace();
	        }
	        
	        return -1;
	    }
	    
	 	//Just a copy of the format method in ClientCommandHandler, because it's private
	    private TextComponentTranslation format(TextFormatting color, String str, Object... args) {
	        TextComponentTranslation ret = new TextComponentTranslation(str, args);
	        ret.getStyle().setColor(color);
	        return ret;
	    }
	}
}
