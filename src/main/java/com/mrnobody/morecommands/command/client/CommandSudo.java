package com.mrnobody.morecommands.command.client;

import java.util.Iterator;

import net.minecraft.command.ICommandManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;

import com.mrnobody.morecommands.command.ClientCommand;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Player;

@Command(
		name = "sudo",
		description = "command.sudo.description",
		example = "command.sudo.example",
		syntax = "command.sudo.syntax",
		videoURL = "command.sudo.videoURL"
		)
public class CommandSudo extends ClientCommand {

	@Override
	public String getName() {
		return "sudo";
	}

	@Override
	public String getUsage() {
		return "command.sudo.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		if (params.length > 1 && MinecraftServer.getServer() instanceof IntegratedServer && ((IntegratedServer) MinecraftServer.getServer()).getPublic()) {
			Player player = null;
			Object playerEntity;
			Iterator players = MinecraftServer.getServer().getConfigurationManager().playerEntityList.iterator();
			
			while (players.hasNext()) {
				playerEntity = players.next();
				
				if (playerEntity instanceof EntityPlayerMP) {
					if (((EntityPlayerMP) playerEntity).getName().equalsIgnoreCase(params[0])) {
						player = new Player((EntityPlayerMP) playerEntity);
						break;
					}
				}
			}
			
			if (player == null) {
				sender.sendLangfileMessage("command.sudo.playerNotFound", new Object[0]);
				return;
			}
			
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
			sender.sendLangfileMessage("command.sudo.executed", new Object[] {command + parameters, player.getPlayerName()});
		}
		else if (!(MinecraftServer.getServer() instanceof IntegratedServer)) {sender.sendLangfileMessage("command.sudo.notInLAN", new Object[0]);}
		else if (!((IntegratedServer) MinecraftServer.getServer()).getPublic()) {sender.sendLangfileMessage("command.sudo.notInLAN", new Object[0]);}
		else {sender.sendLangfileMessage("command.sudo.invalidArgs", new Object[0]);}
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.INTEGRATED;
	}
	
	@Override
	public int getPermissionLevel() {
		return 0;
	}

	@Override
	public boolean registerIfServerModded() {
		return true;
	}
}
