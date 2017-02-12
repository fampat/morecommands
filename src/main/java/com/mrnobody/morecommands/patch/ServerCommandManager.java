package com.mrnobody.morecommands.patch;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mrnobody.morecommands.command.AbstractCommand;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.settings.GlobalSettings;
import com.mrnobody.morecommands.settings.MoreCommandsConfig;
import com.mrnobody.morecommands.settings.PlayerSettings;
import com.mrnobody.morecommands.settings.ServerPlayerSettings;
import com.mrnobody.morecommands.util.LanguageManager;
import com.mrnobody.morecommands.util.Variables;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.CommandNotFoundException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerSelector;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;

/**
 * The patched class of {@link net.minecraft.command.ServerCommandManager} <br>
 * This is mainly to replace variables in the command string.
 * 
 * @author MrNobody98
 *
 */
public class ServerCommandManager extends net.minecraft.command.ServerCommandManager {
	private static final Logger logger = LogManager.getLogger(CommandHandler.class);
	
	public ServerCommandManager(net.minecraft.command.ICommandManager parent) {
		super();
		for (Object command : parent.getCommands().values()) this.registerCommand((ICommand) command);
	}
	
    @Override
    public int executeCommand(ICommandSender p_71556_1_, String p_71556_2_)
    {
        p_71556_2_ = p_71556_2_.trim();

        if (p_71556_2_.startsWith("/"))
        {
            p_71556_2_ = p_71556_2_.substring(1);
        }
        
    	try {
    		String world = p_71556_1_.getEntityWorld().getSaveHandler().getWorldDirectoryName(), dim = p_71556_1_.getEntityWorld().provider.getDimensionName();
    		
    		if (AbstractCommand.isSenderOfEntityType(p_71556_1_, EntityPlayerMP.class)) {
    			ServerPlayerSettings settings = MoreCommands.getEntityProperties(ServerPlayerSettings.class, PlayerSettings.MORECOMMANDS_IDENTIFIER, AbstractCommand.getSenderAsEntity(p_71556_1_, EntityPlayerMP.class));
    			Map<String, String> playerVars = settings == null ? new HashMap<String, String>() : settings.variables;
    			
    			if (MoreCommandsConfig.enableGlobalVars && MoreCommandsConfig.enablePlayerVars)
    				p_71556_2_ = Variables.replaceVars(p_71556_2_, true, playerVars, GlobalSettings.getInstance().variables.get(ImmutablePair.of(world, dim)));
    			else if (MoreCommandsConfig.enablePlayerVars)
    				p_71556_2_ = Variables.replaceVars(p_71556_2_, true, playerVars);
    			else if (MoreCommandsConfig.enableGlobalVars)
    				p_71556_2_ = Variables.replaceVars(p_71556_2_, true, GlobalSettings.getInstance().variables.get(ImmutablePair.of(world, dim)));
    		}
    		else if (MoreCommandsConfig.enableGlobalVars) 
    			p_71556_2_ = Variables.replaceVars(p_71556_2_, true, GlobalSettings.getInstance().variables.get(ImmutablePair.of(world, dim)));
    	}
        catch (Variables.VariablesCouldNotBeResolvedException vcnbre) {
        	p_71556_2_ = vcnbre.getNewString();
        }
    	
        String[] astring = p_71556_2_.split(" ");
        String s1 = astring[0];
        astring = dropFirstString(astring);
        ICommand icommand = (ICommand)this.getCommands().get(s1);
        int i = this.getUsernameIndex(icommand, astring);
        int j = 0;
        ChatComponentTranslation chatcomponenttranslation;

        try
        {
        	if (icommand == null)
        	{
        		throw new CommandNotFoundException();
        	}
        	
            if (icommand.canCommandSenderUseCommand(p_71556_1_))
            {	
                CommandEvent event = new CommandEvent(icommand, p_71556_1_, astring);
                if (MinecraftForge.EVENT_BUS.post(event))
                {
                    if (event.exception != null)
                    {
                        throw event.exception;
                    }
                    return 1;
                }

                if (i > -1)
                {
                    EntityPlayerMP[] aentityplayermp = PlayerSelector.matchPlayers(p_71556_1_, astring[i]);
                    String s2 = astring[i];
                    EntityPlayerMP[] aentityplayermp1 = aentityplayermp;
                    int k = aentityplayermp.length;

                    for (int l = 0; l < k; ++l)
                    {
                        EntityPlayerMP entityplayermp = aentityplayermp1[l];
                        astring[i] = entityplayermp.getCommandSenderName();

                        try
                        {
                            icommand.processCommand(p_71556_1_, astring);
                            ++j;
                        }
                        catch (CommandException commandexception1)
                        {
                            ChatComponentTranslation chatcomponenttranslation1 = new ChatComponentTranslation(commandexception1.getMessage(), commandexception1.getErrorOjbects());
                            chatcomponenttranslation1.getChatStyle().setColor(EnumChatFormatting.RED);
                            p_71556_1_.addChatMessage(chatcomponenttranslation1);
                        }
                    }

                    astring[i] = s2;
                }
                else
                {
                    try
                    {
                        icommand.processCommand(p_71556_1_, astring);
                        ++j;
                    }
                    catch (CommandException commandexception)
                    {
                        chatcomponenttranslation = new ChatComponentTranslation(commandexception.getMessage(), commandexception.getErrorOjbects());
                        chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.RED);
                        p_71556_1_.addChatMessage(chatcomponenttranslation);
                    }
                }
            }
            else
            {
                ChatComponentTranslation chatcomponenttranslation2 = new ChatComponentTranslation("commands.generic.permission", new Object[0]);
                chatcomponenttranslation2.getChatStyle().setColor(EnumChatFormatting.RED);
                p_71556_1_.addChatMessage(chatcomponenttranslation2);
            }
        }
        catch (WrongUsageException wrongusageexception)
        {
            chatcomponenttranslation = new ChatComponentTranslation("commands.generic.usage", new Object[] {new ChatComponentTranslation(wrongusageexception.getMessage(), wrongusageexception.getErrorOjbects())});
            chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.RED);
            p_71556_1_.addChatMessage(chatcomponenttranslation);
        }
        catch (CommandException commandexception2)
        {
            chatcomponenttranslation = new ChatComponentTranslation(commandexception2.getMessage(), commandexception2.getErrorOjbects());
            chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.RED);
            p_71556_1_.addChatMessage(chatcomponenttranslation);
        }
        catch (Throwable throwable)
        {
        	throwable.printStackTrace();
            chatcomponenttranslation = new ChatComponentTranslation("commands.generic.exception", new Object[0]);
            chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.RED);
            p_71556_1_.addChatMessage(chatcomponenttranslation);
            logger.error("Couldn\'t process command: \'" + p_71556_2_ + "\'");
        }

        return j;
    }
    
    private static String[] dropFirstString(String[] p_71559_0_)
    {
        String[] astring1 = new String[p_71559_0_.length - 1];

        for (int i = 1; i < p_71559_0_.length; ++i)
        {
            astring1[i - 1] = p_71559_0_[i];
        }

        return astring1;
    }
    
    private int getUsernameIndex(ICommand p_82370_1_, String[] p_82370_2_)
    {
        if (p_82370_1_ == null)
        {
            return -1;
        }
        else
        {
            for (int i = 0; i < p_82370_2_.length; ++i)
            {
                if (p_82370_1_.isUsernameIndex(p_82370_2_, i) && PlayerSelector.matchesMultiplePlayers(p_82370_2_[i]))
                {
                    return i;
                }
            }

            return -1;
        }
    }
}
