package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.event.EventHandler;
import com.mrnobody.morecommands.event.Listeners.EventListener;
import com.mrnobody.morecommands.util.Coordinate;
import com.mrnobody.morecommands.util.EntityUtils;
import com.mrnobody.morecommands.util.WorldUtils;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
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
		if (event.entity.noClip && event.source == DamageSource.inWall) event.setCanceled(true);
	}
	
	@Override
	public String getCommandName() {
		return "noclip";
	}

	@Override
	public String getCommandUsage() {
		return "command.noclip.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		EntityPlayerMP player = getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class);
		
		if (!player.capabilities.isFlying && !player.noClip)
			throw new CommandException("command.noclip.mustBeFlying", sender);
		
		try {player.noClip = parseTrueFalse(params, 0, !player.noClip);}
		catch (IllegalArgumentException ex) {throw new CommandException("command.noclip.failure", sender);}
		
		sender.sendLangfileMessage(player.noClip ? "command.noclip.enabled" : "command.noclip.disabled");
    	
		if(player.noClip == false)
			ascendPlayer(player);
		
		MoreCommands.INSTANCE.getPacketDispatcher().sendS06Noclip(player, player.noClip);
		return null;
	}

	private static boolean ascendPlayer(EntityPlayerMP player) {
		Coordinate playerPos = EntityUtils.getPosition(player);
		if (WorldUtils.isClearBelow(player.worldObj, playerPos) && playerPos.getY() > 0) {
			return false;
		}
		double y = playerPos.getY() - 1; // in case player was standing on ground
		while (y < 260) {
			if (WorldUtils.isClear(player.worldObj, new Coordinate(playerPos.getX(), y++, playerPos.getZ()))) {
				final double newY;
				if (playerPos.getY() > 0) newY = y - 1;
				else newY = y;
				Coordinate newPos = new Coordinate(playerPos.getX() + 0.5F, newY, playerPos.getZ() + 0.5F);
				EntityUtils.setPosition(player, newPos);
				break;
			}
		}
		return true;
	}
	
	@Override
	public CommandRequirement[] getRequirements() {
		return new CommandRequirement[] {CommandRequirement.MODDED_CLIENT, CommandRequirement.PATCH_ENTITYCLIENTPLAYERMP, CommandRequirement.PATCH_NETHANDLERPLAYSERVER};
	}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	@Override
	public int getDefaultPermissionLevel(String[] args) {
		return 2;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return isSenderOfEntityType(sender, EntityPlayerMP.class);
	}
}
