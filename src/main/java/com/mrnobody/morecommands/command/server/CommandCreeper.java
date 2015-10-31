package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listeners.EventListener;
import com.mrnobody.morecommands.util.GlobalSettings;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.world.ExplosionEvent;

@Command(
		name = "creeper",
		description = "command.creeper.description",
		example = "command.creeper.example",
		syntax = "command.creeper.syntax",
		videoURL = "command.creeper.videoURL"
		)
public class CommandCreeper extends ServerCommand implements EventListener<ExplosionEvent> {
	public CommandCreeper() {
		EventHandler.EXPLOSION.getHandler().register(this);
	}

	@Override
	public void onEvent(ExplosionEvent event) {
		if (event instanceof ExplosionEvent.Start && event.explosion.getExplosivePlacedBy() instanceof EntityCreeper) {
			if (!GlobalSettings.creeperExplosion) {event.setCanceled(true); return;}
			
			EntityCreeper creeper = (EntityCreeper) event.explosion.getExplosivePlacedBy();
			
			if (creeper.getAttackTarget() instanceof EntityPlayerMP) {
				EntityPlayerMP player = (EntityPlayerMP) creeper.getAttackTarget();
				if (!ServerPlayerSettings.getPlayerSettings(player).creeperExplosion) event.setCanceled(true);
			}
		}
	}
	
	@Override
	public String getCommandName() {
		return "creeper";
	}

	@Override
	public String getUsage() {
		return "command.creeper.syntax";
	}
	
	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		ServerPlayerSettings settings = ServerPlayerSettings.getPlayerSettings((EntityPlayerMP) sender.getMinecraftISender());
		
		try {settings.creeperExplosion = parseTrueFalse(params, 0, settings.creeperExplosion);}
		catch (IllegalArgumentException ex) {throw new CommandException("command.creeper.failure", sender);}
		
		sender.sendLangfileMessage(settings.climb ? "command.creeper.on" : "command.creeper.off");
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
