package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Player;

@Command(
		name = "air",
		description = "command.air.description",
		example = "command.air.example",
		syntax = "command.air.syntax",
		videoURL = "command.air.videoURL"
		)
public class CommandAir extends ServerCommand {

	private final int AIR_MIN = 1;
	private final int AIR_MAX = 300;
	
	@Override
	public String getCommandName() {
		return "air";
	}

	@Override
	public String getUsage() {
		return "command.air.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
    	Player player = new Player((EntityPlayerMP) sender.getMinecraftISender());
    	
		if (params.length > 0 && player.getMinecraftPlayer().isInWater()) {
			try {player.setAir(Integer.parseInt(params[0])); sender.sendLangfileMessage("command.air.success", new Object[0]);}
			catch (NumberFormatException e) {
				if (params[0].equalsIgnoreCase("min")) {player.setAir(this.AIR_MIN); sender.sendLangfileMessage("command.air.success", new Object[0]);}
				else if (params[0].equalsIgnoreCase("max")) {player.setAir(this.AIR_MAX); sender.sendLangfileMessage("command.air.success", new Object[0]);}
				else if (params[0].equalsIgnoreCase("get")) {sender.sendLangfileMessage("command.air.get", new Object[] {player.getMinecraftPlayer().getAir()});}
				else {sender.sendLangfileMessage("command.air.invalidParam", new Object[0]);}
			}
		}
		else if (!player.getMinecraftPlayer().isInWater()) {sender.sendLangfileMessage("command.air.notInWater", new Object[0]);}
		else {sender.sendLangfileMessage("command.air.invalidUsage", new Object[0]);}
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}
	
	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	public void unregisterFromHandler() {}
	
	@Override
	public int getPermissionLevel() {
		return 2;
	}
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return sender instanceof EntityPlayerMP;
	}
}
