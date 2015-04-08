package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listener;
import com.mrnobody.morecommands.packet.server.S06PacketNoclip;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Coordinate;
import com.mrnobody.morecommands.wrapper.Player;

@Command(
		name = "noclip",
		description = "command.noclip.description",
		example = "command.noclip.example",
		syntax = "command.noclip.syntax",
		videoURL = "command.noclip.videoURL"
		)
public class CommandNoclip extends ServerCommand implements Listener<LivingAttackEvent> {
	
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
		boolean allowNoclip = false;
		
		if(!player.getMinecraftPlayer().capabilities.isFlying && !player.getMinecraftPlayer().noClip) {
			sender.sendLangfileMessage("command.noclip.mustBeFlying", new Object[0]); return;
		}
    	
		if (params.length >= 1) {
			if (params[0].equalsIgnoreCase("true")) {player.getMinecraftPlayer().noClip = true;}
			else if (params[0].equalsIgnoreCase("false")) {player.getMinecraftPlayer().noClip = false;}
			else if (params[0].equalsIgnoreCase("0")) {player.getMinecraftPlayer().noClip = false;}
			else if (params[0].equalsIgnoreCase("1")) {player.getMinecraftPlayer().noClip = true;}
			else if (params[0].equalsIgnoreCase("on")) {player.getMinecraftPlayer().noClip = true;}
			else if (params[0].equalsIgnoreCase("off")) {player.getMinecraftPlayer().noClip = false;}
    		else if (params[0].equalsIgnoreCase("enable")) {player.getMinecraftPlayer().noClip = true;}
    		else if (params[0].equalsIgnoreCase("disable")) {player.getMinecraftPlayer().noClip = false;}
			else {sender.sendLangfileMessage("command.noclip.failure", new Object[0]); return;}
		}
		else {player.getMinecraftPlayer().noClip = !player.getMinecraftPlayer().noClip;}
    	
		if(!(player.getMinecraftPlayer() instanceof EntityPlayerMP)) {
			player.getMinecraftPlayer().noClip = false;
			sender.sendLangfileMessage("command.noclip.failure", new Object[0]);
			return;
		}
    	
		if(player.getMinecraftPlayer().noClip == false) {
			ascendPlayer(player);
		}
			
		S06PacketNoclip packet = new S06PacketNoclip();
		packet.allowNoclip = player.getMinecraftPlayer().noClip;
		MoreCommands.getMoreCommands().getNetwork().sendTo(packet, (EntityPlayerMP) player.getMinecraftPlayer()); 
			
		sender.sendLangfileMessage(player.getMinecraftPlayer().noClip ? "command.noclip.enabled" : "command.noclip.disabled", new Object[0]);
	}

	public static void checkSafe(EntityPlayerMP player) {
		if(player.noClip && !player.capabilities.isFlying) {
			player.noClip = false;
			
			S06PacketNoclip packet = new S06PacketNoclip();
			packet.allowNoclip = false;
			MoreCommands.getMoreCommands().getNetwork().sendTo(packet, player); 
			
			(new CommandSender(player)).sendLangfileMessage("command.noclip.autodisable", new Object[0]);
			ascendPlayer(new Player(player));
		}
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
				if(playerPos.getY() > 0) {
					newY = y - 1;
				} else {
					newY = y;
				}
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
	public void unregisterFromHandler() {
		EventHandler.ATTACK.getHandler().unregister(this);
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
