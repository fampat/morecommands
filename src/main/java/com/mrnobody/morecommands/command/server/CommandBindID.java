package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.util.Keyboard;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

@Command(
		name = "bindid",
		description = "command.bindid.description",
		example = "command.bindid.example",
		syntax = "command.bindid.syntax",
		videoURL = "command.bindid.videoURL"
		)
public class CommandBindID extends ServerCommand {
	private final CommandHandler commandHandler = (CommandHandler) MinecraftServer.getServer().getCommandManager();

	@Override
	public String getCommandName() {
		return "bindid";
	}

	@Override
	public String getUsage() {
		return "command.bindid.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		ServerPlayerSettings settings = ServerPlayerSettings.getPlayerSettings((EntityPlayerMP) sender.getMinecraftISender());
		
		if (params.length > 1) {
			try {
				int keyid = Integer.parseInt(params[0]);
				String command = params[1];
				
				if (!this.commandHandler.getCommands().containsKey(command) && !settings.clientCommands.contains(command))
					throw new CommandException("command.generic.notFound", sender);
				
				if (params.length > 2) {
					int index = 0;
					String parameters = "";
				
					for (String param : params) {
						if (index > 1) {parameters += " " + param;}
						index++;
					}
				
					command += parameters;
				}
			
				if (Keyboard.getKeyName(keyid) != null) {
					if (settings.clientCommands.contains(command)) settings.clientKeybindMapping.put(keyid, command);
					else settings.serverKeybindMapping.put(keyid, command);
					
					settings.saveSettings();
					
					sender.sendLangfileMessage("command.bindid.success");
				}
				else throw new CommandException("command.bindid.invalidChar", sender);
			} catch (NumberFormatException e) {throw new CommandException("command.bindid.invalidID", sender);}
		}
		else throw new CommandException("command.bindid.invalidUsage", sender);
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[] {Requirement.HANDSHAKE_FINISHED};
	}
	
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
