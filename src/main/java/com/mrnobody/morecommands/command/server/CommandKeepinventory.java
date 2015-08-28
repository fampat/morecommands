package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.patch.EntityPlayerMP;
import com.mrnobody.morecommands.patch.ServerConfigurationManagerDedicated;
import com.mrnobody.morecommands.patch.ServerConfigurationManagerIntegrated;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

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
		ServerPlayerSettings settings = ServerPlayerSettings.getPlayerSettings((EntityPlayerMP) sender.getMinecraftISender());
		boolean keepinventory;
		
        if (params.length > 0) {
        	if (params[0].equalsIgnoreCase("enable") || params[0].equalsIgnoreCase("1")
            	|| params[0].equalsIgnoreCase("on") || params[0].equalsIgnoreCase("true")) {
        		keepinventory = true;
            	sender.sendLangfileMessage("command.keepinventory.on");
            }
            else if (params[0].equalsIgnoreCase("disable") || params[0].equalsIgnoreCase("0")
            		|| params[0].equalsIgnoreCase("off") || params[0].equalsIgnoreCase("false")) {
            	keepinventory = false;
            	sender.sendLangfileMessage("command.keepinventory.off");
            }
            else throw new CommandException("command.keepinventory.failure", sender);
        }
        else {
        	keepinventory = !player.getKeepInventory();
        	sender.sendLangfileMessage(keepinventory ? "command.keepinventory.on" : "command.keepinventory.off");
        }
        
    	settings.keepinventory = keepinventory;
    	player.setKeepInventory(keepinventory);
    	
    	if (MoreCommands.isClientSide())
    		((ServerConfigurationManagerIntegrated) MinecraftServer.getServer().getConfigurationManager()).setKeepInventory(keepinventory);
    	else if (MoreCommands.isServerSide())
    		((ServerConfigurationManagerDedicated) MinecraftServer.getServer().getConfigurationManager()).setKeepInventory(keepinventory);
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
