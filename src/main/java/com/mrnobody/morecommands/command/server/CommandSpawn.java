package com.mrnobody.morecommands.command.server;

import java.util.LinkedHashMap;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Entity;
import com.mrnobody.morecommands.wrapper.EntityLivingBase;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;

@Command(
		name = "spawn",
		description = "command.spawn.description",
		example = "command.spawn.example",
		syntax = "command.spawn.syntax",
		videoURL = "command.spawn.videoURL"
		)
public class CommandSpawn extends StandardCommand implements ServerCommandProperties {
	private static final int PAGE_MAX = 15;
	private static final LinkedHashMap<String, Integer> nameToIdMap = new LinkedHashMap<String, Integer>(Entity.getNameToIdEntityList());

	@Override
	public String getCommandName() {
		return "spawn";
	}

	@Override
	public String getCommandUsage() {
		return "command.spawn.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		if (params.length > 0) {
			if (params[0].equalsIgnoreCase("list")) {
    			int page = 0;
    			nameToIdMap.putAll(Entity.getNameToIdEntityList());
    			String[] names = nameToIdMap.keySet().toArray(new String[nameToIdMap.size()]);
    			
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
    				sender.sendStringMessage(" - '" + names[i] + "' " + " (ID " + nameToIdMap.get(names[i]) + ")");
    			
    			sender.sendLangfileMessage("command.spawn.more", TextFormatting.RED);
				return;
			}
			else if (params[0].equalsIgnoreCase("random")) {
				params[0] = Entity.getNonAbstractEntityList().get((int)(Math.random() * Entity.getNonAbstractEntityList().size()));
			}
			else if (Entity.getEntityClass(params[0]) == null) {
				try {
					params[0] = EntityList.classToStringMapping.get(EntityList.idToClassMapping.get(Integer.parseInt(params[0])));
					if (params[0] == null) throw new CommandException("command.spawn.unknownEntityID", sender);
				} catch (NumberFormatException nfe) {throw new CommandException("command.spawn.unknownEntity", sender);}
			}
			
			params = reparseParamsWithNBTData(params);
			
			int quantity = 1; BlockPos coord = null; NBTBase nbt = null; boolean mergeLists = false;
			
			if (params.length > 1) {
				if (isNBTParam(params[1])) {
					nbt = getNBTFromParam(params[1], sender.getMinecraftISender());
					mergeLists = params.length > 2 ? isMergeLists(params[2]) : mergeLists;
				}
				else {
					try {quantity = Integer.parseInt(params[1]);} 
					catch (NumberFormatException nfe) {sender.sendLangfileMessage("command.spawn.NAN");}
					
					if (params.length > 2 && isNBTParam(params[2])) {
						nbt = getNBTFromParam(params[2], sender.getMinecraftISender());
						mergeLists = params.length > 3 ? isMergeLists(params[3]) : mergeLists;
					}
					else if (params.length > 4) {
						coord = getCoordFromParams(sender.getMinecraftISender(), params, 2);
						
						if (params.length > 5) {
							nbt = getNBTFromParam(params[5], sender.getMinecraftISender());
							mergeLists = params.length > 6 ? isMergeLists(params[6]) : mergeLists;
						}
					}
				}
			}
			
			if (nbt != null && !(nbt instanceof NBTTagCompound))
				throw new CommandException("command.spawn.noCompound", sender);
			
			if (coord == null) {
				if (isSenderOfEntityType(sender.getMinecraftISender(), net.minecraft.entity.EntityLivingBase.class))
					coord = new EntityLivingBase(getSenderAsEntity(sender.getMinecraftISender(), net.minecraft.entity.EntityLivingBase.class)).traceBlock(128);
				else coord = sender.getPosition();
			
				if (coord == null) {
					coord = sender.getPosition();
					coord = new BlockPos(coord.getX() + (Math.random() * 10) - 5, coord.getY(), coord.getZ() + (Math.random() * 10) - 5);
				}
			}
			
			for (int i = 0; i < quantity; i++) {
				if (!Entity.spawnEntity(params[0], coord, sender.getWorld(), (NBTTagCompound) nbt, mergeLists))
					throw new CommandException("command.spawn.couldNotSpawn", sender, params[0]);
			}
		}
		else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
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
