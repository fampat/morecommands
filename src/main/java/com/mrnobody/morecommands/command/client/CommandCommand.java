package com.mrnobody.morecommands.command.client;

import java.util.HashMap;
import java.util.Map;

import com.mrnobody.morecommands.command.ClientCommand;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.core.AppliedPatches;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommand;
import net.minecraftforge.client.ClientCommandHandler;

@Command(
		name = "command",
		description = "command.command.description",
		example = "command.command.example",
		syntax = "command.command.syntax",
		videoURL = "command.command.videoURL"
		)
public class CommandCommand extends ClientCommand {
	private Map<String, ICommand> disabledCommands = new HashMap<String, ICommand>();
	
	@Override
    public String getCommandName()
    {
        return "command";
    }

	@Override
    public String getUsage()
    {
        return "command.command.syntax";
    }

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		if (params.length > 1) {
			if (params[0].equalsIgnoreCase("enable")) {
				ICommand enable = this.disabledCommands.get(params[1]);
				
				if (enable == null && AppliedPatches.serverModded())
					Minecraft.getMinecraft().thePlayer.sendChatMessage("/command enable " + params[1]);
				else if (enable != null) {
					ClientCommandHandler.instance.registerCommand(enable);
					this.disabledCommands.remove(params[1]);
					sender.sendLangfileMessage("command.command.enabled");
				}
				else throw new CommandException("command.command.serverNotModded", sender);
			}
			else if (params[0].equalsIgnoreCase("disable")) {
				if (params[1].equals(this.getCommandName())) throw new CommandException("command.command.wantedToDisable", sender);
				
				ICommand disable = (ICommand) ClientCommandHandler.instance.getCommands().get(params[1]);
				
				if (disable == null && AppliedPatches.serverModded())
					Minecraft.getMinecraft().thePlayer.sendChatMessage("/command disable " + params[1]);
				else if (disable != null) {
					this.disabledCommands.put(disable.getCommandName(), disable);
					ClientCommandHandler.instance.getCommands().remove(params[1]);
					sender.sendLangfileMessage("command.command.disabled");
				}
				else throw new CommandException("command.command.serverNotModded", sender);
			}
			else throw new CommandException("command.command.invalidUsage", sender);
		}
		else throw new CommandException("command.command.invalidUsage", sender);
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
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
	public int getPermissionLevel() {
		return 0;
	}
}
