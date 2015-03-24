package com.mrnobody.morecommands.command.server;

import net.minecraft.world.biome.BiomeGenBase;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Player;

@Command(
		name = "biome",
		description = "command.biome.description",
		example = "command.biome.example",
		syntax = "command.biome.syntax",
		videoURL = "command.biome.videoURL"
		)
public class CommandBiome extends ServerCommand {

	@Override
	public String getCommandName() {
		return "biome";
	}

	@Override
	public String getUsage() {
		return "command.biome.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Player player = sender.toPlayer();
		
		BiomeGenBase biome = player.getWorld().getMinecraftWorld().getBiomeGenForCoords(player.getPosition().getBlockX(), player.getPosition().getBlockZ());
	
		if (params.length > 0) {
			if (params[0].equalsIgnoreCase("info")) sender.sendLangfileMessageToPlayer("command.biome.info", new Object[] {biome.biomeName, biome.biomeID});
			else if (params[0].equalsIgnoreCase("list")) {
				BiomeGenBase[] biomeList = BiomeGenBase.getBiomeGenArray();
    			int page = 1;
    			int PAGE_MAX = 15;
    			boolean validParam = true;
    			
    			if (params.length > 1) {
    				try {page = Integer.parseInt(params[1]);} 
    				catch (NumberFormatException e) {validParam = false;}
    			}
    			
    			if (validParam) {
    				int to = PAGE_MAX * page <= biomeList.length ? PAGE_MAX * page : biomeList.length;
    				int from = to - PAGE_MAX;
    				
    				try{for (int index = from; index < to; index++) {sender.sendStringMessageToPlayer(" - '" + biomeList[index].biomeName + "' " + " (ID " + biomeList[index].biomeID + ")");}}
    				catch (Exception ex) {}
    				sender.sendLangfileMessageToPlayer("command.biome.more", new Object[0]);
    			}
    			else {sender.sendLangfileMessageToPlayer("command.biome.invalidUsage", new Object[0]);}
			}
			else {sender.sendLangfileMessageToPlayer("command.biome.invalidUsage", new Object[0]);}
		}
		else {sender.sendLangfileMessageToPlayer("command.biome.invalidUsage", new Object[0]);}
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
