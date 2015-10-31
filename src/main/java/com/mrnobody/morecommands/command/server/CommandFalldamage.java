package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listeners.EventListener;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.living.LivingFallEvent;

@Command(
		name = "falldamage",
		description = "command.falldamage.description",
		example = "command.falldamage.example",
		syntax = "command.falldamage.syntax",
		videoURL = "command.falldamage.videoURL"
		)
public class CommandFalldamage extends ServerCommand implements EventListener<LivingFallEvent> {
	public CommandFalldamage() {
		EventHandler.FALL.getHandler().register(this);
	}
	
	@Override
	public void onEvent(LivingFallEvent event) {
		if (event.entity instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP) event.entity;
			if (!ServerPlayerSettings.getPlayerSettings(player).falldamage) event.setCanceled(true);
		}
	}
	
	@Override
	public String getCommandName() {
		return "falldamage";
	}

	@Override
	public String getUsage() {
		return "command.falldamage.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		ServerPlayerSettings settings = ServerPlayerSettings.getPlayerSettings((EntityPlayerMP) sender.getMinecraftISender());
    	
		try {settings.falldamage = parseTrueFalse(params, 0, settings.falldamage);}
		catch (IllegalArgumentException ex) {throw new CommandException("command.falldamage.failure", sender);}
		
		sender.sendLangfileMessage(settings.falldamage  ? "command.falldamage.on" : "command.falldamage.off");
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
