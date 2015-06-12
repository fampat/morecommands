package com.mrnobody.morecommands.command.server;

import java.lang.reflect.Method;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.MathHelper;
import net.minecraft.world.Teleporter;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.util.ObfuscationHelper;
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
	public String getCommandName() {
		return "spawnportal";
	}

	@Override
	public String getUsage() {
		return "command.spawnportal.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Player player = new Player((EntityPlayerMP) sender.getMinecraftISender());
		
		if (params.length > 0) {
			if (params[0].equalsIgnoreCase("end")) {
				Coordinate coord = player.getPosition();
		         int x = MathHelper.floor_double(coord.getX());
		         int z = MathHelper.floor_double(coord.getZ());
		         EntityDragon dragon = new EntityDragon(player.getWorld().getMinecraftWorld()); 
		         
		         try {
		        	 Class<?>[] args = new Class<?>[]{Integer.TYPE, Integer.TYPE};
		        	 Method method;
		        	 
		        	 try {
		        		 method = dragon.getClass().getDeclaredMethod("createEnderPortal", args);
		        	 }
		        	 catch (NoSuchMethodException nsme) {
		        		 method = dragon.getClass().getDeclaredMethod(ObfuscationHelper.getObfuscatedName("createEnderPortal"), args);
		        	 }
		        	 
		        	 method.setAccessible(true);
		        	 method.invoke(dragon, new Object[]{x, z});
		         }
		         catch (Throwable t) {
		        	 t.printStackTrace();
		        	 throw new CommandException("command.spawnportal.endError", sender);
		         }
			}
			else if (params[0].equalsIgnoreCase("nether")) {
				(new Teleporter(((EntityPlayerMP) player.getMinecraftPlayer()).getServerForPlayer())).makePortal((EntityPlayerMP) player.getMinecraftPlayer());
			}
			else throw new CommandException("command.spawnportal.unknownPortal", sender);
		}
		else throw new CommandException("command.spawnportal.noArgs", sender);
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
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return sender instanceof EntityPlayerMP;
	}
}
