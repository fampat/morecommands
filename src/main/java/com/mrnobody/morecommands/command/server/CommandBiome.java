package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.biome.Biome;

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
	public String getCommandUsage() {
		return "command.biome.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		Biome biome = sender.getWorld().getBiome(sender.getPosition());
		
		if (params.length > 0) {
			if (params[0].equalsIgnoreCase("info")) sender.sendLangfileMessage("command.biome.info", biome.getBiomeName(), Biome.getIdForBiome(biome));
			else if (params[0].equalsIgnoreCase("list")) {
    			int page = 0;
    			ResourceLocation[] biomes = Biome.REGISTRY.getKeys().toArray(new ResourceLocation[Biome.REGISTRY.getKeys().size()]);
    			
    			if (params.length > 1) {
    				try {
    					page = Integer.parseInt(params[1]) - 1; 
    					if (page < 0) page = 0;
    					else if (page * PAGE_MAX > biomes.length) page = biomes.length / PAGE_MAX;
    				}
    				catch (NumberFormatException e) {throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());}
    			}
    			
    			final int stop = (page + 1) * PAGE_MAX;;
    			for (int i = page * PAGE_MAX; i < stop && i < biomes.length; i++)
    				sender.sendStringMessage(" - '" + biomes[i] + "' (ID " + Biome.getIdForBiome(Biome.REGISTRY.getObject(biomes[i])) + ")");
    			
    			sender.sendLangfileMessage("command.biome.more", TextFormatting.RED);
			}
			else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
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
		return 0;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return true;
	}
}
