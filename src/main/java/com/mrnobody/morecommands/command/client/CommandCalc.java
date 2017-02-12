package com.mrnobody.morecommands.command.client;

import com.mrnobody.morecommands.command.ClientCommandProperties;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.CalculationParser;

@Command(
		description="command.calc.description",
		example="command.calc.example",
		name="calc",
		syntax="command.calc.syntax",
		videoURL="command.calc.videoURL"
		)
public class CommandCalc extends StandardCommand implements ClientCommandProperties {
	@Override
	public String getCommandName() {
		return "calc";
	}
  
	@Override
	public String getCommandUsage() {
		return "command.calc.syntax";
	}
  
	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		double ret;
		
		try {sender.sendLangfileMessage("command.calc.result", ret = CalculationParser.parseCalculation(rejoinParams(params)));}
		catch (NumberFormatException nfe) {throw new CommandException("command.calc.failure", sender, nfe.getMessage());}
		
		return Double.toString(ret);
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
	public int getDefaultPermissionLevel(String[] args) {
		return 0;
	}

	@Override
	public boolean registerIfServerModded() {
		return false;
	}
}
