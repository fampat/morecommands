package com.mrnobody.morecommands.util;

import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandNotFoundException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

/**
 * A dummy command used for aliases. Every time an alias <br>
 * is set a dummy command is registered. The original command is executed
 * by receiving a command event
 * 
 * @author MrNobody98
 * 
 */
public final class DummyCommand implements ICommand {
	private final String name;
	private final boolean isClient;
	
	@Override public int compareTo(ICommand arg0) {return -1;}
	public final String getUsage(ICommandSender p_71518_1_) {return "";}
	public final void execute(MinecraftServer server, ICommandSender sender, String[] msg) throws CommandException {throw new CommandNotFoundException();}
	public final boolean checkPermission(MinecraftServer server, ICommandSender p_71519_1_) {return true;}
	public final List<String> getTabCompletions(MinecraftServer server, ICommandSender p_71516_1_, String[] p_71516_2_, BlockPos pos) {return null;}
	public final boolean isUsernameIndex(String[] names, int index) {return false;}
	public final List<String> getAliases() {return null;}
	
	@Override
	public String getName() {
		return this.name;
	}
	
	/**
	 * @return whether this command was registered client side
	 */
	public boolean isClient() {
		return this.isClient;
	}
	
	public DummyCommand(String name, boolean isClient) {
		this.name = name;
		this.isClient = isClient;
	}
}