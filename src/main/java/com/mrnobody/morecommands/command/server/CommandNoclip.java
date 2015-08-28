package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listeners.Listener;
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
		
		if(!player.getMinecraftPlayer().capabilities.isFlying && !player.getMinecraftPlayer().noClip)
			throw new CommandException("command.noclip.mustBeFlying", sender);
		
        if (params.length > 0) {
        	if (params[0].equalsIgnoreCase("enable") || params[0].equalsIgnoreCase("1")
            	|| params[0].equalsIgnoreCase("on") || params[0].equalsIgnoreCase("true")) {
        		player.getMinecraftPlayer().noClip = true;
            	sender.sendLangfileMessage("command.noclip.enabled");
            }
            else if (params[0].equalsIgnoreCase("disable") || params[0].equalsIgnoreCase("0")
            		|| params[0].equalsIgnoreCase("off") || params[0].equalsIgnoreCase("false")) {
            	player.getMinecraftPlayer().noClip = false;
            	sender.sendLangfileMessage("command.noclip.disabled");
            }
            else throw new CommandException("command.noclip.failure", sender);
        }
        else {
        	player.getMinecraftPlayer().noClip = !player.getMinecraftPlayer().noClip;
        	sender.sendLangfileMessage(player.getMinecraftPlayer().noClip ? "command.noclip.enabled" : "command.noclip.disabled");
        }
    	
		if(!(player.getMinecraftPlayer() instanceof EntityPlayerMP)) {
			player.getMinecraftPlayer().noClip = false;
			throw new CommandException("command.noclip.failure", sender);
		}
    	
		if(player.getMinecraftPlayer().noClip == false) {
			ascendPlayer(player);
		}
		
		MoreCommands.getMoreCommands().getPacketDispatcher().sendS07Noclip(player.getMinecraftPlayer(), player.getMinecraftPlayer().noClip);
	}

	public static void checkSafe(EntityPlayerMP player) {
		if(player.noClip && !player.capabilities.isFlying) {
			player.noClip = false;
			
			MoreCommands.getMoreCommands().getPacketDispatcher().sendS07Noclip(player, false);
			
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
