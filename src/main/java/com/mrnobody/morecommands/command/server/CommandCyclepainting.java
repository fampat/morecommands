package com.mrnobody.morecommands.command.server;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.item.EntityPainting.EnumArt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "cyclepainting",
		description = "command.cyclepainting.description",
		example = "command.cyclepainting.example",
		syntax = "command.cyclepainting.syntax",
		videoURL = "command.cyclepainting.videoURL"
		)
public class CommandCyclepainting extends ServerCommand {

	@Override
	public String getName() {
		return "cyclepainting";
	}

	@Override
	public String getUsage() {
		return "command.cyclepainting.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		EntityPlayer playerEntity = sender.toPlayer().getMinecraftPlayer();
		
		boolean sneaking = playerEntity.isSneaking();
		Entity hit = sender.toPlayer().traceEntity(128.0D);
		
		if (!(hit instanceof EntityPainting) || hit.isDead) {
        	sender.sendLangfileMessageToPlayer("command.cyclepainting.noPainting", new Object[0]);
            return;
        }
		
		EntityPainting picture = (EntityPainting) hit;
		EntityPainting newPicture = new EntityPainting(picture.worldObj, picture.func_174857_n(), picture.field_174860_b);
				
		EnumArt oldArt = picture.art;
		EnumFacing direction = picture.field_174860_b;
		int current = 0;
				
		List<EnumArt> arts = new ArrayList<EnumArt>();
		EnumArt[] all = EnumArt.values();
				
		for (int i = 0; i < all.length; ++i) {
			arts.add(all[i]);
			if (oldArt == all[i]) current = i;
		}
		        
		if (arts.size() <= 1) {
			newPicture.art = oldArt;
			sender.toPlayer().getMinecraftPlayer().worldObj.removeEntity(picture);
			sender.toPlayer().getMinecraftPlayer().worldObj.spawnEntityInWorld(newPicture);
			sender.sendLangfileMessageToPlayer("command.cyclepainting.noMoreArts", new Object[0]);
			return;
		}
		        
		int newArt = sneaking ? (current == 0 ? arts.size() - 1 : current - 1) : (current == arts.size() - 1 ? 0 : current + 1);

		newPicture.art = arts.get(newArt);
		sender.toPlayer().getMinecraftPlayer().worldObj.removeEntity(picture);
		sender.toPlayer().getMinecraftPlayer().worldObj.spawnEntityInWorld(newPicture);
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
		return 0;
	}
}
