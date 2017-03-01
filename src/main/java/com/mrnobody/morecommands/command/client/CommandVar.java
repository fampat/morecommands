package com.mrnobody.morecommands.command.client;

import java.util.Arrays;
import java.util.Map;

import com.mrnobody.morecommands.command.ClientCommandProperties;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.network.PacketHandlerClient;
import com.mrnobody.morecommands.network.PacketHandlerClient.CommandResultCallback;
import com.mrnobody.morecommands.settings.ClientPlayerSettings;
import com.mrnobody.morecommands.settings.MoreCommandsConfig;
import com.mrnobody.morecommands.util.CalculationParser;

import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.ClientCommandHandler;

@Command(
		name = "var",
		description = "command.var.description",
		example = "command.var.example",
		syntax = "command.var.syntax",
		videoURL = "command.var.videoURL"
		)
public class CommandVar extends StandardCommand implements ClientCommandProperties {
	private static final int PAGE_MAX = 15;
	
	@Override
	public String getCommandName() {
		return "var";
	}

	@Override
	public String getCommandUsage() {
		return "command.var.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		if (!isSenderOfEntityType(sender.getMinecraftISender(), EntityClientPlayerMP.class)) throw new CommandException("command.generic.notAPlayer", sender);
		ClientPlayerSettings settings = getPlayerSettings(getSenderAsEntity(sender.getMinecraftISender(), EntityClientPlayerMP.class));
		
		if (!MoreCommandsConfig.enablePlayerVars)
			throw new CommandException("command.var.varsDisabled", sender);
		
    	if (params.length > 0) {    		
    		if (params[0].equalsIgnoreCase("list")) {
    			Map<String, String> vars = settings.variables;
    			String[] keys = vars.keySet().toArray(new String[vars.size()]); int page = 0;
    			
    			if (keys.length == 0) {sender.sendStringMessage("command.var.noVars", EnumChatFormatting.RED); return null;}
    			
    			if (params.length > 1) {
    				try {
    					page = Integer.parseInt(params[1]) - 1; 
    					if (page < 0) page = 0;
    					else if (page * PAGE_MAX > keys.length) page = keys.length / PAGE_MAX;
    				}
    				catch (NumberFormatException e) {throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());}
    			}
    			
    			final int stop = (page + 1) * PAGE_MAX;
    			for (int i = page * PAGE_MAX; i < stop && i < keys.length; i++)
    				sender.sendStringMessage("'" + keys[i] + "' = '" + vars.get(keys[i]) + "'");
    			
    			sender.sendLangfileMessage("command.var.more", EnumChatFormatting.RED);
    		}
    		else if (params[0].equalsIgnoreCase("get") && params.length > 1) {
    			if (!settings.variables.containsKey(params[1]))
    				throw new CommandException("command.var.notFound", sender, params[1]);
    			
    			sender.sendLangfileMessage("command.var.get", params[1], settings.variables.get(params[1]));
    		}
    		else if ((params[0].equalsIgnoreCase("del") || params[0].equalsIgnoreCase("delete") ||
    				params[0].equalsIgnoreCase("rm") || params[0].equalsIgnoreCase("remove")) && params.length > 1) {
    			if (!settings.variables.containsKey(params[1]))
    				throw new CommandException("command.var.notFound", sender, params[1]);
    			
    			settings.variables.remove(params[1]);
    			sender.sendLangfileMessage("command.var.deleted", params[1]);
    		}
    		else if ((params[0].equalsIgnoreCase("delAll") || params[0].equalsIgnoreCase("deleteAll") ||
    				params[0].equalsIgnoreCase("rmAll") || params[0].equalsIgnoreCase("removeAll"))) {
    			
    			settings.variables.clear();
    			sender.sendLangfileMessage("command.var.deletedAll");
    		}
    		else if (params[0].equalsIgnoreCase("set") && params.length > 2) {
    			String value = rejoinParams(Arrays.copyOfRange(params, 2, params.length));
    			
    			settings.variables.put(params[1], value);
    			sender.sendLangfileMessage("command.var.created", params[1], value);
    		}
    		else if (params[0].equalsIgnoreCase("calc") && params.length > 2) {
    			try {
    				String value = Double.toString(CalculationParser.parseCalculation(rejoinParams(Arrays.copyOfRange(params, 2, params.length))));
        			
    				settings.variables.put(params[1], value);
    				sender.sendLangfileMessage("command.var.created", params[1], value);
    			}
    			catch (NumberFormatException nfe) {throw new CommandException("command.var.notNumeric", sender);}
    		}
    		else if (params[0].equalsIgnoreCase("grab") && params.length > 2) {
    			String var = params[1], command = rejoinParams(Arrays.copyOfRange(params, 2, params.length));
    			if (!isSenderOfEntityType(sender.getMinecraftISender(), com.mrnobody.morecommands.patch.EntityClientPlayerMP.class))
    				throw new CommandException("command.generic.clientPlayerNotPatched", sender);
    			
    			if (command.startsWith("macro") || command.startsWith("/macro"))
    				throw new CommandException("command.var.grabMacro", sender);
    			
    			com.mrnobody.morecommands.patch.EntityClientPlayerMP patchedPlayer = getSenderAsEntity(sender.getMinecraftISender(), com.mrnobody.morecommands.patch.EntityClientPlayerMP.class);
    			patchedPlayer.setCaptureNextCommandResult();
    			
    			if (ClientCommandHandler.instance.executeCommand(patchedPlayer, command) != 0) {
    				if (patchedPlayer.getCmdSentToServer() != null) {
        				String cmdSentToServer = patchedPlayer.getCmdSentToServer();
        				patchedPlayer.getCapturedCommandResult(); //reset
        				PacketHandlerClient.addPendingRemoteCommand(cmdSentToServer, new CommandResultHandler(sender, settings, var));
    				}
    				else setCommandResult(sender, settings, var, patchedPlayer.getCapturedCommandResult());
    			}
    			else {
    				patchedPlayer.getCapturedCommandResult(); //reset
    				PacketHandlerClient.addPendingRemoteCommand(command, new CommandResultHandler(sender, settings, var));
    			}
    		}
    		else throw new CommandException("command.var.invalidArg", sender);
    	}
    	else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
    	
    	return null;
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
	public boolean registerIfServerModded() {
		return true;
	}
	
	@Override
	public int getDefaultPermissionLevel(String[] args) {
		return 0;
	}
	
	private static void setCommandResult(CommandSender sender, ClientPlayerSettings settings, String variable, String result) {
		if (result != null && !result.isEmpty()) settings.variables.put(variable, result);
		sender.sendLangfileMessage("command.var.created", variable, result);
	}
	
	private static final class CommandResultHandler implements CommandResultCallback {
		private ClientPlayerSettings settings;
		private String variable;
		private CommandSender sender;
		
		public CommandResultHandler(CommandSender sender, ClientPlayerSettings settings, String variable) {
			this.sender = sender;
			this.settings = settings;
			this.variable = variable;
		}
		
		@Override
		public void setCommandResult(String result) {
			CommandVar.setCommandResult(this.sender, this.settings, this.variable, result);
		}

		@Override
		public void timeout() {}
	};
}
