package com.mrnobody.morecommands.command.server;

import java.util.Arrays;
import java.util.List;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.TargetSelector;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Coordinate;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

@Command(
		description = "command.execute.description",
		example = "command.execute.example",
		name = "command.execute.name",
		syntax = "command.execute.syntax",
		videoURL = "command.execute.videoURL"
		)
public class CommandExecute extends StandardCommand implements ServerCommandProperties {
	@Override
	public String getCommandName() {
		return "execute";
	}

	@Override
	public String getUsage() {
		return "command.execute.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		params = reparseParamsWithNBTData(params); Coordinate relCoord = null;
		
		if (params.length <= 1)
			throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
		
		if (params[1].equalsIgnoreCase("rel") || params[1].equalsIgnoreCase("relative")) {
			if (params.length <= 5) throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
			
			try {relCoord = getCoordFromParams(sender.getMinecraftISender(), params, 2);}
			catch (NumberFormatException nfe) {}
		}
		
		String target = params[0]; params = Arrays.copyOfRange(params, relCoord == null ? 1 : 5, params.length);
		String command = rejoinParams(params);
		
		if (isTargetSelector(target)) {
			if (target.startsWith("@b")) throw new CommandException("command.execute.invalidTarget", sender);
			List<? extends Entity> entities = TargetSelector.EntitySelector.matchEntites(sender.getMinecraftISender(), target, Entity.class);
			
			for (Entity entity : entities)
				MinecraftServer.getServer().getCommandManager().executeCommand(
				new CommandSender.EntityCommandSenderWrapper(entity, sender.getMinecraftISender(), relCoord), command);
		}
		else {
			EntityPlayerMP player = getPlayer(target);
			if (player == null) throw new CommandException("command.execute.playerNotFound", sender, target);
			
			MinecraftServer.getServer().getCommandManager().executeCommand(
			new CommandSender.EntityCommandSenderWrapper(player, sender.getMinecraftISender(), relCoord), command);
		}
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
	public int getDefaultPermissionLevel() {
		return 2;
	}

	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return true;
	}
}
