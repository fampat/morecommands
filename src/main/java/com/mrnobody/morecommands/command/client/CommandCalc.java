package com.mrnobody.morecommands.command.client;

import com.mrnobody.morecommands.command.ClientCommandProperties;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.CalculationParser;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		description="command.calc.description",
		example="command.calc.example",
		name="calc",
		syntax="command.calc.syntax",
		videoURL="command.calc.videoURL"
		)
public class CommandCalc extends StandardCommand implements ClientCommandProperties {
	@Override
	public String getName() {
		return "calc";
	}
  
	@Override
	public String getUsage() {
		return "command.calc.syntax";
	}
  
	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		try {sender.sendLangfileMessage("command.calc.result", CalculationParser.parseCalculation(rejoinParams(params)));}
		catch (NumberFormatException nfe) {throw new CommandException("command.calc.failure", sender, nfe.getMessage());}
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

	@Override
	public boolean registerIfServerModded() {
		return false;
	}
}
