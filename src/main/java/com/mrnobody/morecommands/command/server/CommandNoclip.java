package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listeners.Listener;
import com.mrnobody.morecommands.patch.EntityPlayerMP;
import com.mrnobody.morecommands.patch.NetHandlerPlayServer;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
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
		if (event.entity instanceof EntityPlayerMP && 
			((EntityPlayerMP) event.entity).playerNetServerHandler instanceof NetHandlerPlayServer &&
			((NetHandlerPlayServer) ((EntityPlayerMP) event.entity).playerNetServerHandler).getOverrideNoclip() 
			&& event.source == DamageSource.inWall) event.setCanceled(true);
	}
	
	@Override
	public String getName() {
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
		
		if(!player.capabilities.isFlying)
			throw new CommandException("command.noclip.mustBeFlying", sender);
		
		if(!((NetHandlerPlayServer) player.playerNetServerHandler).enabled)
			throw new CommandException("command.noclip.reflectionError", sender);
		
		NetHandlerPlayServer handler = (NetHandlerPlayServer) player.playerNetServerHandler;
		
        if (params.length > 0) {
        	if (params[0].equalsIgnoreCase("enable") || params[0].equalsIgnoreCase("1")
            	|| params[0].equalsIgnoreCase("on") || params[0].equalsIgnoreCase("true")) {
        		handler.setOverrideNoclip(true);
            	sender.sendLangfileMessage("command.noclip.enabled");
            }
            else if (params[0].equalsIgnoreCase("disable") || params[0].equalsIgnoreCase("0")
            		|| params[0].equalsIgnoreCase("off") || params[0].equalsIgnoreCase("false")) {
            	handler.setOverrideNoclip(false);
            	sender.sendLangfileMessage("command.noclip.disabled");
            }
            else throw new CommandException("command.noclip.failure", sender);
        }
        else {
        	handler.setOverrideNoclip(!handler.getOverrideNoclip());
        	sender.sendLangfileMessage(handler.getOverrideNoclip() ? "command.noclip.enabled" : "command.noclip.disabled");
        }
    	
		if(handler.getOverrideNoclip() == false) {
			ascendPlayer(new Player(player));
		}
			
		MoreCommands.getMoreCommands().getPacketDispatcher().sendS07Noclip(player, handler.getOverrideNoclip());
	}

	public static void checkSafe(NetHandlerPlayServer handler, net.minecraft.entity.player.EntityPlayerMP player) {
		if(handler.getOverrideNoclip() && !player.capabilities.isFlying) {
			handler.setOverrideNoclip(false);
			
			MoreCommands.getMoreCommands().getPacketDispatcher().sendS07Noclip(player, false);
			
			(new CommandSender(player)).sendLangfileMessage("command.noclip.autodisable");
			ascendPlayer(new Player(player));
		}
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
	public Requirement[] getRequirements() {
		return new Requirement[] {
				Requirement.MODDED_CLIENT,
				Requirement.PATCH_ENTITYPLAYERSP,
				Requirement.PATCH_NETHANDLERPLAYSERVER,
				Requirement.PATCH_RENDERGLOBAL
			};
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
