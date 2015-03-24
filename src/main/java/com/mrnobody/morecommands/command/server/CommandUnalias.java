package com.mrnobody.morecommands.command.server;

import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommand;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.command.CommandBase.ServerType;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.util.DummyCommand.DummyServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "unalias",
		description = "command.unalias.description",
		example = "command.unalias.example",
		syntax = "command.unalias.syntax",
		videoURL = "command.unalias.videoURL"
		)
public class CommandUnalias extends ServerCommand {
	private final CommandHandler commandHandler = (CommandHandler) MinecraftServer.getServer().getCommandManager();

	@Override
	public String getCommandName() {
		return "unalias";
	}

	@Override
	public String getUsage() {
		return "command.unalias.syntax";
	}
	
	//ADD REMOVE ALL !!!!!!!!

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		ServerPlayerSettings settings = ServerPlayerSettings.playerSettingsMapping.get(sender.getMinecraftISender());
		
		if (params.length > 0) {
			String alias = params[0];
			
			ICommand command = (ICommand) this.commandHandler.getCommands().get(alias);
			
			if (command != null && command instanceof DummyServerCommand) {
				DummyServerCommand cmd = (DummyServerCommand) command;
				
				if (cmd.getOriginalCommandName(sender.getMinecraftISender()) != null)
					cmd.getSenderCommandMapping().remove(sender.getMinecraftISender());
					cmd.getSenderSideMapping().remove(sender.getMinecraftISender());
			}
			else {sender.sendLangfileMessageToPlayer("command.unalias.notFound", new Object[0]);}
		}
		else {sender.sendLangfileMessageToPlayer("command.alias.invalidUsage", new Object[0]);}
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}
	
	@Override
	public void unregisterFromHandler() {}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	@Override
	public int getPermissionLevel() {
		return 0;
	}
}
