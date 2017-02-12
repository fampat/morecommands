package com.mrnobody.morecommands.command;

import com.mrnobody.morecommands.core.MoreCommands.ServerType;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * A wrapper class for {@link StandardCommand}s that delegates
 * all method calls to the wrapped {@link StandardCommand}
 * 
 * @author MrNobody98
 */
public abstract class CommandBase<T extends StandardCommand> extends AbstractCommand {
	private final T delegate;
	
	protected CommandBase(T delegate) {
		if (delegate == null) throw new NullPointerException("delegate == null");
		this.delegate = delegate;
	}
	
	public T getDelegate() {
		return this.delegate;
	}

	@Override
	public String getCommandName() {
		return this.delegate.getCommandName();
	}
	
	@Override
	public String getCommandUsage() {
		return this.delegate.getCommandUsage();
	}
	
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender p_71519_1_) {
		return true;
	}
	
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender p_71519_1_, String[] params) {
		return true;
	}
	
	@Override
	public final void processCommand(ICommandSender sender, String[] params) throws net.minecraft.command.CommandException {
		IChatComponent error = this.checkRequirements(sender, params, this.getSide());
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
	
	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		return this.delegate.execute(sender, params);
	}

	@Override
	public CommandRequirement[] getRequirements() {
		return this.delegate.getRequirements();
	}

	@Override
	public ServerType getAllowedServerType() {
		return this.delegate.getAllowedServerType();
	}

	@Override
	public int getDefaultPermissionLevel(String[] args) {
		return this.delegate.getDefaultPermissionLevel(args);
	}
	
	/**
	 * @return the side on which the wrapped command should be executed
	 */
	public abstract Side getSide();
}