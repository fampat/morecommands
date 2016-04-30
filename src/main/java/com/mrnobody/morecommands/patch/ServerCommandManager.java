package com.mrnobody.morecommands.patch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mrnobody.morecommands.command.AbstractCommand;
import com.mrnobody.morecommands.core.AppliedPatches.PlayerPatches;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.util.GlobalSettings;
import com.mrnobody.morecommands.util.LanguageManager;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.util.Variables;

import net.minecraft.command.CommandResultStats;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

/**
 * The patched class of {@link net.minecraft.command.ServerCommandManager} <br>
 * Patching this class is needed to use commands e.g. with a command block <br>
 * The vanilla command manager passes e.g. the command block as command sender,
 * this modified version will use players selected by a target selector
 * Another aspect why this patch is needed is to use variables.
 * 
 * @author MrNobody98
 *
 */
public class ServerCommandManager extends net.minecraft.command.ServerCommandManager {	
	public ServerCommandManager(MinecraftServer server, net.minecraft.command.ICommandManager parent) {
		super(server);
		for (Object command : parent.getCommands().values()) this.registerCommand((ICommand) command);
	}
	
	@Override
    public int executeCommand(ICommandSender sender, String rawCommand)
    {
        rawCommand = rawCommand.trim();

        if (rawCommand.startsWith("/"))
        {
            rawCommand = rawCommand.substring(1);
        }
        
    	try {
    		String world = MoreCommands.getProxy().getCurrentWorld(), dim = sender.getEntityWorld().provider.getDimensionType().getName();
    		
    		if (AbstractCommand.isSenderOfEntityType(sender, EntityPlayerMP.class)) {
    			ServerPlayerSettings settings = AbstractCommand.getPlayerSettings(AbstractCommand.getSenderAsEntity(sender, EntityPlayerMP.class));
    			PlayerPatches playerInfo = AbstractCommand.getSenderAsEntity(sender, EntityPlayerMP.class).getCapability(PlayerPatches.PATCHES_CAPABILITY, null);
    			Map<String, String> playerVars = playerInfo != null && playerInfo.clientModded() ? new HashMap<String, String>() : settings.variables; //client takes care of replacing player variables
    			
    			if (GlobalSettings.enableGlobalVars && GlobalSettings.enablePlayerVars)
    				rawCommand = Variables.replaceVars(rawCommand, playerVars, GlobalSettings.getVariables(world, dim));
    			else if (GlobalSettings.enablePlayerVars)
    				rawCommand = Variables.replaceVars(rawCommand, playerVars);
    			else if (GlobalSettings.enableGlobalVars)
    				rawCommand = Variables.replaceVars(rawCommand, GlobalSettings.getVariables(world, dim));
    		}
    		else if (GlobalSettings.enableGlobalVars) 
    			rawCommand = Variables.replaceVars(rawCommand, GlobalSettings.getVariables(world, dim));
    	}
        catch (Variables.VariablesCouldNotBeResolvedException vcnbre) {
            TextComponentString text = new TextComponentString(LanguageManager.translate(MoreCommands.INSTANCE.getCurrentLang(sender), "command.var.cantBeResolved", vcnbre.getVariables().toString()));
            text.getChatStyle().setColor(TextFormatting.RED); sender.addChatMessage(text);
            rawCommand = vcnbre.getNewString();
        }

    	 String[] astring = rawCommand.split(" ");
         String s = astring[0];
         astring = dropFirstString(astring);
         ICommand icommand = this.getCommands().get(s);
         int i = this.getUsernameIndex(icommand, astring);
         int j = 0;

         if (icommand == null)
         {
             TextComponentTranslation textcomponenttranslation = new TextComponentTranslation("commands.generic.notFound", new Object[0]);
             textcomponenttranslation.getChatStyle().setColor(TextFormatting.RED);
             sender.addChatMessage(textcomponenttranslation);
         }
         else if (icommand.checkPermission(this.getServer(), sender))
         {
             net.minecraftforge.event.CommandEvent event = new net.minecraftforge.event.CommandEvent(icommand, sender, astring);
             if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event))
             {
                 if (event.getException() != null)
                 {
                     com.google.common.base.Throwables.propagateIfPossible(event.getException());
                 }
                 return 1;
             }

             if (i > -1)
             {
                 List<Entity> list = EntitySelector.<Entity>matchEntities(sender, astring[i], Entity.class);
                 String s1 = astring[i];
                 sender.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, list.size());

                 for (Entity entity : list)
                 {
                     astring[i] = entity.getUniqueID().toString();

                     if (this.tryExecute(sender, astring, icommand, rawCommand))
                     {
                         ++j;
                     }
                 }

                 astring[i] = s1;
             }
             else
             {
                 sender.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, 1);

                 if (this.tryExecute(sender, astring, icommand, rawCommand))
                 {
                     ++j;
                 }
             }
         }
         else
         {
             TextComponentTranslation textcomponenttranslation1 = new TextComponentTranslation("commands.generic.permission", new Object[0]);
             textcomponenttranslation1.getChatStyle().setColor(TextFormatting.RED);
             sender.addChatMessage(textcomponenttranslation1);
         }

         sender.setCommandStat(CommandResultStats.Type.SUCCESS_COUNT, j);
         return j;
    }
    
    private static String[] dropFirstString(String[] input)
    {
        String[] astring1 = new String[input.length - 1];
        System.arraycopy(input, 1, astring1, 0, input.length - 1);
        return astring1;
    }
    
    private int getUsernameIndex(ICommand command, String[] args)
    {
        if (command == null)
        {
            return -1;
        }
        else
        {
            for (int i = 0; i < args.length; ++i)
            {
                if (command.isUsernameIndex(args, i) && EntitySelector.matchesMultiplePlayers(args[i]))
                {
                    return i;
                }
            }

            return -1;
        }
    }
}
