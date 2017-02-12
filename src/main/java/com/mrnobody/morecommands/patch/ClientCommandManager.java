package com.mrnobody.morecommands.patch;

import static net.minecraft.util.EnumChatFormatting.RED;

import com.mrnobody.morecommands.core.AppliedPatches;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.settings.ClientPlayerSettings;
import com.mrnobody.morecommands.settings.MoreCommandsConfig;
import com.mrnobody.morecommands.settings.PlayerSettings;
import com.mrnobody.morecommands.util.DummyCommand;
import com.mrnobody.morecommands.util.Variables;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;

/**
 * The patched class of {@link ClientCommandHandler} <br>
 * This patch is needed for the alias command. An alias is just
 * a dummy command with no function, but an event is sent if it
 * is executed which triggers the original command. The dummy command
 * will be canceled. Therefore the {@link ClientCommandHandler#executeCommand(ICommandSender, String)}
 * method will return 0, which means the command is sent to the server,
 * although it doesn't exist there. This patch changes the return
 * to 1, which makes forge not sending the command to the server.
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
        
        if (message.startsWith("/"))
        {
            message = message.substring(1);
        }
        
		if (MoreCommandsConfig.enablePlayerVars && sender == Minecraft.getMinecraft().thePlayer) {
			ClientPlayerSettings settings = MoreCommands.getEntityProperties(ClientPlayerSettings.class, PlayerSettings.MORECOMMANDS_IDENTIFIER, Minecraft.getMinecraft().thePlayer);
			
			if (settings != null) {
				try {message = Variables.replaceVars(message, getCommands().containsKey(message.split(" ")[0]) ? true : !AppliedPatches.serverModded(), settings.variables);}
				catch (Variables.VariablesCouldNotBeResolvedException e) {message = e.getNewString();}
			}
		}
        
        String[] temp = message.split(" ");
        String[] args = new String[temp.length - 1];
        String commandName = temp[0];
        System.arraycopy(temp, 1, args, 0, args.length);
        ICommand icommand = (ICommand) getCommands().get(commandName);
        
        try
        {
            if (icommand == null)
            {
                return 0;
            }
            
            if (icommand.canCommandSenderUseCommand(sender))
            {
                CommandEvent event = new CommandEvent(icommand, sender, args);
                if (MinecraftForge.EVENT_BUS.post(event))
                {
                    if (event.exception != null)
                    {
                        throw event.exception;
                    }
                    
                    if (icommand instanceof DummyCommand) return 1;
                    else return 0;
                }
                
                icommand.processCommand(sender, args);
                return 1;
            }
            else
            {
                sender.addChatMessage(format(RED, "commands.generic.permission"));
            }
        }
        catch (WrongUsageException wue)
        {
            sender.addChatMessage(format(RED, "commands.generic.usage", format(RED, wue.getMessage(), wue.getErrorOjbects())));
        }
        catch (CommandException ce)
        {
            sender.addChatMessage(format(RED, ce.getMessage(), ce.getErrorOjbects()));
        }
        catch (Throwable t)
        {
            sender.addChatMessage(format(RED, "commands.generic.exception"));
            t.printStackTrace();
        }

        return -1;
    }
    
    //Just a copy of the format method in ClientCommandHandler, because it's private
    private ChatComponentTranslation format(EnumChatFormatting color, String str, Object... args)
    {
        ChatComponentTranslation ret = new ChatComponentTranslation(str, args);
        ret.getChatStyle().setColor(color);
        return ret;
    }
}
