package com.mrnobody.morecommands.command.client;

import java.util.Iterator;

import com.mrnobody.morecommands.command.ClientCommandProperties;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;

import net.minecraft.command.ICommandManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;

@Command(
		name = "sudo",
		description = "command.sudo.description",
		example = "command.sudo.example",
		syntax = "command.sudo.syntax",
		videoURL = "command.sudo.videoURL"
		)
public class CommandSudo extends StandardCommand implements ClientCommandProperties {

	@Override
	public String getCommandName() {
		return "sudo";
	}

	@Override
	public String getCommandUsage() {
		return "command.sudo.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		if (params.length > 1 && MinecraftServer.getServer() instanceof IntegratedServer && ((IntegratedServer) MinecraftServer.getServer()).getPublic()) {
			EntityPlayer player = null;
			Object playerEntity;
			Iterator players = MinecraftServer.getServer().getConfigurationManager().playerEntityList.iterator();
			
			while (players.hasNext()) {
				playerEntity = players.next();
				
				if (playerEntity instanceof EntityPlayer) {
					if (((EntityPlayer) playerEntity).getCommandSenderName().equalsIgnoreCase(params[0])) {
						player = (EntityPlayer) playerEntity;
						break;
					}
				}
			}
			
			if (player == null) throw new CommandException("command.sudo.playerNotFound", sender);
			
			ICommandManager manager = MinecraftServer.getServer().getCommandManager();
			String command = params[1];
			String parameters = "";
			
			if (params.length > 2) {
				int index = 0;
				
				for (String param : params) {
					if (index > 1) {parameters += " " + param;}
					index++;
				}
			}
			
			manager.executeCommand((new CommandSender(player)).getMinecraftISender(), command + parameters);
			sender.sendLangfileMessage("command.sudo.executed", command + parameters, player.getCommandSenderName());
		}
		else if (!(MinecraftServer.getServer() instanceof IntegratedServer)) throw new CommandException("command.sudo.notInLAN", sender);
		else if (!((IntegratedServer) MinecraftServer.getServer()).getPublic()) throw new CommandException("command.sudo.notInLAN", sender);
		else throw new CommandException("command.sudo.invalidArgs", sender);
		
		return null;
	}
	
	@Override
	public CommandRequirement[] getRequirements() {
		return new CommandRequirement[0];
	}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.INTEGRATED;
	}
	
	@Override
	public int getDefaultPermissionLevel(String[] args) {
		return 0;
	}

	@Override
	public boolean registerIfServerModded() {
		return true;
	}
}
