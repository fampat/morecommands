package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.MathHelper;

import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Player;

@Command(
		name = "cannon",
		description = "command.cannon.description",
		example = "command.cannon.example",
		syntax = "command.cannon.syntax",
		videoURL = "command.cannon.videoURL"
		)
public class CommandCannon extends ServerCommand {

	@Override
	public String getName() {
		return "cannon";
	}

	@Override
	public String getUsage() {
		return "command.cannon.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Player player = new Player((EntityPlayerMP) sender.getMinecraftISender());
		
		EntityTNTPrimed tnt = new EntityTNTPrimed(player.getWorld().getMinecraftWorld());
		
		tnt.setLocationAndAngles(player.getPosition().getX(), player.getPosition().getY() + 1, player.getPosition().getZ(), player.getYaw(), player.getPitch());
		tnt.fuse = 40;
		tnt.motionX = -MathHelper.sin((tnt.rotationYaw / 180F) * 3.141593F) * MathHelper.cos((tnt.rotationPitch / 180F) * 3.141593F);
		tnt.motionZ = MathHelper.cos((tnt.rotationYaw / 180F) * 3.141593F) * MathHelper.cos((tnt.rotationPitch / 180F) * 3.141593F);
		tnt.motionY = -MathHelper.sin((tnt.rotationPitch / 180F) * 3.141593F);
		
		double multiplier = 1;
		if (params.length > 0) {
			try {multiplier = Double.parseDouble(params[0]);} 
			catch (NumberFormatException e) {throw new CommandException("command.cannon.NAN", sender);}
		}
		
		tnt.motionX *= multiplier;
		tnt.motionY *= multiplier;
		tnt.motionZ *= multiplier;

		player.getWorld().getMinecraftWorld().spawnEntityInWorld(tnt);
		sender.sendLangfileMessage("command.cannon.success");
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
