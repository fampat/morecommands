package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.core.MoreCommands.ServerType;
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
    public String getCommandName() {
        return "fluidmovement";
    }
    
    public String getUsage() {
        return "command.fluidmovement.syntax";
    }
    
	@Override
	public void execute(CommandSender sender, String[] params)throws CommandException {
		EntityPlayerMP player = (EntityPlayerMP) sender.getMinecraftISender();
		ServerPlayerSettings settings = ServerPlayerSettings.getPlayerSettings(player);
    	
		try {settings.fluidmovement = parseTrueFalse(params, 0, settings.fluidmovement);}
		catch (IllegalArgumentException ex) {throw new CommandException("command.fluidmovement.failure", sender);}
		
		sender.sendLangfileMessage(settings.fluidmovement  ? "command.fluidmovement.on" : "command.fluidmovement.off");        
        MoreCommands.getMoreCommands().getPacketDispatcher().sendS13FluidMovement(player, settings.fluidmovement);
	}
    
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[] {Requirement.MODDED_CLIENT, Requirement.PATCH_ENTITYCLIENTPLAYERMP};
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
