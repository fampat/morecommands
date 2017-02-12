package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.EntityUtils;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.FMLControlledNamespacedRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

@Command(
		name = "spawn",
		description = "command.spawn.description",
		example = "command.spawn.example",
		syntax = "command.spawn.syntax",
		videoURL = "command.spawn.videoURL"
		)
public class CommandSpawn extends StandardCommand implements ServerCommandProperties {
	private static final int PAGE_MAX = 15;

	@Override
	public String getCommandName() {
		return "spawn";
	}

	@Override
	public String getCommandUsage() {
		return "command.spawn.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		if (params.length > 0) {
			if (params[0].equalsIgnoreCase("list")) {
    			int page = 0;
    			ResourceLocation[] names = ForgeRegistries.ENTITIES.getKeys().toArray(new ResourceLocation[ForgeRegistries.ENTITIES.getKeys().size()]);
    			
    			if (params.length > 1) {
    				try {
    					page = Integer.parseInt(params[1]) - 1; 
    					if (page < 0) page = 0;
    					else if (page * PAGE_MAX > names.length) page = names.length / PAGE_MAX;
    				}
    				catch (NumberFormatException e) {throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());}
    			}
    			
    			final int stop = (page + 1) * PAGE_MAX;
    			for (int i = page * PAGE_MAX; i < stop && i < names.length; i++)
    				sender.sendStringMessage(" - '" + names[i] + "' " + " (ID " + ((FMLControlledNamespacedRegistry<EntityEntry>) ForgeRegistries.ENTITIES).getId(names[i]) + ")");
    			
    			sender.sendLangfileMessage("command.spawn.more", TextFormatting.RED);
				return null;
			}
			else if (params[0].equalsIgnoreCase("random")) {
				ResourceLocation[] list = ForgeRegistries.ENTITIES.getKeys().toArray(new ResourceLocation[ForgeRegistries.ENTITIES.getKeys().size()]);
				params[0] = list[(int) (Math.random() * list.length)].toString();
			}
			else if (EntityUtils.getEntityClass(new ResourceLocation(params[0]), true) == null) {
				try {
					ResourceLocation loc = EntityUtils.getEntityName(Integer.parseInt(params[0]));
					params[0] = loc == null ? null : loc.toString();
					if (params[0] == null) throw new CommandException("command.spawn.unknownEntityID", sender);
				} catch (NumberFormatException nfe) {throw new CommandException("command.spawn.unknownEntity", sender);}
			}
			
			params = reparseParamsWithNBTData(params);
			
			int quantity = 1; BlockPos coord = null; NBTBase nbt = null; boolean mergeLists = false;
			
			if (params.length > 1) {
				if (isNBTParam(params[1])) {
					nbt = getNBTFromParam(params[1]);
					mergeLists = params.length > 2 ? isMergeLists(params[2]) : mergeLists;
				}
				else {
					try {quantity = Integer.parseInt(params[1]);} 
					catch (NumberFormatException nfe) {sender.sendLangfileMessage("command.spawn.NAN");}
					
					if (params.length > 2 && isNBTParam(params[2])) {
						nbt = getNBTFromParam(params[2]);
						mergeLists = params.length > 3 ? isMergeLists(params[3]) : mergeLists;
					}
					else if (params.length > 4) {
						coord = getCoordFromParams(sender.getMinecraftISender(), params, 2);
						
						if (params.length > 5) {
							nbt = getNBTFromParam(params[5]);
							mergeLists = params.length > 6 ? isMergeLists(params[6]) : mergeLists;
						}
					}
				}
			}
			
			if (nbt != null && !(nbt instanceof NBTTagCompound))
				throw new CommandException("command.spawn.noCompound", sender);
			
			if (coord == null) {
				if (isSenderOfEntityType(sender.getMinecraftISender(), EntityLivingBase.class))
					coord = EntityUtils.traceBlock(getSenderAsEntity(sender.getMinecraftISender(), EntityLivingBase.class), 128);
				else coord = sender.getPosition();
			
				if (coord == null) {
					coord = sender.getPosition();
					coord = new BlockPos(coord.getX() + (Math.random() * 10) - 5, coord.getY(), coord.getZ() + (Math.random() * 10) - 5);
				}
			}
			
			for (int i = 0; i < quantity; i++) {
				if (!EntityUtils.spawnEntity(new ResourceLocation(params[0]), true, coord, sender.getWorld(), (NBTTagCompound) nbt, mergeLists))
					throw new CommandException("command.spawn.couldNotSpawn", sender, params[0]);
			}
		}
		else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
		
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
		return 2;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return true;
	}
}
