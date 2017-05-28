package com.mrnobody.morecommands.patch;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Sets;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.patch.PatchManager.AppliedPatches;
import com.mrnobody.morecommands.settings.GlobalSettings;
import com.mrnobody.morecommands.settings.MoreCommandsConfig;
import com.mrnobody.morecommands.settings.PlayerSettings;
import com.mrnobody.morecommands.settings.ServerPlayerSettings;
import com.mrnobody.morecommands.util.Coordinate;
import com.mrnobody.morecommands.util.EntityUtils;
import com.mrnobody.morecommands.util.ObfuscatedNames.ObfuscatedField;
import com.mrnobody.morecommands.util.ReflectionHelper;
import com.mrnobody.morecommands.util.Variables;
import com.mrnobody.morecommands.util.WorldUtils;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

/**
 * This patch replaces {@link net.minecraft.network.NetHandlerPlayServer} instances with a modified version<br>
 * It handles incoming packets from the client. The patch is needed to allow <br>
 * noclipping and to replace variables in the chat.
 * 
 * @author MrNobody98
 *
 */
public class PatchNetHandlerPlayServer implements PatchManager.ForgeEventBasedPatch {
	private String displayName;
	
	PatchNetHandlerPlayServer(String displayName) {
		this.displayName = displayName;
	}
	
	@Override
	public Collection<Class<? extends Event>> forgeEventClasses() {
		return Sets.<Class<? extends Event>>newHashSet(EntityJoinWorldEvent.class);
	}
	
	@Override
	public <T extends Event> boolean applyForgeEventPatch(T e) {
		EntityJoinWorldEvent event = (EntityJoinWorldEvent) e;
		
		EntityPlayerMP player = (EntityPlayerMP) event.entity;
		AppliedPatches patches = PatchManager.instance().getAppliedPatchesForPlayer(player);
		
		if (player.playerNetServerHandler.playerEntity == event.entity && !(player.playerNetServerHandler instanceof NetHandlerPlayServer)) {
			net.minecraft.network.NetHandlerPlayServer handler = player.playerNetServerHandler;
			player.playerNetServerHandler = new NetHandlerPlayServer(player.mcServer, handler.netManager, handler.playerEntity);
			
			if (patches != null) 
				patches.setPatchSuccessfullyApplied(this.displayName, true);
		}
		
		return true;
	}
	
	@Override
	public <T extends Event> boolean needsToBeApplied(T event) {
		return ((EntityJoinWorldEvent) event).entity instanceof EntityPlayerMP;
	}
	
	@Override
	public <T extends Event> boolean printLogFor(T event) {
		return true;
	}

	@Override
	public String getDisplayName() {
		return this.displayName;
	}

	@Override
	public String getFailureConsequences() {
		return "Noclip disabled, variable replacement disabled server side";
	}
	
	public static class NetHandlerPlayServer extends net.minecraft.network.NetHandlerPlayServer {
	    NetHandlerPlayServer(MinecraftServer par1, NetworkManager par2, EntityPlayerMP par3) {
	        super(par1, par2, par3);
	    }
	    
	    @Override
	    public void processChatMessage(C01PacketChatMessage p_147354_1_) {
	    	String message = p_147354_1_.func_149439_c();
	    	
	    	ServerPlayerSettings settings = MoreCommands.getEntityProperties(ServerPlayerSettings.class, PlayerSettings.MORECOMMANDS_IDENTIFIER, this.playerEntity);
			Map<String, String> playerVars = settings == null ? new HashMap<String, String>() : settings.variables;
			boolean replaceIgnored;
			
			if (message.length() > 1 && message.charAt(0) == '%') {
				int end = message.indexOf('%', 1);
				String val = end > 0 ? playerVars.get(message.substring(1, end)) : null;
				
				replaceIgnored = val == null || !val.startsWith("/") ||
								(message.length() - 1 != end && message.charAt(end + 1) != ' ') ||
								!MinecraftServer.getServer().getCommandManager().getCommands().containsKey(val.substring(1));
			}
			else replaceIgnored = !message.startsWith("/") || !MinecraftServer.getServer().getCommandManager().getCommands().containsKey(message.substring(1).split(" ")[0]);
			
	    	try {
	    		String world = this.playerEntity.getEntityWorld().getSaveHandler().getWorldDirectoryName(), dim = this.playerEntity.getEntityWorld().provider.getDimensionName();
	    		
				if (MoreCommandsConfig.enableGlobalVars && MoreCommandsConfig.enablePlayerVars)
					message = Variables.replaceVars(message, replaceIgnored, playerVars, GlobalSettings.getInstance().variables.get(ImmutablePair.of(world, dim)));
				else if (MoreCommandsConfig.enablePlayerVars)
					message = Variables.replaceVars(message, replaceIgnored, playerVars);
				else if (MoreCommandsConfig.enableGlobalVars)
					message = Variables.replaceVars(message, replaceIgnored, GlobalSettings.getInstance().variables.get(ImmutablePair.of(world, dim)));
	    	}
	        catch (Variables.VariablesCouldNotBeResolvedException vcnbre) {
	            message = vcnbre.getNewString();
	        }
	    	
	    	super.processChatMessage(new C01PacketChatMessage(message));
	    }

	    @Override
	    public void processPlayer(C03PacketPlayer packet) {
	        checkSafe(this.playerEntity);
	        super.processPlayer(packet);
	    }
	    
		private static void checkSafe(EntityPlayerMP player) {
			if (player.noClip && !player.capabilities.isFlying) {
				player.noClip = false;
				
				MoreCommands.INSTANCE.getPacketDispatcher().sendS06Noclip(player, false);
				
				(new CommandSender(player)).sendLangfileMessage("command.noclip.autodisable");
				ascendPlayer(player);
			}
		}

		private static boolean ascendPlayer(EntityPlayerMP player) {
			Coordinate playerPos = EntityUtils.getPosition(player);
			if(WorldUtils.isClearBelow(player.worldObj, playerPos) && playerPos.getY() > 0) {
				return false;
			}
			double y = playerPos.getY() - 1; // in case player was standing on ground
			while (y < 260) {
				if(WorldUtils.isClear(player.worldObj, new Coordinate(playerPos.getX(), y++, playerPos.getZ()))) {
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
	}
}
