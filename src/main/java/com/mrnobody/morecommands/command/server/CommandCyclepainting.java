package com.mrnobody.morecommands.command.server;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.item.EntityPainting.EnumArt;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Entity;

@Command(
		name = "cyclepainting",
		description = "command.cyclepainting.description",
		example = "command.cyclepainting.example",
		syntax = "command.cyclepainting.syntax",
		videoURL = "command.cyclepainting.videoURL"
		)
public class CommandCyclepainting extends ServerCommand {

	@Override
	public String getCommandName() {
		return "cyclepainting";
	}

	@Override
	public String getUsage() {
		return "command.cyclepainting.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Entity entity = new Entity((EntityLivingBase) sender.getMinecraftISender());
		
		boolean sneaking = entity.getMinecraftEntity().isSneaking();
		net.minecraft.entity.Entity hit = entity.traceEntity(128.0D);
		
		if (!(hit instanceof EntityPainting) || hit.isDead)
			throw new CommandException("command.cyclepainting.noPainting", sender);
		
		EntityPainting picture = (EntityPainting) hit;
		EntityPainting newPicture = new EntityPainting(picture.worldObj, picture.field_146063_b, picture.field_146064_c, picture.field_146062_d, picture.hangingDirection);
				
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
			entity.getMinecraftEntity().worldObj.removeEntity(picture);
			entity.getMinecraftEntity().worldObj.spawnEntityInWorld(newPicture);
			throw new CommandException("command.cyclepainting.noMoreArts", sender);
		}
		        
		int newArt = sneaking ? (current == 0 ? arts.size() - 1 : current - 1) : (current == arts.size() - 1 ? 0 : current + 1);

		newPicture.art = arts.get(newArt);
		entity.getMinecraftEntity().worldObj.removeEntity(picture);
		entity.getMinecraftEntity().worldObj.spawnEntityInWorld(newPicture);
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
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return sender instanceof EntityLivingBase;
	}
}
