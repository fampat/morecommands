package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.event.EventHandler;
import com.mrnobody.morecommands.event.Listeners.EventListener;
import com.mrnobody.morecommands.patch.NetHandlerPlayServer;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Player;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

@Command(
		name = "noclip",
		description = "command.noclip.description",
		example = "command.noclip.example",
		syntax = "command.noclip.syntax",
		videoURL = "command.noclip.videoURL"
		)
public class CommandNoclip extends StandardCommand implements ServerCommandProperties, EventListener<LivingAttackEvent> {
	
	public CommandNoclip() {EventHandler.ATTACK.register(this);}
	
	@Override
	public void onEvent(LivingAttackEvent event) {
		if (event.entity instanceof EntityPlayerMP && 
			((EntityPlayerMP) event.entity).playerNetServerHandler instanceof NetHandlerPlayServer &&
			((NetHandlerPlayServer) ((EntityPlayerMP) event.entity).playerNetServerHandler).getOverrideNoclip() 
			&& event.source == DamageSource.inWall) event.setCanceled(true);
	}
	
	@Override
	public String getCommandName() {
		return "noclip";
	}

	@Override
	public String getUsage() {
		return "command.noclip.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		EntityPlayerMP player = (EntityPlayerMP) sender.getMinecraftISender();
		boolean allowNoclip = false;
		
		if (!player.capabilities.isFlying)
			throw new CommandException("command.noclip.mustBeFlying", sender);
		
		if (!((NetHandlerPlayServer) player.playerNetServerHandler).enabled)
			throw new CommandException("command.noclip.reflectionError", sender);
		
		NetHandlerPlayServer handler = (NetHandlerPlayServer) player.playerNetServerHandler;
		
		try {handler.setOverrideNoclip(parseTrueFalse(params, 0, handler.getOverrideNoclip()));}
		catch (IllegalArgumentException ex) {throw new CommandException("command.noclip.failure", sender);}
		
		sender.sendLangfileMessage(handler.getOverrideNoclip() ? "command.noclip.enabled" : "command.noclip.disabled");
    	
		if(handler.getOverrideNoclip() == false) {
			ascendPlayer(new Player(player));
		}
		
		MoreCommands.INSTANCE.getPacketDispatcher().sendS06Noclip(player, handler.getOverrideNoclip());
	}

	private static boolean ascendPlayer(Player player) {
		BlockPos playerPos = player.getPosition();
		if(player.getWorld().isClearBelow(playerPos) && playerPos.getY() > 0) {
			return false;
		}
		double y = playerPos.getY() - 1;
		while (y < 260) {
			if(player.getWorld().isClear(new BlockPos(playerPos.getX(), y++, playerPos.getZ()))) {
				final double newY;
				if(playerPos.getY() > 0) {
					newY = y - 1;
				} else {
					newY = y;
				}
				BlockPos newPos = new BlockPos(playerPos.getX() + 0.5F, newY, playerPos.getZ() + 0.5F);
				player.setPosition(newPos);
				break;
			}
		}
		return true;
	}
	
	@Override
	public CommandRequirement[] getRequirements() {
		return new CommandRequirement[] {
				CommandRequirement.MODDED_CLIENT,
				CommandRequirement.PATCH_ENTITYPLAYERSP,
				CommandRequirement.PATCH_NETHANDLERPLAYSERVER,
				CommandRequirement.PATCH_RENDERGLOBAL
			};
	}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	@Override
	public int getDefaultPermissionLevel() {
		return 2;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return isSenderOfEntityType(sender, EntityPlayerMP.class);
	}
}
