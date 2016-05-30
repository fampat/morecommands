package com.mrnobody.morecommands.command.server;

import java.util.ArrayList;
import java.util.List;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.biome.BiomeGenBase;

@Command(
		name = "biome",
		description = "command.biome.description",
		example = "command.biome.example",
		syntax = "command.biome.syntax",
		videoURL = "command.biome.videoURL"
		)
public class CommandBiome extends StandardCommand implements ServerCommandProperties {
	private static final int PAGE_MAX = 15;
	
	@Override
	public String getCommandName() {
		return "biome";
	}

	@Override
	public String getUsage() {
		return "command.biome.syntax";
	}
	
	private BiomeGenBase[] dropNulls(BiomeGenBase[] biomes) {
		List<BiomeGenBase> list = new ArrayList<BiomeGenBase>(biomes.length);
		for (BiomeGenBase b : biomes) if (b != null) list.add(b);
		return list.toArray(new BiomeGenBase[list.size()]);
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		BiomeGenBase biome = sender.getWorld().getMinecraftWorld().getBiomeGenForCoords(sender.getPosition());
		
		if (params.length > 0) {
			if (params[0].equalsIgnoreCase("info")) sender.sendLangfileMessage("command.biome.info", biome.biomeName, biome.biomeID);
			else if (params[0].equalsIgnoreCase("list")) {
    			int page = 0;
    			BiomeGenBase[] biomeList = dropNulls(BiomeGenBase.getBiomeGenArray());
    			
    			if (params.length > 1) {
    				try {
    					page = Integer.parseInt(params[1]) - 1; 
    					if (page < 0) page = 0;
    					else if (page * PAGE_MAX > biomeList.length) page = biomeList.length / PAGE_MAX;
    				}
    				catch (NumberFormatException e) {throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());}
    			}
    			
    			final int stop = (page + 1) * PAGE_MAX;;
    			for (int i = page * PAGE_MAX; i < stop && i < biomeList.length; i++)
    				sender.sendStringMessage(" - '" + biomeList[i].biomeName + "' " + " (ID " + biomeList[i].biomeID + ")");
    			
    			sender.sendLangfileMessage("command.biome.more", EnumChatFormatting.RED);
			}
			else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
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
		return 0;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return true;
	}
}
