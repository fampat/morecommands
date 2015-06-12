package com.mrnobody.morecommands.command.client;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;

import com.mrnobody.morecommands.command.ClientCommand;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listeners.Listener;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import cpw.mods.fml.common.gameevent.TickEvent;

@Command(
		name = "freeze",
		description = "command.freeze.description",
		example = "command.freeze.example",
		syntax = "command.freeze.syntax",
		videoURL = "command.freeze.videoURL"
		)
public class CommandFreeze extends ClientCommand implements Listener<TickEvent> {
	private boolean freeze = false;
	
	public CommandFreeze() {
		EventHandler.TICK.getHandler().register(this);
	}
	
	@Override
	public void onEvent(TickEvent event) {
		if (this.freeze && Minecraft.getMinecraft().theWorld != null) {
			List<Entity> loadedEntities = Minecraft.getMinecraft().theWorld.loadedEntityList;
			if (loadedEntities == null) return;
			
			for (int i = 0; i < loadedEntities.size(); i++) {
				if (loadedEntities.get(i) instanceof EntityLiving && !(loadedEntities.get(i) instanceof EntityPlayer)) {
					EntityLiving entity = (EntityLiving) loadedEntities.get(i);
					
					entity.setPosition(entity.prevPosX, entity.prevPosY, entity.prevPosZ);
					
					entity.motionX = 0;
					entity.motionY = 0;
					entity.motionZ = 0;
					
					entity.setAttackTarget(null);
					
					if (entity instanceof EntityCreature) ((EntityCreature) entity).attackTime = 20;
					if (entity instanceof EntityCreeper) ((EntityCreeper) entity).setCreeperState(-1);
				}
			}
		}
	}
	
	@Override
	public String getCommandName() {
		return "freeze";
	}

	@Override
	public String getUsage() {
		return "command.freeze.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
        if (params.length > 0) {
        	if (params[0].equalsIgnoreCase("enable") || params[0].equalsIgnoreCase("1")
            	|| params[0].equalsIgnoreCase("on") || params[0].equalsIgnoreCase("true")) {
        		this.freeze = true;
            	sender.sendLangfileMessage("command.freeze.on");
            }
            else if (params[0].equalsIgnoreCase("disable") || params[0].equalsIgnoreCase("0")
            		|| params[0].equalsIgnoreCase("off") || params[0].equalsIgnoreCase("false")) {
            	this.freeze = false;
            	sender.sendLangfileMessage("command.freeze.off");
            }
            else throw new CommandException("command.freeze.failure", sender);
        }
        else {
        	this.freeze = !this.freeze;
        	sender.sendLangfileMessage(this.freeze ? "command.freeze.on" : "command.freeze.off");
        }
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.INTEGRATED;
	}
	
	@Override
	public boolean registerIfServerModded() {
		return true;
	}
	
	@Override
	public int getPermissionLevel() {
		return 0;
	}
}
