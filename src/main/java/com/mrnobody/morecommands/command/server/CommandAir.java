package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import com.mrnobody.morecommands.core.MoreCommands.ServerType;
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
			try {player.setAir(Integer.parseInt(params[0])); sender.sendLangfileMessage("command.air.success");}
			catch (NumberFormatException e) {
				if (params[0].equalsIgnoreCase("min")) {player.setAir(this.AIR_MIN); sender.sendLangfileMessage("command.air.success");}
				else if (params[0].equalsIgnoreCase("max")) {player.setAir(this.AIR_MAX); sender.sendLangfileMessage("command.air.success");}
				else if (params[0].equalsIgnoreCase("get")) {sender.sendLangfileMessage("command.air.get", player.getMinecraftPlayer().getAir());}
				else throw new CommandException("command.air.invalidParam", sender);
			}
		}
		else if (!player.getMinecraftPlayer().isInWater()) throw new CommandException("command.air.notInWater", sender);
		else throw new CommandException("command.air.invalidUsage", sender);
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
		return 2;
	}
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return sender instanceof EntityPlayerMP;
	}
}
