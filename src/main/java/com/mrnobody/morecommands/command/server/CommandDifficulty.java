package com.mrnobody.morecommands.command.server;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.EnumDifficulty;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.command.CommandBase.ServerType;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import cpw.mods.fml.relauncher.Side;

@Command(
		name = "difficulty",
		description = "command.difficulty.description",
		example = "command.difficulty.example",
		syntax = "command.difficulty.syntax",
		videoURL = "command.difficulty.videoURL"
		)
public class CommandDifficulty extends ServerCommand {

	@Override
	public String getCommandName() {
		return "difficulty";
	}

	@Override
	public String getUsage() {
		return "command.difficulty.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		if (params.length > 0) {
			MinecraftServer server = MinecraftServer.getServer();
			EnumDifficulty diff;
			
			if (params[0].equalsIgnoreCase("peaceful")|| params[0].equalsIgnoreCase("0")) diff = EnumDifficulty.PEACEFUL;
			else if (params[0].equalsIgnoreCase("easy")|| params[0].equalsIgnoreCase("1")) diff = EnumDifficulty.EASY;
			else if (params[0].equalsIgnoreCase("normal")|| params[0].equalsIgnoreCase("2")) diff = EnumDifficulty.NORMAL;
			else if (params[0].equalsIgnoreCase("hard")|| params[0].equalsIgnoreCase("3")) diff = EnumDifficulty.HARD;
			else {sender.sendLangfileMessageToPlayer("command.difficulty.invalidDifficulty", new Object[0]); return;}
			
			server.func_147139_a(diff);
			sender.toPlayer().getMinecraftPlayer().worldObj.difficultySetting = diff;
			String difficulty = "";
			
			switch(diff) {
				case PEACEFUL: difficulty = "peaceful"; break;
				case EASY: difficulty = "easy"; break;
				case NORMAL: difficulty = "normal"; break;
				case HARD: difficulty = "hard"; break;
			}
			
			sender.sendLangfileMessageToPlayer("command.difficulty.setto", new Object[] {difficulty});
		}
		else {sender.sendLangfileMessageToPlayer("command.difficulty.invalidUsage", new Object[0]);}
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
		return 2;
	}
}
