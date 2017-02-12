package com.mrnobody.morecommands.patch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.mrnobody.morecommands.command.AbstractCommand;
import com.mrnobody.morecommands.settings.GlobalSettings;
import com.mrnobody.morecommands.settings.MoreCommandsConfig;
import com.mrnobody.morecommands.settings.PlayerSettings;
import com.mrnobody.morecommands.settings.ServerPlayerSettings;
import com.mrnobody.morecommands.util.Variables;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
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
    		String world = sender.getEntityWorld().getSaveHandler().getWorldDirectory().getName(), dim = sender.getEntityWorld().provider.getDimensionType().getName();
    		
    		if (AbstractCommand.isSenderOfEntityType(sender, EntityPlayerMP.class)) {
    			ServerPlayerSettings settings = AbstractCommand.getSenderAsEntity(sender, EntityPlayerMP.class).getCapability(PlayerSettings.SETTINGS_CAP_SERVER, null);
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
    	
        String[] astring = rawCommand.split(" ");
        String s = astring[0];
        astring = dropFirstString(astring);
        ICommand icommand = (ICommand)this.getCommands().get(s);
        int i = 0;

        try
        {
            int j = this.getUsernameIndex(icommand, astring);

            if (icommand == null)
            {
                TextComponentTranslation textcomponenttranslation1 = new TextComponentTranslation("commands.generic.notFound", new Object[0]);
                textcomponenttranslation1.getStyle().setColor(TextFormatting.RED);
                sender.addChatMessage(textcomponenttranslation1);
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
                if (event.getParameters() != null) astring = event.getParameters();

                if (j > -1)
                {
                    List<Entity> list = EntitySelector.<Entity>matchEntities(sender, astring[j], Entity.class);
                    String s1 = astring[j];
                    sender.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, list.size());

                    if (list.isEmpty())
                    {
                        throw new PlayerNotFoundException("commands.generic.selector.notFound", new Object[] {astring[j]});
                    }

                    for (Entity entity : list)
                    {
                        astring[j] = entity.getCachedUniqueIdString();

                        if (this.tryExecute(sender, astring, icommand, rawCommand))
                        {
                            ++i;
                        }
                    }

                    astring[j] = s1;
                }
                else
                {
                    sender.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, 1);

                    if (this.tryExecute(sender, astring, icommand, rawCommand))
                    {
                        ++i;
                    }
                }
            }
            else
            {
                TextComponentTranslation textcomponenttranslation2 = new TextComponentTranslation("commands.generic.permission", new Object[0]);
                textcomponenttranslation2.getStyle().setColor(TextFormatting.RED);
                sender.addChatMessage(textcomponenttranslation2);
            }
        }
        catch (CommandException commandexception)
        {
            TextComponentTranslation textcomponenttranslation = new TextComponentTranslation(commandexception.getMessage(), commandexception.getErrorObjects());
            textcomponenttranslation.getStyle().setColor(TextFormatting.RED);
            sender.addChatMessage(textcomponenttranslation);
        }

        sender.setCommandStat(CommandResultStats.Type.SUCCESS_COUNT, i);
        return i;
    }
    
    private static String[] dropFirstString(String[] input)
    {
        String[] astring1 = new String[input.length - 1];
        System.arraycopy(input, 1, astring1, 0, input.length - 1);
        return astring1;
    }
    
    private int getUsernameIndex(ICommand command, String[] args) throws CommandException
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
