package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.CalculationParser;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;

@Command(
		description="command.calc.description",
		example="command.calc.example",
		name="calc",
		syntax="command.calc.syntax",
		videoURL="command.calc.videoURL"
		)
public class CommandCalc extends StandardCommand implements ServerCommandProperties {
	@Override
	public String getCommandName() {
		return "calc";
	}
  
	@Override
	public String getUsage() {
		return "command.calc.syntax";
	}
  
	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		String calc = ""; for (String param : params) calc = calc + " " + param;
		try {sender.sendLangfileMessage("command.calc.result", CalculationParser.parseCalculation(calc));}
		catch (NumberFormatException nfe) {throw new CommandException("command.calc.failure", sender, nfe.getMessage());}
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return true;
	}
	
	@Override
	public CommandRequirement[] getRequirements() {
		return new CommandRequirement[0];
	}
  
	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	@Override
	public int getDefaultPermissionLevel() {
		return 0;
	}
}
