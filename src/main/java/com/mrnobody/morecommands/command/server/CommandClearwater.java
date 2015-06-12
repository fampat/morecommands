package com.mrnobody.morecommands.command.server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.command.ICommandSender;
import net.minecraft.init.Blocks;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.util.GlobalSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

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
        if (params.length > 0) {
        	if (params[0].equalsIgnoreCase("enable") || params[0].equalsIgnoreCase("1")
            	|| params[0].equalsIgnoreCase("on") || params[0].equalsIgnoreCase("true")) {
        		GlobalSettings.clearwater = true;
            	sender.sendLangfileMessage("command.clearwater.on");
            }
            else if (params[0].equalsIgnoreCase("disable") || params[0].equalsIgnoreCase("0")
            		|| params[0].equalsIgnoreCase("off") || params[0].equalsIgnoreCase("false")) {
            	GlobalSettings.clearwater = false;
            	sender.sendLangfileMessage("command.clearwater.off");
            }
            else throw new CommandException("command.clearwater.failure", sender);
        }
        else {
        	GlobalSettings.clearwater = !GlobalSettings.clearwater;
        	sender.sendLangfileMessage(GlobalSettings.clearwater ? "command.clearwater.on" : "command.clearwater.off");
        }
        
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
		return true;
	}
}
