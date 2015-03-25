package com.mrnobody.morecommands.command.server;

import java.util.List;

import net.minecraft.util.Vec3;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Entity;
import com.mrnobody.morecommands.wrapper.Player;

@Command(
		name = "bring",
		description = "command.bring.description",
		example = "command.bring.example",
		syntax = "command.bring.syntax",
		videoURL = "command.bring.videoURL"
		)
public class CommandBring extends ServerCommand {

	@Override
	public String getName() {
		return "bring";
	}

	@Override
	public String getUsage() {
		return "command.bring.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params)throws CommandException {
		Player player = sender.toPlayer();
		double radius = 128.0D;
		String entityType = "item";
		
		if (params.length > 1) {
			try {radius = Double.parseDouble(params[1]);}
			catch (NumberFormatException e) {sender.sendLangfileMessageToPlayer("command.bring.NAN", new Object[0]); return;}
		}
		
		if (params.length > 0) {
			if (Entity.getEntityClass(params[0]) == null) {
				try {radius = Double.parseDouble(params[0]);}
				catch (NumberFormatException e) {sender.sendLangfileMessageToPlayer("command.bring.unknownEntity", new Object[0]); return;}
			}
			else entityType = params[0];
		}
				
		if (radius > 0 && radius < 256) {
			List<net.minecraft.entity.Entity> foundEntities = Entity.findEntities(entityType, player.getPosition(), player.getWorld(), radius);
			Vec3 vec3D = player.getMinecraftPlayer().getLook(1.0F);
					
			double d = 5.0D;
			double offsetY = player.getMinecraftPlayer().posY + player.getMinecraftPlayer().getEyeHeight();
			double d1 = player.getMinecraftPlayer().posX + vec3D.xCoord * d;
			double d2 = offsetY  + vec3D.yCoord * d;
			double d3 = player.getMinecraftPlayer().posZ + vec3D.zCoord * d;
					
			for (net.minecraft.entity.Entity entity : foundEntities) {
				if (entity == player.getMinecraftPlayer()) continue;
				entity.setPosition(d1, d2 + 0.5D, d3);
			}
		}
		else {sender.sendLangfileMessageToPlayer("command.bring.invalidRadius", new Object[0]);}
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
