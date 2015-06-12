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
		
		if(!player.capabilities.isFlying) {
			sender.sendLangfileMessage("command.noclip.mustBeFlying", new Object[0]); return;
		}
		
		if(!((NetHandlerPlayServer) player.playerNetServerHandler).enabled) {
			sender.sendLangfileMessage("command.noclip.reflectionError", new Object[0]); return;
		}
		
		NetHandlerPlayServer handler = (NetHandlerPlayServer) player.playerNetServerHandler;
    	
		if (params.length >= 1) {
			if (params[0].equalsIgnoreCase("true")) {handler.setOverrideNoclip(true);}
			else if (params[0].equalsIgnoreCase("false")) {handler.setOverrideNoclip(false);}
			else if (params[0].equalsIgnoreCase("0")) {handler.setOverrideNoclip(false);}
			else if (params[0].equalsIgnoreCase("1")) {handler.setOverrideNoclip(true);}
			else if (params[0].equalsIgnoreCase("on")) {handler.setOverrideNoclip(true);}
			else if (params[0].equalsIgnoreCase("off")) {handler.setOverrideNoclip(false);}
    		else if (params[0].equalsIgnoreCase("enable")) {handler.setOverrideNoclip(true);}
    		else if (params[0].equalsIgnoreCase("disable")) {handler.setOverrideNoclip(false);}
			else {sender.sendLangfileMessage("command.noclip.failure", new Object[0]); return;}
		}
		else {handler.setOverrideNoclip(!handler.getOverrideNoclip());}
    	
		if(handler.getOverrideNoclip() == false) {
			ascendPlayer(new Player(player));
		}
			
		MoreCommands.getMoreCommands().getPacketDispatcher().sendS06Noclip(player, handler.getOverrideNoclip());
		sender.sendLangfileMessage(handler.getOverrideNoclip() ? "command.noclip.enabled" : "command.noclip.disabled", new Object[0]);
	}

	public static void checkSafe(NetHandlerPlayServer handler, net.minecraft.entity.player.EntityPlayerMP player) {
		if(handler.getOverrideNoclip() && !player.capabilities.isFlying) {
			handler.setOverrideNoclip(false);
			
			MoreCommands.getMoreCommands().getPacketDispatcher().sendS06Noclip(player, false);
			
			(new CommandSender(player)).sendLangfileMessage("command.noclip.autodisable", new Object[0]);
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
