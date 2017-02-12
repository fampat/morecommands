package com.mrnobody.morecommands.command.server;

import com.mojang.authlib.GameProfile;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListOpsEntry;

@Command(
		description = "command.op.description",
		example = "command.op.example",
		name = "command.op.name",
		syntax = "command.op.syntax",
		videoURL = "command.op.videoURL"
		)
public class CommandOp extends StandardCommand implements ServerCommandProperties {
	@Override
	public String getCommandName() {
		return "op";
	}

	@Override
	public String getCommandUsage() {
		return "command.op.syntax";
	}
	
	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		if (params.length > 0) {
			MinecraftServer server = MinecraftServer.getServer();
			GameProfile profile = server.getPlayerProfileCache().getGameProfileForUsername(params[0]);
			int permLevel = server.getOpPermissionLevel();
			
            if (profile == null)
                throw new CommandException("command.op.playerNotFound", sender, params[0]);
            
            if (params.length > 1) {
            	try {permLevel = parseInt(params[1], 0, server.getOpPermissionLevel());}
            	catch (NumberInvalidException nie) {throw new CommandException(nie);}
            }
            
            server.getConfigurationManager().getOppedPlayers().addEntry(new UserListOpsEntry(profile, permLevel));
            notifyOperators(sender.getMinecraftISender(), this, "commands.op.success", params[0]);
        }
		else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
		
		return null;
	}

	@Override
	public CommandRequirement[] getRequirements() {
		return new CommandRequirement[0];
	}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.DEDICATED;
	}

	@Override
	public int getDefaultPermissionLevel(String[] args) {
		return getRequiredPermissionLevel();
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return MinecraftServer.getServer().getOpPermissionLevel();
	}

	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return true;
	}
}
