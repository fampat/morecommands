package com.mrnobody.morecommands.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.command.CommandNotFoundException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

/**
 * A dummy command used for aliases. Every time an alias <br>
 * is set a dummy command is registered. The original command is executed
 * by receiving a command event
 * 
 * @author MrNobody98
 * 
 */
public abstract class DummyCommand implements ICommand {
	public final int compareTo(Object o) {return -1;}
	public final String getCommandUsage(ICommandSender p_71518_1_) {return "";}
	public final void execute(ICommandSender sender, String[] msg) throws CommandNotFoundException {throw new CommandNotFoundException();}
	public final boolean canCommandSenderUse(ICommandSender p_71519_1_) {return true;}
	public final List addTabCompletionOptions(ICommandSender sender,String[] args, BlockPos pos) {return null;}
	public final boolean isUsernameIndex(String[] names, int index) {return false;}
	public final List getAliases() {return null;}
	
	public static class DummyServerCommand extends DummyCommand {
		private String name;
		
		private Map<ICommandSender, String> senderCommandMapping = new HashMap<ICommandSender, String>();
		private Map<ICommandSender, Boolean> senderSideMapping = new HashMap<ICommandSender, Boolean>();
		
		public String getOriginalCommandName(ICommandSender sender) {return this.senderCommandMapping.get(sender);}
		
		public void setOriginalCommandName(ICommandSender sender, String command) {this.senderCommandMapping.put(sender, command);}
		
		public DummyServerCommand(String name, ICommandSender sender, String command, boolean clientSide) {
			this.name = name;
			this.senderCommandMapping.put(sender, command);
			this.senderSideMapping.put(sender, clientSide);
		}
		
	    public String getName() {return this.name;}
	    
	    public Map<ICommandSender, String> getSenderCommandMapping() {return this.senderCommandMapping;}
	    
	    public Map<ICommandSender, Boolean> getSenderSideMapping() {return this.senderSideMapping;}
	}
	
	public static class DummyClientCommand extends DummyCommand {
		private String name;
		private String command;
		
		public String getOriginalCommandName() {return command;}
		
		public void setOriginalCommandName(String command) {this.command = command;}
		
		public DummyClientCommand(String name, String command) {
			this.name = name;
			this.command = command;
		}
		
	    public String getName() {return this.name;}
	}
}
