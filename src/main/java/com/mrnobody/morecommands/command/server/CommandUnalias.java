package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.util.DummyCommand.DummyServerCommand;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

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
	public String getName() {
		return "unalias";
	}

	@Override
	public String getUsage() {
		return "command.unalias.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		ServerPlayerSettings settings = ServerPlayerSettings.getPlayerSettings((EntityPlayerMP) sender.getMinecraftISender());
		
		if (params.length > 0) {
			String alias = params[0];
			
			ICommand command = (ICommand) this.commandHandler.getCommands().get(alias);
			
			if (command != null && command instanceof DummyServerCommand) {
				DummyServerCommand cmd = (DummyServerCommand) command;
				
				if (cmd.getOriginalCommandName(sender.getMinecraftISender()) != null)
					cmd.getSenderCommandMapping().remove(sender.getMinecraftISender());
					cmd.getSenderSideMapping().remove(sender.getMinecraftISender());
			}
			else throw new CommandException("command.unalias.notFound", sender);
		}
		else throw new CommandException("command.unalias.invalidUsage", sender);
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[] {Requirement.HANDSHAKE_FINISHED_IF_CLIENT_MODDED};
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
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return sender instanceof EntityPlayerMP;
	}
}
