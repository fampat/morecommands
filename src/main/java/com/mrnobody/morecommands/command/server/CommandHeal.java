package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.command.CommandBase.Requirement;
import com.mrnobody.morecommands.command.CommandBase.ServerType;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Player;

import cpw.mods.fml.relauncher.Side;

@Command(
		name = "heal",
		description = "command.heal.description",
		example = "command.heal.example",
		syntax = "command.heal.syntax",
		videoURL = "command.heal.videoURL"
		)
public class CommandHeal extends ServerCommand {
	private final float MAX_HEALTH = 20.0f;

	@Override
	public String getCommandName() {
		return "heal";
	}

	@Override
	public String getUsage() {
		return "command.heal.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params)throws CommandException {
		Player player = new Player((EntityPlayerMP) sender.getMinecraftISender());
		
		if (params.length > 0) {
			try {player.heal(Float.parseFloat(params[0])); sender.sendLangfileMessage("command.heal.success", new Object[0]);}
			catch (NumberFormatException e) {sender.sendLangfileMessage("command.heal.NAN", new Object[0]);}
		}
		else {
			player.heal(MAX_HEALTH - player.getHealth());
			sender.sendLangfileMessage("command.heal.success", new Object[0]);
		}
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}
	
	@Override
	public void unregisterFromHandler() {}

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
