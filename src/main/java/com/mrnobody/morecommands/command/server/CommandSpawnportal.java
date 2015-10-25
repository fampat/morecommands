package com.mrnobody.morecommands.command.server;

import java.lang.reflect.Method;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;
import net.minecraft.world.Teleporter;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

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
		if (params.length > 0) {
			if (params[0].equalsIgnoreCase("end")) {
				BlockPos coord = sender.getPosition();
				coord = new BlockPos(coord.getX() + 10, coord.getY(), coord.getZ() + 10);
				EntityDragon dragon = new EntityDragon(sender.getWorld().getMinecraftWorld()); 
		         
		         try {
		        	 Method method;
		        	 
		        	 try {
		        		 method = dragon.getClass().getDeclaredMethod("createEnderPortal", BlockPos.class);
		        	 }
		        	 catch (NoSuchMethodException nsme) {
		        		 method = dragon.getClass().getDeclaredMethod("func_175499_a", BlockPos.class);
		        	 }
		        	 
		        	 method.setAccessible(true);
		        	 method.invoke(dragon, coord);
		         }
		         catch (Throwable t) {
		        	 t.printStackTrace();
		        	 throw new CommandException("command.spawnportal.endError", sender);
		         }
			}
			else if (params[0].equalsIgnoreCase("nether")) {
				(new Teleporter(((EntityPlayerMP) sender.getMinecraftISender()).getServerForPlayer())).makePortal((EntityPlayerMP) sender.getMinecraftISender());
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
