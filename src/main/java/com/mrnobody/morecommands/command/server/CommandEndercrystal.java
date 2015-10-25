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
		name = "endercrystal",
		description = "command.endercrystal.description",
		example = "command.endercrystal.example",
		syntax = "command.endercrystal.syntax",
		videoURL = "command.endercrystal.videoURL"
		)
public class CommandEndercrystal extends ServerCommand {

	@Override
	public String getCommandName() {
		return "endercrystal";
	}

	@Override
	public String getUsage() {
		return "command.endercrystal.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Entity entity = new Entity((EntityLivingBase) sender.getMinecraftISender());
		Coordinate spawn = entity.traceBlock(128.0D);
		
		if (spawn == null) throw new CommandException("command.endercrystal.notFound", sender);
		else Entity.spawnEntity("EnderCrystal", spawn, entity.getWorld());
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
		return sender instanceof EntityLivingBase;
	}
}
