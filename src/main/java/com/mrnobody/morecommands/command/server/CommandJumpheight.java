package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "jumpheight",
		description = "command.jumpheight.description",
		example = "command.jumpheight.example",
		syntax = "command.jumpheight.syntax",
		videoURL = "command.jumpheight.videoURL"
		)
public class CommandJumpheight extends ServerCommand {
	@Override
	public String getName() {
		return "jumpheight";
	}

	@Override
	public String getUsage() {
		return "command.jumpheight.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		double gravity;
		
		if (params.length > 0) {
			if (params[0].equalsIgnoreCase("reset")) {gravity = 1.0D; sender.sendLangfileMessage("command.jumpheight.reset");}
			else {
				try {
					gravity = Double.parseDouble(params[0]);
					sender.sendLangfileMessage("command.jumpheight.success");
				}
				catch (NumberFormatException nfe) {throw new CommandException("command.jumpheight.NAN", sender);}
			}
			
			MoreCommands.getMoreCommands().getPacketDispatcher().sendS11Gravity((EntityPlayerMP) sender.getMinecraftISender(), gravity);
		}
		else 
			throw new CommandException("command.jumpheight.invalidUsage", sender);
	}

	@Override
	public Requirement[] getRequirements() {
		return new Requirement[] {Requirement.MODDED_CLIENT, Requirement.PATCH_ENTITYPLAYERSP};
	}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}

	@Override
	public int getPermissionLevel() {
		return 2;
	}
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return sender instanceof EntityPlayerMP;
	}
}
