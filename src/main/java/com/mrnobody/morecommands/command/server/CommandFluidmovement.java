package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

@Command(
		name = "fluidmovement",
		description = "command.fluidmovement.description",
		example = "command.fluidmovement.example",
		syntax = "command.fluidmovement.syntax",
		videoURL = "command.fluidmovement.videoURL"
		)
public class CommandFluidmovement extends ServerCommand {
    public String getName() {
        return "fluidmovement";
    }
    
    public String getUsage() {
        return "command.fluidmovement.syntax";
    }
    
	@Override
	public void execute(CommandSender sender, String[] params)throws CommandException {
		EntityPlayerMP player = (EntityPlayerMP) sender.getMinecraftISender();
		ServerPlayerSettings settings = ServerPlayerSettings.getPlayerSettings(player);
    	
        if (params.length > 0) {
        	if (params[0].equalsIgnoreCase("enable") || params[0].equalsIgnoreCase("1")
            	|| params[0].equalsIgnoreCase("on") || params[0].equalsIgnoreCase("true")) {
        		settings.fluidmovement = true;
            	sender.sendLangfileMessage("command.fluidmovement.on");
            }
            else if (params[0].equalsIgnoreCase("disable") || params[0].equalsIgnoreCase("0")
            		|| params[0].equalsIgnoreCase("off") || params[0].equalsIgnoreCase("false")) {
            	settings.fluidmovement = false;
            	sender.sendLangfileMessage("command.fluidmovement.off");
            }
            else throw new CommandException("command.fluidmovement.failure", sender);
        }
        else {
        	settings.fluidmovement = !settings.fluidmovement;
        	sender.sendLangfileMessage(settings.fluidmovement ? "command.fluidmovement.on" : "command.fluidmovement.off");
        }
        
        MoreCommands.getMoreCommands().getPacketDispatcher().sendS13FluidMovement(player, settings.fluidmovement);
	}
    
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[] {Requirement.MODDED_CLIENT, Requirement.PATCH_ENTITYPLAYERSP};
	}
	
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
