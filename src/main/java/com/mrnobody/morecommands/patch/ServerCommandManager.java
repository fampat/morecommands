package com.mrnobody.morecommands.patch;

import java.util.Arrays;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandNotFoundException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerSelector;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;

import com.google.common.base.Throwables;
import com.mrnobody.morecommands.command.CommandBase;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.util.DummyCommand;
import com.mrnobody.morecommands.util.GlobalSettings;
import com.mrnobody.morecommands.util.LanguageManager;
import com.mrnobody.morecommands.util.ServerPlayerSettings;

/**
 * The patched class of {@link net.minecraft.command.ServerCommandManager} <br>
 * Patching this class is needed to use commands e.g. with a command block <br>
 * The vanilla command manager passes e.g. the command block as command sender,
 * this modified version will use players selected by a target selector.
 * Another aspect why this patch is needed is to use variables.
 * 
 * @author MrNobody98
 *
 */
public class ServerCommandManager extends net.minecraft.command.ServerCommandManager {
	private static class VarCouldNotBeResolvedException extends Exception {
		private String var;
		
		public VarCouldNotBeResolvedException(String var) {
			this.var = var;
		}
	}
	
	public ServerCommandManager(net.minecraft.command.ICommandManager parent) {
		super();
		for (Object command : parent.getCommands().values()) this.registerCommand((ICommand) parent.getCommands().get(command));
	}
	
	private static String replaceVars(String string, ServerPlayerSettings settings) throws VarCouldNotBeResolvedException {
		String varIdentifier = "";
		String newString = "";
		boolean isReadingVarIdentifier = false;
		
		for (char ch : string.toCharArray()) {
			if (ch == '%') {
				if (isReadingVarIdentifier) {
					isReadingVarIdentifier = false;
					
					if (varIdentifier.isEmpty()) newString += "%";
					else {
						if (!settings.varMapping.containsKey(varIdentifier))
							throw new VarCouldNotBeResolvedException(varIdentifier);
						newString += settings.varMapping.get(varIdentifier);
					}
					
					varIdentifier = "";
				}
				else isReadingVarIdentifier = true;
			}
			else {
				if (isReadingVarIdentifier) varIdentifier += ch;
				else newString += ch;
			}
		}
		
		return newString;
	}
	
    @Override
    public int executeCommand(ICommandSender p_71556_1_, String p_71556_2_)
    {
        p_71556_2_ = p_71556_2_.trim();

        if (p_71556_2_.startsWith("/"))
        {
            p_71556_2_ = p_71556_2_.substring(1);
        }
        
        if (GlobalSettings.enableVars && ServerPlayerSettings.playerSettingsMapping.containsKey(p_71556_1_)) {
        	try {p_71556_2_ = replaceVars(p_71556_2_, ServerPlayerSettings.playerSettingsMapping.get(p_71556_1_));}
            catch (VarCouldNotBeResolvedException vcnbre) {
            	ChatComponentText text = new ChatComponentText(LanguageManager.getTranslation(MoreCommands.getMoreCommands().getCurrentLang(p_71556_1_), "command.var.cantBeResolved", vcnbre.var));
            	text.getChatStyle().setColor(EnumChatFormatting.RED); p_71556_1_.addChatMessage(text);
            }
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
        	if (icommand == null || icommand instanceof CommandBase || icommand instanceof DummyCommand) {
                if (astring.length > 0 && astring[astring.length - 1].startsWith("@"))
                {
                    EntityPlayerMP[] aentityplayermp = PlayerSelector.matchPlayers(p_71556_1_, astring[astring.length - 1]);
                    astring = (String[]) Arrays.copyOfRange(astring, 0, astring.length - 1);
                    int showError = -1;
                    
                    for (int l = 0; l < aentityplayermp.length; ++l)
                    {
                    	EntityPlayerMP entityplayermp = aentityplayermp[l];
                    	
                    	if (ServerPlayerSettings.playerSettingsMapping.containsKey(entityplayermp)) {
                    		ServerPlayerSettings settings = ServerPlayerSettings.playerSettingsMapping.get(entityplayermp);
                    		
                    		if (settings.clientCommands.contains(s1)) {
                    			MoreCommands.getMoreCommands().getPacketDispatcher().sendS09ExecuteClientCommand(entityplayermp, s1 + " " + String.join(" ", astring));
                    			++j;
                    			continue;
                    		}
                    	}
                    	
                    	if (icommand == null) showError = 0;
                    	else if (icommand != null && icommand.canCommandSenderUseCommand(p_71556_1_)) {
                            CommandEvent event = new CommandEvent(icommand, entityplayermp, astring);
                            if (MinecraftForge.EVENT_BUS.post(event))
                            {
                                if (event.exception != null)
                                {
                                    Throwables.propagateIfPossible(event.exception);
                                }
                                continue;
                            }
                    		
                            try
                            {
                                icommand.processCommand(entityplayermp, astring);
                                ++j;
                            }
                            catch (CommandException commandexception1)
                            {
                                ChatComponentTranslation chatcomponenttranslation1 = new ChatComponentTranslation(commandexception1.getMessage(), commandexception1.getErrorOjbects());
                                chatcomponenttranslation1.getChatStyle().setColor(EnumChatFormatting.RED);
                                entityplayermp.addChatMessage(chatcomponenttranslation1);
                            }
                    	}
                    	else showError = 1;
                    }
                    
                    if (showError == 0) throw new CommandNotFoundException();
                    else if (showError == 1) {
                        ChatComponentTranslation chatcomponenttranslation2 = new ChatComponentTranslation("commands.generic.permission", new Object[0]);
                        chatcomponenttranslation2.getChatStyle().setColor(EnumChatFormatting.RED);
                        p_71556_1_.addChatMessage(chatcomponenttranslation2);
                    }
                }
                else
                {
                    if (icommand == null) throw new CommandNotFoundException();
                	else if (icommand.canCommandSenderUseCommand(p_71556_1_)) {
                        net.minecraftforge.event.CommandEvent event = new net.minecraftforge.event.CommandEvent(icommand, p_71556_1_, astring);
                        if (MinecraftForge.EVENT_BUS.post(event))
                        {
                            if (event.exception != null)
                            {
                                throw event.exception;
                            }
                            return 1;
                        }
                        
                        try {
                            icommand.processCommand(p_71556_1_, astring);
                            ++j;
                        }
                        catch (CommandException commandexception) {
                            chatcomponenttranslation = new ChatComponentTranslation(commandexception.getMessage(), commandexception.getErrorOjbects());
                            chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.RED);
                            p_71556_1_.addChatMessage(chatcomponenttranslation);
                        }
                	}
                    else
                    {
                        ChatComponentTranslation chatcomponenttranslation2 = new ChatComponentTranslation("commands.generic.permission", new Object[0]);
                        chatcomponenttranslation2.getChatStyle().setColor(EnumChatFormatting.RED);
                        p_71556_1_.addChatMessage(chatcomponenttranslation2);
                    }
                }
        	}
        	else {
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
            chatcomponenttranslation = new ChatComponentTranslation("commands.generic.exception", new Object[0]);
            chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.RED);
            p_71556_1_.addChatMessage(chatcomponenttranslation);
            MinecraftServer.getServer().logWarning("Couldn\'t process command: \'" + p_71556_2_ + "\'");
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
