package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLivingBase;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Coordinate;
import com.mrnobody.morecommands.wrapper.Entity;

@Command(
		name = "explode",
		description = "command.explode.description",
		example = "command.explode.example",
		syntax = "command.explode.syntax",
		videoURL = "command.explode.videoURL"
		)
public class CommandExplode extends ServerCommand {

	@Override
	public String getCommandName() {
		return "explode";
	}

	@Override
	public String getUsage() {
		return "command.explode.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Entity entity = new Entity((net.minecraft.entity.EntityLivingBase) sender.getMinecraftISender());
		int size = 4;
		Coordinate spawn = entity.traceBlock(128.0D);
		double x = 0.0D, y = 0.0D, z = 0.0D;
		boolean success = (spawn != null);
		if (spawn != null) {
			x = spawn.getX();
			y = spawn.getY();
			z = spawn.getZ();
		}
		
		if (params.length > 0) {
			if (params.length > 3) {
				try {
					x = Double.parseDouble(params[1]);
					y = Double.parseDouble(params[2]);
					z = Double.parseDouble(params[3]);
					success = true;
				}
				catch (NumberFormatException e) {sender.sendLangfileMessage("command.explode.NAN", new Object[0]);}
			}
			
			try {size = Integer.parseInt(params[0]);}
			catch (NumberFormatException e) {sender.sendLangfileMessage("command.explode.NAN", new Object[0]);}
		}
		
		if (success) {
			entity.getWorld().createExplosion(entity.getMinecraftEntity(), new Coordinate(x, y, z), size);
			sender.sendLangfileMessage("command.explode.booooom", new Object[0]);
		}
		else {sender.sendLangfileMessage("command.explode.notInSight", new Object[0]);}
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
		return sender instanceof EntityLivingBase;
	}
}
