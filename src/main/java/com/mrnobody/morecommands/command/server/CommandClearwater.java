package com.mrnobody.morecommands.command.server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.GlobalSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.block.Block;
import net.minecraft.command.ICommandSender;
import net.minecraft.init.Blocks;

@Command(
		name = "clearwater",
		description = "command.clearwater.description",
		example = "command.clearwater.example",
		syntax = "command.clearwater.syntax",
		videoURL = "command.clearwater.videoURL"
		)
public class CommandClearwater extends ServerCommand {
	private Map<Block, Integer> lightOpacities = new HashMap<Block, Integer>();
	
	public CommandClearwater() {
		this.lightOpacities.put(Blocks.water, Blocks.water.getLightOpacity());
		this.lightOpacities.put(Blocks.flowing_water, Blocks.flowing_water.getLightOpacity());
	}
	
	@Override
	public String getName() {
		return "clearwater";
	}

	@Override
	public String getUsage() {
		return "command.clearwater.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params)throws CommandException {
		try {GlobalSettings.clearwater = parseTrueFalse(params, 0, GlobalSettings.clearwater);}
		catch (IllegalArgumentException ex) {throw new CommandException("command.clearwater.failure", sender);}
		
		sender.sendLangfileMessage(GlobalSettings.clearwater ? "command.clearwater.on" : "command.clearwater.off");
        
    	Block block;
    	Iterator<Block> blocks = this.lightOpacities.keySet().iterator();
    		
    	while (blocks.hasNext()) {
    		block = blocks.next();
    			
    		if (GlobalSettings.clearwater) block.setLightOpacity(0);
    		else block.setLightOpacity(this.lightOpacities.get(block));
    	}
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
		return 2;
	}
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return true;
	}
}
