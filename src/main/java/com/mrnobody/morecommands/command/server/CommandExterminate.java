package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.BlockPos;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "exterminate",
		description = "command.exterminate.description",
		example = "command.exterminate.example",
		syntax = "command.exterminate.syntax",
		videoURL = "command.exterminate.videoURL"
		)
public class CommandExterminate extends ServerCommand {

	@Override
	public String getName() {
		return "exterminate";
	}

	@Override
	public String getUsage() {
		return "command.exterminate.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		int strength = 4;
		com.mrnobody.morecommands.wrapper.Entity entity = new com.mrnobody.morecommands.wrapper.Entity((Entity) sender.getMinecraftISender());
		Entity hit = entity.traceEntity(128.0D);
		
		if (hit != null) {
			if (hit instanceof EntityLiving)  {
				if (params.length > 0) {
					try {strength = Integer.parseInt(params[0]);}
					catch (NumberFormatException nfe) {sender.sendLangfileMessage("command.exterminate.invalidArg", new Object[0]);}
				}
				
				entity.getWorld().createExplosion(entity.getMinecraftEntity(), new BlockPos(hit.posX, hit.posY, hit.posZ), strength);
				
				sender.sendLangfileMessage("command.exterminate.boooom", new Object[0]);
			}
			else sender.sendLangfileMessage("command.exterminate.notLiving", new Object[0]);
		}
		else sender.sendLangfileMessage("command.exterminate.notFound", new Object[0]);
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
		return sender instanceof Entity;
	}
}
