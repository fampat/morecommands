package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.util.Keyboard;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

@Command(
		name = "unbind",
		description = "command.unbind.description",
		example = "command.unbind.example",
		syntax = "command.unbind.syntax",
		videoURL = "command.unbind.videoURL"
		)
public class CommandUnbind extends ServerCommand {
	@Override
	public String getCommandName() {
		return "unbind";
	}

	@Override
	public String getUsage() {
		return "command.unbind.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		ServerPlayerSettings settings = ServerPlayerSettings.getPlayerSettings((EntityPlayerMP) sender.getMinecraftISender());
		
		if (params.length > 0) {
			int keyid;
			
			try {
				keyid = Integer.parseInt(params[0]);
			}
			catch (NumberFormatException e) {
				keyid = Keyboard.getKeyIndex(params[0].toUpperCase());
			}
			
			if (keyid != Keyboard.KEY_NONE) {
				settings.clientKeybindMapping.remove(keyid);
				settings.serverKeybindMapping.remove(keyid);
				settings.saveSettings();
				sender.sendLangfileMessage("command.unbind.success");
			}
			else if (params[0].equalsIgnoreCase("all")) {
				settings.serverKeybindMapping.clear();
				settings.clientKeybindMapping.clear();
				settings.saveSettings();
				sender.sendLangfileMessage("command.unbind.success");
			}
			else throw new CommandException("command.unbind.bindingNotFound", sender);
		}
		else throw new CommandException("command.unbind.invalidUsage", sender);
	}

	@Override
	public Requirement[] getRequirements() {
		return new Requirement[] {Requirement.MODDED_CLIENT};
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
