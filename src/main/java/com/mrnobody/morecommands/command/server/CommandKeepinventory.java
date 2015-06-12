package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.command.CommandBase.Requirement;
import com.mrnobody.morecommands.command.CommandBase.ServerType;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.patch.EntityPlayerMP;
import com.mrnobody.morecommands.patch.ServerConfigurationManagerDedicated;
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
		EntityPlayerMP player = (EntityPlayerMP) sender.getMinecraftISender();
		ServerPlayerSettings ability = ServerPlayerSettings.playerSettingsMapping.get(sender.getMinecraftISender());
		
        boolean keepinventory = false;
        boolean success = false;
        	
        if (params.length >= 1) {
        	if (params[0].equalsIgnoreCase("true")) {keepinventory = true; success = true;}
        	else if (params[0].equalsIgnoreCase("false")) {keepinventory = false; success = true;}
        	else if (params[0].equalsIgnoreCase("0")) {keepinventory = false; success = true;}
        	else if (params[0].equalsIgnoreCase("1")) {keepinventory = true; success = true;}
        	else if (params[0].equalsIgnoreCase("on")) {keepinventory = true; success = true;}
        	else if (params[0].equalsIgnoreCase("off")) {keepinventory = false; success = true;}
    		else if (params[0].equalsIgnoreCase("enable")) {keepinventory = true; success = true;}
    		else if (params[0].equalsIgnoreCase("disable")) {keepinventory = false; success = true;}
        	else {success = false;}
        }
	    else {keepinventory = !player.getKeepInventory(); success = true;}
	    	
	    if (success) {
	    	ability.keepinventory = keepinventory;
	    	player.setKeepInventory(keepinventory);
	    	if (MoreCommands.isClientSide())
	    		((ServerConfigurationManagerIntegrated) MinecraftServer.getServer().getConfigurationManager()).setKeepInventory(keepinventory);
	    	else if (MoreCommands.isServerSide())
	    		((ServerConfigurationManagerDedicated) MinecraftServer.getServer().getConfigurationManager()).setKeepInventory(keepinventory);
	    }
        	
        sender.sendLangfileMessage(success ? keepinventory ? "command.keepinventory.on" : "command.keepinventory.off" : "command.keepinventory.failure", new Object[0]);
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
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return sender instanceof EntityPlayerMP;
	}
}
