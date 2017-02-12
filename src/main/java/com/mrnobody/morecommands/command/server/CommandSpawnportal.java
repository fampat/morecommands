package com.mrnobody.morecommands.command.server;

import java.lang.reflect.Method;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.ObfuscatedNames.ObfuscatedMethod;
import com.mrnobody.morecommands.util.ReflectionHelper;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.util.BlockPos;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

@Command(
		name = "spawnportal",
		description = "command.spawnportal.description",
		example = "command.spawnportal.example",
		syntax = "command.spawnportal.syntax",
		videoURL = "command.spawnportal.videoURL"
		)
public class CommandSpawnportal extends StandardCommand implements ServerCommandProperties {
	private final Method createEnderPortal = ReflectionHelper.getMethod(ObfuscatedMethod.EntityDragon_func_175499_a);
	
	@Override
	public String getCommandName() {
		return "spawnportal";
	}

	@Override
	public String getCommandUsage() {
		return "command.spawnportal.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		Entity entity = getSenderAsEntity(sender.getMinecraftISender(), Entity.class);
		
		if (params.length > 0) {
			if (params[0].equalsIgnoreCase("end")) {
				BlockPos coord = new BlockPos(entity);
				EntityDragon dragon = new EntityDragon(entity.worldObj); 
		         
				try {this.createEnderPortal.invoke(new EntityDragon(entity.worldObj), coord);}
				catch (Throwable t) {
					t.printStackTrace();
					throw new CommandException("command.spawnportal.endError", sender);
				}
			}
			else if (params[0].equalsIgnoreCase("nether")) {
				new Teleporter((WorldServer) entity.worldObj).makePortal(entity);
			}
			else throw new CommandException("command.spawnportal.unknownPortal", sender);
		}
		else throw new CommandException("command.spawnportal.noArgs", sender);
		
		return null;
	}
	
	@Override
	public CommandRequirement[] getRequirements() {
		return new CommandRequirement[0];
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
		return isSenderOfEntityType(sender, Entity.class);
	}
}
