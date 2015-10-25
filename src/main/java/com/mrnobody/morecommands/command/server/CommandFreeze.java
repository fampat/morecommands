package com.mrnobody.morecommands.command.server;

import java.util.ArrayList;
import java.util.List;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listeners.EventListener;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Command(
		name = "freeze",
		description = "command.freeze.description",
		example = "command.freeze.example",
		syntax = "command.freeze.syntax",
		videoURL = "command.freeze.videoURL"
		)
public class CommandFreeze extends ServerCommand implements EventListener<TickEvent> {
	private List<World> worldsToFreeze = new ArrayList<World>();
	
	public CommandFreeze() {
		EventHandler.TICK.getHandler().register(this);
	}
	
	@Override
	public void onEvent(TickEvent ev) {
		if (!(ev instanceof TickEvent.WorldTickEvent) || ev.phase != TickEvent.Phase.END) return;
		TickEvent.WorldTickEvent event = (TickEvent.WorldTickEvent) ev;
		
		if (event.world != null && this.worldsToFreeze.contains(event.world)) {
			List<Entity> loadedEntities = event.world.loadedEntityList;
			if (loadedEntities == null) return;
			
			for (int i = 0; i < loadedEntities.size(); i++) {
				if (loadedEntities.get(i) instanceof EntityLiving && !(loadedEntities.get(i) instanceof EntityPlayer)) {
					EntityLiving entity = (EntityLiving) loadedEntities.get(i);
					
					entity.setPosition(entity.prevPosX, entity.prevPosY, entity.prevPosZ);
					
					entity.motionX = 0;
					entity.motionY = 0;
					entity.motionZ = 0;
					
					entity.setAttackTarget(null);
					
					//if (entity instanceof EntityCreature) ((EntityCreature) entity).attackTime = 20;
					if (entity instanceof EntityCreeper) ((EntityCreeper) entity).setCreeperState(-1);
				}
			}
		}
	}
	
	@Override
	public String getName() {
		return "freeze";
	}

	@Override
	public String getUsage() {
		return "command.freeze.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		World world = sender.getWorld().getMinecraftWorld();
        if (params.length > 0) {
        	if (params[0].equalsIgnoreCase("enable") || params[0].equalsIgnoreCase("1")
            	|| params[0].equalsIgnoreCase("on") || params[0].equalsIgnoreCase("true")) {
        		this.worldsToFreeze.add(world);
            	sender.sendLangfileMessage("command.freeze.on");
            }
            else if (params[0].equalsIgnoreCase("disable") || params[0].equalsIgnoreCase("0")
            		|| params[0].equalsIgnoreCase("off") || params[0].equalsIgnoreCase("false")) {
            	this.worldsToFreeze.remove(world);
            	sender.sendLangfileMessage("command.freeze.off");
            }
            else throw new CommandException("command.freeze.failure", sender);
        }
        else {
        	if (this.worldsToFreeze.contains(world)) this.worldsToFreeze.remove(world);
        	else this.worldsToFreeze.add(world);
        	sender.sendLangfileMessage(this.worldsToFreeze.contains(world) ? "command.freeze.on" : "command.freeze.off");
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
