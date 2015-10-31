package com.mrnobody.morecommands.command.client;

import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommand;
import net.minecraftforge.client.ClientCommandHandler;

import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.command.ClientCommand;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.util.ClientPlayerSettings;
import com.mrnobody.morecommands.util.DummyCommand.DummyClientCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "unalias",
		description = "command.unalias.description",
		example = "command.unalias.example",
		syntax = "command.unalias.syntax",
		videoURL = "command.unalias.videoURL"
		)
public class CommandUnalias extends ClientCommand {
	private final CommandHandler commandHandler = ClientCommandHandler.instance;

	@Override
	public String getName() {
		return "unalias";
	}

	@Override
	public String getUsage() {
		return "command.unalias.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		if (params.length > 0) {
			String alias = params[0];
			
			ICommand command = (ICommand) this.commandHandler.getCommands().get(alias);
			
			if (command != null && command instanceof DummyClientCommand) {
				this.commandHandler.getCommands().remove(alias);
				ClientPlayerSettings.aliasMapping.remove(alias);
				ClientPlayerSettings.saveSettings();
				sender.sendLangfileMessage("command.unalias.success");
			}
			else throw new CommandException("command.unalias.notFound", sender);
		}
		else throw new CommandException("command.unalias.invalidUsage", sender);
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
		return false;
	}
	
	@Override
	public int getPermissionLevel() {
		return 0;
	}
}
