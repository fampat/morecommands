package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listeners.EventListener;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Coordinate;
import com.mrnobody.morecommands.wrapper.Player;

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
public class CommandNoclip extends ServerCommand implements EventListener<LivingAttackEvent> {
	
	public CommandNoclip() {EventHandler.ATTACK.getHandler().register(this);}
	
	@Override
	public void onEvent(LivingAttackEvent event) {
		if (event.entity.noClip && event.source == DamageSource.inWall) event.setCanceled(true);
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
		Player player = new Player((EntityPlayerMP) sender.getMinecraftISender());
		
		if (!player.getMinecraftPlayer().capabilities.isFlying && !player.getMinecraftPlayer().noClip)
			throw new CommandException("command.noclip.mustBeFlying", sender);
		
		try {player.getMinecraftPlayer().noClip = parseTrueFalse(params, 0, player.getMinecraftPlayer().noClip);}
		catch (IllegalArgumentException ex) {throw new CommandException("command.noclip.failure", sender);}
		
		sender.sendLangfileMessage(player.getMinecraftPlayer().noClip ? "command.noclip.enabled" : "command.noclip.disabled");
    	
		if(!(player.getMinecraftPlayer() instanceof EntityPlayerMP)) {
			player.getMinecraftPlayer().noClip = false;
			throw new CommandException("command.noclip.failure", sender);
		}
    	
		if(player.getMinecraftPlayer().noClip == false) {
			ascendPlayer(player);
		}
		
		MoreCommands.getMoreCommands().getPacketDispatcher().sendS07Noclip(player.getMinecraftPlayer(), player.getMinecraftPlayer().noClip);
	}

	private static boolean ascendPlayer(Player player) {
		Coordinate playerPos = player.getPosition();
		if(player.getWorld().isClearBelow(playerPos) && playerPos.getY() > 0) {
			return false;
		}
		double y = playerPos.getY() - 1; // in case player was standing on ground
		while (y < 260) {
			if(player.getWorld().isClear(new Coordinate(playerPos.getX(), y++, playerPos.getZ()))) {
				final double newY;
				if (playerPos.getY() > 0) newY = y - 1;
				else newY = y;
				Coordinate newPos = new Coordinate(playerPos.getX() + 0.5F, newY, playerPos.getZ() + 0.5F);
				player.setPosition(newPos);
				break;
			}
		}
		return true;
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[] {Requirement.MODDED_CLIENT, Requirement.PATCH_ENTITYCLIENTPLAYERMP, Requirement.PATCH_NETHANDLERPLAYSERVER};
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
