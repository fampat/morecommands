package com.mrnobody.morecommands.command.server;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listeners.TwoEventListener;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;

@Command(
		name = "infiniteitems",
		description = "command.infiniteitems.description",
		example = "command.infiniteitems.example",
		syntax = "command.infiniteitems.syntax",
		videoURL = "command.infiniteitems.videoURL"
		)
public class CommandInfiniteitems extends ServerCommand implements TwoEventListener<PlaceEvent, PlayerDestroyItemEvent>{
	public void onEvent1(PlaceEvent event) {
		this.onPlace(event);
	}
	  
	public void onEvent2(PlayerDestroyItemEvent event) {
		this.onDestroy(event);
	}
	
	private class Stack {
		private EntityPlayer player;
		private int stack;
		private boolean incr;
	    
		private Stack(EntityPlayer player, int stack) {
			this.player = player;
			this.stack = stack;
			this.incr = (player.inventory.mainInventory[stack].stackSize > 1);
		}
	}
	
	private class StackObserver extends Thread {
		private long lastTime = System.currentTimeMillis();
	    
		@Override
		public void run() {
			Stack stack;
			
			while (!this.isInterrupted() && !(MinecraftServer.getServer() == null || !MinecraftServer.getServer().isServerRunning())) {
				try {
					if (this.lastTime < System.currentTimeMillis()) {
						
						while ((stack = stacks.poll()) != null) {
							if (stack.player.inventory.mainInventory[stack.stack] != null && stack.incr)
								stack.player.inventory.mainInventory[stack.stack].stackSize += 1;
						}
						
						this.lastTime = System.currentTimeMillis();
					}
				}
				catch (Exception ex) {}
			}
		}
	}
	
	private Queue<Stack> stacks;
	private StackObserver observer;
	
	public CommandInfiniteitems() {
		EventHandler.PLACE.getHandler().register(this, true);
		EventHandler.ITEM_DESTROY.getHandler().register(this, false);
		this.stacks = new ConcurrentLinkedQueue<Stack>();
		this.observer = new StackObserver();
		this.observer.start();
	}
	
	public void onPlace(PlaceEvent event) {
		if (event.player instanceof EntityPlayerMP
			&& ServerPlayerSettings.getPlayerSettings((EntityPlayerMP) event.player).infiniteitems
			&& event.player.getCurrentEquippedItem() != null)
			this.stacks.offer(new Stack(event.player, event.player.inventory.currentItem));
	  }
	  
	public void onDestroy(PlayerDestroyItemEvent event) {
		if (event.entityPlayer instanceof EntityPlayerMP
			&& ServerPlayerSettings.getPlayerSettings((EntityPlayerMP) event.entityPlayer).infiniteitems
			&& event.original != null && event.original.stackSize < 1) event.original.stackSize += 1;
	}
	
	@Override
	public String getCommandName() {
		return "infiniteitems";
	}

	@Override
	public String getUsage() {
		return "command.infiniteitems.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		EntityPlayerMP player = (EntityPlayerMP) sender.getMinecraftISender();
		ServerPlayerSettings settings = ServerPlayerSettings.getPlayerSettings((EntityPlayerMP) sender.getMinecraftISender());
    	
		try {settings.infiniteitems = parseTrueFalse(params, 0, settings.infiniteitems);}
		catch (IllegalArgumentException ex) {throw new CommandException("command.infiniteitems.failure", sender);}
		
		sender.sendLangfileMessage(settings.infiniteitems ? "command.infiniteitems.on" : "command.infiniteitems.off");
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
		return sender instanceof EntityPlayerMP;
	}
}
