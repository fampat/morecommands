package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
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
	public String getName() {
		return "endercrystal";
	}

	@Override
	public String getUsage() {
		return "command.endercrystal.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Entity entity = new Entity((net.minecraft.entity.Entity) sender.getMinecraftISender());
		BlockPos spawn = entity.traceBlock(128.0D);
		
		if (spawn == null) throw new CommandException("command.endercrystal.notFound", sender);
		else Entity.spawnEntity("EnderCrystal", spawn, entity.getWorld());
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
		return sender instanceof net.minecraft.entity.Entity;
	}
}
