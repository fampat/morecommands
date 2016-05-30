package com.mrnobody.morecommands.command.server;

import java.util.List;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.TargetSelector;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Command(
		name = "cleardrops",
		description = "command.cleardrops.description",
		example = "command.cleardrops.example",
		syntax = "command.cleardrops.syntax",
		videoURL = "command.cleardrops.videoURL"
		)
public class CommandCleardrops extends StandardCommand implements ServerCommandProperties {

	@Override
	public String getCommandName() {
		return "cleardrops";
	}

	@Override
	public String getCommandUsage() {
		return "command.cleardrops.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		BlockPos pos = sender.getPosition();
		World world = sender.getWorld().getMinecraftWorld();
		int radius = 128;
		Item item = null;
		int meta = -1;
		int removedDrops = 0;
		NBTBase nbt = null;
		boolean equalLists = false;
		
		if (params.length > 0) {
			try {radius = Integer.parseInt(params[0]);}
			catch (NumberFormatException nfe) {throw new CommandException("command.cleardrops.NAN", sender);}
		}
		
		if (params.length > 1 && !params[1].equals("*"))
			item = getItem(params[1]);
		
		if (params.length > 2 && !params[2].equals("*")) {
			try {meta = Integer.parseInt(params[2]);}
			catch (NumberFormatException nfe) {throw new CommandException("command.cleardrops.NAN", sender);}
		}
		
		if (params.length > 3) {
			params = reparseParamsWithNBTData(params);
			nbt = getNBTFromParam(params[3], sender.getMinecraftISender());
			if (params.length > 4) equalLists = isEqualLists(params[4]);
		}
		
		AxisAlignedBB boundingBox = new AxisAlignedBB(
				pos.getX() - radius, pos.getY() - radius, pos.getZ() - radius,
				pos.getX() + radius, pos.getY() + radius, pos.getZ() + radius);
		
		List<?> nearbyEntities = world.getEntitiesWithinAABB(EntityItem.class, boundingBox);
		
		for (int entityIndex = 0; entityIndex < nearbyEntities.size(); entityIndex++) {
			Entity entity = (Entity) nearbyEntities.get(entityIndex);
			
			if (entity instanceof EntityItem) {
				EntityItem entityItem = (EntityItem)entity;
				ItemStack stack = entityItem.getEntityItem();
				
				if (!(item == null || item == stack.getItem())) continue;
				if (!(meta == -1 || meta == stack.getItemDamage())) continue;
				if (!(nbt == null || TargetSelector.nbtContains(stack.getTagCompound(), nbt, !equalLists))) continue;
				
				world.removeEntity(entityItem);
				removedDrops++;
			}
		}
		
		sender.sendLangfileMessage("command.cleardrops.removed", removedDrops);
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
	public int getDefaultPermissionLevel() {
		return 2;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return true;
	}
}
