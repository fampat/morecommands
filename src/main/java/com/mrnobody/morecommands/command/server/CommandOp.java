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
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListOpsEntry;
import net.minecraftforge.fml.common.FMLCommonHandler;

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
			MinecraftServer server = sender.getServer();
			GameProfile profile = server.getPlayerProfileCache().getGameProfileForUsername(params[0]);
			int permLevel = server.getOpPermissionLevel();
			
            if (profile == null)
                throw new CommandException("command.op.playerNotFound", sender, params[0]);
            
            if (params.length > 1) {
            	try {permLevel = parseInt(params[1], 0, server.getOpPermissionLevel());}
            	catch (NumberInvalidException nie) {throw new CommandException(nie);}
            }
            
            server.getPlayerList().getOppedPlayers().addEntry(new UserListOpsEntry(profile, permLevel, server.getPlayerList().bypassesPlayerLimit(profile)));
            sendPlayerPermissionLevel(server.getPlayerList().getPlayerByUUID(profile.getId()), permLevel);
            notifyCommandListener(sender.getMinecraftISender(), this, "commands.op.success", params[0]);
        }
		else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
		
		return null;
	}
	
	//Copied from net.minecraft.server.management.PlayerList
	private void sendPlayerPermissionLevel(EntityPlayerMP player, int permLevel) {
		if (player != null && player.connection != null) {
			byte b;
			
			if (permLevel <= 0) b = 24;
			else if (permLevel >= 4) b = 28;
			else b = (byte) (24 + permLevel);
			
			player.connection.sendPacket(new SPacketEntityStatus(player, b));
		}
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
		return FMLCommonHandler.instance().getMinecraftServerInstance().getOpPermissionLevel();
	}

	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return true;
	}
}
