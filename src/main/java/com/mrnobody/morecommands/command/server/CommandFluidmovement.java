package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.patch.EntityPlayerMP;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;

@Command(
		name = "fluidmovement",
		description = "command.fluidmovement.description",
		example = "command.fluidmovement.example",
		syntax = "command.fluidmovement.syntax",
		videoURL = "command.fluidmovement.videoURL"
		)
public class CommandFluidmovement extends StandardCommand implements ServerCommandProperties {
    public String getName() {
        return "fluidmovement";
    }
    
    public String getUsage() {
        return "command.fluidmovement.syntax";
    }
    
	@Override
	public void execute(CommandSender sender, String[] params)throws CommandException {
		EntityPlayerMP player = getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class);
		ServerPlayerSettings settings = getPlayerSettings(player);
    	
		try {player.setFluidMovement(parseTrueFalse(params, 0, player.getFluidMovement()));}
		catch (IllegalArgumentException ex) {throw new CommandException("command.fluidmovement.failure", sender);}
		
		sender.sendLangfileMessage(player.getFluidMovement() ? "command.fluidmovement.on" : "command.fluidmovement.off");        
        MoreCommands.INSTANCE.getPacketDispatcher().sendS11FluidMovement(player, player.getFluidMovement());
	}
    
	@Override
	public CommandRequirement[] getRequirements() {
		return new CommandRequirement[] {CommandRequirement.MODDED_CLIENT, CommandRequirement.PATCH_ENTITYPLAYERSP, CommandRequirement.PATCH_ENTITYPLAYERMP};
	}
	
	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	@Override
	public int getDefaultPermissionLevel() {
		return 2;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return isSenderOfEntityType(sender, EntityPlayerMP.class);
	}
}
