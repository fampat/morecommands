package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Player;

@Command(
		name = "breathe",
		description = "command.breathe.description",
		example = "command.breathe.example",
		syntax = "command.breathe.syntax",
		videoURL = "command.breathe.videoURL"
		)
public class CommandBreathe extends ServerCommand {
	private final int AIR_MAX = 300;

	@Override
	public String getCommandName() {
		return "breathe";
	}

	@Override
	public String getUsage() {
		return "command.breathe.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Player player = new Player((EntityPlayerMP) sender.getMinecraftISender());
		int air = 0;
		
		if (params.length > 0) {
			try {air = Integer.parseInt(params[0]);}
			catch (NumberFormatException e) {throw new CommandException("command.breathe.noNumber", sender);}
		}
		else air = this.AIR_MAX;
		
		if (player.getMinecraftPlayer().isInWater()) {player.setAir(player.getMinecraftPlayer().getAir() + air > this.AIR_MAX ? this.AIR_MAX : player.getMinecraftPlayer().getAir() + air);}
		else throw new CommandException("command.breathe.notInWater", sender);
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
