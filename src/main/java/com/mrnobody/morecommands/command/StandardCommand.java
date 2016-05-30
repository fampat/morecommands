package com.mrnobody.morecommands.command;

import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
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
    	if (this.checkRequirements(sender, params, this instanceof ClientCommandProperties ? Side.CLIENT : Side.SERVER)) {
        	try {this.execute(new CommandSender(sender), params);}
        	catch (CommandException e) {throw new net.minecraft.command.CommandException(e.getMessage());}
    	}
	}
}
