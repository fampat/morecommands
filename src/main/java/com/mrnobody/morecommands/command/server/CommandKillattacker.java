package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listeners.EventListener;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

@Command(
		name = "killattacker",
		description = "command.killattacker.description",
		example = "command.killattacker.example",
		syntax = "command.killattacker.syntax",
		videoURL = "command.killattacker.videoURL"
		)
public class CommandKillattacker extends ServerCommand implements EventListener<LivingAttackEvent> {
	public CommandKillattacker() {
		EventHandler.ATTACK.getHandler().register(this);
	}

	@Override
	public void onEvent(LivingAttackEvent event) {
		if (!(event.entity instanceof EntityPlayerMP)) return;
		
		if (ServerPlayerSettings.getPlayerSettings((EntityPlayerMP) event.entity).killattacker) {
			if (event.source.getSourceOfDamage() != null && (event.source.getSourceOfDamage() instanceof EntityCreature || event.source.getSourceOfDamage() instanceof EntityArrow)) {
				if (event.source.getSourceOfDamage() instanceof EntityCreature) 
					event.source.getSourceOfDamage().attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) event.entity), Float.MAX_VALUE);
				if (event.source.getSourceOfDamage() instanceof EntityArrow && ((EntityArrow) event.source.getSourceOfDamage()).shootingEntity instanceof EntityCreature)
					((EntityArrow) event.source.getSourceOfDamage()).shootingEntity.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) event.entity), Float.MAX_VALUE);
			}
		}
	}
	
	@Override
	public String getName() {
		return "killattacker";
	}

	@Override
	public String getUsage() {
		return "command.killattacker.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params)throws CommandException {
		ServerPlayerSettings settings = ServerPlayerSettings.getPlayerSettings((EntityPlayerMP) sender.getMinecraftISender());
    	
		try {settings.killattacker = parseTrueFalse(params, 0, settings.killattacker);}
		catch (IllegalArgumentException ex) {throw new CommandException("command.killattacker.failure", sender);}
		
		sender.sendLangfileMessage(settings.killattacker ? "command.killattacker.on" : "command.killattacker.off");
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
