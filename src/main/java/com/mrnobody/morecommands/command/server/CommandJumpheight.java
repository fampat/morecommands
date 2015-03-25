package com.mrnobody.morecommands.command.server;

import net.minecraft.command.CommandBase;
import net.minecraft.entity.player.EntityPlayerMP;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.packet.server.S10PacketGravity;
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
	public void unregisterFromHandler() {}

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
			if (params[0].equalsIgnoreCase("reset")) {gravity = 1.0D; sender.sendLangfileMessageToPlayer("command.jumpheight.reset", new Object[0]);}
			else {
				try {
					gravity = Double.parseDouble(params[0]);
					sender.sendLangfileMessageToPlayer("command.jumpheight.success", new Object[0]);
				}
				catch (NumberFormatException nfe) {
					sender.sendLangfileMessageToPlayer("command.jumpheight.NAN", new Object[0]);
					return;
				}
			}
			
			S10PacketGravity packet = new S10PacketGravity();
			packet.gravity = gravity;
			MoreCommands.getMoreCommands().getNetwork().sendTo(packet, (EntityPlayerMP) sender.getMinecraftISender());
		}
		else {
			sender.sendLangfileMessageToPlayer("command.jumpheight.invalidUsage", new Object[0]);
		}
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
}
