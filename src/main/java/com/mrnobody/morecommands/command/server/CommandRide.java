package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;

@Command(
		name = "ride",
		description = "command.ride.description",
		example = "command.ride.example",
		syntax = "command.ride.syntax",
		videoURL = "command.ride.videoURL"
		)
public class CommandRide extends ServerCommand {

	@Override
	public String getCommandName() {
		return "ride";
	}

	@Override
	public String getUsage() {
		return "command.ride.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		EntityPlayerMP player = (EntityPlayerMP) sender.getMinecraftISender();
		Entity hit = (new com.mrnobody.morecommands.wrapper.Entity(player)).traceEntity(128.0D);
		
		if (player.ridingEntity != null) {
			sender.sendLangfileMessage("command.ride.dismounted", new Object[0]);
			player.mountEntity(null);
			return;
		}
		
		if (hit != null) {
			if (hit instanceof EntityLiving) {
				player.mountEntity(hit);
				sender.sendLangfileMessage("command.ride.mounted", new Object[0]);
			}
			else sender.sendLangfileMessage("command.ride.notLiving", new Object[0]);
		}
		else sender.sendLangfileMessage("command.ride.notFound", new Object[0]);
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
	public int getPermissionLevel() {
		return 0;
	}

	@Override
	public void unregisterFromHandler() {}
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return sender instanceof EntityPlayerMP;
	}
}
