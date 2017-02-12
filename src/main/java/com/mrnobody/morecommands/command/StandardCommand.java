package com.mrnobody.morecommands.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * The base class for a standard command. A standard command is a regular
 * command which has only one name and provides only one functionality
 * corresponding to that name
 * 
 * @author MrNobody98
 */
public abstract class StandardCommand extends AbstractCommand {
	@Override
	public final void execute(MinecraftServer server, ICommandSender sender, String[] params) throws net.minecraft.command.CommandException {
		ITextComponent error = this.checkRequirements(sender, params, this instanceof ClientCommandProperties ? Side.CLIENT : Side.SERVER);
		ResultAcceptingCommandSender resultAcceptor = sender instanceof ResultAcceptingCommandSender ? (ResultAcceptingCommandSender) sender : null;
		
    	if (error == null) {
        	try {
        		String result = this.execute(new CommandSender(sender), params);
        		if (resultAcceptor != null) resultAcceptor.setCommandResult(this.getCommandName(), params, result);
        	}
        	catch (CommandException e) {
        		if (e.getCause() instanceof net.minecraft.command.CommandException) {
        			if (resultAcceptor != null) resultAcceptor.setCommandResult(this.getCommandName(), params, e.getCause().getMessage());
        			if (e.getCause().getMessage() != null) throw (net.minecraft.command.CommandException) e.getCause();
        		}
        		else {
        			if (resultAcceptor != null) resultAcceptor.setCommandResult(this.getCommandName(), params, e.getMessage());
        			if (e.getMessage() != null) throw new net.minecraft.command.CommandException(e.getMessage());
        		}
        	}
    	}
    	else {
    		sender.addChatMessage(error);
    		if (resultAcceptor != null) resultAcceptor.setCommandResult(this.getCommandName(), params, error.getUnformattedText());
    	}
	}
}
