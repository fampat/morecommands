package com.mrnobody.morecommands.patch;

import static net.minecraft.util.text.TextFormatting.RED;

import java.util.HashSet;
import java.util.Set;

import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.util.ClientPlayerSettings;
import com.mrnobody.morecommands.util.DummyCommand;
import com.mrnobody.morecommands.util.GlobalSettings;
import com.mrnobody.morecommands.util.LanguageManager;
import com.mrnobody.morecommands.util.PlayerSettings;
import com.mrnobody.morecommands.util.Variables;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;

/**
 * The patched class of {@link ClientCommandHandler} <br>
 * This patch is needed for the alias command. An alias is just
 * a dummy command with no function, but an event is sent if it
 * is executed triggering the original command. The dummy command
 * will be canceled. Therefore the {@link ClientCommandHandler#executeCommand(ICommandSender, String)}
 * method will return 0, which means the command is sent to the server,
 * although it doesn't exist there. This patch changes the return
 * to 1, which makes forge not sending the command to the server.
 * Another aspect why this patch is needed is to use variables.
 * 
 * @author MrNobody98
 *
 */
public class ClientCommandManager extends ClientCommandHandler {
	public ClientCommandManager(ClientCommandHandler parent) {
		super();
		for (Object command : parent.getCommands().values()) this.registerCommand((ICommand) command);
	}
	
    @Override
    public int executeCommand(ICommandSender sender, String message)
    {
        message = message.trim();
        boolean slash = message.startsWith("/");

        if (message.startsWith("/"))
        {
            message = message.substring(1);
        }
        
        Set<String> unresolvedVars = new HashSet<String>();
        
        if (GlobalSettings.enablePlayerVars) {
        	ClientPlayerSettings settings = Minecraft.getMinecraft().thePlayer.getCapability(PlayerSettings.SETTINGS_CAP_CLIENT, null);
        	
        	if (settings != null) {
            	try {message = Variables.replaceVars(message, settings.variables);}
            	catch (Variables.VariablesCouldNotBeResolvedException e) {unresolvedVars = e.getVariables(); message = e.getNewString();}
        	}
        }

        String[] temp = message.split(" ");
        String[] args = new String[temp.length - 1];
        String commandName = temp[0];
        System.arraycopy(temp, 1, args, 0, args.length);
        ICommand icommand = getCommands().get(commandName);

        try
        {
            if (icommand == null)
            {
            	Minecraft.getMinecraft().thePlayer.sendChatMessage(slash ? "/" + message : message); return 1;
            }
            
            if (!unresolvedVars.isEmpty()) {
            	TextComponentString text = new TextComponentString(LanguageManager.translate(MoreCommands.INSTANCE.getCurrentLang(sender), "command.var.cantBeResolved", unresolvedVars.toString()));
            	text.getStyle().setColor(RED); sender.addChatMessage(text);
            }
            
            if (icommand.checkPermission(this.getServer(), sender)) {
                CommandEvent event = new CommandEvent(icommand, sender, args);
                if (MinecraftForge.EVENT_BUS.post(event))
                {
                    if (event.getException() != null)
                    {
                        throw event.getException();
                    }
                    if (icommand instanceof DummyCommand) return 1;
                    else {Minecraft.getMinecraft().thePlayer.sendChatMessage("/" + message); return 1;}
                }

                this.tryExecute(sender, args, icommand, message);
                return 1;
            }
            else {
                sender.addChatMessage(format(RED, "commands.generic.permission"));
            }
        }
        catch (WrongUsageException wue)
        {
            sender.addChatMessage(format(RED, "commands.generic.usage", format(RED, wue.getMessage(), wue.getErrorObjects())));
        }
        catch (CommandException ce)
        {
            sender.addChatMessage(format(RED, ce.getMessage(), ce.getErrorObjects()));
        }
        catch (Throwable t)
        {
            sender.addChatMessage(format(RED, "commands.generic.exception"));
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
