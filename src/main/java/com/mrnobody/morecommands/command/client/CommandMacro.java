package com.mrnobody.morecommands.command.client;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;
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

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.ClientCommandHandler;

@Command(
	name = "macro",
	description = "command.macro.description",
	syntax = "command.macro.syntax",
	example = "command.macro.example",
	videoURL = "command.macro.videoURL"
		)
public class CommandMacro extends StandardCommand implements ClientCommandProperties {
	@Override
	public boolean registerIfServerModded() {
		return true;
	}

	@Override
	public String getCommandName() {
		return "macro";
	}

	@Override
	public String getCommandUsage() {
		return "command.macro.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		if (!isSenderOfEntityType(sender.getMinecraftISender(), EntityClientPlayerMP.class))
			throw new CommandException("command.generic.notAPlayer", sender);
		
		ClientPlayerSettings settings = getPlayerSettings(getSenderAsEntity(sender.getMinecraftISender(), EntityClientPlayerMP.class));
		
		if (params.length > 0) {
			if ((params[0].equalsIgnoreCase("delete") || params[0].equalsIgnoreCase("del") || params[0].equalsIgnoreCase("remove") || params[0].equalsIgnoreCase("rem")) && params.length > 1) {
				if (!settings.macros.containsKey(params[1])) throw new CommandException("command.macro.notFound", sender, params[1]);
				else {
					settings.macros.remove(params[1]);
					sender.sendLangfileMessage("command.macro.deleteSuccess", params[1]);
				}
			}
			else if ((params[0].equalsIgnoreCase("exec") || params[0].equalsIgnoreCase("execute")) && params.length > 1) {
				List<String> commands = settings.macros.get(params[1]);
				
				if (commands != null)
					new MacroExecutor(sender, commands).continueExecution();
				else 
					throw new CommandException("command.macro.notFound", sender, params[1]);
			}
			else if ((params[0].equalsIgnoreCase("add") || params[0].equalsIgnoreCase("new") || params[0].equalsIgnoreCase("create") || params[0].equalsIgnoreCase("edit")) && params.length > 2) {
				if (settings.macros.containsKey(params[1]) && (params[0].equalsIgnoreCase("add") || params[0].equalsIgnoreCase("new") || params[0].equalsIgnoreCase("create")))
						throw new CommandException("command.macro.exists", sender, params[1]);
				
				settings.macros.put(params[1], Lists.newArrayList(rejoinParams(Arrays.copyOfRange(params, 2, params.length)).split(";")));
				sender.sendLangfileMessage("command.macro.createSuccess", params[1]);
			}
			else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
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
	public int getDefaultPermissionLevel(String[] args) {
		return 0;
	}
	
	private static class MacroExecutor implements CommandResultCallback {
		private int execIdx = 0;
		private List<String> commands;
		private CommandSender sender;
		private String variable;
		
		public MacroExecutor(CommandSender sender, List<String> commands) {
			this.sender = sender;
			this.commands = commands;
		}
		
		public void continueExecution() {
			for (; this.execIdx < this.commands.size(); this.execIdx++) {
				if (this.commands.get(this.execIdx).startsWith("/var grab ") || this.commands.get(this.execIdx).startsWith("var grab")) {
					grabVarAndBlock(this.commands.get(this.execIdx++));
					break;
				}
				else {
					String command = this.commands.get(this.execIdx);
					
					if (ClientCommandHandler.instance.executeCommand(Minecraft.getMinecraft().thePlayer, command) == 0)
						Minecraft.getMinecraft().thePlayer.sendChatMessage(command.startsWith("/") ? command : "/" + command);
				}
			}
		}
		
		private void grabVarAndBlock(String command) {
			command = command.substring("var grab ".length() + (command.startsWith("/") ? 1 : 0));
			this.variable = command.split(" ")[0]; command = command.substring(this.variable.length()).trim();
			
			if (!isSenderOfEntityType(sender.getMinecraftISender(), com.mrnobody.morecommands.patch.EntityClientPlayerMP.class))
				this.sender.sendLangfileMessage("command.generic.clientPlayerNotPatched", EnumChatFormatting.RED);
			
			if (command.startsWith("macro") || command.startsWith("/macro"))
				this.sender.sendLangfileMessage("command.var.grabMacro", EnumChatFormatting.RED);
			
			com.mrnobody.morecommands.patch.EntityClientPlayerMP patchedPlayer = getSenderAsEntity(this.sender.getMinecraftISender(), com.mrnobody.morecommands.patch.EntityClientPlayerMP.class);
			patchedPlayer.setCaptureNextCommandResult();
			
			if (ClientCommandHandler.instance.executeCommand(patchedPlayer, command) != 0) {
				if (patchedPlayer.getCmdSentToServer() != null) {
    				String cmdSentToServer = patchedPlayer.getCmdSentToServer();
    				patchedPlayer.getCapturedCommandResult(); //reset
    				PacketHandlerClient.addPendingRemoteCommand(cmdSentToServer, this);
				}
				else setCommandResult(patchedPlayer.getCapturedCommandResult());
			}
			else {
				patchedPlayer.getCapturedCommandResult(); //reset
				PacketHandlerClient.addPendingRemoteCommand(command, this);
			}
		}
		
		@Override
		public void setCommandResult(String result) {
			com.mrnobody.morecommands.patch.EntityClientPlayerMP patchedPlayer = getSenderAsEntity(this.sender.getMinecraftISender(), com.mrnobody.morecommands.patch.EntityClientPlayerMP.class);
			ClientPlayerSettings settings = getPlayerSettings(patchedPlayer);
			
			if (result != null && !result.isEmpty()) settings.variables.put(this.variable, result);
			this.sender.sendLangfileMessage("command.var.created", variable, result);
			
			continueExecution();
		}
		
		@Override
		public void timeout() {
			continueExecution();
		}
	}
}
