package com.mrnobody.morecommands.command.server;

import java.lang.reflect.Method;

import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.Teleporter;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Coordinate;
import com.mrnobody.morecommands.wrapper.Player;

@Command(
		name = "spawnportal",
		description = "command.spawnportal.description",
		example = "command.spawnportal.example",
		syntax = "command.spawnportal.syntax",
		videoURL = "command.spawnportal.videoURL"
		)
public class CommandSpawnportal extends ServerCommand {

	@Override
	public String getName() {
		return "spawnportal";
	}

	@Override
	public String getUsage() {
		return "command.spawnportal.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Player player = sender.toPlayer();
		
		if (params.length > 0) {
			if (params[0].equalsIgnoreCase("end")) {
				Coordinate coord = player.getPosition();
				coord = new Coordinate(coord.getX() + 10, coord.getY(), coord.getZ() + 10);
		         EntityDragon dragon = new EntityDragon(player.getWorld().getMinecraftWorld()); 
		         
		         try {
		        	 Method method;
		        	 
		        	 try {
		        		 method = dragon.getClass().getDeclaredMethod("createEnderPortal", BlockPos.class);
		        	 }
		        	 catch (NoSuchMethodException nsme) {
		        		 method = dragon.getClass().getDeclaredMethod("func_175499_a", BlockPos.class);
		        	 }
		        	 
		        	 method.setAccessible(true);
		        	 method.invoke(dragon, coord.toBlockPos());
		         }
		         catch (Throwable t) {
		        	 t.printStackTrace();
		        	 sender.sendLangfileMessageToPlayer("command.spawnportal.endError", new Object[0]);
		         }
			}
			else if (params[0].equalsIgnoreCase("nether")) {
				(new Teleporter(((EntityPlayerMP) player.getMinecraftPlayer()).getServerForPlayer())).makePortal((EntityPlayerMP) player.getMinecraftPlayer());
			}
			else {
				sender.sendLangfileMessageToPlayer("command.spawnportal.unknownPortal", new Object[0]);
			}
		}
		else {
			sender.sendLangfileMessageToPlayer("command.spawnportal.noArgs", new Object[0]);
		}
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}
	
	@Override
	public void unregisterFromHandler() {}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	@Override
	public int getPermissionLevel() {
		return 2;
	}
}
