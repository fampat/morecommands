package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.world.biome.BiomeGenBase;

import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "biome",
		description = "command.biome.description",
		example = "command.biome.example",
		syntax = "command.biome.syntax",
		videoURL = "command.biome.videoURL"
		)
public class CommandBiome extends ServerCommand {

	@Override
	public String getName() {
		return "biome";
	}

	@Override
	public String getUsage() {
		return "command.biome.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		BiomeGenBase biome = sender.getWorld().getMinecraftWorld().getBiomeGenForCoords(sender.getPosition());
	
		if (params.length > 0) {
			if (params[0].equalsIgnoreCase("info")) sender.sendLangfileMessage("command.biome.info", biome.biomeName, biome.biomeID);
			else if (params[0].equalsIgnoreCase("list")) {
				BiomeGenBase[] biomeList = BiomeGenBase.getBiomeGenArray();
    			int page = 1;
    			int PAGE_MAX = 15;
    			boolean validParam = true;
    			
    			if (params.length > 1) {
    				try {page = Integer.parseInt(params[1]);} 
    				catch (NumberFormatException e) {throw new CommandException("command.biome.invalidUsage", sender);}
    			}
    			
				int to = PAGE_MAX * page <= biomeList.length ? PAGE_MAX * page : biomeList.length;
				int from = to - PAGE_MAX;
				
				try{for (int index = from; index < to; index++) {sender.sendStringMessage(" - '" + biomeList[index].biomeName + "' " + " (ID " + biomeList[index].biomeID + ")");}}
				catch (Exception ex) {}
    			sender.sendLangfileMessage("command.biome.more");
			}
			else throw new CommandException("command.biome.invalidUsage", sender);
		}
		else throw new CommandException("command.biome.invalidUsage", sender);
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}

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
		return true;
	}
}
