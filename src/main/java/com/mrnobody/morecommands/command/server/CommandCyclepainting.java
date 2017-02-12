package com.mrnobody.morecommands.command.server;

import java.util.ArrayList;
import java.util.List;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.EntityUtils;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.item.EntityPainting.EnumArt;

@Command(
		name = "cyclepainting",
		description = "command.cyclepainting.description",
		example = "command.cyclepainting.example",
		syntax = "command.cyclepainting.syntax",
		videoURL = "command.cyclepainting.videoURL"
		)
public class CommandCyclepainting extends StandardCommand implements ServerCommandProperties {

	@Override
	public String getCommandName() {
		return "cyclepainting";
	}

	@Override
	public String getCommandUsage() {
		return "command.cyclepainting.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		Entity entity = getSenderAsEntity(sender.getMinecraftISender(), Entity.class);
		
		boolean sneaking = entity.isSneaking();
		Entity hit = EntityUtils.traceEntity(entity, 128.0D);
		
		if (!(hit instanceof EntityPainting) || hit.isDead)
			throw new CommandException("command.cyclepainting.noPainting", sender);
		
		EntityPainting picture = (EntityPainting) hit;
		EntityPainting newPicture = new EntityPainting(picture.worldObj, picture.func_174857_n(), picture.field_174860_b);
				
		EnumArt oldArt = picture.art;
		int current = 0;
				
		List<EnumArt> arts = new ArrayList<EnumArt>();
		EnumArt[] all = EnumArt.values();
				
		for (int i = 0; i < all.length; ++i) {
			arts.add(all[i]);
			if (oldArt == all[i]) current = i;
		}
		        
		if (arts.size() <= 1) {
			newPicture.art = oldArt;
			entity.worldObj.removeEntity(picture);
			entity.worldObj.spawnEntityInWorld(newPicture);
			throw new CommandException("command.cyclepainting.noMoreArts", sender);
		}
		        
		int newArt = sneaking ? (current == 0 ? arts.size() - 1 : current - 1) : (current == arts.size() - 1 ? 0 : current + 1);

		newPicture.art = arts.get(newArt);
		entity.worldObj.removeEntity(picture);
		entity.worldObj.spawnEntityInWorld(newPicture);
		
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
		return 0;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return isSenderOfEntityType(sender, Entity.class);
	}
}
