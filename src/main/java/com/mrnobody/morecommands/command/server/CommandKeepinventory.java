package com.mrnobody.morecommands.command.server;

import net.minecraft.server.MinecraftServer;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.command.CommandBase.Requirement;
import com.mrnobody.morecommands.command.CommandBase.ServerType;
import com.mrnobody.morecommands.patch.EntityPlayerMP;
import com.mrnobody.morecommands.patch.ServerConfigurationManagerIntegrated;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import cpw.mods.fml.relauncher.Side;

@Command(
		name = "keepinventory",
		description = "command.keepinventory.description",
		example = "command.keepinventory.example",
		syntax = "command.keepinventory.syntax",
		videoURL = "command.keepinventory.videoURL"
		)
public class CommandKeepinventory extends ServerCommand {

	@Override
	public String getCommandName() {
		return "keepinventory";
	}

	@Override
	public String getUsage() {
		return "command.keepinventory.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params)throws CommandException {
		EntityPlayerMP player = (EntityPlayerMP) sender.toPlayer().getMinecraftPlayer();
		ServerPlayerSettings ability = ServerPlayerSettings.playerSettingsMapping.get(sender.getMinecraftISender());
		
        boolean keepinventory = false;
        boolean success = false;
        	
        if (params.length >= 1) {
        	if (params[0].toLowerCase().equals("true")) {keepinventory = true; success = true;}
        	else if (params[0].toLowerCase().equals("false")) {keepinventory = false; success = true;}
        	else if (params[0].toLowerCase().equals("0")) {keepinventory = false; success = true;}
        	else if (params[0].toLowerCase().equals("1")) {keepinventory = true; success = true;}
        	else if (params[0].toLowerCase().equals("on")) {keepinventory = true; success = true;}
        	else if (params[0].toLowerCase().equals("off")) {keepinventory = false; success = true;}
        	else {success = false;}
        }
	    else {keepinventory = !player.getKeepInventory(); success = true;}
	    	
	    if (success) {
	    	ability.keepinventory = keepinventory;
	    	player.setKeepInventory(keepinventory);
	    	((ServerConfigurationManagerIntegrated) MinecraftServer.getServer().getConfigurationManager()).setKeepInventory(keepinventory);
	    }
        	
        sender.sendLangfileMessageToPlayer(success ? keepinventory ? "command.keepinventory.on" : "command.keepinventory.off" : "command.keepinventory.failure", new Object[0]);
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[] {Requirement.PATCH_ENTITYPLAYERMP, Requirement.PATCH_SERVERCONFIGMANAGER};
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
